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
      <E535AccountPostingResponse xmlns="urn:demo:rcbs:e535">
        <requestId>%s</requestId>
        <status>SUCCESS</status>
        <reference>%s</reference>
        <message>Posted</message>
        <e535Reference>%s</e535Reference>
      </E535AccountPostingResponse>
      """.formatted(escapeXml(requestId), escapeXml(ref), escapeXml(ref));
    }

    @PostMapping(
            value = "/eportal/E536",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public String postE536(@RequestBody String requestXml) throws Exception {
        return flavoredResponse("E536", requestXml);
    }

    @PostMapping(
            value = "/eportal/E537",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public String postE537(@RequestBody String requestXml) throws Exception {
        return flavoredResponse("E537", requestXml);
    }

    @PostMapping(
            value = "/eportal/E538",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public String postE538(@RequestBody String requestXml) throws Exception {
        return flavoredResponse("E538", requestXml);
    }

    @PostMapping(
            value = "/eportal/E539",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE},
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public String postE539(@RequestBody String requestXml) throws Exception {
        return flavoredResponse("E539", requestXml);
    }

    private static String flavoredResponse(String api, String requestXml) throws Exception {
        Thread.sleep(200);
        String requestId = extractRequestId(requestXml);
        String ref = api + "-" + UUID.randomUUID().toString().substring(0, 8);
        String ns = "urn:demo:rcbs:" + api.toLowerCase();
        String local = api.toLowerCase() + "Reference";
        return """
      <%sAccountPostingResponse xmlns="%s">
        <requestId>%s</requestId>
        <status>SUCCESS</status>
        <reference>%s</reference>
        <message>Posted</message>
        <%s>%s</%s>
      </%sAccountPostingResponse>
      """.formatted(api, ns, escapeXml(requestId), escapeXml(ref), local, escapeXml(ref), local, api);
    }

    private static String extractRequestId(String xml) {
        Matcher m = REQUEST_ID.matcher(xml);
        return m.find() ? m.group(1).trim() : "UNKNOWN";
    }

    private static String escapeXml(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
