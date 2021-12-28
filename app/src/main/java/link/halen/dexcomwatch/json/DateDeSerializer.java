package link.halen.dexcomwatch.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateDeSerializer extends StdDeserializer<LocalDateTime> {

    protected DateDeSerializer() {
        this(null);
    }

    protected DateDeSerializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String stringToParse = jsonParser.getText();
        if(stringToParse.contains("+")) {
            stringToParse = stringToParse.substring(0, stringToParse.indexOf("+"));
        }
        stringToParse = stringToParse.replaceAll("\\D","");
        return Instant.ofEpochMilli(Long.valueOf(stringToParse)).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
