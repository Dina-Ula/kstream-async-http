package demo.rcbs;

import demo.rcbs.xml.E537AccountPostingRequest;
import demo.rcbs.xml.E537AccountPostingResponse;

public final class E537PostingService extends AbstractPostingService<E537AccountPostingRequest, E537AccountPostingResponse> {
    public E537PostingService(
            RcbsPostingService postingService,
            XmlObjectConverterWithJaxbElement<E537AccountPostingRequest> reqConverter,
            XmlObjectConverterWithJaxbElement<E537AccountPostingResponse> resConverter
    ) {
        this(postingService, reqConverter, resConverter, null, null);
    }

    public E537PostingService(
            RcbsPostingService postingService,
            XmlObjectConverterWithJaxbElement<E537AccountPostingRequest> reqConverter,
            XmlObjectConverterWithJaxbElement<E537AccountPostingResponse> resConverter,
            SingletonJaxbXmlObjectConverterWithJaxbElement<E537AccountPostingRequest> singletonReqConverter,
            SingletonJaxbXmlObjectConverterWithJaxbElement<E537AccountPostingResponse> singletonResConverter
    ) {
        super(
                postingService,
                reqConverter,
                resConverter,
                singletonReqConverter,
                singletonResConverter,
                E537AccountPostingRequest::getRequestId
        );
    }

    @Override
    public RcbsApiType apiType() {
        return RcbsApiType.E537;
    }
}
