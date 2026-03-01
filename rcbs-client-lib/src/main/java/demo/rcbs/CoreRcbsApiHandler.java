package demo.rcbs;

import demo.rcbs.xml.AccountPostingRequest;
import demo.rcbs.xml.AccountPostingResponse;

public final class CoreRcbsApiHandler extends AbstractJaxbRcbsApiHandler<AccountPostingRequest, AccountPostingResponse> {
    public static final String REQUEST_ROOT = "AccountPostingRequest";

    public CoreRcbsApiHandler(RcbsClientConfig cfg) {
        super(
                cfg,
                new JaxbSchemaBinding<>(
                        AccountPostingRequest.class,
                        AccountPostingResponse.class,
                        "xsd/core/account-posting-core.xsd"
                ),
                AccountPostingRequest::getRequestId
        );
    }

    @Override
    public String requestRootElement() {
        return REQUEST_ROOT;
    }
}
