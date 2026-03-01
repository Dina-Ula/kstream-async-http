package demo.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.*;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class StreamsApp {
    public static void main(String[] args) {
        String bootstrap = System.getProperty("bootstrap", "localhost:9092");
        String httpUrl   = System.getProperty("httpUrl", "http://localhost:8080/post");
        int rateLimitPerSecond = Integer.parseInt(System.getProperty("rateLimitPerSecond", "200"));

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstreams-async-http-xml-demo");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);

        // faster commits for demo
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);

        StreamsBuilder builder = new StreamsBuilder();

        String storeName = "request-store";
        StoreBuilder<KeyValueStore<String, StoreEntry>> store =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(storeName),
                        Serdes.String(),
                        new JsonSerde<>()
                );

        builder.addStateStore(store);

        ProcessorSupplier<String, String, String, String> supplier =
                () -> new AsyncHttpProcessor(storeName, httpUrl, 32, 120, rateLimitPerSecond);

        builder.stream("xml-requests", Consumed.with(Serdes.String(), Serdes.String()))
                .process(supplier, storeName)
                .to("xml-responses");

        Topology topo = builder.build();
        KafkaStreams streams = new KafkaStreams(topo, props);
        CountDownLatch latch = new CountDownLatch(1);

        streams.setStateListener((newState, oldState) -> {
            if (newState == KafkaStreams.State.NOT_RUNNING) {
                latch.countDown();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            streams.close();
            latch.countDown();
        }));
        streams.start();

        System.out.println("Streams started. Consuming from xml-requests, producing to xml-responses");
        System.out.println("HTTP URL: " + httpUrl);
        System.out.println("Rate limit per second: " + rateLimitPerSecond);

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
