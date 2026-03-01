package demo.streams;

import org.apache.kafka.common.serialization.*;

import java.io.*;

public class JsonSerde<T extends Serializable> implements Serde<T> {
    private final Serializer<T> serializer = (topic, data) -> {
        if (data == null) return null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(data);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    private final Deserializer<T> deserializer = (topic, bytes) -> {
        if (bytes == null) return null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            @SuppressWarnings("unchecked")
            T obj = (T) ois.readObject();
            return obj;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    };

    @Override public Serializer<T> serializer() { return serializer; }
    @Override public Deserializer<T> deserializer() { return deserializer; }
}
