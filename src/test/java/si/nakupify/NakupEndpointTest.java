package si.nakupify;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import si.nakupify.service.NakupService;
import si.nakupify.service.dto.ElementDTO;
import si.nakupify.service.dto.ErrorDTO;
import si.nakupify.service.dto.PairDTO;
import si.nakupify.service.dto.PaymentOrderDTO;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class NakupEndpointTest {

    @InjectMock
    NakupService nakupService;

    private PaymentOrderDTO paymentOrder() {
        PaymentOrderDTO paymentOrderDTO = new PaymentOrderDTO();
        paymentOrderDTO.setId_buyer(1L);
        paymentOrderDTO.setId_seller(1L);
        paymentOrderDTO.setAmount(50.00F);
        paymentOrderDTO.setCurrency("EUR");
        paymentOrderDTO.setId_order("ABC123");
        paymentOrderDTO.setRedirect_url("https://paypal.com/");
        paymentOrderDTO.setReturn_url("https://nakupify.com/");
        paymentOrderDTO.setCancel_url("https://nakupify.com/");
        paymentOrderDTO.setStreet("Street");
        paymentOrderDTO.setHouse_number("123A");
        paymentOrderDTO.setCity("Ljubljana");
        paymentOrderDTO.setPostal_code("1000");
        paymentOrderDTO.setCountry("SI");
        paymentOrderDTO.setItems(List.of(new ElementDTO(1L, 1L, "Test", 10.0F, 5)));
        return paymentOrderDTO;
    }

    private ErrorDTO errorDTO(int code, String message) {
        return new ErrorDTO(code, message);
    }

    @Test
    void startPayment_test() {
        when(nakupService.startNakup(any())).thenReturn(new PairDTO<>(paymentOrder(), null));

        String requestBody = """
        {
          "id_buyer": 1,
          "id_seller": 1,
          "currency": "EUR",
          "return_url": "https://nakupify.com/",
          "cancel_url": "https://nakupify.com/",
          "street": "Street",
          "house_number": "123A",
          "city": "Ljubljana",
          "postal_code": "1000",
          "country": "SI"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/v1/nakup/start")
        .then()
                .statusCode(201)
                .body("id_order", equalTo("ABC123"))
                .body("redirect_url", equalTo("https://paypal.com/"))
                .body("amount", equalTo(50.0f))
                .body("items", hasSize(1));

        verify(nakupService).startNakup(any());
    }

    @Test
    void confirmPayment_test() {
        when(nakupService.confirmNakup(any())).thenReturn(new PairDTO<>(paymentOrder(), null));

        String requestBody = """
        {
            "id_buyer": 1,
            "id_seller": 1,
            "amount": 50.00,
            "currency": "EUR",
            "id_order": "ABC123",
            "redirect_url": "https://paypal.com/",
            "return_url": "https://nakupify.com/",
            "cancel_url": "https://nakupify.com/",
            "street": "Street",
            "house_number": "123A",
            "city": "Ljubljana",
            "postal_code": "1000",
            "country": "SI",
            "items": [
                {
                    "id_kosarica": 1,
                    "id_izdelek": 1,
                    "naziv": "Test",
                    "cena": 10.00,
                    "kolicina": 5
                }
            ]
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/v1/nakup/confirm")
        .then()
                .statusCode(201)
                .body("id_order", equalTo("ABC123"))
                .body("redirect_url", equalTo("https://paypal.com/"))
                .body("amount", equalTo(50.0f))
                .body("items", hasSize(1));

        verify(nakupService).confirmNakup(any());
    }
}
