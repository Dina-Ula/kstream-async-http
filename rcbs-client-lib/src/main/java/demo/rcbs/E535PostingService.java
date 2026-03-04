package demo.rcbs;

import demo.rcbs.xml.E535AccountPostingRequest;
import demo.rcbs.xml.E535AccountPostingResponse;

public final class E535PostingService extends AbstractPostingService<E535AccountPostingRequest, E535AccountPostingResponse> {
    public E535PostingService(
            RcbsPostingService postingService,
            XmlObjectConverterWithJaxbElement<E535AccountPostingRequest> reqConverter,
            XmlObjectConverterWithJaxbElement<E535AccountPostingResponse> resConverter
    ) {
        this(postingService, reqConverter, resConverter, null, null);
    }

    public E535PostingService(
            RcbsPostingService postingService,
            XmlObjectConverterWithJaxbElement<E535AccountPostingRequest> reqConverter,
            XmlObjectConverterWithJaxbElement<E535AccountPostingResponse> resConverter,
            SingletonJaxbXmlObjectConverterWithJaxbElement<E535AccountPostingRequest> singletonReqConverter,
            SingletonJaxbXmlObjectConverterWithJaxbElement<E535AccountPostingResponse> singletonResConverter
    ) {
        super(
                postingService,
                reqConverter,
                resConverter,
                singletonReqConverter,
                singletonResConverter,
                E535AccountPostingRequest::getRequestId
        );
    }

    @Override
    public RcbsApiType apiType() {
        return RcbsApiType.E535;
    }
}
