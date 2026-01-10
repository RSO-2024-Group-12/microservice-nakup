package si.nakupify.service;

import com.paypal.sdk.models.LinkDescription;
import com.paypal.sdk.models.Order;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.xhtmlrenderer.pdf.ITextRenderer;
import si.nakupify.service.client.KosaricaClient;
import si.nakupify.service.client.NarocilaClient;
import si.nakupify.service.client.PaypalClient;
import si.nakupify.service.client.SkladisceClient;
import si.nakupify.service.dto.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@ApplicationScoped
public class NakupService {

    @Inject
    PaypalClient paypalClient;

    @Inject
    KosaricaClient kosaricaClient;

    @Inject
    SkladisceClient skladisceClient;

    @Inject
    Mailer mailer;

    @Inject
    NarocilaClient narocilaClient;

    private Logger log = Logger.getLogger(NakupService.class.getName());

    @PostConstruct
    private void init() {
        log.info("Inicializacija microservice-nakup.");
    }

    @PreDestroy
    private void destroy() {
        log.info("Ustavitev microservice-nakup.");
    }

    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }

    public PairDTO<byte[], ErrorDTO> prepareMail(PaymentOrderDTO paymentOrderDTO) {
        InputStream is = NakupService.class.getClassLoader().getResourceAsStream("/templates/racun.html");

        if (is == null) {
            ErrorDTO error = new ErrorDTO(500, "Napaka pri pripravi računa.");
            return new PairDTO<>(null, error);
        }

        String invoice;
        try {
            invoice = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            ErrorDTO error = new ErrorDTO(500, "Napaka pri pripravi računa.");
            return new PairDTO<>(null, error);
        }

        invoice = invoice.replace("${prodajalec}", paymentOrderDTO.getStore());
        invoice = invoice.replace("${racun}", paymentOrderDTO.getId_order().toString());
        invoice = invoice.replace("${datum}", LocalDate.now().toString());
        invoice = invoice.replace("${kupec}", paymentOrderDTO.getRecipient());

        String naslov = paymentOrderDTO.getStreet() + " " + paymentOrderDTO.getHouse_number() + "<br/>" +
                paymentOrderDTO.getPostal_code() + ", " + paymentOrderDTO.getCity() + "<br/>" +
                paymentOrderDTO.getCountry();
        invoice = invoice.replace("${naslov}", naslov);
        invoice = invoice.replace("${total}", paymentOrderDTO.getAmount().toString());

        String items = "";
        int i = 1;
        for (ElementDTO element : paymentOrderDTO.getItems()) {
            Float total = element.getCena() * element.getKolicina();
            String item = "<tr>" +
                    "<td>" + i + "</td>" +
                    "<td>" + element.getNaziv() + "</td>" +
                    "<td style=\"text-align: right;\">" + element.getKolicina() + "</td>" +
                    "<td style=\"text-align: right;\">" + paymentOrderDTO.getCurrency() + " " + element.getCena() + "</td>" +
                    "<td style=\"text-align: right;\">" + paymentOrderDTO.getCurrency() + " " + total + "</td>" +
                    "</tr>";
            items = items + item;
        }
        invoice = invoice.replace("${items}", items);

        ITextRenderer renderer = new ITextRenderer();
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        renderer.setDocumentFromString(invoice);
        renderer.layout();
        renderer.createPDF(os);
        return new PairDTO<>(os.toByteArray(), null);
    }

    public PairDTO<PaymentOrderDTO, ErrorDTO> startNakup(PaymentOrderDTO paymentOrderDTO) {
        PaymentOrderDTO payment = new PaymentOrderDTO();
        payment.setId_buyer(paymentOrderDTO.getId_buyer());
        payment.setRecipient(paymentOrderDTO.getRecipient());
        payment.setRecipient_email(paymentOrderDTO.getRecipient_email());
        payment.setId_seller(paymentOrderDTO.getId_seller());
        payment.setStore(paymentOrderDTO.getStore());
        payment.setCurrency(paymentOrderDTO.getCurrency());
        payment.setReturn_url(paymentOrderDTO.getReturn_url());
        payment.setCancel_url(paymentOrderDTO.getCancel_url());
        payment.setStreet(paymentOrderDTO.getStreet());
        payment.setHouse_number(paymentOrderDTO.getHouse_number());
        payment.setPostal_code(paymentOrderDTO.getPostal_code());
        payment.setCity(paymentOrderDTO.getCity());
        payment.setCountry(paymentOrderDTO.getCountry());

        Long id_kosarica = paymentOrderDTO.getId_buyer();
        PairDTO<KosaricaDTO, ErrorDTO> pair1 = kosaricaClient.getKosarica(id_kosarica);
        KosaricaDTO kosarica = pair1.getValue();
        ErrorDTO error1 = pair1.getError();

        if (error1 != null) {
            return new PairDTO<>(null, error1);
        }

        if (kosarica.getKosarica().size() == 0) {
            ErrorDTO error2 = new ErrorDTO(400, "Košarica za izvedbo nakupa ne sme biti prazna.");
            return new PairDTO<>(null, error2);
        }

        float amount = 0;
        List<ElementDTO> items = new ArrayList<>();
        for (ElementDTO element : kosarica.getKosarica()) {
            amount += element.getCena() * element.getKolicina();
            items.add(element);
        }
        payment.setAmount(amount);
        payment.setItems(items);

        PairDTO<Order, ErrorDTO> pair2 = paypalClient.createOrder(payment);
        Order order = pair2.getValue();
        ErrorDTO error3 = pair2.getError();

        if (error3 != null) {
            return new PairDTO<>(null, error3);
        }

        payment.setId_order(order.getId());

        for (LinkDescription link : order.getLinks()) {
            if (link.getRel().equals("approve")) {
                payment.setRedirect_url(link.getHref());
                break;
            }
        }

        return new PairDTO<>(payment, null);
    }

    public PairDTO<PaymentOrderDTO, ErrorDTO> confirmNakup(PaymentOrderDTO paymentOrderDTO) {
        PairDTO<Order, ErrorDTO> pair1 = paypalClient.captureOrder(paymentOrderDTO);
        Order order = pair1.getValue();
        ErrorDTO error = pair1.getError();

        if (error != null) {
            return new PairDTO<>(null, error);
        }

        if (order.getStatus().toString() != "COMPLETED") {
            ErrorDTO errorPayment = new ErrorDTO(409, "Plačilo ni bilo izvedeno uspešno.");
            return new PairDTO<>(null, errorPayment);
        }

        for (ElementDTO element : paymentOrderDTO.getItems()) {
            RequestDTO request = new RequestDTO();
            request.setId_request(UUID.randomUUID().toString());
            request.setType("SOLD");
            request.setId_product(element.getId_izdelek());
            request.setId_user(paymentOrderDTO.getId_buyer());
            request.setQuantityAdd(0);
            request.setQuantityRemove(element.getKolicina());

            PairDTO<ResponseDTO, ErrorDTO> pair2 = skladisceClient.postRequestDTO(request);
            ErrorDTO error2 = pair2.getError();

            if (error2 != null) {
                return new PairDTO<>(null, error2);
            }
        }

        ErrorDTO error3 = kosaricaClient.deleteKosarica(paymentOrderDTO.getId_buyer());
        if (error3 != null) {
            return new PairDTO<>(null, error3);
        }

        PairDTO<byte[], ErrorDTO> pair =  prepareMail(paymentOrderDTO);
        byte[] pdf = pair.getValue();
        ErrorDTO error4 = pair.getError();

        if (error4 != null) {
            return new PairDTO<>(null, error4);
        }

        mailer.send(
                Mail.withText(
                        paymentOrderDTO.getRecipient_email(),
                        "Račun",
                        "Pozdravljeni. V priponki smo vam poslali račun za nakup. Hvala da ste izbrali nas."
                )
                .addAttachment(
                        "Racun.pdf",
                        pdf,
                        "application/pdf"
                )
        );

        ErrorDTO error5 = narocilaClient.postNarocila(paymentOrderDTO);
        if (error5 != null) {
            return new PairDTO<>(null, error5);
        }

        return new PairDTO<>(paymentOrderDTO, null);
    }
}
