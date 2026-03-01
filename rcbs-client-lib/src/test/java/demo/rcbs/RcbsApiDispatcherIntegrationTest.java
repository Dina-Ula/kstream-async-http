package demo.rcbs;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RcbsApiDispatcherIntegrationTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @Timeout(20)
    void shouldRouteByRequestTypeToDifferentEndpoints() throws Exception {
        AtomicInteger coreHits = new AtomicInteger();
        AtomicInteger e535Hits = new AtomicInteger();
        int port = startServer(coreHits, e535Hits);
        String baseUrl = "http://localhost:" + port;

        RcbsClientConfig coreCfg = config(baseUrl + "/post");
        RcbsClientConfig e535Cfg = config(baseUrl + "/eportal/E535");

        try (RcbsApiDispatcher dispatcher = new RcbsApiDispatcher(List.of(
                new CoreRcbsApiHandler(coreCfg),
                new E535RcbsApiHandler(e535Cfg)
        ))) {
            String coreResp = dispatcher.postAsync(coreRequestXml("REQ-C1")).get(5, TimeUnit.SECONDS);
            String e535Resp = dispatcher.postAsync(e535RequestXml("REQ-E1")).get(5, TimeUnit.SECONDS);

            assertTrue(coreResp.contains("AccountPostingResponse"));
            assertTrue(e535Resp.contains("E535AccountPostingResponse"));
            assertEquals(1, coreHits.get());
            assertEquals(1, e535Hits.get());
        }
    }

    @Test
    void shouldFailForUnknownRequestRoot() throws Exception {
        try (RcbsApiDispatcher dispatcher = new RcbsApiDispatcher(List.of())) {
            ExecutionException ex = org.junit.jupiter.api.Assertions.assertThrows(
                    ExecutionException.class,
                    () -> dispatcher.postAsync("<Unknown/>").get()
            );
            assertTrue(ex.getCause().getMessage().contains("No RcbsApiHandler configured"));
        }
    }

    private static RcbsClientConfig config(String url) {
        return new RcbsClientConfig(url, 50, 50, 2000, 5000, 8, 1000, 20, 100);
    }

    private int startServer(AtomicInteger coreHits, AtomicInteger e535Hits) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/post", exchange -> {
            coreHits.incrementAndGet();
            String reqId = "REQ-UNKNOWN";
            String body = """
                    <AccountPostingResponse xmlns="urn:demo:rcbs">
                      <requestId>%s</requestId>
                      <status>SUCCESS</status>
                      <reference>REF-C</reference>
                      <message>Posted</message>
                    </AccountPostingResponse>
                    """.formatted(reqId);
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.createContext("/eportal/E535", exchange -> {
            e535Hits.incrementAndGet();
            String reqId = "REQ-UNKNOWN";
            String body = """
                    <E535AccountPostingResponse xmlns="urn:demo:rcbs:e535" xmlns:c="urn:demo:rcbs">
                      <c:requestId>%s</c:requestId>
                      <c:status>SUCCESS</c:status>
                      <c:reference>REF-E</c:reference>
                      <c:message>Posted</c:message>
                      <e535Reference>E535-REF</e535Reference>
                    </E535AccountPostingResponse>
                    """.formatted(reqId);
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        return server.getAddress().getPort();
    }

    private static String coreRequestXml(String requestId) {
        return """
                <AccountPostingRequest xmlns="urn:demo:rcbs">
                  <requestId>%s</requestId>
                  <amount>1.00</amount>
                </AccountPostingRequest>
                """.formatted(requestId);
    }

    private static String e535RequestXml(String requestId) {
        return """
                <E535AccountPostingRequest xmlns="urn:demo:rcbs:e535" xmlns:c="urn:demo:rcbs">
                  <c:requestId>%s</c:requestId>
                  <c:amount>1.00</c:amount>
                  <tenantId>T-1</tenantId>
                </E535AccountPostingRequest>
                """.formatted(requestId);
    }
}
