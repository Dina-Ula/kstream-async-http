package demo.rcbs;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class AbstractPostingService<RQ, RS> implements AutoCloseable {
    private final RcbsPostingService postingService;
    private final XmlObjectConverterWithJaxbElement<RQ> reqConverter;
    private final XmlObjectConverterWithJaxbElement<RS> resConverter;
    private final SingletonJaxbXmlObjectConverterWithJaxbElement<RQ> singletonReqConverter;
    private final SingletonJaxbXmlObjectConverterWithJaxbElement<RS> singletonResConverter;
    private final Function<RQ, String> requestIdExtractor;

    protected AbstractPostingService(
            RcbsPostingService postingService,
            XmlObjectConverterWithJaxbElement<RQ> reqConverter,
            XmlObjectConverterWithJaxbElement<RS> resConverter,
            Function<RQ, String> requestIdExtractor
    ) {
        this(postingService, reqConverter, resConverter, null, null, requestIdExtractor);
    }

    protected AbstractPostingService(
            RcbsPostingService postingService,
            XmlObjectConverterWithJaxbElement<RQ> reqConverter,
            XmlObjectConverterWithJaxbElement<RS> resConverter,
            SingletonJaxbXmlObjectConverterWithJaxbElement<RQ> singletonReqConverter,
            SingletonJaxbXmlObjectConverterWithJaxbElement<RS> singletonResConverter,
            Function<RQ, String> requestIdExtractor
    ) {
        this.postingService = Objects.requireNonNull(postingService, "postingService");
        this.reqConverter = Objects.requireNonNull(reqConverter, "reqConverter");
        this.resConverter = Objects.requireNonNull(resConverter, "resConverter");
        this.singletonReqConverter = singletonReqConverter;
        this.singletonResConverter = singletonResConverter;
        this.requestIdExtractor = Objects.requireNonNull(requestIdExtractor, "requestIdExtractor");
    }

    public abstract RcbsApiType apiType();

    public CompletableFuture<String> postAsync(String requestXml) {
        RQ request = reqConverter.unmarshal(requestXml);
        String normalizedRequest = reqConverter.marshal(request);
        String requestId = requestIdExtractor.apply(request);
        if (requestId == null || requestId.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("requestId must not be blank"));
        }
        return postingService.postAsync(requestId, normalizedRequest)
                .thenApply(resConverter::unmarshal)
                .thenApply(resConverter::marshal);
    }

    public String postSync(String requestXml) {
        RQ request = reqConverter.unmarshal(requestXml);
        String normalizedRequest = reqConverter.marshal(request);
        String requestId = requestIdExtractor.apply(request);
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        String responseXml = postingService.postSync(requestId, normalizedRequest);
        RS response = resConverter.unmarshal(responseXml);
        return resConverter.marshal(response);
    }

    public String postSyncUsingSingletonJaxb(String requestXml) {
        if (singletonReqConverter == null || singletonResConverter == null) {
            throw new IllegalStateException("Singleton JAXB converter path is not configured for " + apiType());
        }
        RQ request = singletonReqConverter.unmarshal(requestXml);
        String normalizedRequest = singletonReqConverter.marshal(request);
        String requestId = requestIdExtractor.apply(request);
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        String responseXml = postingService.postSync(requestId, normalizedRequest);
        RS response = singletonResConverter.unmarshal(responseXml);
        return singletonResConverter.marshal(response);
    }

    @Override
    public void close() {
        postingService.close();
    }
}
