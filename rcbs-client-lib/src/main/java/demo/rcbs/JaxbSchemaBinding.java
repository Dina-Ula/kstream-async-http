package demo.rcbs;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Objects;

/**
 * Thread-safe JAXB/XSD codec for request/response pairs.
 */
public final class JaxbSchemaBinding<RQ, RS> {
    private final Class<RQ> requestClass;
    private final Class<RS> responseClass;
    private final JAXBContext context;
    private final Schema schema;
    private final ThreadLocal<Marshaller> marshallerTl;
    private final ThreadLocal<Unmarshaller> unmarshallerTl;

    public JaxbSchemaBinding(Class<RQ> requestClass, Class<RS> responseClass, String xsdPath) {
        this.requestClass = Objects.requireNonNull(requestClass, "requestClass");
        this.responseClass = Objects.requireNonNull(responseClass, "responseClass");
        try {
            this.schema = loadSchema(Objects.requireNonNull(xsdPath, "xsdPath"));
            this.context = JAXBContext.newInstance(requestClass, responseClass);
            this.marshallerTl = ThreadLocal.withInitial(this::newMarshaller);
            this.unmarshallerTl = ThreadLocal.withInitial(this::newUnmarshaller);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize JAXB binding for " + xsdPath, e);
        }
    }

    public String marshalRequest(RQ request) {
        return marshal(request);
    }

    public String marshalResponse(RS response) {
        return marshal(response);
    }

    public RQ unmarshalRequest(String requestXml) {
        return unmarshal(requestXml, requestClass);
    }

    public RS unmarshalResponse(String responseXml) {
        return unmarshal(responseXml, responseClass);
    }

    private <T> String marshal(T value) {
        try {
            StringWriter out = new StringWriter();
            marshallerTl.get().marshal(value, out);
            return out.toString();
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Failed to marshal XML", e);
        }
    }

    private <T> T unmarshal(String xml, Class<T> type) {
        try {
            JAXBElement<T> elem = unmarshallerTl.get()
                    .unmarshal(new StreamSource(new StringReader(xml)), type);
            return elem.getValue();
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Failed to unmarshal XML", e);
        }
    }

    private Marshaller newMarshaller() {
        try {
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            m.setSchema(schema);
            return m;
        } catch (JAXBException e) {
            throw new IllegalStateException("Failed to create JAXB marshaller", e);
        }
    }

    private Unmarshaller newUnmarshaller() {
        try {
            Unmarshaller u = context.createUnmarshaller();
            u.setSchema(schema);
            return u;
        } catch (JAXBException e) {
            throw new IllegalStateException("Failed to create JAXB unmarshaller", e);
        }
    }

    private static Schema loadSchema(String xsdPath) throws SAXException {
        URL schemaUrl = JaxbSchemaBinding.class.getClassLoader().getResource(xsdPath);
        if (schemaUrl == null) {
            throw new IllegalStateException("XSD not found on classpath: " + xsdPath);
        }
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        return sf.newSchema(schemaUrl);
    }
}
