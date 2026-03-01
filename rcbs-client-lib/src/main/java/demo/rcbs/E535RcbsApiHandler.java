package demo.rcbs;

import demo.rcbs.xml.E535AccountPostingRequest;
import demo.rcbs.xml.E535AccountPostingResponse;

public final class E535RcbsApiHandler extends AbstractJaxbRcbsApiHandler<E535AccountPostingRequest, E535AccountPostingResponse> {
    public static final String REQUEST_ROOT = "E535AccountPostingRequest";

    public E535RcbsApiHandler(RcbsClientConfig cfg) {
        super(
                cfg,
                new JaxbSchemaBinding<>(
                        E535AccountPostingRequest.class,
                        E535AccountPostingResponse.class,
                        "xsd/e535/account-posting-e535.xsd"
                ),
                E535AccountPostingRequest::getRequestId
        );
    }

    @Override
    public String requestRootElement() {
        return REQUEST_ROOT;
    }
}
