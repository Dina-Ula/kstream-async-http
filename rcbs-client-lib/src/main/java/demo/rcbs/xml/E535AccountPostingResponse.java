package demo.rcbs.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "E535AccountPostingResponse", namespace = "urn:demo:rcbs:e535")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "E535AccountPostingResponseType", namespace = "urn:demo:rcbs:e535",
        propOrder = {"requestId", "status", "reference", "message", "e535Reference"})
public class E535AccountPostingResponse {
    @XmlElement(name = "requestId", namespace = "urn:demo:rcbs:e535", required = true)
    private String requestId;

    @XmlElement(name = "status", namespace = "urn:demo:rcbs:e535", required = true)
    private String status;

    @XmlElement(name = "reference", namespace = "urn:demo:rcbs:e535")
    private String reference;

    @XmlElement(name = "message", namespace = "urn:demo:rcbs:e535")
    private String message;

    @XmlElement(name = "e535Reference", namespace = "urn:demo:rcbs:e535")
    private String e535Reference;

    public E535AccountPostingResponse() {}

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getE535Reference() {
        return e535Reference;
    }

    public void setE535Reference(String e535Reference) {
        this.e535Reference = e535Reference;
    }
}
