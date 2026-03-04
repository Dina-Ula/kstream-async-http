package demo.rcbs;

import com.sun.net.httpserver.HttpServer;
import demo.rcbs.xml.E535AccountPostingRequest;
import demo.rcbs.xml.E535AccountPostingResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RcbsSingletonJaxbSyncConcurrencyFailureIntegrationTest {

    private static final Pattern REQUEST_ID =
            Pattern.compile("<requestId>(.*?)</requestId>", Pattern.DOTALL);

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @Timeout(30)
    void concurrentPostSyncUsingSingletonJaxbShouldFail() throws Exception {
        int port = startServer();
        try (DefaultRcbsClient client = buildClient(port)) {
            ExecutorService pool = Executors.newFixedThreadPool(64);
            try {
                int tasks = 1200;
                List<Callable<Void>> work = new ArrayList<>(tasks);
                AtomicInteger failures = new AtomicInteger();
                for (int i = 0; i < tasks; i++) {
                    final int id = i;
                    work.add(() -> {
                        String requestId = "SINGLETON-REQ-" + id;
                        String requestXml = requestXml(requestId);
                        try {
                            String responseXml = client.postSyncUsingSingletonJaxb(RcbsApiType.E535, requestXml);
                            if (!responseXml.contains("E535AccountPostingResponse")) {
                                failures.incrementAndGet();
                            }
                        } catch (Exception ex) {
                            failures.incrementAndGet();
                        }
                        return null;
                    });
                }

                List<Future<Void>> futures = pool.invokeAll(work);
                for (Future<Void> future : futures) {
                    try {
                        future.get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        failures.incrementAndGet();
                    }
                }

                assertTrue(
                        failures.get() > 0,
                        "Expected at least one failure under concurrent postSyncUsingSingletonJaxb calls"
                );
            } finally {
                pool.shutdownNow();
            }
        }
    }

    private DefaultRcbsClient buildClient(int port) throws Exception {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(E535AccountPostingRequest.class, E535AccountPostingResponse.class);
        marshaller.setSchemas(new ClassPathResource("xsd/e535/account-posting-e535.xsd"));

        XmlObjectConverterWithJaxbElement<E535AccountPostingRequest> reqConverter =
                new XmlObjectConverterWithJaxbElement<>(E535AccountPostingRequest.class, marshaller, marshaller);
        XmlObjectConverterWithJaxbElement<E535AccountPostingResponse> resConverter =
                new XmlObjectConverterWithJaxbElement<>(E535AccountPostingResponse.class, marshaller, marshaller);

        var singletonMarshaller = marshaller.createMarshaller();
        var singletonUnmarshaller = marshaller.createUnmarshaller();
        SingletonJaxbXmlObjectConverterWithJaxbElement<E535AccountPostingRequest> singletonReqConverter =
                new SingletonJaxbXmlObjectConverterWithJaxbElement<>(
                        E535AccountPostingRequest.class,
                        singletonMarshaller,
                        singletonUnmarshaller
                );
        SingletonJaxbXmlObjectConverterWithJaxbElement<E535AccountPostingResponse> singletonResConverter =
                new SingletonJaxbXmlObjectConverterWithJaxbElement<>(
                        E535AccountPostingResponse.class,
                        singletonMarshaller,
                        singletonUnmarshaller
                );

        RcbsClientConfig cfg = new RcbsClientConfig(
                "http://localhost:" + port + "/eportal/E535",
                300, 300,
                2000, 8000,
                64, 10000,
                1000,
                10000
        );
        E535PostingService e535Service = new E535PostingService(
                new RcbsPostingService(cfg),
                reqConverter,
                resConverter,
                singletonReqConverter,
                singletonResConverter
        );
        return new DefaultRcbsClient(List.of(e535Service));
    }

    private int startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setExecutor(Executors.newFixedThreadPool(64));
        server.createContext("/eportal/E535", exchange -> {
            byte[] requestBytes;
            try (InputStream in = exchange.getRequestBody()) {
                requestBytes = in.readAllBytes();
            }
            String requestId = extractRequestId(new String(requestBytes, StandardCharsets.UTF_8));
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.sendResponseHeaders(500, -1);
                exchange.close();
                return;
            }

            String responseXml = """
                    <E535AccountPostingResponse xmlns="urn:demo:rcbs:e535">
                      <requestId>%s</requestId>
                      <status>SUCCESS</status>
                      <reference>REF-%s</reference>
                      <message>Posted</message>
                      <e535Reference>E535-%s</e535Reference>
                    </E535AccountPostingResponse>
                    """.formatted(escapeXml(requestId), escapeXml(requestId), escapeXml(requestId));

            byte[] bytes = responseXml.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        return server.getAddress().getPort();
    }

    private static String requestXml(String requestId) {
        return """
                <E535AccountPostingRequest xmlns="urn:demo:rcbs:e535">
                  <requestId>%s</requestId>
                  <amount>1.00</amount>
                  <tenantId>T1</tenantId>
                </E535AccountPostingRequest>
                """.formatted(requestId);
    }

    private static String extractRequestId(String xml) {
        Matcher m = REQUEST_ID.matcher(xml);
        return m.find() ? m.group(1).trim() : "UNKNOWN";
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
