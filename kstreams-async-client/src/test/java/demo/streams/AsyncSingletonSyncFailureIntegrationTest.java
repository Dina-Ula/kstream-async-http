package demo.streams;

import com.sun.net.httpserver.HttpServer;
import demo.rcbs.DefaultRcbsClient;
import demo.rcbs.E535PostingService;
import demo.rcbs.RcbsApiType;
import demo.rcbs.RcbsClient;
import demo.rcbs.RcbsClientConfig;
import demo.rcbs.RcbsPostingService;
import demo.rcbs.SingletonJaxbXmlObjectConverterWithJaxbElement;
import demo.rcbs.XmlObjectConverterWithJaxbElement;
import demo.rcbs.xml.E535AccountPostingRequest;
import demo.rcbs.xml.E535AccountPostingResponse;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AsyncSingletonSyncFailureIntegrationTest {

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
    void asyncTopologyUsingSingletonJaxbShouldFailUnderConcurrency() throws Exception {
        int port = startDelayedHttpServer(20);
        String rcbsBaseUrl = "http://localhost:" + port;

        Topology topology = buildTopology(rcbsBaseUrl);
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "async-singleton-sync-failure-it");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);

        assertThrows(StreamsException.class, () -> {
            try (TopologyTestDriver driver = new TopologyTestDriver(topology, props)) {
                TestInputTopic<String, String> input = driver.createInputTopic(
                        "xml-requests", new StringSerializer(), new StringSerializer());

                for (int i = 0; i < 500; i++) {
                    String requestId = "SINGLETON-REQ-" + i;
                    input.pipeInput(requestId, requestXml(requestId));
                    driver.advanceWallClockTime(java.time.Duration.ofMillis(5));
                }

                // keep driving punctuations so async completion errors surface on stream thread
                for (int i = 0; i < 2000; i++) {
                    driver.advanceWallClockTime(java.time.Duration.ofMillis(5));
                }
            }
        });
    }

    private Topology buildTopology(String rcbsBaseUrl) throws Exception {
        StreamsBuilder builder = new StreamsBuilder();
        RcbsClient rcbsClient = singletonRcbsClient(rcbsBaseUrl);
        ProcessorSupplier<String, String, String, String> supplier =
                () -> new AsyncSingletonSyncHttpProcessor(rcbsClient, RcbsApiType.E535, 120);

        builder.stream("xml-requests", Consumed.with(Serdes.String(), Serdes.String()))
                .process(supplier)
                .to("xml-responses");

        return builder.build();
    }

    private RcbsClient singletonRcbsClient(String rcbsBaseUrl) throws Exception {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(E535AccountPostingRequest.class, E535AccountPostingResponse.class);
        marshaller.setSchemas(new ClassPathResource("xsd/e535/account-posting-e535.xsd"));

        XmlObjectConverterWithJaxbElement<E535AccountPostingRequest> reqConverter =
                new XmlObjectConverterWithJaxbElement<>(E535AccountPostingRequest.class, marshaller, marshaller);
        XmlObjectConverterWithJaxbElement<E535AccountPostingResponse> resConverter =
                new XmlObjectConverterWithJaxbElement<>(E535AccountPostingResponse.class, marshaller, marshaller);

        var singletonRuntimeMarshaller = marshaller.createMarshaller();
        var singletonRuntimeUnmarshaller = marshaller.createUnmarshaller();
        SingletonJaxbXmlObjectConverterWithJaxbElement<E535AccountPostingRequest> singletonReqConverter =
                new SingletonJaxbXmlObjectConverterWithJaxbElement<>(
                        E535AccountPostingRequest.class,
                        singletonRuntimeMarshaller,
                        singletonRuntimeUnmarshaller
                );
        SingletonJaxbXmlObjectConverterWithJaxbElement<E535AccountPostingResponse> singletonResConverter =
                new SingletonJaxbXmlObjectConverterWithJaxbElement<>(
                        E535AccountPostingResponse.class,
                        singletonRuntimeMarshaller,
                        singletonRuntimeUnmarshaller
                );

        E535PostingService e535PostingService = new E535PostingService(
                new RcbsPostingService(RcbsClientConfig.defaults(rcbsBaseUrl + "/eportal/E535")),
                reqConverter,
                resConverter,
                singletonReqConverter,
                singletonResConverter
        );
        return new DefaultRcbsClient(List.of(e535PostingService));
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
