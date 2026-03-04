package demo.rcbs;

import com.sun.net.httpserver.HttpServer;
import demo.rcbs.xml.E535AccountPostingRequest;
import demo.rcbs.xml.E535AccountPostingResponse;
import demo.rcbs.xml.E536AccountPostingRequest;
import demo.rcbs.xml.E536AccountPostingResponse;
import demo.rcbs.xml.E537AccountPostingRequest;
import demo.rcbs.xml.E537AccountPostingResponse;
import demo.rcbs.xml.E538AccountPostingRequest;
import demo.rcbs.xml.E538AccountPostingResponse;
import demo.rcbs.xml.E539AccountPostingRequest;
import demo.rcbs.xml.E539AccountPostingResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiApiConcurrentThroughputIntegrationTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @Timeout(30)
    void shouldHandleConcurrentBurstAcrossAllApiTypes() throws Exception {
        AtomicInteger e535Hits = new AtomicInteger();
        AtomicInteger e536Hits = new AtomicInteger();
        AtomicInteger e537Hits = new AtomicInteger();
        AtomicInteger e538Hits = new AtomicInteger();
        AtomicInteger e539Hits = new AtomicInteger();

        int port = startServer(Map.of(
                "E535", e535Hits,
                "E536", e536Hits,
                "E537", e537Hits,
                "E538", e538Hits,
                "E539", e539Hits
        ));

        try (DefaultRcbsClient client = buildClient(port)) {
            int totalRequests = 100;
            List<CompletableFuture<String>> futures = new ArrayList<>(totalRequests);
            List<RcbsApiType> apiTypes = List.of(
                    RcbsApiType.E535, RcbsApiType.E536, RcbsApiType.E537, RcbsApiType.E538, RcbsApiType.E539);

            long startNs = System.nanoTime();
            for (int i = 0; i < totalRequests; i++) {
                RcbsApiType apiType = apiTypes.get(i % apiTypes.size());
                String requestId = apiType.name() + "-REQ-" + i;
                futures.add(client.postAsync(apiType, requestXml(apiType, requestId)));
            }

            int completed = 0;
            for (CompletableFuture<String> future : futures) {
                try {
                    String response = future.get(8, TimeUnit.SECONDS);
                    assertTrue(response.contains("SUCCESS"));
                    completed++;
                } catch (ExecutionException e) {
                    throw (e.getCause() instanceof Exception ex) ? ex : e;
                }
            }
            long elapsedMs = Duration.ofNanos(System.nanoTime() - startNs).toMillis();
            double tps = totalRequests / (elapsedMs / 1000.0d);

            System.out.println("Multi-API concurrent test: completed=" + completed
                    + ", elapsedMs=" + elapsedMs + ", tps=" + tps
                    + ", hits={E535=" + e535Hits.get()
                    + ",E536=" + e536Hits.get()
                    + ",E537=" + e537Hits.get()
                    + ",E538=" + e538Hits.get()
                    + ",E539=" + e539Hits.get() + "}");

            assertEquals(totalRequests, completed, "Expected all concurrent requests to complete");
            assertEquals(20, e535Hits.get());
            assertEquals(20, e536Hits.get());
            assertEquals(20, e537Hits.get());
            assertEquals(20, e538Hits.get());
            assertEquals(20, e539Hits.get());

            // Baseline asked by user is 5 TPS. This demonstrates concurrency across mixed APIs.
            assertTrue(tps > 5.0d, "Expected > 5 TPS for mixed concurrent API load");
            // Sequential would be ~20s for 100 calls at 200ms. This proves concurrent execution.
            assertTrue(elapsedMs < 15_000, "Expected concurrent completion significantly faster than sequential");
        }
    }

    private DefaultRcbsClient buildClient(int port) {
        E535PostingService e535 = new E535PostingService(
                new RcbsPostingService(cfg(port, "E535")),
                converterReq("E535"),
                converterRes("E535")
        );
        E536PostingService e536 = new E536PostingService(
                new RcbsPostingService(cfg(port, "E536")),
                converterReq("E536"),
                converterRes("E536")
        );
        E537PostingService e537 = new E537PostingService(
                new RcbsPostingService(cfg(port, "E537")),
                converterReq("E537"),
                converterRes("E537")
        );
        E538PostingService e538 = new E538PostingService(
                new RcbsPostingService(cfg(port, "E538")),
                converterReq("E538"),
                converterRes("E538")
        );
        E539PostingService e539 = new E539PostingService(
                new RcbsPostingService(cfg(port, "E539")),
                converterReq("E539"),
                converterRes("E539")
        );
        return new DefaultRcbsClient(List.of(e535, e536, e537, e538, e539));
    }

    private RcbsClientConfig cfg(int port, String api) {
        return new RcbsClientConfig(
                "http://localhost:" + port + "/eportal/" + api,
                100, 100, 2000, 8000, 32, 5000, 120, 500
        );
    }

    @SuppressWarnings("unchecked")
    private <T> XmlObjectConverterWithJaxbElement<T> converterReq(String api) {
        Jaxb2Marshaller m = marshaller(api);
        return switch (api) {
            case "E535" -> (XmlObjectConverterWithJaxbElement<T>) new XmlObjectConverterWithJaxbElement<>(E535AccountPostingRequest.class, m, m);
            case "E536" -> (XmlObjectConverterWithJaxbElement<T>) new XmlObjectConverterWithJaxbElement<>(E536AccountPostingRequest.class, m, m);
            case "E537" -> (XmlObjectConverterWithJaxbElement<T>) new XmlObjectConverterWithJaxbElement<>(E537AccountPostingRequest.class, m, m);
            case "E538" -> (XmlObjectConverterWithJaxbElement<T>) new XmlObjectConverterWithJaxbElement<>(E538AccountPostingRequest.class, m, m);
            case "E539" -> (XmlObjectConverterWithJaxbElement<T>) new XmlObjectConverterWithJaxbElement<>(E539AccountPostingRequest.class, m, m);
            default -> throw new IllegalArgumentException("Unsupported api " + api);
        };
    }

    @SuppressWarnings("unchecked")
    private <T> XmlObjectConverterWithJaxbElement<T> converterRes(String api) {
        Jaxb2Marshaller m = marshaller(api);
        return switch (api) {
            case "E535" -> (XmlObjectConverterWithJaxbElement<T>) new XmlObjectConverterWithJaxbElement<>(E535AccountPostingResponse.class, m, m);
            case "E536" -> (XmlObjectConverterWithJaxbElement<T>) new XmlObjectConverterWithJaxbElement<>(E536AccountPostingResponse.class, m, m);
            case "E537" -> (XmlObjectConverterWithJaxbElement<T>) new XmlObjectConverterWithJaxbElement<>(E537AccountPostingResponse.class, m, m);
            case "E538" -> (XmlObjectConverterWithJaxbElement<T>) new XmlObjectConverterWithJaxbElement<>(E538AccountPostingResponse.class, m, m);
            case "E539" -> (XmlObjectConverterWithJaxbElement<T>) new XmlObjectConverterWithJaxbElement<>(E539AccountPostingResponse.class, m, m);
            default -> throw new IllegalArgumentException("Unsupported api " + api);
        };
    }

    private Jaxb2Marshaller marshaller(String api) {
        Jaxb2Marshaller m = new Jaxb2Marshaller();
        switch (api) {
            case "E535" -> {
                m.setClassesToBeBound(E535AccountPostingRequest.class, E535AccountPostingResponse.class);
                m.setSchemas(new ClassPathResource("xsd/e535/account-posting-e535.xsd"));
            }
            case "E536" -> {
                m.setClassesToBeBound(E536AccountPostingRequest.class, E536AccountPostingResponse.class);
                m.setSchemas(new ClassPathResource("xsd/e536/account-posting-e536.xsd"));
            }
            case "E537" -> {
                m.setClassesToBeBound(E537AccountPostingRequest.class, E537AccountPostingResponse.class);
                m.setSchemas(new ClassPathResource("xsd/e537/account-posting-e537.xsd"));
            }
            case "E538" -> {
                m.setClassesToBeBound(E538AccountPostingRequest.class, E538AccountPostingResponse.class);
                m.setSchemas(new ClassPathResource("xsd/e538/account-posting-e538.xsd"));
            }
            case "E539" -> {
                m.setClassesToBeBound(E539AccountPostingRequest.class, E539AccountPostingResponse.class);
                m.setSchemas(new ClassPathResource("xsd/e539/account-posting-e539.xsd"));
            }
            default -> throw new IllegalArgumentException("Unsupported api " + api);
        }
        return m;
    }

    private int startServer(Map<String, AtomicInteger> hits) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setExecutor(Executors.newFixedThreadPool(64));
        for (String api : List.of("E535", "E536", "E537", "E538", "E539")) {
            String path = "/eportal/" + api;
            server.createContext(path, exchange -> {
                hits.get(api).incrementAndGet();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    exchange.sendResponseHeaders(500, -1);
                    exchange.close();
                    return;
                }
                String requestId = extractRequestId(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                String lower = api.toLowerCase();
                String response = """
                        <%sAccountPostingResponse xmlns="urn:demo:rcbs:%s">
                          <requestId>%s</requestId>
                          <status>SUCCESS</status>
                          <reference>%s-REF</reference>
                          <message>Posted</message>
                          <%sReference>%s-R</%sReference>
                        </%sAccountPostingResponse>
                        """.formatted(api, lower, escapeXml(requestId), api, lower, api, lower, api);
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/xml");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            });
        }
        server.start();
        return server.getAddress().getPort();
    }

    private static String requestXml(RcbsApiType apiType, String requestId) {
        String api = apiType.name();
        String lower = api.toLowerCase();
        return """
                <%sAccountPostingRequest xmlns="urn:demo:rcbs:%s">
                  <requestId>%s</requestId>
                  <amount>1.00</amount>
                  <tenantId>T-%s</tenantId>
                </%sAccountPostingRequest>
                """.formatted(api, lower, requestId, api, api);
    }

    private static String extractRequestId(String xml) {
        int start = xml.indexOf("<requestId>");
        int end = xml.indexOf("</requestId>");
        if (start < 0 || end < 0 || end <= start) return "UNKNOWN";
        return xml.substring(start + "<requestId>".length(), end).trim();
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
