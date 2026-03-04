package demo.rcbs;

import java.util.concurrent.CompletableFuture;

public interface RcbsClient {
    CompletableFuture<String> postAsync(RcbsApiType apiType, String requestXml);

    String postSync(RcbsApiType apiType, String requestXml);

    String postSyncUsingSingletonJaxb(RcbsApiType apiType, String requestXml);
}
