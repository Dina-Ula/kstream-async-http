package demo.rcbs;

import demo.rcbs.xml.E536AccountPostingRequest;
import demo.rcbs.xml.E536AccountPostingResponse;

public final class E536PostingService extends AbstractPostingService<E536AccountPostingRequest, E536AccountPostingResponse> {
    public E536PostingService(
            RcbsPostingService postingService,
            XmlObjectConverterWithJaxbElement<E536AccountPostingRequest> reqConverter,
            XmlObjectConverterWithJaxbElement<E536AccountPostingResponse> resConverter
    ) {
        this(postingService, reqConverter, resConverter, null, null);
    }

    public E536PostingService(
            RcbsPostingService postingService,
            XmlObjectConverterWithJaxbElement<E536AccountPostingRequest> reqConverter,
            XmlObjectConverterWithJaxbElement<E536AccountPostingResponse> resConverter,
            SingletonJaxbXmlObjectConverterWithJaxbElement<E536AccountPostingRequest> singletonReqConverter,
            SingletonJaxbXmlObjectConverterWithJaxbElement<E536AccountPostingResponse> singletonResConverter
    ) {
        super(
                postingService,
                reqConverter,
                resConverter,
                singletonReqConverter,
                singletonResConverter,
                E536AccountPostingRequest::getRequestId
        );
    }

    @Override
    public RcbsApiType apiType() {
        return RcbsApiType.E536;
    }
}
