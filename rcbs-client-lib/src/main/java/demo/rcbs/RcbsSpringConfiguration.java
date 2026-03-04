package demo.rcbs;

import demo.rcbs.xml.E535AccountPostingRequest;
import demo.rcbs.xml.E535AccountPostingResponse;
import demo.rcbs.xml.E536AccountPostingRequest;
import demo.rcbs.xml.E536AccountPostingResponse;
import demo.rcbs.xml.E537AccountPostingRequest;
import demo.rcbs.xml.E537AccountPostingResponse;
import demo.rcbs.xml.E538AccountPostingRequest;
import demo.rcbs.xml.E538AccountPostingResponse;
import demo.rcbs.xml.E539AccountPostingRequest;
import demo.rcbs.xml.E539AccountPostingResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RcbsSpringProperties.class)
public class RcbsSpringConfiguration {

    @Bean(name = "e535Marshaller")
    public Jaxb2Marshaller e535Marshaller() {
        return marshaller(E535AccountPostingRequest.class, E535AccountPostingResponse.class, "xsd/e535/account-posting-e535.xsd");
    }

    @Bean(name = "e536Marshaller")
    public Jaxb2Marshaller e536Marshaller() {
        return marshaller(E536AccountPostingRequest.class, E536AccountPostingResponse.class, "xsd/e536/account-posting-e536.xsd");
    }

    @Bean(name = "e537Marshaller")
    public Jaxb2Marshaller e537Marshaller() {
        return marshaller(E537AccountPostingRequest.class, E537AccountPostingResponse.class, "xsd/e537/account-posting-e537.xsd");
    }

    @Bean(name = "e538Marshaller")
    public Jaxb2Marshaller e538Marshaller() {
        return marshaller(E538AccountPostingRequest.class, E538AccountPostingResponse.class, "xsd/e538/account-posting-e538.xsd");
    }

    @Bean(name = "e539Marshaller")
    public Jaxb2Marshaller e539Marshaller() {
        return marshaller(E539AccountPostingRequest.class, E539AccountPostingResponse.class, "xsd/e539/account-posting-e539.xsd");
    }

    @Bean(name = "e535Unmarshaller")
    public Unmarshaller e535Unmarshaller(@Qualifier("e535Marshaller") Jaxb2Marshaller marshaller) { return marshaller; }

    @Bean(name = "e536Unmarshaller")
    public Unmarshaller e536Unmarshaller(@Qualifier("e536Marshaller") Jaxb2Marshaller marshaller) { return marshaller; }

    @Bean(name = "e537Unmarshaller")
    public Unmarshaller e537Unmarshaller(@Qualifier("e537Marshaller") Jaxb2Marshaller marshaller) { return marshaller; }

    @Bean(name = "e538Unmarshaller")
    public Unmarshaller e538Unmarshaller(@Qualifier("e538Marshaller") Jaxb2Marshaller marshaller) { return marshaller; }

    @Bean(name = "e539Unmarshaller")
    public Unmarshaller e539Unmarshaller(@Qualifier("e539Marshaller") Jaxb2Marshaller marshaller) { return marshaller; }

    @Bean(name = "e535SingletonRuntimeMarshaller")
    public jakarta.xml.bind.Marshaller e535SingletonRuntimeMarshaller(
            @Qualifier("e535Marshaller") Jaxb2Marshaller marshaller
    ) throws Exception {
        return marshaller.createMarshaller();
    }

    @Bean(name = "e535SingletonRuntimeUnmarshaller")
    public jakarta.xml.bind.Unmarshaller e535SingletonRuntimeUnmarshaller(
            @Qualifier("e535Marshaller") Jaxb2Marshaller marshaller
    ) throws Exception {
        return marshaller.createUnmarshaller();
    }

    @Bean(name = "e536SingletonRuntimeMarshaller")
    public jakarta.xml.bind.Marshaller e536SingletonRuntimeMarshaller(
            @Qualifier("e536Marshaller") Jaxb2Marshaller marshaller
    ) throws Exception {
        return marshaller.createMarshaller();
    }

    @Bean(name = "e536SingletonRuntimeUnmarshaller")
    public jakarta.xml.bind.Unmarshaller e536SingletonRuntimeUnmarshaller(
            @Qualifier("e536Marshaller") Jaxb2Marshaller marshaller
    ) throws Exception {
        return marshaller.createUnmarshaller();
    }

    @Bean(name = "e537SingletonRuntimeMarshaller")
    public jakarta.xml.bind.Marshaller e537SingletonRuntimeMarshaller(
            @Qualifier("e537Marshaller") Jaxb2Marshaller marshaller
    ) throws Exception {
        return marshaller.createMarshaller();
    }

    @Bean(name = "e537SingletonRuntimeUnmarshaller")
    public jakarta.xml.bind.Unmarshaller e537SingletonRuntimeUnmarshaller(
            @Qualifier("e537Marshaller") Jaxb2Marshaller marshaller
    ) throws Exception {
        return marshaller.createUnmarshaller();
    }

    @Bean(name = "e538SingletonRuntimeMarshaller")
    public jakarta.xml.bind.Marshaller e538SingletonRuntimeMarshaller(
            @Qualifier("e538Marshaller") Jaxb2Marshaller marshaller
    ) throws Exception {
        return marshaller.createMarshaller();
    }

    @Bean(name = "e538SingletonRuntimeUnmarshaller")
    public jakarta.xml.bind.Unmarshaller e538SingletonRuntimeUnmarshaller(
            @Qualifier("e538Marshaller") Jaxb2Marshaller marshaller
    ) throws Exception {
        return marshaller.createUnmarshaller();
    }

    @Bean(name = "e539SingletonRuntimeMarshaller")
    public jakarta.xml.bind.Marshaller e539SingletonRuntimeMarshaller(
            @Qualifier("e539Marshaller") Jaxb2Marshaller marshaller
    ) throws Exception {
        return marshaller.createMarshaller();
    }

    @Bean(name = "e539SingletonRuntimeUnmarshaller")
    public jakarta.xml.bind.Unmarshaller e539SingletonRuntimeUnmarshaller(
            @Qualifier("e539Marshaller") Jaxb2Marshaller marshaller
    ) throws Exception {
        return marshaller.createUnmarshaller();
    }

    @Bean(name = "e535ReqXmlConverter")
    public XmlObjectConverterWithJaxbElement<E535AccountPostingRequest> e535ReqXmlConverter(
            @Qualifier("e535Marshaller") Jaxb2Marshaller marshaller,
            @Qualifier("e535Unmarshaller") Unmarshaller unmarshaller
    ) {
        return new XmlObjectConverterWithJaxbElement<>(E535AccountPostingRequest.class, marshaller, unmarshaller);
    }

    @Bean(name = "e535ReqSingletonXmlConverter")
    public SingletonJaxbXmlObjectConverterWithJaxbElement<E535AccountPostingRequest> e535ReqSingletonXmlConverter(
            @Qualifier("e535SingletonRuntimeMarshaller") jakarta.xml.bind.Marshaller marshaller,
            @Qualifier("e535SingletonRuntimeUnmarshaller") jakarta.xml.bind.Unmarshaller unmarshaller
    ) {
        return new SingletonJaxbXmlObjectConverterWithJaxbElement<>(E535AccountPostingRequest.class, marshaller, unmarshaller);
    }

    @Bean(name = "e535ResXmlConverter")
    public XmlObjectConverterWithJaxbElement<E535AccountPostingResponse> e535ResXmlConverter(
            @Qualifier("e535Marshaller") Jaxb2Marshaller marshaller,
            @Qualifier("e535Unmarshaller") Unmarshaller unmarshaller
    ) {
        return new XmlObjectConverterWithJaxbElement<>(E535AccountPostingResponse.class, marshaller, unmarshaller);
    }

    @Bean(name = "e535ResSingletonXmlConverter")
    public SingletonJaxbXmlObjectConverterWithJaxbElement<E535AccountPostingResponse> e535ResSingletonXmlConverter(
            @Qualifier("e535SingletonRuntimeMarshaller") jakarta.xml.bind.Marshaller marshaller,
            @Qualifier("e535SingletonRuntimeUnmarshaller") jakarta.xml.bind.Unmarshaller unmarshaller
    ) {
        return new SingletonJaxbXmlObjectConverterWithJaxbElement<>(E535AccountPostingResponse.class, marshaller, unmarshaller);
    }

    @Bean(name = "e536ReqXmlConverter")
    public XmlObjectConverterWithJaxbElement<E536AccountPostingRequest> e536ReqXmlConverter(
            @Qualifier("e536Marshaller") Jaxb2Marshaller marshaller,
            @Qualifier("e536Unmarshaller") Unmarshaller unmarshaller
    ) {
        return new XmlObjectConverterWithJaxbElement<>(E536AccountPostingRequest.class, marshaller, unmarshaller);
    }

    @Bean(name = "e536ReqSingletonXmlConverter")
    public SingletonJaxbXmlObjectConverterWithJaxbElement<E536AccountPostingRequest> e536ReqSingletonXmlConverter(
            @Qualifier("e536SingletonRuntimeMarshaller") jakarta.xml.bind.Marshaller marshaller,
            @Qualifier("e536SingletonRuntimeUnmarshaller") jakarta.xml.bind.Unmarshaller unmarshaller
    ) {
        return new SingletonJaxbXmlObjectConverterWithJaxbElement<>(E536AccountPostingRequest.class, marshaller, unmarshaller);
    }

    @Bean(name = "e536ResXmlConverter")
    public XmlObjectConverterWithJaxbElement<E536AccountPostingResponse> e536ResXmlConverter(
            @Qualifier("e536Marshaller") Jaxb2Marshaller marshaller,
            @Qualifier("e536Unmarshaller") Unmarshaller unmarshaller
    ) {
        return new XmlObjectConverterWithJaxbElement<>(E536AccountPostingResponse.class, marshaller, unmarshaller);
    }

    @Bean(name = "e536ResSingletonXmlConverter")
    public SingletonJaxbXmlObjectConverterWithJaxbElement<E536AccountPostingResponse> e536ResSingletonXmlConverter(
            @Qualifier("e536SingletonRuntimeMarshaller") jakarta.xml.bind.Marshaller marshaller,
            @Qualifier("e536SingletonRuntimeUnmarshaller") jakarta.xml.bind.Unmarshaller unmarshaller
    ) {
        return new SingletonJaxbXmlObjectConverterWithJaxbElement<>(E536AccountPostingResponse.class, marshaller, unmarshaller);
    }

    @Bean(name = "e537ReqXmlConverter")
    public XmlObjectConverterWithJaxbElement<E537AccountPostingRequest> e537ReqXmlConverter(
            @Qualifier("e537Marshaller") Jaxb2Marshaller marshaller,
            @Qualifier("e537Unmarshaller") Unmarshaller unmarshaller
    ) {
        return new XmlObjectConverterWithJaxbElement<>(E537AccountPostingRequest.class, marshaller, unmarshaller);
    }

    @Bean(name = "e537ReqSingletonXmlConverter")
    public SingletonJaxbXmlObjectConverterWithJaxbElement<E537AccountPostingRequest> e537ReqSingletonXmlConverter(
            @Qualifier("e537SingletonRuntimeMarshaller") jakarta.xml.bind.Marshaller marshaller,
            @Qualifier("e537SingletonRuntimeUnmarshaller") jakarta.xml.bind.Unmarshaller unmarshaller
    ) {
        return new SingletonJaxbXmlObjectConverterWithJaxbElement<>(E537AccountPostingRequest.class, marshaller, unmarshaller);
    }

    @Bean(name = "e537ResXmlConverter")
    public XmlObjectConverterWithJaxbElement<E537AccountPostingResponse> e537ResXmlConverter(
            @Qualifier("e537Marshaller") Jaxb2Marshaller marshaller,
            @Qualifier("e537Unmarshaller") Unmarshaller unmarshaller
    ) {
        return new XmlObjectConverterWithJaxbElement<>(E537AccountPostingResponse.class, marshaller, unmarshaller);
    }

    @Bean(name = "e537ResSingletonXmlConverter")
    public SingletonJaxbXmlObjectConverterWithJaxbElement<E537AccountPostingResponse> e537ResSingletonXmlConverter(
            @Qualifier("e537SingletonRuntimeMarshaller") jakarta.xml.bind.Marshaller marshaller,
            @Qualifier("e537SingletonRuntimeUnmarshaller") jakarta.xml.bind.Unmarshaller unmarshaller
    ) {
        return new SingletonJaxbXmlObjectConverterWithJaxbElement<>(E537AccountPostingResponse.class, marshaller, unmarshaller);
    }

    @Bean(name = "e538ReqXmlConverter")
    public XmlObjectConverterWithJaxbElement<E538AccountPostingRequest> e538ReqXmlConverter(
            @Qualifier("e538Marshaller") Jaxb2Marshaller marshaller,
            @Qualifier("e538Unmarshaller") Unmarshaller unmarshaller
    ) {
        return new XmlObjectConverterWithJaxbElement<>(E538AccountPostingRequest.class, marshaller, unmarshaller);
    }

    @Bean(name = "e538ReqSingletonXmlConverter")
    public SingletonJaxbXmlObjectConverterWithJaxbElement<E538AccountPostingRequest> e538ReqSingletonXmlConverter(
            @Qualifier("e538SingletonRuntimeMarshaller") jakarta.xml.bind.Marshaller marshaller,
            @Qualifier("e538SingletonRuntimeUnmarshaller") jakarta.xml.bind.Unmarshaller unmarshaller
    ) {
        return new SingletonJaxbXmlObjectConverterWithJaxbElement<>(E538AccountPostingRequest.class, marshaller, unmarshaller);
    }

    @Bean(name = "e538ResXmlConverter")
    public XmlObjectConverterWithJaxbElement<E538AccountPostingResponse> e538ResXmlConverter(
            @Qualifier("e538Marshaller") Jaxb2Marshaller marshaller,
            @Qualifier("e538Unmarshaller") Unmarshaller unmarshaller
    ) {
        return new XmlObjectConverterWithJaxbElement<>(E538AccountPostingResponse.class, marshaller, unmarshaller);
    }

    @Bean(name = "e538ResSingletonXmlConverter")
    public SingletonJaxbXmlObjectConverterWithJaxbElement<E538AccountPostingResponse> e538ResSingletonXmlConverter(
            @Qualifier("e538SingletonRuntimeMarshaller") jakarta.xml.bind.Marshaller marshaller,
            @Qualifier("e538SingletonRuntimeUnmarshaller") jakarta.xml.bind.Unmarshaller unmarshaller
    ) {
        return new SingletonJaxbXmlObjectConverterWithJaxbElement<>(E538AccountPostingResponse.class, marshaller, unmarshaller);
    }

    @Bean(name = "e539ReqXmlConverter")
    public XmlObjectConverterWithJaxbElement<E539AccountPostingRequest> e539ReqXmlConverter(
            @Qualifier("e539Marshaller") Jaxb2Marshaller marshaller,
            @Qualifier("e539Unmarshaller") Unmarshaller unmarshaller
    ) {
        return new XmlObjectConverterWithJaxbElement<>(E539AccountPostingRequest.class, marshaller, unmarshaller);
    }

    @Bean(name = "e539ReqSingletonXmlConverter")
    public SingletonJaxbXmlObjectConverterWithJaxbElement<E539AccountPostingRequest> e539ReqSingletonXmlConverter(
            @Qualifier("e539SingletonRuntimeMarshaller") jakarta.xml.bind.Marshaller marshaller,
            @Qualifier("e539SingletonRuntimeUnmarshaller") jakarta.xml.bind.Unmarshaller unmarshaller
    ) {
        return new SingletonJaxbXmlObjectConverterWithJaxbElement<>(E539AccountPostingRequest.class, marshaller, unmarshaller);
    }

    @Bean(name = "e539ResXmlConverter")
    public XmlObjectConverterWithJaxbElement<E539AccountPostingResponse> e539ResXmlConverter(
            @Qualifier("e539Marshaller") Jaxb2Marshaller marshaller,
            @Qualifier("e539Unmarshaller") Unmarshaller unmarshaller
    ) {
        return new XmlObjectConverterWithJaxbElement<>(E539AccountPostingResponse.class, marshaller, unmarshaller);
    }

    @Bean(name = "e539ResSingletonXmlConverter")
    public SingletonJaxbXmlObjectConverterWithJaxbElement<E539AccountPostingResponse> e539ResSingletonXmlConverter(
            @Qualifier("e539SingletonRuntimeMarshaller") jakarta.xml.bind.Marshaller marshaller,
            @Qualifier("e539SingletonRuntimeUnmarshaller") jakarta.xml.bind.Unmarshaller unmarshaller
    ) {
        return new SingletonJaxbXmlObjectConverterWithJaxbElement<>(E539AccountPostingResponse.class, marshaller, unmarshaller);
    }

    @Bean(name = "e535RcbsPostingService")
    public RcbsPostingService e535RcbsPostingService(RcbsSpringProperties props) {
        return new RcbsPostingService(props.configFor(props.getE535Url()));
    }

    @Bean(name = "e536RcbsPostingService")
    public RcbsPostingService e536RcbsPostingService(RcbsSpringProperties props) {
        return new RcbsPostingService(props.configFor(props.getE536Url()));
    }

    @Bean(name = "e537RcbsPostingService")
    public RcbsPostingService e537RcbsPostingService(RcbsSpringProperties props) {
        return new RcbsPostingService(props.configFor(props.getE537Url()));
    }

    @Bean(name = "e538RcbsPostingService")
    public RcbsPostingService e538RcbsPostingService(RcbsSpringProperties props) {
        return new RcbsPostingService(props.configFor(props.getE538Url()));
    }

    @Bean(name = "e539RcbsPostingService")
    public RcbsPostingService e539RcbsPostingService(RcbsSpringProperties props) {
        return new RcbsPostingService(props.configFor(props.getE539Url()));
    }

    @Bean
    public E535PostingService e535PostingService(
            @Qualifier("e535RcbsPostingService") RcbsPostingService postingService,
            @Qualifier("e535ReqXmlConverter") XmlObjectConverterWithJaxbElement<E535AccountPostingRequest> reqConverter,
            @Qualifier("e535ResXmlConverter") XmlObjectConverterWithJaxbElement<E535AccountPostingResponse> resConverter,
            @Qualifier("e535ReqSingletonXmlConverter") SingletonJaxbXmlObjectConverterWithJaxbElement<E535AccountPostingRequest> singletonReqConverter,
            @Qualifier("e535ResSingletonXmlConverter") SingletonJaxbXmlObjectConverterWithJaxbElement<E535AccountPostingResponse> singletonResConverter
    ) {
        return new E535PostingService(postingService, reqConverter, resConverter, singletonReqConverter, singletonResConverter);
    }

    @Bean
    public E536PostingService e536PostingService(
            @Qualifier("e536RcbsPostingService") RcbsPostingService postingService,
            @Qualifier("e536ReqXmlConverter") XmlObjectConverterWithJaxbElement<E536AccountPostingRequest> reqConverter,
            @Qualifier("e536ResXmlConverter") XmlObjectConverterWithJaxbElement<E536AccountPostingResponse> resConverter,
            @Qualifier("e536ReqSingletonXmlConverter") SingletonJaxbXmlObjectConverterWithJaxbElement<E536AccountPostingRequest> singletonReqConverter,
            @Qualifier("e536ResSingletonXmlConverter") SingletonJaxbXmlObjectConverterWithJaxbElement<E536AccountPostingResponse> singletonResConverter
    ) {
        return new E536PostingService(postingService, reqConverter, resConverter, singletonReqConverter, singletonResConverter);
    }

    @Bean
    public E537PostingService e537PostingService(
            @Qualifier("e537RcbsPostingService") RcbsPostingService postingService,
            @Qualifier("e537ReqXmlConverter") XmlObjectConverterWithJaxbElement<E537AccountPostingRequest> reqConverter,
            @Qualifier("e537ResXmlConverter") XmlObjectConverterWithJaxbElement<E537AccountPostingResponse> resConverter,
            @Qualifier("e537ReqSingletonXmlConverter") SingletonJaxbXmlObjectConverterWithJaxbElement<E537AccountPostingRequest> singletonReqConverter,
            @Qualifier("e537ResSingletonXmlConverter") SingletonJaxbXmlObjectConverterWithJaxbElement<E537AccountPostingResponse> singletonResConverter
    ) {
        return new E537PostingService(postingService, reqConverter, resConverter, singletonReqConverter, singletonResConverter);
    }

    @Bean
    public E538PostingService e538PostingService(
            @Qualifier("e538RcbsPostingService") RcbsPostingService postingService,
            @Qualifier("e538ReqXmlConverter") XmlObjectConverterWithJaxbElement<E538AccountPostingRequest> reqConverter,
            @Qualifier("e538ResXmlConverter") XmlObjectConverterWithJaxbElement<E538AccountPostingResponse> resConverter,
            @Qualifier("e538ReqSingletonXmlConverter") SingletonJaxbXmlObjectConverterWithJaxbElement<E538AccountPostingRequest> singletonReqConverter,
            @Qualifier("e538ResSingletonXmlConverter") SingletonJaxbXmlObjectConverterWithJaxbElement<E538AccountPostingResponse> singletonResConverter
    ) {
        return new E538PostingService(postingService, reqConverter, resConverter, singletonReqConverter, singletonResConverter);
    }

    @Bean
    public E539PostingService e539PostingService(
            @Qualifier("e539RcbsPostingService") RcbsPostingService postingService,
            @Qualifier("e539ReqXmlConverter") XmlObjectConverterWithJaxbElement<E539AccountPostingRequest> reqConverter,
            @Qualifier("e539ResXmlConverter") XmlObjectConverterWithJaxbElement<E539AccountPostingResponse> resConverter,
            @Qualifier("e539ReqSingletonXmlConverter") SingletonJaxbXmlObjectConverterWithJaxbElement<E539AccountPostingRequest> singletonReqConverter,
            @Qualifier("e539ResSingletonXmlConverter") SingletonJaxbXmlObjectConverterWithJaxbElement<E539AccountPostingResponse> singletonResConverter
    ) {
        return new E539PostingService(postingService, reqConverter, resConverter, singletonReqConverter, singletonResConverter);
    }

    @Bean
    public RcbsClient rcbsClient(
            E535PostingService e535PostingService,
            E536PostingService e536PostingService,
            E537PostingService e537PostingService,
            E538PostingService e538PostingService,
            E539PostingService e539PostingService
    ) {
        return new DefaultRcbsClient(List.of(
                e535PostingService,
                e536PostingService,
                e537PostingService,
                e538PostingService,
                e539PostingService
        ));
    }

    private static Jaxb2Marshaller marshaller(Class<?> req, Class<?> res, String xsdPath) {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(req, res);
        marshaller.setSchemas(new ClassPathResource(xsdPath));
        return marshaller;
    }
}
