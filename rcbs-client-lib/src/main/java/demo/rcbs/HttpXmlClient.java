package demo.rcbs;

import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.pox.dom.DomPoxMessageFactory;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;
import org.springframework.xml.transform.StringSource;

import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.time.Duration;

public final class HttpXmlClient implements AutoCloseable {
    private final WebServiceTemplate template;

    public HttpXmlClient(int maxTotal, int maxPerRoute, int connectMs, int readMs) {
        DomPoxMessageFactory messageFactory = new DomPoxMessageFactory();
        HttpUrlConnectionMessageSender sender = new HttpUrlConnectionMessageSender();
        sender.setConnectionTimeout(Duration.ofMillis(connectMs));
        sender.setReadTimeout(Duration.ofMillis(readMs));

        this.template = new WebServiceTemplate(messageFactory);
        this.template.setMessageSender(sender);
    }

    public String postXml(String url, String xml) throws Exception {
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        template.sendSourceAndReceiveToResult(url, new StringSource(xml), result);
        return writer.toString();
    }

    @Override public void close() throws Exception {
        // no-op for HttpUrlConnectionMessageSender
    }
}
