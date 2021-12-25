package link.halen.dexcomwatch.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class GlucoseDeSerializer   extends StdDeserializer<String> {

    protected GlucoseDeSerializer() {
        this(null);
    }

    protected GlucoseDeSerializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        Double doubleToParse = Double.valueOf(jsonParser.getText());
        NumberFormat formatter = new DecimalFormat("#0.0");
        return formatter.format(doubleToParse/18);
    }
}