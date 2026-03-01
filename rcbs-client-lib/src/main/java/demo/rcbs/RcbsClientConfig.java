package demo.rcbs;

public record RcbsClientConfig(
        String url,
        int httpMaxTotal,
        int httpMaxPerRoute,
        int httpConnectMs,
        int httpReadMs,
        int ioThreads,
        int ioQueueSize,
        int maxConcurrentCalls,   // bulkhead limit
        int rateLimitPerSecond    // rate limiter limit
) {
    public static RcbsClientConfig defaults(String url) {
        return new RcbsClientConfig(
                url,
                200, 200,
                2000, 10000,
                32, 10_000,
                120,
                200
        );
    }
}