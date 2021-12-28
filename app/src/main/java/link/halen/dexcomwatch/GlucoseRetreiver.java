package link.halen.dexcomwatch;

import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import link.halen.dexcomwatch.pojos.AccountIdProps;
import link.halen.dexcomwatch.pojos.GlucoseReqProps;
import link.halen.dexcomwatch.pojos.GlucoseValue;
import link.halen.dexcomwatch.pojos.LoginProps;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class GlucoseRetreiver implements Runnable {

    private final String serverUS = "share2.dexcom.com";
    private final String serverEU = "shareous1.dexcom.com";
    private final String applicationId = "d89443d2-327c-4a6f-89e5-496bbb0317db";
    private final String agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36";
    private final String authUrl = "https://" + serverEU + "/ShareWebServices/Services/General/AuthenticatePublisherAccount";
    private final String loginUrl = "https://" + serverEU + "/ShareWebServices/Services/General/LoginPublisherAccountById";
    private final String accept = "application/json";
    private final String contentType = "application/json";
    private final String LatestGlucose = "https://" + serverEU + "/ShareWebServices/Services/Publisher/ReadPublisherLatestGlucoseValues";
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private String accountName;
    private String password;
    private TextView textView;
    private String sessionId;


    public GlucoseRetreiver(TextView textView) {
        this.textView = textView;
    }

    public void setAccountName(String accountName) {
        sessionId = null;
        this.accountName = accountName;
    }

    public void setPassword(String password) {
        sessionId = null;
        this.password = password;
    }

    @Override
    public void run() {
        running.set(true);
        stopped.set(false);
        while (running.get()) {
            if (sessionId == null) {
                sessionId = authorize();
            }
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(LatestGlucose)
                        .addHeader("Content-Type", contentType)
                        .addHeader("User-Agent", agent)
                        .addHeader("Accept", accept)
                        .post(RequestBody.create(payloadGlucose(sessionId), MediaType.get("application/json; charset=utf-8")))
                        .build();
                Response response = client.newCall(request).execute();
                if (response.code() > 399) {
                    sessionId = null;
                    break;
                }
                String glucose = response.body().string();
                log.info("Glucose: " + glucose);
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                List<GlucoseValue> glucoseValue = objectMapper.readValue(glucose, new TypeReference<ArrayList<GlucoseValue>>() {
                });
                glucoseValue.forEach(g -> {
                    log.info("Object: " + g);
                    textView.post(() -> textView.setText(g.getValue()));
                });
                long duration = calculateDurationNextExecution(glucoseValue.get(0));
                log.info("Time in millis to next call: " + duration);
                Thread.sleep(duration);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("stopped retreiving data.");
            } catch (Exception ex) {
                log.error("Not being able to retreive glucose: " + ex.getMessage());
            }
        }
        stopped.set(true);
    }


    public void interrupt() {
        running.set(false);
        this.interrupt();
    }

    boolean isRunning() {
        return running.get();
    }

    boolean isStopped() {
        return stopped.get();
    }

    private long calculateDurationNextExecution(GlucoseValue glucoseValue) {
        long oldTime = glucoseValue.getWt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long current = System.currentTimeMillis();
        long newTime = 10000 + (oldTime + (5 * 60 * 1000)) - current;
        if (newTime < 10000l) {
            newTime = 10000l;
        }
        return newTime;
    }

    // Login to Dexcom's server.
    public String authorize() {
        String accountId = getAccountId();
        if (accountId != null) {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(loginUrl)
                        .addHeader("Content-Type", contentType)
                        .addHeader("User-Agent", agent)
                        .addHeader("Accept", accept)
                        .post(RequestBody.create(payloadLogin(accountId), MediaType.get("application/json; charset=utf-8")))
                        .build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String sessionId = response.body().string().replace("\"", "");
                    log.info("Found sessionId: " + sessionId);
                    return sessionId;
                }
            } catch (Exception e) {
                log.error("Application error; " + e.getMessage());
            }
        }
        return null;
    }

    private String getAccountId() {
        Response response = null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(authUrl)
                    .addHeader("Content-Type", contentType)
                    .addHeader("User-Agent", agent)
                    .addHeader("Accept", accept)
                    .post(RequestBody.create(payload(), MediaType.get("application/json; charset=utf-8")))
                    .build();
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String accountId = response.body().string().replace("\"", "");
                log.info("Found accountId: " + accountId);
                return accountId;
            }
        } catch (Exception e) {
            log.error("Application error: " + e.getMessage());
        }
        return null;
    }

    private String payload() throws JsonProcessingException {
        AccountIdProps accountId = AccountIdProps.builder().accountName(accountName)
                .password(password)
                .applicationId(applicationId).build();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(accountId);
        log.info("AccountId Props: " + json);
        return json;
    }

    private String payloadLogin(String accountId) throws JsonProcessingException {
        LoginProps loginProps = LoginProps.builder().accountId(accountId)
                .password(password)
                .applicationId(applicationId).build();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(loginProps);
        log.info("AccountId Props: " + json);
        return json;
    }

    private String payloadGlucose(String sessionId) throws JsonProcessingException {
        GlucoseReqProps glucoseReqProps = GlucoseReqProps.builder().sessionId(sessionId)
                .minutes("1440")
                .maxCount("1").build();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(glucoseReqProps);
        log.info("Glucose Props: " + json);
        return json;
    }
}
