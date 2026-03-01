package demo.rcbs;

import com.sun.net.httpserver.HttpServer;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RcbsPostingServiceRateLimitIntegrationTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @Timeout(20)
    void rateLimiterShouldRejectBurstBeyondConfiguredLimit() throws Exception {
        AtomicInteger httpCallCount = new AtomicInteger(0);
        int port = startFastServer(httpCallCount);
        String url = "http://localhost:" + port + "/post";

        int rateLimitPerSecond = 5;
        int totalRequests = 30;

        RcbsClientConfig cfg = new RcbsClientConfig(
                url,
                100, 100,
                2000, 5000,
                32, 1000,
                100,
                rateLimitPerSecond
        );

        int success = 0;
        int rateLimited = 0;

        try (RcbsPostingService service = new RcbsPostingService(cfg)) {
            List<CompletableFuture<String>> futures = new ArrayList<>();
            for (int i = 0; i < totalRequests; i++) {
                String id = "REQ-" + i;
                futures.add(service.postAsync(id, requestXml(id)));
            }

            for (CompletableFuture<String> future : futures) {
                try {
                    future.get(5, TimeUnit.SECONDS);
                    success++;
                } catch (ExecutionException e) {
                    Throwable root = rootCause(e);
                    if (root instanceof RequestNotPermitted) {
                        rateLimited++;
                    } else {
                        throw e;
                    }
                }
            }
        }

        System.out.println("Rate-limit test: success=" + success + ", rateLimited=" + rateLimited
                + ", httpCalls=" + httpCallCount.get());

        assertTrue(rateLimited > 0, "Expected some calls to be rejected by the rate limiter");
        assertTrue(success <= rateLimitPerSecond + 1,
                "Expected roughly at most one second worth of permits in a burst");
        assertTrue(httpCallCount.get() == success,
                "Only successful permits should hit the HTTP server");
    }

    private int startFastServer(AtomicInteger callCount) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/post", exchange -> {
            callCount.incrementAndGet();
            String body = "<AccountPostingResponse><requestId>"
                    + UUID.randomUUID()
                    + "</requestId><status>SUCCESS</status></AccountPostingResponse>";
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.setExecutor(Executors.newFixedThreadPool(16));
        server.start();
        return server.getAddress().getPort();
    }

    private static String requestXml(String requestId) {
        return "<AccountPostingRequest><requestId>"
                + requestId
                + "</requestId><amount>1.00</amount></AccountPostingRequest>";
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) {
            cur = cur.getCause();
        }
        return cur;
    }
}
