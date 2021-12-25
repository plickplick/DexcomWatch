package link.halen.dexcomwatch;

import android.os.SystemClock;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

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
    private String serverUS = "share2.dexcom.com";
    private String serverEU = "shareous1.dexcom.com";
    private String applicationId = "d89443d2-327c-4a6f-89e5-496bbb0317db";
    private String agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36";
    private String authUrl = "https://" + serverEU + "/ShareWebServices/Services/General/AuthenticatePublisherAccount";
    private String loginUrl = "https://" + serverEU + "/ShareWebServices/Services/General/LoginPublisherAccountById";
    private String accept = "application/json";
    private String contentType = "application/json";
    private String LatestGlucose = "https://" + serverEU + "/ShareWebServices/Services/Publisher/ReadPublisherLatestGlucoseValues";
    private String accountName = "patrik.halen";
    private String password = "aikaiK89";
    private TextView textView;
    private String sessionId;

    public GlucoseRetreiver(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void run() {
        getGlucose();
    }

    private void getGlucose() {
        if (sessionId == null) {
            sessionId = authorize();
        }
        try {
            //String url = LatestGlucose + "?" + payloadGlucose(sessionId);
            //log.info("Glucose url: " + url);
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
                getGlucose();
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
            SystemClock.sleep(duration);
            getGlucose();
        } catch (Exception e) {
            log.error("Not being able to retreive glucose: " + e.getMessage());
        }
        getGlucose();
    }
//            HttpClient client = HttpClients.custom().build();
//            HttpUriRequest request = RequestBuilder.post()
//                    .setUri(new URI(LatestGlucose))
//                    .setHeader(HttpHeaders.CONTENT_TYPE, contentType)
//                    .setHeader(HttpHeaders.USER_AGENT, agent)
//                    .setHeader(HttpHeaders.ACCEPT, accept)
//                    .setEntity(new StringEntity(payloadGlucose(sessionId), contentType, "UTF-8"))
//                    .build();
//            HttpResponse response = client.execute(request);
//            if (response.getStatusLine().getStatusCode() > 399) {
//                sessionId = null;
//                getGlucose();
//            }
//            String glucose = EntityUtils.toString(response.getEntity());
//            log.info("Glucose: " + glucose);


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
        //return "{\"password\": " + "\"" + password + "\"" + ", \"applicationId\": " + "\"" + applicationId + "\"" + ", \"accountName\": " + "\"" + accountName + "\"" + "}";
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
