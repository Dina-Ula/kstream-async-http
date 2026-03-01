package demo.streams;

import com.sun.net.httpserver.HttpServer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncHttpThroughputIntegrationTest {

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
    @Timeout(30)
    void asyncProcessorShouldExceedFiveTpsWithDelayedHttp() throws Exception {
        int port = startDelayedHttpServer();
        String httpUrl = "http://localhost:" + port + "/post";

        Topology topology = buildTopology(httpUrl);
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "async-throughput-it");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);

        int recordCount = 60;
        long startedNs = System.nanoTime();

        try (TopologyTestDriver driver = new TopologyTestDriver(topology, props)) {
            TestInputTopic<String, String> input = driver.createInputTopic(
                    "xml-requests", new StringSerializer(), new StringSerializer());
            TestOutputTopic<String, String> output = driver.createOutputTopic(
                    "xml-responses", new StringDeserializer(), new StringDeserializer());

            for (int i = 0; i < recordCount; i++) {
                String requestId = "REQ-" + i;
                input.pipeInput(requestId, requestXml(requestId));
            }

            List<String> responses = new ArrayList<>();
            long deadline = System.nanoTime() + Duration.ofSeconds(12).toNanos();
            while (responses.size() < recordCount && System.nanoTime() < deadline) {
                driver.advanceWallClockTime(Duration.ofMillis(20));
                while (!output.isEmpty()) {
                    responses.add(output.readValue());
                }
                Thread.sleep(10);
            }

            long elapsedMs = Duration.ofNanos(System.nanoTime() - startedNs).toMillis();
            double tps = recordCount / (elapsedMs / 1000.0d);
            System.out.println("Measured async throughput: " + tps + " TPS over " + recordCount + " records");

            assertEquals(recordCount, responses.size(),
                    "Expected all responses before timeout; received=" + responses.size());
            assertTrue(tps > 5.0d,
                    "Expected throughput > 5 TPS with async HTTP, got " + tps + " TPS");
        }
    }

    private Topology buildTopology(String httpUrl) {
        StreamsBuilder builder = new StreamsBuilder();

        String storeName = "request-store";
        StoreBuilder<KeyValueStore<String, StoreEntry>> store =
                Stores.keyValueStoreBuilder(
                        Stores.inMemoryKeyValueStore(storeName),
                        Serdes.String(),
                        new JsonSerde<>()
                );
        builder.addStateStore(store);

        ProcessorSupplier<String, String, String, String> supplier =
                () -> new AsyncHttpProcessor(storeName, httpUrl, 32, 120, 200);

        builder.stream("xml-requests", Consumed.with(Serdes.String(), Serdes.String()))
                .process(supplier, storeName)
                .to("xml-responses");

        return builder.build();
    }

    private int startDelayedHttpServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/post", exchange -> {
            byte[] requestBytes;
            try (InputStream in = exchange.getRequestBody()) {
                requestBytes = in.readAllBytes();
            }

            String requestXml = new String(requestBytes, StandardCharsets.UTF_8);
            String requestId = extractRequestId(requestXml);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.sendResponseHeaders(500, -1);
                exchange.close();
                return;
            }

            String responseXml = "<AccountPostingResponse xmlns=\"urn:demo:rcbs\">"
                    + "<requestId>" + escapeXml(requestId) + "</requestId>"
                    + "<status>SUCCESS</status>"
                    + "<reference>REF-" + UUID.randomUUID().toString().substring(0, 8) + "</reference>"
                    + "<message>Posted</message>"
                    + "</AccountPostingResponse>";

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
        return "<AccountPostingRequest xmlns=\"urn:demo:rcbs\">"
                + "<requestId>" + requestId + "</requestId>"
                + "<amount>100.00</amount>"
                + "</AccountPostingRequest>";
    }

    private static String extractRequestId(String xml) {
        Matcher m = REQUEST_ID.matcher(xml);
        return m.find() ? m.group(1).trim() : "UNKNOWN";
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
