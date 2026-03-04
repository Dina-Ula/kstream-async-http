package demo.rcbs;

import com.sun.net.httpserver.HttpServer;
import demo.rcbs.xml.E535AccountPostingRequest;
import demo.rcbs.xml.E535AccountPostingResponse;
import demo.rcbs.xml.E536AccountPostingRequest;
import demo.rcbs.xml.E536AccountPostingResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RcbsClientRoutingIntegrationTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @Timeout(20)
    void shouldRouteByApiTypeAndUseE535Converters() throws Exception {
        AtomicInteger e535Hits = new AtomicInteger();
        AtomicInteger e536Hits = new AtomicInteger();
        int port = startServer(e535Hits, e536Hits);
        RcbsClientConfig e535Cfg = new RcbsClientConfig(
                "http://localhost:" + port + "/eportal/E535",
                50, 50, 2000, 5000, 8, 1000, 20, 100
        );
        RcbsClientConfig e536Cfg = new RcbsClientConfig(
                "http://localhost:" + port + "/eportal/E536",
                50, 50, 2000, 5000, 8, 1000, 20, 100
        );

        Jaxb2Marshaller marshaller = e535Marshaller();
        XmlObjectConverterWithJaxbElement<E535AccountPostingRequest> reqConverter =
                new XmlObjectConverterWithJaxbElement<>(E535AccountPostingRequest.class, marshaller, marshaller);
        XmlObjectConverterWithJaxbElement<E535AccountPostingResponse> resConverter =
                new XmlObjectConverterWithJaxbElement<>(E535AccountPostingResponse.class, marshaller, marshaller);

        E535PostingService e535Service = new E535PostingService(new RcbsPostingService(e535Cfg), reqConverter, resConverter);
        Jaxb2Marshaller e536Marshaller = e536Marshaller();
        XmlObjectConverterWithJaxbElement<E536AccountPostingRequest> e536ReqConverter =
                new XmlObjectConverterWithJaxbElement<>(E536AccountPostingRequest.class, e536Marshaller, e536Marshaller);
        XmlObjectConverterWithJaxbElement<E536AccountPostingResponse> e536ResConverter =
                new XmlObjectConverterWithJaxbElement<>(E536AccountPostingResponse.class, e536Marshaller, e536Marshaller);
        E536PostingService e536Service = new E536PostingService(new RcbsPostingService(e536Cfg), e536ReqConverter, e536ResConverter);

        try (DefaultRcbsClient client = new DefaultRcbsClient(List.of(e535Service, e536Service))) {
            String response = client.postAsync(RcbsApiType.E535, e535RequestXml("REQ-E1"))
                    .get(5, TimeUnit.SECONDS);
            String e536Response = client.postAsync(RcbsApiType.E536, e536RequestXml("REQ-E2"))
                    .get(5, TimeUnit.SECONDS);
            assertTrue(response.contains("E535AccountPostingResponse"));
            assertTrue(response.contains("<requestId>REQ-E1</requestId>"));
            assertTrue(e536Response.contains("E536AccountPostingResponse"));
            assertTrue(e536Response.contains("<requestId>REQ-E2</requestId>"));
            assertEquals(1, e535Hits.get());
            assertEquals(1, e536Hits.get());
        }
    }

    @Test
    @Timeout(20)
    void shouldRouteByApiTypeSynchronously() throws Exception {
        AtomicInteger e535Hits = new AtomicInteger();
        AtomicInteger e536Hits = new AtomicInteger();
        int port = startServer(e535Hits, e536Hits);
        RcbsClientConfig e535Cfg = new RcbsClientConfig(
                "http://localhost:" + port + "/eportal/E535",
                50, 50, 2000, 5000, 8, 1000, 20, 100
        );
        RcbsClientConfig e536Cfg = new RcbsClientConfig(
                "http://localhost:" + port + "/eportal/E536",
                50, 50, 2000, 5000, 8, 1000, 20, 100
        );

        Jaxb2Marshaller marshaller = e535Marshaller();
        XmlObjectConverterWithJaxbElement<E535AccountPostingRequest> reqConverter =
                new XmlObjectConverterWithJaxbElement<>(E535AccountPostingRequest.class, marshaller, marshaller);
        XmlObjectConverterWithJaxbElement<E535AccountPostingResponse> resConverter =
                new XmlObjectConverterWithJaxbElement<>(E535AccountPostingResponse.class, marshaller, marshaller);

        E535PostingService e535Service = new E535PostingService(new RcbsPostingService(e535Cfg), reqConverter, resConverter);
        Jaxb2Marshaller e536Marshaller = e536Marshaller();
        XmlObjectConverterWithJaxbElement<E536AccountPostingRequest> e536ReqConverter =
                new XmlObjectConverterWithJaxbElement<>(E536AccountPostingRequest.class, e536Marshaller, e536Marshaller);
        XmlObjectConverterWithJaxbElement<E536AccountPostingResponse> e536ResConverter =
                new XmlObjectConverterWithJaxbElement<>(E536AccountPostingResponse.class, e536Marshaller, e536Marshaller);
        E536PostingService e536Service = new E536PostingService(new RcbsPostingService(e536Cfg), e536ReqConverter, e536ResConverter);

        try (DefaultRcbsClient client = new DefaultRcbsClient(List.of(e535Service, e536Service))) {
            String e535Response = client.postSync(RcbsApiType.E535, e535RequestXml("REQ-E1"));
            String e536Response = client.postSync(RcbsApiType.E536, e536RequestXml("REQ-E2"));

            assertTrue(e535Response.contains("E535AccountPostingResponse"));
            assertTrue(e535Response.contains("<requestId>REQ-E1</requestId>"));
            assertTrue(e536Response.contains("E536AccountPostingResponse"));
            assertTrue(e536Response.contains("<requestId>REQ-E2</requestId>"));
            assertEquals(1, e535Hits.get());
            assertEquals(1, e536Hits.get());
        }
    }

    @Test
    void shouldFailForUnsupportedApiType() throws Exception {
        try (DefaultRcbsClient client = new DefaultRcbsClient(List.of())) {
            ExecutionException ex = org.junit.jupiter.api.Assertions.assertThrows(
                    ExecutionException.class,
                    () -> client.postAsync(RcbsApiType.E535, "<x/>").get()
            );
            assertTrue(ex.getCause().getMessage().contains("No posting service configured"));
            IllegalArgumentException syncEx = assertThrows(
                    IllegalArgumentException.class,
                    () -> client.postSync(RcbsApiType.E535, "<x/>")
            );
            assertTrue(syncEx.getMessage().contains("No posting service configured"));
        }
    }

    private int startServer(AtomicInteger e535Hits, AtomicInteger e536Hits) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/eportal/E535", exchange -> {
            e535Hits.incrementAndGet();
            String body = """
                    <E535AccountPostingResponse xmlns="urn:demo:rcbs:e535">
                      <requestId>REQ-E1</requestId>
                      <status>SUCCESS</status>
                      <reference>REF-E</reference>
                      <message>Posted</message>
                      <e535Reference>E535-REF</e535Reference>
                    </E535AccountPostingResponse>
                    """;
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.createContext("/eportal/E536", exchange -> {
            e536Hits.incrementAndGet();
            String body = """
                    <E536AccountPostingResponse xmlns="urn:demo:rcbs:e536">
                      <requestId>REQ-E2</requestId>
                      <status>SUCCESS</status>
                      <reference>REF-E2</reference>
                      <message>Posted</message>
                      <e536Reference>E536-REF</e536Reference>
                    </E536AccountPostingResponse>
                    """;
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        return server.getAddress().getPort();
    }

    private static Jaxb2Marshaller e535Marshaller() {
        Jaxb2Marshaller m = new Jaxb2Marshaller();
        m.setClassesToBeBound(E535AccountPostingRequest.class, E535AccountPostingResponse.class);
        m.setSchemas(new ClassPathResource("xsd/e535/account-posting-e535.xsd"));
        return m;
    }

    private static Jaxb2Marshaller e536Marshaller() {
        Jaxb2Marshaller m = new Jaxb2Marshaller();
        m.setClassesToBeBound(E536AccountPostingRequest.class, E536AccountPostingResponse.class);
        m.setSchemas(new ClassPathResource("xsd/e536/account-posting-e536.xsd"));
        return m;
    }

    private static String e535RequestXml(String requestId) {
        return """
                <E535AccountPostingRequest xmlns="urn:demo:rcbs:e535">
                  <requestId>%s</requestId>
                  <amount>1.00</amount>
                  <tenantId>T-1</tenantId>
                </E535AccountPostingRequest>
                """.formatted(requestId);
    }

    private static String e536RequestXml(String requestId) {
        return """
                <E536AccountPostingRequest xmlns="urn:demo:rcbs:e536">
                  <requestId>%s</requestId>
                  <amount>1.00</amount>
                  <tenantId>T-2</tenantId>
                </E536AccountPostingRequest>
                """.formatted(requestId);
    }
}
