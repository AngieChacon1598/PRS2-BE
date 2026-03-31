package pe.edu.vallegrande.ms_inventory.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.io.IOException;

@ReadingConverter
public class JsonNodeReadConverter implements Converter<Object, JsonNode> {

     private final ObjectMapper mapper = new ObjectMapper();

     @Override
     public JsonNode convert(Object source) {
          try {
               if (source instanceof Json) {
                    return mapper.readTree(((Json) source).asString());
               } else {
                    // fallback: si viene otro tipo, intentamos convertirlo a String
                    return mapper.readTree(source.toString());
               }
          } catch (IOException e) {
               throw new RuntimeException("Error convirtiendo JSON desde DB", e);
          }
     }
}
