package pe.edu.vallegrande.configurationservice.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;

import java.util.List;

@Configuration
public class R2dbcConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 🔹 Converter: PostgreSQL JSONB → JsonNode
    @ReadingConverter
    public class JsonToJsonNodeConverter implements Converter<Json, JsonNode> {
        @Override
        public JsonNode convert(Json source) {
            try {
                return objectMapper.readTree(source.asString());
            } catch (Exception e) {
                throw new RuntimeException("Error converting JSONB to JsonNode", e);
            }
        }
    }

    // 🔹 Converter: JsonNode → PostgreSQL JSONB
    @WritingConverter
    public class JsonNodeToJsonConverter implements Converter<JsonNode, Json> {
        @Override
        public Json convert(JsonNode source) {
            try {
                return Json.of(objectMapper.writeValueAsString(source));
            } catch (Exception e) {
                throw new RuntimeException("Error converting JsonNode to JSONB", e);
            }
        }
    }

    // ✅ Usar el dialecto detectado automáticamente desde el ConnectionFactory
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory) {
        R2dbcDialect dialect = DialectResolver.getDialect(connectionFactory);
        return R2dbcCustomConversions.of(
                dialect,
                List.of(
                        new JsonToJsonNodeConverter(),
                        new JsonNodeToJsonConverter()
                )
        );
    }
}