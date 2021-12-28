package link.halen.dexcomwatch.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;

import link.halen.dexcomwatch.json.DateDeSerializer;
import link.halen.dexcomwatch.json.GlucoseDeSerializer;
import link.halen.dexcomwatch.json.TrendDeSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class GlucoseValue {
    @JsonDeserialize(using = DateDeSerializer.class)
    @JsonProperty("WT")
    private LocalDateTime wt;
    @JsonDeserialize(using = DateDeSerializer.class)
    @JsonProperty("ST")
    private LocalDateTime st;
    @JsonDeserialize(using = DateDeSerializer.class)
    @JsonProperty("DT")
    private LocalDateTime dt;
    @JsonDeserialize(using = GlucoseDeSerializer.class)
    @JsonProperty("Value")
    private String value;
    @JsonDeserialize(using = TrendDeSerializer.class)
    @JsonProperty("Trend")
    private Trend trend;
}

