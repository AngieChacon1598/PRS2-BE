package pe.edu.vallegrande.ms_inventory.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class R2dbcConfig extends AbstractR2dbcConfiguration {

     private final ConnectionFactory connectionFactory;

     public R2dbcConfig(ConnectionFactory connectionFactory) {
          this.connectionFactory = connectionFactory;
     }

     @Override
     public ConnectionFactory connectionFactory() {
          return connectionFactory;
     }

     // Configuración de JSON y LocalDate para R2DBC
     @Bean
     @Override
     public R2dbcCustomConversions r2dbcCustomConversions() {
          List<Converter<?, ?>> converters = new ArrayList<>();

          // Converters para JsonNode
          converters.add(new JsonNodeReadConverter());
          converters.add(new JsonNodeWriteConverter());

          // Converters para LocalDate <-> LocalDateTime
          converters.add(new LocalDateToLocalDateTimeWritingConverter());
          converters.add(new LocalDateTimeToLocalDateReadingConverter());

          return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
     }

     // Crear el R2dbcEntityTemplate una sola vez
     @Bean
     public R2dbcEntityTemplate r2dbcEntityTemplate() {
          return new R2dbcEntityTemplate(connectionFactory());
     }

     /**
      * Convierte LocalDate a LocalDateTime al escribir en la BD
      * (PostgreSQL DATE se maneja como LocalDateTime en R2DBC)
      */
     @WritingConverter
     public static class LocalDateToLocalDateTimeWritingConverter
               implements Converter<LocalDate, LocalDateTime> {
          @Override
          public LocalDateTime convert(@NonNull LocalDate source) {
               return source.atStartOfDay();
          }
     }

     /**
      * Convierte LocalDateTime a LocalDate al leer desde la BD
      * (PostgreSQL DATE se lee como LocalDateTime en R2DBC)
      */
     @ReadingConverter
     public static class LocalDateTimeToLocalDateReadingConverter
               implements Converter<LocalDateTime, LocalDate> {
          @Override
          public LocalDate convert(@NonNull LocalDateTime source) {
               return source.toLocalDate();
          }
     }
}