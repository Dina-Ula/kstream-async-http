package demo.rcbs;

import demo.rcbs.xml.E535AccountPostingRequest;
import demo.rcbs.xml.E535AccountPostingResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertTrue;

class E535XmlConverterConcurrencyTest {

    @Test
    @Timeout(20)
    void shouldMarshalAndUnmarshalConcurrently() throws Exception {
        Jaxb2Marshaller marshaller = e535Marshaller();
        XmlObjectConverterWithJaxbElement<E535AccountPostingRequest> reqConverter =
                new XmlObjectConverterWithJaxbElement<>(E535AccountPostingRequest.class, marshaller, marshaller);
        XmlObjectConverterWithJaxbElement<E535AccountPostingResponse> resConverter =
                new XmlObjectConverterWithJaxbElement<>(E535AccountPostingResponse.class, marshaller, marshaller);

        ExecutorService pool = Executors.newFixedThreadPool(32);
        try {
            int tasks = 300;
            List<Callable<Boolean>> work = new ArrayList<>(tasks);
            for (int i = 0; i < tasks; i++) {
                final int id = i;
                work.add(() -> {
                    String requestId = "REQ-" + id;
                    E535AccountPostingRequest req = new E535AccountPostingRequest(requestId, new BigDecimal("100.25"), "TENANT-1");
                    String reqXml = reqConverter.marshal(req);
                    E535AccountPostingRequest reqBack = reqConverter.unmarshal(reqXml);
                    String responseXml = """
                            <E535AccountPostingResponse xmlns="urn:demo:rcbs:e535">
                              <requestId>%s</requestId>
                              <status>SUCCESS</status>
                              <reference>REF-%d</reference>
                              <message>Posted</message>
                              <e535Reference>E535-%d</e535Reference>
                            </E535AccountPostingResponse>
                            """.formatted(requestId, id, id);
                    E535AccountPostingResponse response = resConverter.unmarshal(responseXml);
                    return requestId.equals(reqBack.getRequestId())
                            && requestId.equals(response.getRequestId())
                            && "SUCCESS".equals(response.getStatus());
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

    private static Jaxb2Marshaller e535Marshaller() {
        Jaxb2Marshaller m = new Jaxb2Marshaller();
        m.setClassesToBeBound(E535AccountPostingRequest.class, E535AccountPostingResponse.class);
        m.setSchemas(new ClassPathResource("xsd/e535/account-posting-e535.xsd"));
        return m;
    }
}
