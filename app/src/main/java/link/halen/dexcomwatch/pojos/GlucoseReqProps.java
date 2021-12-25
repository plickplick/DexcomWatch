package link.halen.dexcomwatch.pojos;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GlucoseReqProps {
    private String sessionId;
    private String minutes;
    private String maxCount;
}
