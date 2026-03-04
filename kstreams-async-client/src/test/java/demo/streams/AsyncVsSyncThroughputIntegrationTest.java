package demo.streams;

import com.sun.net.httpserver.HttpServer;
import demo.rcbs.DefaultRcbsClient;
import demo.rcbs.E535PostingService;
import demo.rcbs.RcbsApiType;
import demo.rcbs.RcbsClient;
import demo.rcbs.RcbsClientConfig;
import demo.rcbs.RcbsPostingService;
import demo.rcbs.XmlObjectConverterWithJaxbElement;
import demo.rcbs.xml.E535AccountPostingRequest;
import demo.rcbs.xml.E535AccountPostingResponse;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncVsSyncThroughputIntegrationTest {

    private static final Pattern REQUEST_ID =
            Pattern.compile("<requestId>(.*?)</requestId>", Pattern.DOTALL);

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @Timeout(40)
    void asyncShouldProcessMoreResponsesThanSyncWithinSameTimeWindow() throws Exception {
        int port = startDelayedHttpServer(200);
        String rcbsBaseUrl = "http://localhost:" + port;

        int attempts = 120;
        Duration window = Duration.ofSeconds(4);

        int asyncCompleted = runAsyncWindow(rcbsBaseUrl, attempts, window);
        int syncCompleted = runSyncWindow(rcbsBaseUrl, attempts, window);

        System.out.println("Throughput comparison: asyncCompleted=" + asyncCompleted
                + ", syncCompleted=" + syncCompleted
                + ", windowMs=" + window.toMillis()
                + ", attempts=" + attempts);

        assertTrue(asyncCompleted > syncCompleted,
                "Expected async to complete more requests than sync in same time window");
    }

    private int runAsyncWindow(String rcbsBaseUrl, int attempts, Duration window) {
        Topology topology = buildAsyncTopology(rcbsBaseUrl);
        Properties props = defaultStreamsProps("async-vs-sync-async-it");

        try (TopologyTestDriver driver = new TopologyTestDriver(topology, props)) {
            TestInputTopic<String, String> input = driver.createInputTopic(
                    "xml-requests", new StringSerializer(), new StringSerializer());
            TestOutputTopic<String, String> output = driver.createOutputTopic(
                    "xml-responses", new StringDeserializer(), new StringDeserializer());

            for (int i = 0; i < attempts; i++) {
                String requestId = "A-REQ-" + i;
                input.pipeInput(requestId, requestXml(requestId));
            }

            long deadline = System.nanoTime() + window.toNanos();
            int completed = 0;
            while (System.nanoTime() < deadline) {
                driver.advanceWallClockTime(Duration.ofMillis(20));
                while (!output.isEmpty()) {
                    output.readValue();
                    completed++;
                }
            }
            return completed;
        }
    }

    private int runSyncWindow(String rcbsBaseUrl, int attempts, Duration window) {
        Topology topology = buildSyncTopology(rcbsBaseUrl);
        Properties props = defaultStreamsProps("async-vs-sync-sync-it");

        try (TopologyTestDriver driver = new TopologyTestDriver(topology, props)) {
            TestInputTopic<String, String> input = driver.createInputTopic(
                    "xml-requests", new StringSerializer(), new StringSerializer());
            TestOutputTopic<String, String> output = driver.createOutputTopic(
                    "xml-responses", new StringDeserializer(), new StringDeserializer());

            long deadline = System.nanoTime() + window.toNanos();
            int completed = 0;
            int sent = 0;
            while (System.nanoTime() < deadline && sent < attempts) {
                String requestId = "S-REQ-" + sent;
                input.pipeInput(requestId, requestXml(requestId));
                sent++;

                while (!output.isEmpty()) {
                    output.readValue();
                    completed++;
                }
            }
            while (!output.isEmpty()) {
                output.readValue();
                completed++;
            }
            return completed;
        }
    }

    private Topology buildAsyncTopology(String rcbsBaseUrl) {
        StreamsBuilder builder = new StreamsBuilder();

        String storeName = "request-store";
        StoreBuilder<KeyValueStore<String, StoreEntry>> store =
                Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(storeName),
                        Serdes.String(),
                        new JsonSerde<>()
                );
        builder.addStateStore(store);

        RcbsClient rcbsClient = rcbsClient(rcbsBaseUrl);
        ProcessorSupplier<String, String, String, String> supplier =
                () -> new AsyncHttpProcessor(storeName, rcbsClient, RcbsApiType.E535, 120);

        builder.stream("xml-requests", Consumed.with(Serdes.String(), Serdes.String()))
                .process(supplier, storeName)
                .to("xml-responses");

        return builder.build();
    }

    private Topology buildSyncTopology(String rcbsBaseUrl) {
        StreamsBuilder builder = new StreamsBuilder();

        RcbsClient rcbsClient = rcbsClient(rcbsBaseUrl);
        ProcessorSupplier<String, String, String, String> supplier =
                () -> new SyncHttpProcessor(rcbsClient, RcbsApiType.E535);

        builder.stream("xml-requests", Consumed.with(Serdes.String(), Serdes.String()))
                .process(supplier)
                .to("xml-responses");

        return builder.build();
    }

    private RcbsClient rcbsClient(String rcbsBaseUrl) {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(E535AccountPostingRequest.class, E535AccountPostingResponse.class);
        marshaller.setSchemas(new ClassPathResource("xsd/e535/account-posting-e535.xsd"));

        XmlObjectConverterWithJaxbElement<E535AccountPostingRequest> reqConverter =
                new XmlObjectConverterWithJaxbElement<>(E535AccountPostingRequest.class, marshaller, marshaller);
        XmlObjectConverterWithJaxbElement<E535AccountPostingResponse> resConverter =
                new XmlObjectConverterWithJaxbElement<>(E535AccountPostingResponse.class, marshaller, marshaller);

        E535PostingService e535PostingService = new E535PostingService(
                new RcbsPostingService(RcbsClientConfig.defaults(rcbsBaseUrl + "/eportal/E535")),
                reqConverter,
                resConverter
        );
        return new DefaultRcbsClient(List.of(e535PostingService));
    }

    private static Properties defaultStreamsProps(String appId) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        return props;
    }

    private int startDelayedHttpServer(long delayMs) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/eportal/E535", exchange -> {
            byte[] requestBytes;
            try (InputStream in = exchange.getRequestBody()) {
                requestBytes = in.readAllBytes();
            }

            String requestXml = new String(requestBytes, StandardCharsets.UTF_8);
            String requestId = extractRequestId(requestXml);

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.sendResponseHeaders(500, -1);
                exchange.close();
                return;
            }

            String responseXml = "<E535AccountPostingResponse xmlns=\"urn:demo:rcbs:e535\">"
                    + "<requestId>" + escapeXml(requestId) + "</requestId>"
                    + "<status>SUCCESS</status>"
                    + "<reference>REF-" + UUID.randomUUID().toString().substring(0, 8) + "</reference>"
                    + "<message>Posted</message>"
                    + "<e535Reference>E535-" + UUID.randomUUID().toString().substring(0, 8) + "</e535Reference>"
                    + "</E535AccountPostingResponse>";

            byte[] responseBytes = responseXml.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.close();
        });
        server.setExecutor(Executors.newFixedThreadPool(64));
        server.start();
        return server.getAddress().getPort();
    }

    private static String requestXml(String requestId) {
        return "<E535AccountPostingRequest xmlns=\"urn:demo:rcbs:e535\">"
                + "<requestId>" + requestId + "</requestId>"
                + "<amount>100.00</amount>"
                + "<tenantId>T1</tenantId>"
                + "</E535AccountPostingRequest>";
    }

    private static String extractRequestId(String xml) {
        Matcher m = REQUEST_ID.matcher(xml);
        return m.find() ? m.group(1).trim() : "UNKNOWN";
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
