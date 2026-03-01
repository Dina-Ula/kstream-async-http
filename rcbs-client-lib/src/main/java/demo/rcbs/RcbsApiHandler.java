package demo.rcbs;

import java.util.concurrent.CompletableFuture;

public interface RcbsApiHandler extends AutoCloseable {
    String requestRootElement();
    CompletableFuture<String> postAsync(String requestXml);

    @Override
    void close();
}
