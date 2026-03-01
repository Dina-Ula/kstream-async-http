package demo.rcbs;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class RcbsApiDispatcher implements AutoCloseable {
    private static final Pattern ROOT_ELEMENT = Pattern.compile("<\\s*(?:[A-Za-z_][\\w\\-.]*:)?([A-Za-z_][\\w\\-.]*)\\b");
    private final Map<String, RcbsApiHandler> handlers;

    public RcbsApiDispatcher(Collection<RcbsApiHandler> handlers) {
        this.handlers = Objects.requireNonNull(handlers, "handlers").stream()
                .collect(Collectors.toMap(RcbsApiHandler::requestRootElement, h -> h, (a, b) -> {
                    throw new IllegalArgumentException("Duplicate handler for request root: " + a.requestRootElement());
                }));
    }

    public CompletableFuture<String> postAsync(String requestXml) {
        String root = extractRootElement(requestXml);
        RcbsApiHandler handler = handlers.get(root);
        if (handler == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("No RcbsApiHandler configured for request root: " + root));
        }
        return handler.postAsync(requestXml);
    }

    @Override
    public void close() {
        for (RcbsApiHandler h : handlers.values()) {
            try {
                h.close();
            } catch (Exception ignored) {
            }
        }
    }

    static String extractRootElement(String xml) {
        Matcher m = ROOT_ELEMENT.matcher(Objects.requireNonNull(xml, "xml"));
        if (!m.find()) {
            throw new IllegalArgumentException("Unable to determine XML root element");
        }
        return m.group(1);
    }
}
