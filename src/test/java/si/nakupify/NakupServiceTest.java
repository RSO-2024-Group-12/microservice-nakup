package si.nakupify;

import com.paypal.sdk.models.OrderStatus;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import si.nakupify.service.NakupService;
import si.nakupify.service.client.KosaricaClient;
import si.nakupify.service.client.NarocilaClient;
import si.nakupify.service.client.PaypalClient;
import si.nakupify.service.client.SkladisceClient;
import si.nakupify.service.dto.*;
import com.paypal.sdk.models.Order;
import com.paypal.sdk.models.LinkDescription;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@QuarkusTest
public class NakupServiceTest {

    @InjectSpy
    NakupService nakupService;

    @InjectMock
    PaypalClient paypalClient;

    @InjectMock
    KosaricaClient kosaricaClient;

    @InjectMock
    SkladisceClient skladisceClient;

    @InjectMock
    NarocilaClient narocilaClient;

    Mailer mailer;

    private PaymentOrderDTO makePaymentOrderDTO(int mode) {
        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        paymentOrderDTO.setId_buyer(1L);
        paymentOrderDTO.setId_seller(1L);
        paymentOrderDTO.setCurrency("EUR");
        paymentOrderDTO.setReturn_url("https://nakupify.com/");
        paymentOrderDTO.setCancel_url("https://nakupify.com/");
        paymentOrderDTO.setStreet("Street");
        paymentOrderDTO.setHouse_number("123A");
        paymentOrderDTO.setCity("Ljubljana");
        paymentOrderDTO.setPostal_code("1000");
        paymentOrderDTO.setCountry("SI");

        if (mode == 1) {
            paymentOrderDTO.setAmount(50.00F);
            paymentOrderDTO.setId_order("ABC123");
            paymentOrderDTO.setRedirect_url("https://paypal.com/");
            paymentOrderDTO.setItems(List.of(new ElementDTO(1L, 1L, "Test", 10.0F, 5)));
        }

        return paymentOrderDTO;
    }

    @BeforeEach
    void setup() {
        mailer = Mockito.mock(Mailer.class);
        nakupService.setMailer(mailer);
    }

    @Test
    void startNakup_test() {
        KosaricaDTO kosaricaDTO = new KosaricaDTO();
        kosaricaDTO.setId_uporabnik(1L);
        kosaricaDTO.setKosarica(List.of(new ElementDTO(1L, 1L, "Test", 10.0F, 5)));
        when(kosaricaClient.getKosarica(1L)).thenReturn(new PairDTO<>(kosaricaDTO, null));

        LinkDescription linkDescription = new LinkDescription();
        linkDescription.setRel("approve");
        linkDescription.setHref("https://paypal.com/");

        Order order = new Order();
        order.setId("ABC123");
        order.setLinks(List.of(linkDescription));
        when(paypalClient.createOrder(any())).thenReturn(new PairDTO<>(order, null));

        PairDTO<PaymentOrderDTO, ErrorDTO> result = nakupService.startNakup(makePaymentOrderDTO(0));

        assertNotNull(result);
        assertNotNull(result.getValue());
        assertNull(result.getError());

        PaymentOrderDTO paymentOrderDTO = result.getValue();

        assertEquals(1L, paymentOrderDTO.getId_buyer());
        assertEquals(1L, paymentOrderDTO.getId_seller());
        assertEquals("EUR", paymentOrderDTO.getCurrency());
        assertEquals("https://nakupify.com/", paymentOrderDTO.getReturn_url());
        assertEquals("https://nakupify.com/", paymentOrderDTO.getCancel_url());
        assertEquals("Street", paymentOrderDTO.getStreet());
        assertEquals("123A", paymentOrderDTO.getHouse_number());
        assertEquals("Ljubljana", paymentOrderDTO.getCity());
        assertEquals("1000", paymentOrderDTO.getPostal_code());
        assertEquals("SI", paymentOrderDTO.getCountry());
        assertEquals(50.00F, paymentOrderDTO.getAmount());
        assertEquals("ABC123", paymentOrderDTO.getId_order());
        assertEquals("https://paypal.com/", paymentOrderDTO.getRedirect_url());
        assertEquals(1, paymentOrderDTO.getItems().size());

        verify(kosaricaClient).getKosarica(anyLong());
        verify(paypalClient).createOrder(any());
    }

    @Test
    void confirmNakup_test() {
        Order order = new Order();
        order.setStatus(OrderStatus.valueOf("COMPLETED"));
        when(paypalClient.captureOrder(any())).thenReturn(new PairDTO<>(order, null));

        ResponseDTO responseDTO = new ResponseDTO("Test", true);
        when(skladisceClient.postRequestDTO(any())).thenReturn(new PairDTO<>(responseDTO, null));

        when(kosaricaClient.deleteKosarica(1L)).thenReturn(null);

        doReturn(new PairDTO<>("<html>Test invoice</html>".getBytes(StandardCharsets.UTF_8), null))
                .when(nakupService).prepareMail(any());

        //when(nakupService.prepareMail(any())).thenReturn(new PairDTO<>("<html>Test invoice</html>".getBytes(StandardCharsets.UTF_8), null));

        doNothing().when(mailer).send(any(Mail.class));

        when(narocilaClient.postNarocila(any())).thenReturn(null);

        PairDTO<PaymentOrderDTO, ErrorDTO> result = nakupService.confirmNakup(makePaymentOrderDTO(1));

        assertNotNull(result);
        assertNotNull(result.getValue());
        assertNull(result.getError());

        PaymentOrderDTO paymentOrderDTO = result.getValue();

        assertEquals(1L, paymentOrderDTO.getId_buyer());
        assertEquals(1L, paymentOrderDTO.getId_seller());
        assertEquals("EUR", paymentOrderDTO.getCurrency());
        assertEquals("https://nakupify.com/", paymentOrderDTO.getReturn_url());
        assertEquals("https://nakupify.com/", paymentOrderDTO.getCancel_url());
        assertEquals("Street", paymentOrderDTO.getStreet());
        assertEquals("123A", paymentOrderDTO.getHouse_number());
        assertEquals("Ljubljana", paymentOrderDTO.getCity());
        assertEquals("1000", paymentOrderDTO.getPostal_code());
        assertEquals("SI", paymentOrderDTO.getCountry());
        assertEquals(50.00F, paymentOrderDTO.getAmount());
        assertEquals("ABC123", paymentOrderDTO.getId_order());
        assertEquals("https://paypal.com/", paymentOrderDTO.getRedirect_url());
        assertEquals(1, paymentOrderDTO.getItems().size());

        verify(paypalClient).captureOrder(any());
        verify(skladisceClient).postRequestDTO(any());
        verify(kosaricaClient).deleteKosarica(anyLong());
        verify(nakupService).prepareMail(any());
        verify(mailer).send(any());
        verify(narocilaClient).postNarocila(any());
    }
}
