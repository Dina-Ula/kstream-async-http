package demo.rcbs.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.math.BigDecimal;

@XmlRootElement(name = "E537AccountPostingRequest", namespace = "urn:demo:rcbs:e537")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "E537AccountPostingRequestType", namespace = "urn:demo:rcbs:e537",
        propOrder = {"requestId", "amount", "tenantId"})
public class E537AccountPostingRequest {
    @XmlElement(name = "requestId", namespace = "urn:demo:rcbs:e537", required = true)
    private String requestId;

    @XmlElement(name = "amount", namespace = "urn:demo:rcbs:e537", required = true)
    private BigDecimal amount;

    @XmlElement(name = "tenantId", namespace = "urn:demo:rcbs:e537", required = true)
    private String tenantId;

    public E537AccountPostingRequest() {}

    public E537AccountPostingRequest(String requestId, BigDecimal amount, String tenantId) {
        this.requestId = requestId;
        this.amount = amount;
        this.tenantId = tenantId;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
