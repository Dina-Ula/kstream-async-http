package demo.rcbs;

import demo.rcbs.xml.AccountPostingRequest;
import demo.rcbs.xml.AccountPostingResponse;

/**
 * Core XSD binding facade retained for compatibility.
 */
public final class RcbsXmlBinding {
    private static final String CORE_XSD = "xsd/core/account-posting-core.xsd";
    private final JaxbSchemaBinding<AccountPostingRequest, AccountPostingResponse> delegate =
            new JaxbSchemaBinding<>(AccountPostingRequest.class, AccountPostingResponse.class, CORE_XSD);

    public String marshalRequest(AccountPostingRequest request) {
        return delegate.marshalRequest(request);
    }

    public AccountPostingRequest unmarshalRequest(String requestXml) {
        return delegate.unmarshalRequest(requestXml);
    }

    public String marshalResponse(AccountPostingResponse response) {
        return delegate.marshalResponse(response);
    }

    public AccountPostingResponse unmarshalResponse(String responseXml) {
        return delegate.unmarshalResponse(responseXml);
    }
}
