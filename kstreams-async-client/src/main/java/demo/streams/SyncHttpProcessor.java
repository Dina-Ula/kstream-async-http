package demo.streams;

import demo.rcbs.RcbsApiType;
import demo.rcbs.RcbsClient;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyncHttpProcessor implements Processor<String, String, String, String> {

    private static final Pattern REQUEST_ID =
            Pattern.compile("<requestId>(.*?)</requestId>", Pattern.DOTALL);

    private final RcbsClient rcbsClient;
    private final RcbsApiType defaultApiType;
    private ProcessorContext<String, String> ctx;

    public SyncHttpProcessor(RcbsClient rcbsClient, RcbsApiType defaultApiType) {
        this.rcbsClient = rcbsClient;
        this.defaultApiType = defaultApiType;
    }

    @Override
    public void init(ProcessorContext<String, String> context) {
        this.ctx = context;
    }

    @Override
    public void process(Record<String, String> record) {
        String requestXml = record.value();
        String requestId = extractRequestId(requestXml);
        RcbsApiType apiType = resolveApiType(record.key());

        /*ExecutorService executorService = Executors.newFixedThreadPool(32);

        executorService.execute(() -> {
            String responseXml = rcbsClient.postSyncUsingSingletonJaxb(apiType, requestXml);
            System.out.println("responseXml: " + responseXml);
        });
        ctx.forward(new Record<>(requestId, "Testing", record.timestamp()));*/

        String responseXml = rcbsClient.postSync(apiType, requestXml);
        ctx.forward(new Record<>(requestId, responseXml, record.timestamp()));
    }

    private String extractRequestId(String xml) {
        Matcher m = REQUEST_ID.matcher(xml);
        return m.find() ? m.group(1).trim() : "UNKNOWN";
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
}
