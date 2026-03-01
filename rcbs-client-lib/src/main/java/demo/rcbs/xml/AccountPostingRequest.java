package demo.rcbs.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.math.BigDecimal;

@XmlRootElement(name = "AccountPostingRequest", namespace = "urn:demo:rcbs")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccountPostingRequestType", namespace = "urn:demo:rcbs", propOrder = {"requestId", "amount"})
public class AccountPostingRequest {
    @XmlElement(name = "requestId", namespace = "urn:demo:rcbs", required = true)
    private String requestId;

    @XmlElement(name = "amount", namespace = "urn:demo:rcbs", required = true)
    private BigDecimal amount;

    public AccountPostingRequest() {}

    public AccountPostingRequest(String requestId, BigDecimal amount) {
        this.requestId = requestId;
        this.amount = amount;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
