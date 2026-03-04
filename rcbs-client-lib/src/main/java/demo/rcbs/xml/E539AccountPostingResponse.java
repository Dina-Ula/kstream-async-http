package demo.rcbs.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "E539AccountPostingResponse", namespace = "urn:demo:rcbs:e539")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "E539AccountPostingResponseType", namespace = "urn:demo:rcbs:e539",
        propOrder = {"requestId", "status", "reference", "message", "e539Reference"})
public class E539AccountPostingResponse {
    @XmlElement(name = "requestId", namespace = "urn:demo:rcbs:e539", required = true)
    private String requestId;

    @XmlElement(name = "status", namespace = "urn:demo:rcbs:e539", required = true)
    private String status;

    @XmlElement(name = "reference", namespace = "urn:demo:rcbs:e539")
    private String reference;

    @XmlElement(name = "message", namespace = "urn:demo:rcbs:e539")
    private String message;

    @XmlElement(name = "e539Reference", namespace = "urn:demo:rcbs:e539")
    private String e539Reference;

    public E539AccountPostingResponse() {}

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getE539Reference() { return e539Reference; }
    public void setE539Reference(String value) { this.e539Reference = value; }
}
