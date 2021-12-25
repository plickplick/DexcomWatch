package link.halen.dexcomwatch.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

import link.halen.dexcomwatch.pojos.Trend;

public class TrendDeSerializer  extends StdDeserializer<Trend> {

    protected TrendDeSerializer() {
        this(null);
    }

    protected TrendDeSerializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Trend deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String stringToParse = jsonParser.getText();
        stringToParse = stringToParse.toLowerCase().replace(" ","");
        return Trend.valueOfName(stringToParse);
    }
}
