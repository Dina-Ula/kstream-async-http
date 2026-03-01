package demo.streams;

import java.io.Serializable;

public class StoreEntry implements Serializable {
    public String requestId;
    public String requestXml;

    public String status; // PENDING / IN_FLIGHT / SUCCEEDED / RETRYABLE
    public int attemptNo;
    public long nextRetryAtMs;
    public long updatedAtMs;

    public StoreEntry() {}

    public StoreEntry(String requestId, String requestXml, String status,
                      int attemptNo, long nextRetryAtMs, long updatedAtMs) {
        this.requestId = requestId;
        this.requestXml = requestXml;
        this.status = status;
        this.attemptNo = attemptNo;
        this.nextRetryAtMs = nextRetryAtMs;
        this.updatedAtMs = updatedAtMs;
    }
}