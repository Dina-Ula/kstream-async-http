package demo.rcbs;

import io.github.resilience4j.bulkhead.*;
import io.github.resilience4j.circuitbreaker.*;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.*;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * This is the library entry point.
 * Caller uses postAsync(...) and attaches callbacks. No need for caller-side executor wrapping.
 */
public final class RcbsPostingService implements AutoCloseable {

    private final HttpXmlClient http;
    private final ExecutorService ioPool;
    private final TcclAsyncRunner asyncRunner;

    private final CircuitBreaker cb;
    private final RateLimiter rl;
    private final Bulkhead bh;

    private final String url;

    public RcbsPostingService(RcbsClientConfig cfg) {
        Objects.requireNonNull(cfg);

        this.url = cfg.url();

        this.http = new HttpXmlClient(
                cfg.httpMaxTotal(), cfg.httpMaxPerRoute(),
                cfg.httpConnectMs(), cfg.httpReadMs()
        );

        this.ioPool = new ThreadPoolExecutor(
                cfg.ioThreads(), cfg.ioThreads(),
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(cfg.ioQueueSize()),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("rcbs-io-" + t.getId());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        this.asyncRunner = new TcclAsyncRunner(ioPool);

        this.cb = CircuitBreaker.of("rcbs",
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                        .slidingWindowSize(50)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .build()
        );

        this.rl = RateLimiter.of("rcbs",
                RateLimiterConfig.custom()
                        .limitForPeriod(cfg.rateLimitPerSecond())
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ZERO) // fail-fast
                        .build()
        );

        this.bh = Bulkhead.of("rcbs",
                BulkheadConfig.custom()
                        .maxConcurrentCalls(cfg.maxConcurrentCalls())
                        .maxWaitDuration(Duration.ZERO) // fail-fast if saturated
                        .build()
        );
    }

    /** Async call: returns immediately with a future; blocking HTTP runs on rcbs-io-* threads. */
    public CompletableFuture<String> postAsync(String requestId, String requestXml) {

        Supplier<String> blockingCall = () -> {
            try {
                return http.postXml(url, requestXml);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        Supplier<String> protectedCall = Decorators.ofSupplier(blockingCall)
                .withCircuitBreaker(cb)
                .withRateLimiter(rl)
                .withBulkhead(bh)
                .decorate();

        return asyncRunner.runAsync(protectedCall);
    }

    @Override
    public void close() {
        try { http.close(); } catch (Exception ignored) {}
        ioPool.shutdown();
    }
}