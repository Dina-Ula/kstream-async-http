package demo.streams;

import demo.rcbs.RcbsApiType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.async")
public class AsyncProcessorProperties {
    private int maxInFlight = 120;
    private String storeName = "request-store";
    private RcbsApiType apiType = RcbsApiType.E535;

    public int getMaxInFlight() {
        return maxInFlight;
    }

    public void setMaxInFlight(int maxInFlight) {
        this.maxInFlight = maxInFlight;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public RcbsApiType getApiType() {
        return apiType;
    }

    public void setApiType(RcbsApiType apiType) {
        this.apiType = apiType;
    }
}
