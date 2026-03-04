package demo.rcbs.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "E537AccountPostingResponse", namespace = "urn:demo:rcbs:e537")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "E537AccountPostingResponseType", namespace = "urn:demo:rcbs:e537",
        propOrder = {"requestId", "status", "reference", "message", "e537Reference"})
public class E537AccountPostingResponse {
    @XmlElement(name = "requestId", namespace = "urn:demo:rcbs:e537", required = true)
    private String requestId;

    @XmlElement(name = "status", namespace = "urn:demo:rcbs:e537", required = true)
    private String status;

    @XmlElement(name = "reference", namespace = "urn:demo:rcbs:e537")
    private String reference;

    @XmlElement(name = "message", namespace = "urn:demo:rcbs:e537")
    private String message;

    @XmlElement(name = "e537Reference", namespace = "urn:demo:rcbs:e537")
    private String e537Reference;

    public E537AccountPostingResponse() {}

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getE537Reference() { return e537Reference; }
    public void setE537Reference(String value) { this.e537Reference = value; }
}
