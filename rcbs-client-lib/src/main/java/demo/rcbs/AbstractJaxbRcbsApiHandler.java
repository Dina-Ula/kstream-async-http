package demo.rcbs;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

abstract class AbstractJaxbRcbsApiHandler<RQ, RS> implements RcbsApiHandler {
    private final RcbsPostingService postingService;
    private final JaxbSchemaBinding<RQ, RS> binding;
    private final Function<RQ, String> requestIdExtractor;

    protected AbstractJaxbRcbsApiHandler(
            RcbsClientConfig cfg,
            JaxbSchemaBinding<RQ, RS> binding,
            Function<RQ, String> requestIdExtractor
    ) {
        this.postingService = new RcbsPostingService(Objects.requireNonNull(cfg, "cfg"));
        this.binding = Objects.requireNonNull(binding, "binding");
        this.requestIdExtractor = Objects.requireNonNull(requestIdExtractor, "requestIdExtractor");
    }

    @Override
    public CompletableFuture<String> postAsync(String requestXml) {
        RQ request = binding.unmarshalRequest(requestXml);
        String normalizedRequest = binding.marshalRequest(request);
        String requestId = requestIdExtractor.apply(request);
        if (requestId == null || requestId.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("requestId must not be blank"));
        }
        return postingService.postAsync(requestId, normalizedRequest)
                .thenApply(binding::unmarshalResponse)
                .thenApply(binding::marshalResponse);
    }

    @Override
    public void close() {
        postingService.close();
    }
}
