package demo.rcbs.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "E538AccountPostingResponse", namespace = "urn:demo:rcbs:e538")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "E538AccountPostingResponseType", namespace = "urn:demo:rcbs:e538",
        propOrder = {"requestId", "status", "reference", "message", "e538Reference"})
public class E538AccountPostingResponse {
    @XmlElement(name = "requestId", namespace = "urn:demo:rcbs:e538", required = true)
    private String requestId;

    @XmlElement(name = "status", namespace = "urn:demo:rcbs:e538", required = true)
    private String status;

    @XmlElement(name = "reference", namespace = "urn:demo:rcbs:e538")
    private String reference;

    @XmlElement(name = "message", namespace = "urn:demo:rcbs:e538")
    private String message;

    @XmlElement(name = "e538Reference", namespace = "urn:demo:rcbs:e538")
    private String e538Reference;

    public E538AccountPostingResponse() {}

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getE538Reference() { return e538Reference; }
    public void setE538Reference(String value) { this.e538Reference = value; }
}
