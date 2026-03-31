#!/usr/bin/env bash
# Diagnostic collector for docker compose environment
# Produces logs, env, images and curl responses for gateway and patrimonio

set -u
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="$SCRIPT_DIR/diag-output-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$OUT_DIR"

cmd_exists() { command -v "$1" >/dev/null 2>&1; }

# choose docker compose command
DC=""
if cmd_exists docker && docker compose version >/dev/null 2>&1; then
  DC="docker compose"
elif cmd_exists docker-compose; then
  DC="docker-compose"
elif cmd_exists docker; then
  DC="docker compose" # try anyway
else
  echo "ERROR: docker not found in PATH" >&2
  exit 1
fi

echo "Using compose command: $DC"
echo "Output dir: $OUT_DIR"

echo "--- PWD ---" | tee "$OUT_DIR/01_pwd.txt"
pwd | tee -a "$OUT_DIR/01_pwd.txt"

echo "--- Compose services (raw) ---" | tee "$OUT_DIR/02_compose_services.txt"
$DC ps --services 2>/dev/null | tee -a "$OUT_DIR/02_compose_services.txt" || true

echo "--- Compose ps (detailed) ---" | tee "$OUT_DIR/03_compose_ps.txt"
$DC ps 2>/dev/null | tee -a "$OUT_DIR/03_compose_ps.txt" || true

echo "--- docker ps (host) ---" | tee "$OUT_DIR/04_docker_ps.txt"
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Ports}}\t{{.Status}}' | tee -a "$OUT_DIR/04_docker_ps.txt" || true

# helper: find service name containing substring (case-insensitive)
find_service() {
  substr="$1"
  svc="$($DC ps --services 2>/dev/null | grep -i "$substr" | head -n1 || true)"
  if [ -n "$svc" ]; then
    echo "$svc"
    return 0
  fi
  # fallback: search docker ps names
  name="$(docker ps --format '{{.Names}}' | grep -i "$substr" | head -n1 || true)"
  echo "$name"
}

PATR_SVC=$(find_service patrimonio)
GW_SVC=$(find_service gateway)

echo "Detected services: patrimonio='$PATR_SVC' gateway='$GW_SVC'" | tee "$OUT_DIR/05_detected_services.txt"

# get container id/name for a compose service or docker name
get_container_for() {
  svcname="$1"
  if [ -z "$svcname" ]; then
    echo ""; return 0
  fi
  # try docker compose to get container id
  cid=""
  cid=$($DC ps -q "$svcname" 2>/dev/null || true)
  if [ -n "$cid" ]; then
    echo "$cid"; return 0
  fi
  # fallback to docker ps matching name
  cid=$(docker ps --filter "name=$svcname" --format '{{.Names}}' | head -n1 || true)
  echo "$cid"
}

PATR_CONT=$(get_container_for "$PATR_SVC")
GW_CONT=$(get_container_for "$GW_SVC")

echo "PATR container: $PATR_CONT" | tee -a "$OUT_DIR/05_detected_services.txt"
echo "GW container: $GW_CONT" | tee -a "$OUT_DIR/05_detected_services.txt"

# collect env for patrimonio
if [ -n "$PATR_CONT" ]; then
  echo "--- patrimonio env (filtered) ---" | tee "$OUT_DIR/06_patr_env.txt"
  docker exec "$PATR_CONT" env 2>/dev/null | grep -E "MS_|CONFIG|AUTH|PATRIMONIO|MOVEMENTS" | tee -a "$OUT_DIR/06_patr_env.txt" || true
  echo "--- patrimonio image ---" | docker inspect --format='Image: {{.Config.Image}}' "$PATR_CONT" 2>/dev/null | tee -a "$OUT_DIR/06_patr_env.txt" || true
else
  echo "No patrimonio container found, skipping env collection" | tee "$OUT_DIR/06_patr_env.txt"
fi

# collect logs
echo "--- logs patrimonio (tail 200) ---" | tee "$OUT_DIR/07_patr_logs.txt"
if [ -n "$PATR_SVC" ]; then
  $DC logs --tail=200 --no-color "$PATR_SVC" 2>/dev/null | tee -a "$OUT_DIR/07_patr_logs.txt" || true
elif [ -n "$PATR_CONT" ]; then
  docker logs --tail 200 "$PATR_CONT" 2>/dev/null | tee -a "$OUT_DIR/07_patr_logs.txt" || true
else
  echo "No patrimonio logs available" | tee -a "$OUT_DIR/07_patr_logs.txt"
fi

echo "--- logs gateway (tail 200) ---" | tee "$OUT_DIR/08_gw_logs.txt"
if [ -n "$GW_SVC" ]; then
  $DC logs --tail=200 --no-color "$GW_SVC" 2>/dev/null | tee -a "$OUT_DIR/08_gw_logs.txt" || true
elif [ -n "$GW_CONT" ]; then
  docker logs --tail 200 "$GW_CONT" 2>/dev/null | tee -a "$OUT_DIR/08_gw_logs.txt" || true
else
  echo "No gateway logs available" | tee -a "$OUT_DIR/08_gw_logs.txt"
fi

# determine gateway host:port
GATEWAY_URL="http://localhost:5000"
if [ -n "$GW_SVC" ]; then
  mapped="$($DC port "$GW_SVC" 5000 2>/dev/null || true)"
  if [ -n "$mapped" ]; then
    # mapped like 0.0.0.0:5000 or 127.0.0.1:5000
    hostport="${mapped##*:}"
    GATEWAY_URL="http://localhost:$hostport"
  fi
fi

echo "Using gateway base: $GATEWAY_URL" | tee "$OUT_DIR/09_urls.txt"

# determine patrimonio direct port if exposed
PATR_URL=""
if [ -n "$PATR_SVC" ]; then
  mapped_patr="$($DC port "$PATR_SVC" 5003 2>/dev/null || true)"
  if [ -n "$mapped_patr" ]; then
    port_only="${mapped_patr##*:}"
    PATR_URL="http://localhost:$port_only"
  fi
fi
if [ -z "$PATR_URL" ]; then
  echo "No direct patrimonio mapped port found, will skip direct curl." | tee -a "$OUT_DIR/09_urls.txt"
else
  echo "Patrimonio direct base: $PATR_URL" | tee -a "$OUT_DIR/09_urls.txt"
fi

# perform curl tests (no auth and with placeholder token)
TEST_PATH="/gateway/api/v1/assets/responsible"
echo "--- curl via gateway (no auth) ---" | tee "$OUT_DIR/10_curl_noauth.txt"
curl -s -S -D "$OUT_DIR/hdrs_noauth.txt" "$GATEWAY_URL$TEST_PATH" -o "$OUT_DIR/resp_noauth.txt" || true
echo "Response saved to $OUT_DIR/resp_noauth.txt and headers to $OUT_DIR/hdrs_noauth.txt" | tee -a "$OUT_DIR/10_curl_noauth.txt"

echo "--- curl via gateway (with token placeholder) ---" | tee "$OUT_DIR/11_curl_auth.txt"
curl -s -S -D "$OUT_DIR/hdrs_auth.txt" -H "Authorization: Bearer TOKEN" -H "Accept: application/json" "$GATEWAY_URL$TEST_PATH" -o "$OUT_DIR/resp_auth.txt" || true
echo "Response saved to $OUT_DIR/resp_auth.txt and headers to $OUT_DIR/hdrs_auth.txt" | tee -a "$OUT_DIR/11_curl_auth.txt"

if [ -n "$PATR_URL" ]; then
  echo "--- curl direct patrimonio (no auth) ---" | tee "$OUT_DIR/12_patr_noauth.txt"
  curl -s -S -D "$OUT_DIR/hdrs_patr_noauth.txt" "$PATR_URL/api/v1/assets/responsible" -o "$OUT_DIR/resp_patr_noauth.txt" || true
  echo "Saved $OUT_DIR/resp_patr_noauth.txt" | tee -a "$OUT_DIR/12_patr_noauth.txt"

  echo "--- curl direct patrimonio (with token placeholder) ---" | tee "$OUT_DIR/13_patr_auth.txt"
  curl -s -S -D "$OUT_DIR/hdrs_patr_auth.txt" -H "Authorization: Bearer TOKEN" -H "Accept: application/json" "$PATR_URL/api/v1/assets/responsible" -o "$OUT_DIR/resp_patr_auth.txt" || true
  echo "Saved $OUT_DIR/resp_patr_auth.txt" | tee -a "$OUT_DIR/13_patr_auth.txt"
fi

echo "--- quick grep for 400 or WebClient error in patrimonio logs ---" | tee "$OUT_DIR/14_grep_400.txt"
grep -nE "WebClient error response|400|Bad Request|ResponseStatusException" "$OUT_DIR/07_patr_logs.txt" | tee -a "$OUT_DIR/14_grep_400.txt" || true

echo "DONE. Collected outputs in: $OUT_DIR"
echo "Please attach the files: resp_auth.txt, resp_noauth.txt, resp_patr_auth.txt (if any), hdrs_auth.txt and the log files from the output dir for analysis." 
