package si.nakupify.service.client;

import com.paypal.sdk.Environment;
import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.authentication.ClientCredentialsAuthModel;
import com.paypal.sdk.exceptions.ApiException;
import com.paypal.sdk.http.response.ApiResponse;
import com.paypal.sdk.models.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.event.Level;
import si.nakupify.service.dto.ErrorDTO;
import si.nakupify.service.dto.PairDTO;
import si.nakupify.service.dto.PaymentOrderDTO;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

@ApplicationScoped
public class PaypalClient {

    @ConfigProperty(name="paypal.client.id")
    private String clientId;

    @ConfigProperty(name="paypal.client.secret")
    private String clientSecret;

    private PaypalServerSdkClient paypalClient;

    private Logger log = Logger.getLogger(PaypalClient.class.getName());

    @PostConstruct
    public void init() {
        paypalClient = new PaypalServerSdkClient.Builder()
                .environment(Environment.SANDBOX)
                .clientCredentialsAuth(
                        new ClientCredentialsAuthModel.Builder(
                                clientId,
                                clientSecret
                        ).build()
                )
                .loggingConfig(builder -> builder
                        .level(Level.DEBUG)
                        .requestConfig(logConfig -> logConfig.body(true))
                        .responseConfig(logConfig -> logConfig.headers(true)))
                .httpClientConfig(builder -> builder
                        .timeout(0))
                .build();
    }

    public PairDTO<Order, ErrorDTO> createOrder (PaymentOrderDTO paymentOrder) {
        CreateOrderInput createOrderInput = new CreateOrderInput.Builder(
                null,
                new OrderRequest.Builder(
                        CheckoutPaymentIntent.CAPTURE,
                        Arrays.asList(
                                new PurchaseUnitRequest.Builder(
                                        new AmountWithBreakdown.Builder(
                                                paymentOrder.getCurrency(),
                                                paymentOrder.getAmount().toString()
                                        ).build()
                                ).build()
                        )
                )
                .applicationContext(
                        new OrderApplicationContext.Builder()
                            .returnUrl(paymentOrder.getReturn_url())
                            .cancelUrl(paymentOrder.getCancel_url())
                            .build()
                )
                .build()
        )
        .prefer("return=minimal")
        .build();

        try{
            ApiResponse<Order> response = paypalClient.getOrdersController().createOrder(createOrderInput);
            return new PairDTO<>(response.getResult(), null);
        } catch (ApiException exp) {
            log.info("PayPal service error: " + exp.getMessage());
            ErrorDTO error = new ErrorDTO(exp.getResponseCode(), exp.getMessage());
            return new PairDTO<>(null, error);
        } catch (IOException exp) {
            log.info("PayPal service error: " + exp.getMessage());
            ErrorDTO error = new ErrorDTO(503, exp.getMessage());
            return new PairDTO<>(null, error);
        }
    }

    public PairDTO<Order, ErrorDTO> captureOrder (PaymentOrderDTO paymentOrder) {
        CaptureOrderInput captureOrderInput = new CaptureOrderInput.Builder(
                paymentOrder.getId_order(),
                null
        )
        .prefer("return=minimal")
        .build();

        try{
            ApiResponse<Order> response = paypalClient.getOrdersController().captureOrder(captureOrderInput);
            return new PairDTO<>(response.getResult(), null);
        } catch (ApiException exp) {
            log.info("PayPal service error: " + exp.getMessage());
            ErrorDTO error = new ErrorDTO(exp.getResponseCode(), exp.getMessage());
            return new PairDTO<>(null, error);
        } catch (IOException exp) {
            log.info("PayPal service error: " + exp.getMessage());
            ErrorDTO error = new ErrorDTO(503, exp.getMessage());
            return new PairDTO<>(null, error);
        }
    }

}
