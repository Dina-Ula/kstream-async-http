package demo.rcbs.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "E536AccountPostingResponse", namespace = "urn:demo:rcbs:e536")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "E536AccountPostingResponseType", namespace = "urn:demo:rcbs:e536",
        propOrder = {"requestId", "status", "reference", "message", "e536Reference"})
public class E536AccountPostingResponse {
    @XmlElement(name = "requestId", namespace = "urn:demo:rcbs:e536", required = true)
    private String requestId;

    @XmlElement(name = "status", namespace = "urn:demo:rcbs:e536", required = true)
    private String status;

    @XmlElement(name = "reference", namespace = "urn:demo:rcbs:e536")
    private String reference;

    @XmlElement(name = "message", namespace = "urn:demo:rcbs:e536")
    private String message;

    @XmlElement(name = "e536Reference", namespace = "urn:demo:rcbs:e536")
    private String e536Reference;

    public E536AccountPostingResponse() {}

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getE536Reference() { return e536Reference; }
    public void setE536Reference(String value) { this.e536Reference = value; }
}
