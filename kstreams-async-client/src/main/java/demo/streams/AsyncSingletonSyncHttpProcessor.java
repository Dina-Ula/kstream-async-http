package demo.streams;

import demo.rcbs.RcbsApiType;
import demo.rcbs.RcbsClient;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dedicated topology processor used to reproduce concurrency failures when singleton JAXB
 * marshaller/unmarshaller are used from multiple async tasks.
 */
public class AsyncSingletonSyncHttpProcessor implements Processor<String, String, String, String> {

    private static final Pattern REQUEST_ID = Pattern.compile("<requestId>(.*?)</requestId>", Pattern.DOTALL);

    private final RcbsClient rcbsClient;
    private final RcbsApiType defaultApiType;
    private final Semaphore inFlight;
    private final ExecutorService pool;
    private final Queue<Completion> completions = new ConcurrentLinkedQueue<>();

    private ProcessorContext<String, String> ctx;

    private record Completion(String requestId, String responseXml, Throwable error) {}

    public AsyncSingletonSyncHttpProcessor(RcbsClient rcbsClient, RcbsApiType defaultApiType, int maxInFlight) {
        this.rcbsClient = rcbsClient;
        this.defaultApiType = defaultApiType;
        this.inFlight = new Semaphore(maxInFlight);
        this.pool = Executors.newFixedThreadPool(maxInFlight);
    }

    @Override
    public void init(ProcessorContext<String, String> context) {
        this.ctx = context;
        context.schedule(Duration.ofMillis(10), PunctuationType.WALL_CLOCK_TIME, this::onPunctuate);
    }

    @Override
    public void process(Record<String, String> record) {
        if (!inFlight.tryAcquire()) {
            return;
        }
        String requestXml = record.value();
        String requestId = extractRequestId(requestXml);
        RcbsApiType apiType = resolveApiType(record.key());

        CompletableFuture
                .supplyAsync(() -> rcbsClient.postSyncUsingSingletonJaxb(apiType, requestXml), pool)
                .whenComplete((responseXml, error) -> {
                    Throwable cause = (error instanceof CompletionException ce && ce.getCause() != null)
                            ? ce.getCause() : error;
                    completions.add(new Completion(requestId, responseXml, cause));
                    inFlight.release();
                });
    }

    private void onPunctuate(long timestamp) {
        Completion c;
        while ((c = completions.poll()) != null) {
            if (c.error != null) {
                throw new IllegalStateException("Singleton JAXB async call failed for requestId=" + c.requestId, c.error);
            }
            ctx.forward(new Record<>(c.requestId, c.responseXml, timestamp));
        }
    }

    @Override
    public void close() {
        pool.shutdownNow();
    }

    private RcbsApiType resolveApiType(String recordKey) {
        if (recordKey == null || recordKey.isBlank()) {
            return defaultApiType;
        }
        int idx = recordKey.indexOf(':');
        if (idx <= 0) {
            return defaultApiType;
        }
        String prefix = recordKey.substring(0, idx).trim();
        try {
            return RcbsApiType.valueOf(prefix);
        } catch (IllegalArgumentException ignored) {
            return defaultApiType;
        }
    }

    private static String extractRequestId(String xml) {
        Matcher m = REQUEST_ID.matcher(xml);
        return m.find() ? m.group(1).trim() : "UNKNOWN";
    }
}
