package link.halen.dexcomwatch.pojos;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginProps {
    private String password;
    private String applicationId;
    private String accountId;
}
