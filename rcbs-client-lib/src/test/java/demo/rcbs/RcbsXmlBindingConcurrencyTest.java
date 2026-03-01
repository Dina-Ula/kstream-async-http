package demo.rcbs;

import demo.rcbs.xml.AccountPostingRequest;
import demo.rcbs.xml.AccountPostingResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RcbsXmlBindingConcurrencyTest {

    @Test
    @Timeout(20)
    void shouldMarshalAndUnmarshalConcurrently() throws Exception {
        RcbsXmlBinding binding = new RcbsXmlBinding();
        ExecutorService pool = Executors.newFixedThreadPool(32);
        try {
            int tasks = 300;
            List<Callable<Boolean>> work = new ArrayList<>(tasks);
            for (int i = 0; i < tasks; i++) {
                final int id = i;
                work.add(() -> {
                    String requestId = "REQ-" + id;
                    AccountPostingRequest req = new AccountPostingRequest(requestId, new BigDecimal("100.25"));
                    String xml = binding.marshalRequest(req);
                    String responseXml = """
                            <AccountPostingResponse xmlns="urn:demo:rcbs">
                              <requestId>%s</requestId>
                              <status>SUCCESS</status>
                              <reference>REF-%d</reference>
                              <message>Posted</message>
                            </AccountPostingResponse>
                            """.formatted(requestId, id);
                    AccountPostingResponse response = binding.unmarshalResponse(responseXml);
                    return requestId.equals(response.getRequestId()) && "SUCCESS".equals(response.getStatus());
                });
            }

            List<Future<Boolean>> futures = pool.invokeAll(work);
            for (Future<Boolean> future : futures) {
                try {
                    assertTrue(future.get());
                } catch (ExecutionException e) {
                    throw (e.getCause() instanceof Exception ex) ? ex : e;
                }
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void shouldRejectXmlThatDoesNotMatchXsd() {
        RcbsXmlBinding binding = new RcbsXmlBinding();
        String invalidResponse = """
                <AccountPostingResponse xmlns="urn:demo:rcbs">
                  <status>SUCCESS</status>
                </AccountPostingResponse>
                """;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> binding.unmarshalResponse(invalidResponse));
        assertTrue(ex.getMessage().contains("Failed to unmarshal XML"));
    }

    @Test
    void shouldMarshalRequestWithExpectedRoot() {
        RcbsXmlBinding binding = new RcbsXmlBinding();
        AccountPostingRequest req = new AccountPostingRequest("REQ-777", new BigDecimal("12.34"));
        String xml = binding.marshalRequest(req);

        assertTrue(xml.contains("AccountPostingRequest"));
        assertTrue(xml.contains("<requestId>REQ-777</requestId>"));
        assertTrue(xml.contains("<amount>12.34</amount>"));
        assertEquals(true, xml.contains("urn:demo:rcbs"));
    }
}
