package demo.rcbs;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Intentionally shares one JAXB Marshaller/Unmarshaller instance.
 * This mirrors legacy wiring that can expose concurrency issues under parallel usage.
 */
public final class SingletonJaxbXmlObjectConverterWithJaxbElement<T> {
    private final Class<T> targetType;
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;

    public SingletonJaxbXmlObjectConverterWithJaxbElement(
            Class<T> targetType,
            Marshaller marshaller,
            Unmarshaller unmarshaller
    ) {
        this.targetType = targetType;
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
    }

    public String marshal(T value) {
        try {
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            marshaller.marshal(value, result);
            return writer.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to marshal XML", e);
        }
    }

    @SuppressWarnings("unchecked")
    public T unmarshal(String xml) {
        try {
            Source source = new StreamSource(new StringReader(xml));
            Object value = unmarshaller.unmarshal(source);
            if (value instanceof JAXBElement<?> jaxbElement) {
                Object inner = jaxbElement.getValue();
                if (targetType.isInstance(inner)) {
                    return (T) inner;
                }
            }
            if (targetType.isInstance(value)) {
                return (T) value;
            }
            throw new IllegalArgumentException("Unexpected XML type: " + value.getClass().getName());
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to unmarshal XML", e);
        }
    }
}
