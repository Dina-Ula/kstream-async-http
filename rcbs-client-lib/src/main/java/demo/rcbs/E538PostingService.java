package demo.rcbs;

import demo.rcbs.xml.E538AccountPostingRequest;
import demo.rcbs.xml.E538AccountPostingResponse;

public final class E538PostingService extends AbstractPostingService<E538AccountPostingRequest, E538AccountPostingResponse> {
    public E538PostingService(
            RcbsPostingService postingService,
            XmlObjectConverterWithJaxbElement<E538AccountPostingRequest> reqConverter,
            XmlObjectConverterWithJaxbElement<E538AccountPostingResponse> resConverter
    ) {
        this(postingService, reqConverter, resConverter, null, null);
    }

    public E538PostingService(
            RcbsPostingService postingService,
            XmlObjectConverterWithJaxbElement<E538AccountPostingRequest> reqConverter,
            XmlObjectConverterWithJaxbElement<E538AccountPostingResponse> resConverter,
            SingletonJaxbXmlObjectConverterWithJaxbElement<E538AccountPostingRequest> singletonReqConverter,
            SingletonJaxbXmlObjectConverterWithJaxbElement<E538AccountPostingResponse> singletonResConverter
    ) {
        super(
                postingService,
                reqConverter,
                resConverter,
                singletonReqConverter,
                singletonResConverter,
                E538AccountPostingRequest::getRequestId
        );
    }

    @Override
    public RcbsApiType apiType() {
        return RcbsApiType.E538;
    }
}
