package demo.server;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class PostingController {

    private static final Pattern REQUEST_ID =
            Pattern.compile("<requestId>(.*?)</requestId>", Pattern.DOTALL);

    @PostMapping(
            value = "/post",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public String post(@RequestBody String requestXml) throws Exception {
        // Simulate slow downstream latency
        Thread.sleep(200);

        String requestId = extractRequestId(requestXml);
        String ref = "REF-" + UUID.randomUUID().toString().substring(0, 8);

        return """
      <AccountPostingResponse xmlns="urn:demo:rcbs">
        <requestId>%s</requestId>
        <status>SUCCESS</status>
        <reference>%s</reference>
        <message>Posted</message>
      </AccountPostingResponse>
      """.formatted(escapeXml(requestId), escapeXml(ref));
    }

    @PostMapping(
            value = "/eportal/E535",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public String postE535(@RequestBody String requestXml) throws Exception {
        Thread.sleep(200);
        String requestId = extractRequestId(requestXml);
        String ref = "E535-" + UUID.randomUUID().toString().substring(0, 8);

        return """
      <E535AccountPostingResponse xmlns="urn:demo:rcbs:e535" xmlns:c="urn:demo:rcbs">
        <c:requestId>%s</c:requestId>
        <c:status>SUCCESS</c:status>
        <c:reference>%s</c:reference>
        <c:message>Posted</c:message>
        <e535Reference>%s</e535Reference>
      </E535AccountPostingResponse>
      """.formatted(escapeXml(requestId), escapeXml(ref), escapeXml(ref));
    }

    private static String extractRequestId(String xml) {
        Matcher m = REQUEST_ID.matcher(xml);
        return m.find() ? m.group(1).trim() : "UNKNOWN";
    }

    private static String escapeXml(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
