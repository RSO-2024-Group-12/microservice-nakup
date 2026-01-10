package si.nakupify.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import si.nakupify.service.dto.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class NarocilaClient {

    private HttpClient client;
    private ObjectMapper mapper;

    @ConfigProperty(name="narocila.url")
    private String narocilaUrl;

    private Logger log = Logger.getLogger(NarocilaClient.class.getName());

    @PostConstruct
    public void init() {
        client = HttpClient.newBuilder().build();
        mapper = new ObjectMapper();
    }

    public ErrorDTO comunicationError(PaymentOrderDTO paymentOrderDTO) {
        ErrorDTO error = new ErrorDTO(503, "Napaka pri komunikaciji z microservice-narocila.");
        return error;
    }

    @Retry(
            maxRetries = 3,
            delay = 500
    )
    @Timeout(2000)
    @CircuitBreaker(
            requestVolumeThreshold = 5,
            failureRatio = 0.5,
            delay = 10000
    )
    @Fallback(fallbackMethod = "comunicationError")
    public ErrorDTO postNarocila(PaymentOrderDTO paymentOrderDTO) {
        try {
            OrderRequestDTO orderRequestDTO = new OrderRequestDTO();
            orderRequestDTO.setUserId(paymentOrderDTO.getId_buyer());
            orderRequestDTO.setRecipientName(paymentOrderDTO.getRecipient());
            orderRequestDTO.setStreet(paymentOrderDTO.getStreet());
            orderRequestDTO.setHouseNumber(paymentOrderDTO.getHouse_number());
            orderRequestDTO.setCity(paymentOrderDTO.getCity());
            orderRequestDTO.setPostalCode(paymentOrderDTO.getPostal_code());
            orderRequestDTO.setCountry(paymentOrderDTO.getCountry());
            orderRequestDTO.setPaymentMethod("PayPal");
            orderRequestDTO.setPaid(true);
            orderRequestDTO.setShippingCostCents(0L);

            List<OrderItemRequestDTO> items = new ArrayList<>();
            for (ElementDTO element : paymentOrderDTO.getItems()) {
                Long price = (long) Math.round(element.getCena() * 100);
                OrderItemRequestDTO item = new OrderItemRequestDTO(element.getId_izdelek(), element.getKolicina(), price);
                items.add(item);
            }
            orderRequestDTO.setItems(items);

            String url = narocilaUrl;
            String payload = mapper.writeValueAsString(orderRequestDTO);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return null;
        } catch (Exception e) {
            log.severe("Communication error: Napaka pri komunikaciji z microservice-narocila. Napaka: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
