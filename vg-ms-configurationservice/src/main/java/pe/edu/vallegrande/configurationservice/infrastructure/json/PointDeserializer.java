package pe.edu.vallegrande.configurationservice.infrastructure.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.r2dbc.postgresql.codec.Point;

import java.io.IOException;

public class PointDeserializer extends JsonDeserializer<Point> {
    @Override
    public Point deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node == null || node.isNull()) {
            return null;
        }
        // Support either {"x": -77.03, "y": -12.04} or {"lon": -77.03, "lat": -12.04}
        JsonNode xNode = node.get("x");
        JsonNode yNode = node.get("y");
        if ((xNode == null || yNode == null) && node.has("lon") && node.has("lat")) {
            xNode = node.get("lon");
            yNode = node.get("lat");
        }
        if (xNode == null || yNode == null || !xNode.isNumber() || !yNode.isNumber()) {
            throw InvalidFormatException.from(p, "Invalid Point JSON. Expected {x:number, y:number}", node, Point.class);
        }
        double x = xNode.asDouble();
        double y = yNode.asDouble();
        return Point.of(x, y);
    }
}

