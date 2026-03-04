package demo.rcbs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rcbs")
public class RcbsSpringProperties {
    private String e535Url = "http://localhost:8080/eportal/E535";
    private String e536Url = "http://localhost:8080/eportal/E536";
    private String e537Url = "http://localhost:8080/eportal/E537";
    private String e538Url = "http://localhost:8080/eportal/E538";
    private String e539Url = "http://localhost:8080/eportal/E539";
    private int httpMaxTotal = 200;
    private int httpMaxPerRoute = 200;
    private int httpConnectMs = 2000;
    private int httpReadMs = 10000;
    private int ioThreads = 32;
    private int ioQueueSize = 10_000;
    private int maxConcurrentCalls = 120;
    private int rateLimitPerSecond = 200;

    public RcbsClientConfig configFor(String url) {
        return new RcbsClientConfig(
                url,
                httpMaxTotal,
                httpMaxPerRoute,
                httpConnectMs,
                httpReadMs,
                ioThreads,
                ioQueueSize,
                maxConcurrentCalls,
                rateLimitPerSecond
        );
    }

    public String getE535Url() { return e535Url; }
    public void setE535Url(String e535Url) { this.e535Url = e535Url; }
    public String getE536Url() { return e536Url; }
    public void setE536Url(String e536Url) { this.e536Url = e536Url; }
    public String getE537Url() { return e537Url; }
    public void setE537Url(String e537Url) { this.e537Url = e537Url; }
    public String getE538Url() { return e538Url; }
    public void setE538Url(String e538Url) { this.e538Url = e538Url; }
    public String getE539Url() { return e539Url; }
    public void setE539Url(String e539Url) { this.e539Url = e539Url; }
    public int getHttpMaxTotal() { return httpMaxTotal; }
    public void setHttpMaxTotal(int httpMaxTotal) { this.httpMaxTotal = httpMaxTotal; }
    public int getHttpMaxPerRoute() { return httpMaxPerRoute; }
    public void setHttpMaxPerRoute(int httpMaxPerRoute) { this.httpMaxPerRoute = httpMaxPerRoute; }
    public int getHttpConnectMs() { return httpConnectMs; }
    public void setHttpConnectMs(int httpConnectMs) { this.httpConnectMs = httpConnectMs; }
    public int getHttpReadMs() { return httpReadMs; }
    public void setHttpReadMs(int httpReadMs) { this.httpReadMs = httpReadMs; }
    public int getIoThreads() { return ioThreads; }
    public void setIoThreads(int ioThreads) { this.ioThreads = ioThreads; }
    public int getIoQueueSize() { return ioQueueSize; }
    public void setIoQueueSize(int ioQueueSize) { this.ioQueueSize = ioQueueSize; }
    public int getMaxConcurrentCalls() { return maxConcurrentCalls; }
    public void setMaxConcurrentCalls(int maxConcurrentCalls) { this.maxConcurrentCalls = maxConcurrentCalls; }
    public int getRateLimitPerSecond() { return rateLimitPerSecond; }
    public void setRateLimitPerSecond(int rateLimitPerSecond) { this.rateLimitPerSecond = rateLimitPerSecond; }
}
