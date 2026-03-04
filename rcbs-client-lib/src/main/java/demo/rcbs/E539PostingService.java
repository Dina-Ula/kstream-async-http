package demo.rcbs;

import demo.rcbs.xml.E539AccountPostingRequest;
import demo.rcbs.xml.E539AccountPostingResponse;

public final class E539PostingService extends AbstractPostingService<E539AccountPostingRequest, E539AccountPostingResponse> {
    public E539PostingService(
            RcbsPostingService postingService,
            XmlObjectConverterWithJaxbElement<E539AccountPostingRequest> reqConverter,
            XmlObjectConverterWithJaxbElement<E539AccountPostingResponse> resConverter
    ) {
        this(postingService, reqConverter, resConverter, null, null);
    }

    public E539PostingService(
            RcbsPostingService postingService,
            XmlObjectConverterWithJaxbElement<E539AccountPostingRequest> reqConverter,
            XmlObjectConverterWithJaxbElement<E539AccountPostingResponse> resConverter,
            SingletonJaxbXmlObjectConverterWithJaxbElement<E539AccountPostingRequest> singletonReqConverter,
            SingletonJaxbXmlObjectConverterWithJaxbElement<E539AccountPostingResponse> singletonResConverter
    ) {
        super(
                postingService,
                reqConverter,
                resConverter,
                singletonReqConverter,
                singletonResConverter,
                E539AccountPostingRequest::getRequestId
        );
    }

    @Override
    public RcbsApiType apiType() {
        return RcbsApiType.E539;
    }
}
