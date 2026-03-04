package demo.streams;

import demo.rcbs.RcbsClient;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

@SpringBootApplication(scanBasePackages = "demo")
@EnableConfigurationProperties(AsyncProcessorProperties.class)
public class KStreamAsyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(KStreamAsyncApplication.class, args);
    }

    @Bean
    public Function<KStream<String, String>, KStream<String, String>> rcbsProcess(
            RcbsClient rcbsClient,
            AsyncProcessorProperties properties
    ) {
        return input -> input.process(
                () -> new AsyncHttpProcessor(
                        properties.getStoreName(),
                        rcbsClient,
                        properties.getApiType(),
                        properties.getMaxInFlight()
                ),
                properties.getStoreName()
        );
    }

    @Bean
    public StoreBuilder<KeyValueStore<String, StoreEntry>> requestStore(AsyncProcessorProperties properties) {
        return Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(properties.getStoreName()),
                Serdes.String(),
                new JsonSerde<>()
        );
    }

    @Bean
    //@ConditionalOnProperty(prefix = "app.sync", name = "enabled", havingValue = "true")
    public Function<KStream<String, String>, KStream<String, String>> rcbsSyncProcess(
            RcbsClient rcbsClient,
            AsyncProcessorProperties properties
    ) {
        return input -> input.process(
                () -> new SyncHttpProcessor(rcbsClient, properties.getApiType())
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.singleton-sync-async", name = "enabled", havingValue = "true")
    public Function<KStream<String, String>, KStream<String, String>> rcbsSingletonSyncAsyncProcess(
            RcbsClient rcbsClient,
            AsyncProcessorProperties properties
    ) {
        return input -> input.process(
                () -> new AsyncSingletonSyncHttpProcessor(
                        rcbsClient,
                        properties.getApiType(),
                        properties.getMaxInFlight()
                )
        );
    }
}
