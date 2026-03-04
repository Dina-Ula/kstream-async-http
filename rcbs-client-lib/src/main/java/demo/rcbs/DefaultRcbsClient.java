package demo.rcbs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class DefaultRcbsClient implements RcbsClient, AutoCloseable {
    private final Map<RcbsApiType, AbstractPostingService<?, ?>> servicesByType;

    public DefaultRcbsClient(List<AbstractPostingService<?, ?>> services) {
        this.servicesByType = services.stream().collect(Collectors.toMap(
                AbstractPostingService::apiType,
                s -> s,
                (a, b) -> {
                    throw new IllegalArgumentException("Duplicate posting service for apiType=" + a.apiType());
                }
        ));
    }

    @Override
    public CompletableFuture<String> postAsync(RcbsApiType apiType, String requestXml) {
        AbstractPostingService<?, ?> service = servicesByType.get(apiType);
        if (service == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("No posting service configured for apiType=" + apiType));
        }
        return service.postAsync(requestXml);
    }

    @Override
    public String postSync(RcbsApiType apiType, String requestXml) {
        AbstractPostingService<?, ?> service = servicesByType.get(apiType);
        if (service == null) {
            throw new IllegalArgumentException("No posting service configured for apiType=" + apiType);
        }
        return service.postSync(requestXml);
    }

    @Override
    public String postSyncUsingSingletonJaxb(RcbsApiType apiType, String requestXml) {
        AbstractPostingService<?, ?> service = servicesByType.get(apiType);
        if (service == null) {
            throw new IllegalArgumentException("No posting service configured for apiType=" + apiType);
        }
        return service.postSyncUsingSingletonJaxb(requestXml);
    }

    @Override
    public void close() {
        for (AbstractPostingService<?, ?> service : servicesByType.values()) {
            try {
                service.close();
            } catch (Exception ignored) {
            }
        }
    }
}
