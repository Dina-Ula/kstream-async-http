package demo.streams;

import demo.rcbs.RcbsClientConfig;
import demo.rcbs.RcbsPostingService;
import org.apache.kafka.streams.processor.api.*;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsyncHttpProcessor implements Processor<String, String, String, String> {

    private static final Pattern REQUEST_ID =
            Pattern.compile("<requestId>(.*?)</requestId>", Pattern.DOTALL);

    private final String storeName;
    private final String httpUrl;

    private final Semaphore inFlight;
    private final int maxInFlight;
    private final int poolSize;
    private final int rateLimitPerSecond;
    private final Queue<Completion> completions = new ConcurrentLinkedQueue<>();

    private ProcessorContext<String, String> ctx;
    private KeyValueStore<String, StoreEntry> store;
    private RcbsPostingService client;

    public AsyncHttpProcessor(String storeName, String httpUrl, int poolSize, int maxInFlight, int rateLimitPerSecond) {
        this.storeName = storeName;
        this.httpUrl = httpUrl;
        this.maxInFlight = maxInFlight;
        this.poolSize = poolSize;
        this.rateLimitPerSecond = rateLimitPerSecond;
        this.inFlight = new Semaphore(maxInFlight);
    }

    private record Completion(String requestId, int attemptNo, String responseXml, Throwable error) {}

    @Override
    public void init(ProcessorContext<String, String> context) {
        this.ctx = context;
        this.store = context.getStateStore(storeName);
        this.client = new RcbsPostingService(
                new RcbsClientConfig(
                        httpUrl,
                        200, 200,
                        2000, 10000,
                        poolSize, 10_000,
                        maxInFlight,
                        rateLimitPerSecond
                )
        );

        // drain completions frequently (low latency)
        context.schedule(Duration.ofMillis(10), PunctuationType.WALL_CLOCK_TIME, this::onPunctuate);
    }

    @Override
    public void process(org.apache.kafka.streams.processor.api.Record<String, String> record) {
        String requestXml = record.value();
        String requestId = extractRequestId(requestXml);
        long now = System.currentTimeMillis();

        StoreEntry existing = store.get(requestId);
        if (existing == null) {
            existing = new StoreEntry(requestId, requestXml, "PENDING", 0, now, now);
            store.put(requestId, existing);
        }

        // trigger if eligible
        if (("PENDING".equals(existing.status) || "RETRYABLE".equals(existing.status))
                && existing.nextRetryAtMs <= now) {

            if (inFlight.tryAcquire()) {
                int attempt = existing.attemptNo + 1;
                StoreEntry inFlightEntry = new StoreEntry(
                        requestId, existing.requestXml, "IN_FLIGHT", attempt, existing.nextRetryAtMs, now
                );
                store.put(requestId, inFlightEntry);

                dispatchAsync(inFlightEntry);
            } else {
                // backpressure -> small retry delay
                existing.status = "RETRYABLE";
                existing.nextRetryAtMs = now + 50;
                existing.updatedAtMs = now;
                store.put(requestId, existing);
            }
        }

        // Don't forward original record; we emit only response on completion
        // (If you want to forward request too, call ctx.forward(record);)
    }

    private void dispatchAsync(StoreEntry entry) {
        client.postAsync(entry.requestId, entry.requestXml)
                .whenComplete((respXml, err) -> {
                    try {
                        Throwable cause = (err instanceof CompletionException ce && ce.getCause() != null)
                                ? ce.getCause()
                                : err;
                        completions.add(new Completion(entry.requestId, entry.attemptNo, respXml, cause));
                    } finally {
                        inFlight.release();
                    }
                });
    }

    private void onPunctuate(long timestamp) {
        long now = System.currentTimeMillis();

        // A) Drain completions and emit responses
        Completion c;
        while ((c = completions.poll()) != null) {
            StoreEntry current = store.get(c.requestId);
            if (current == null) continue;
            if (current.attemptNo != c.attemptNo) continue; // stale completion

            if (c.error == null) {
                current.status = "SUCCEEDED";
                current.updatedAtMs = now;
                store.put(c.requestId, current);

                // send response to Kafka output topic (via forward)
                ctx.forward(new org.apache.kafka.streams.processor.api.Record<>(c.requestId, c.responseXml, now));
                // optionally delete to keep state small
                // store.delete(c.requestId);

            } else {
                current.status = "RETRYABLE";
                current.updatedAtMs = now;
                current.nextRetryAtMs = now + backoffMs(current.attemptNo);
                store.put(c.requestId, current);
            }
        }

        // B) Kick due retries (naive scan for demo; in prod keep an index)
        try (var it = store.all()) {
            while (it.hasNext()) {
                var kv = it.next();
                StoreEntry e = kv.value;
                if ("RETRYABLE".equals(e.status) && e.nextRetryAtMs <= now) {
                    if (inFlight.tryAcquire()) {
                        e.status = "IN_FLIGHT";
                        e.attemptNo = e.attemptNo + 1;
                        e.updatedAtMs = now;
                        store.put(e.requestId, e);
                        dispatchAsync(e);
                    }
                }
            }
        }
    }

    private long backoffMs(int attemptNo) {
        long base = 200L;
        long backoff = base * (1L << Math.min(attemptNo, 5));
        return Math.min(backoff, 10_000L);
    }

    private String extractRequestId(String xml) {
        Matcher m = REQUEST_ID.matcher(xml);
        return m.find() ? m.group(1).trim() : "UNKNOWN";
    }

    @Override
    public void close() {
        try { client.close(); } catch (Exception ignored) {}
    }
}
