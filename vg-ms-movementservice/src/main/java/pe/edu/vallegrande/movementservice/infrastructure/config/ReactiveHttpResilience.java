package pe.edu.vallegrande.movementservice.infrastructure.config;

import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

@Component
public class ReactiveHttpResilience {

    private final OutboundHttpProperties props;

    public ReactiveHttpResilience(OutboundHttpProperties props) {
        this.props = props;
    }

    public <T> Mono<T> run(ReactiveCircuitBreaker circuitBreaker, Mono<T> call,
            Function<Throwable, Mono<T>> fallback) {
        return circuitBreaker.run(
                call.retryWhen(retrySpec()),
                fallback
        );
    }

    private Retry retrySpec() {
        return Retry.backoff(props.getRetryTotalAttempts(), Duration.ofMillis(props.getRetryInitialIntervalMs()))
                .maxBackoff(Duration.ofSeconds(props.getRetryMaxIntervalSeconds()))
                .filter(this::isTransientFailure)
                .jitter(0.5);
    }

    private boolean isTransientFailure(Throwable throwable) {
        Throwable t = throwable;
        while (t != null) {
            if (t instanceof TimeoutException) {
                return true;
            }
            if (t instanceof IOException) {
                return true;
            }
            if (t instanceof WebClientResponseException ex) {
                int code = ex.getStatusCode().value();
                return code == 408 || code == 429 || code >= 500;
            }
            t = t.getCause();
        }
        return false;
    }
}
