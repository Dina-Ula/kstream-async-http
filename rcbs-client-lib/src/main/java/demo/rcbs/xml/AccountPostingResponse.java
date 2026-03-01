package demo.rcbs.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "AccountPostingResponse", namespace = "urn:demo:rcbs")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccountPostingResponseType", namespace = "urn:demo:rcbs",
        propOrder = {"requestId", "status", "reference", "message"})
public class AccountPostingResponse {
    @XmlElement(name = "requestId", namespace = "urn:demo:rcbs", required = true)
    private String requestId;

    @XmlElement(name = "status", namespace = "urn:demo:rcbs", required = true)
    private String status;

    @XmlElement(name = "reference", namespace = "urn:demo:rcbs")
    private String reference;

    @XmlElement(name = "message", namespace = "urn:demo:rcbs")
    private String message;

    public AccountPostingResponse() {}

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
}
