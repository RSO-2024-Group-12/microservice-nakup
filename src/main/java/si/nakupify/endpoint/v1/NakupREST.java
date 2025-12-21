package si.nakupify.endpoint.v1;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import si.nakupify.service.NakupService;
import si.nakupify.service.client.KosaricaClient;
import si.nakupify.service.client.PaypalClient;
import si.nakupify.service.dto.ErrorDTO;
import si.nakupify.service.dto.PairDTO;
import si.nakupify.service.dto.PaymentOrderDTO;

import java.util.logging.Logger;

@Path("/v1/nakup")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NakupREST {

    @Inject
    PaypalClient paypalClient;

    @Inject
    KosaricaClient kosaricaClient;

    @Inject
    NakupService nakupService;

    private Logger log = Logger.getLogger(NakupREST.class.getName());

    public ErrorDTO validacija(PaymentOrderDTO paymentOrderDTO, int mode) {
        if (paymentOrderDTO == null) {
            log.info("Validation fail: PaymentOrderDTO ne sme biti null");
            String msg = "Mora biti podan PaymentOrderDTO!";
            return new ErrorDTO(400, msg);
        }

        if (paymentOrderDTO.getId_buyer() == null || paymentOrderDTO.getId_seller() == null) {
            log.info("Validation fail: PaymentOrderDTO mora imeti podana polja: id_buyer, id_seller");
            String msg = "Polji id_buyer in id_seller morata biti podana!";
            return new ErrorDTO(400, msg);
        }

        if (paymentOrderDTO.getCurrency() == null || paymentOrderDTO.getCurrency().isBlank()) {
            log.info("Validation fail: ");
            String msg = "";
            return new ErrorDTO(400, msg);
        }

        if (paymentOrderDTO.getReturn_url() == null || paymentOrderDTO.getReturn_url().isBlank() ||
                paymentOrderDTO.getCancel_url() == null || paymentOrderDTO.getCancel_url().isBlank()) {
            log.info("Validation fail: PaymentOrderDTO mora imeti podana polja: currency");
            String msg = "Polje currency mora biti podano!";
            return new ErrorDTO(400, msg);
        }

        if (paymentOrderDTO.getStreet() == null || paymentOrderDTO.getStreet().isBlank() ||
                paymentOrderDTO.getHouse_number() == null || paymentOrderDTO.getHouse_number().isBlank() ||
                paymentOrderDTO.getCity() == null || paymentOrderDTO.getCity().isBlank() ||
                paymentOrderDTO.getPostal_code() == null || paymentOrderDTO.getPostal_code().isBlank() ||
                paymentOrderDTO.getCountry() == null || paymentOrderDTO.getCountry().isBlank()) {
            log.info("Validation fail: PaymentOrderDTO mora imeti podana polja: street, house_number, city, postal_code, country");
            String msg = "Polja street, house_number, city, postal_code, country morajo biti podana!";
            return new ErrorDTO(400, msg);
        }

        if (mode == 1) {
            if (paymentOrderDTO.getAmount() == null) {
                log.info("Validation fail: PaymentOrderDTO mora imeti podana polja: amount");
                String msg = "Polje amount mora biti podano!";
                return new ErrorDTO(400, msg);
            }

            if (paymentOrderDTO.getId_order() == null || paymentOrderDTO.getId_order().isBlank()) {
                log.info("Validation fail: PaymentOrderDTO mora imeti podana polja: id_order");
                String msg = "Polje id_order mora biti podano!";
                return new ErrorDTO(400, msg);
            }

            if (paymentOrderDTO.getRedirect_url() == null || paymentOrderDTO.getRedirect_url().isBlank()) {
                log.info("Validation fail: PaymentOrderDTO mora imeti podana polja: redirect_url");
                String msg = "Polje redirect_url mora biti podano!";
                return new ErrorDTO(400, msg);
            }

            if (paymentOrderDTO.getItems() == null || paymentOrderDTO.getItems().isEmpty()) {
                log.info("Validation fail: PaymentOrderDTO mora imeti podana polja: items");
                String msg = "Polje items mora biti podano!";
                return new ErrorDTO(400, msg);
            }
        }

        return null;
    }

    @GET
    public Response test() {
        //paypalClient.createOrder();

        //PairDTO<KosaricaDTO, ErrorDTO> pair = kosaricaClient.getKosarica(3L);
        //KosaricaDTO kosarica = pair.getValue();
        //ErrorDTO error = pair.getError();

        //ErrorDTO errorDTO = kosaricaClient.deleteKosarica(3L);

        return Response.status(200).build();
    }

    @POST
    @Path("/start")
    public Response startPayment(PaymentOrderDTO paymentOrderDTO) {
        ErrorDTO validationError = validacija(paymentOrderDTO, 0);
        if (validationError != null) {
            return Response.status(400).entity(validationError).build();
        }

        PairDTO<PaymentOrderDTO, ErrorDTO> pair = nakupService.startNakup(paymentOrderDTO);
        PaymentOrderDTO order = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            return Response.status(error.getErrorCode()).entity(error).build();
        }

        return Response.status(201).entity(order).build();
    }

    @POST
    @Path("/confirm")
    public Response confirmPayment(PaymentOrderDTO paymentOrderDTO) {
        ErrorDTO validationError = validacija(paymentOrderDTO, 1);
        if (validationError != null) {
            return Response.status(400).entity(validationError).build();
        }

        PairDTO<PaymentOrderDTO, ErrorDTO> pair = nakupService.confirmNakup(paymentOrderDTO);
        PaymentOrderDTO order = pair.getValue();
        ErrorDTO error = pair.getError();

        if (error != null) {
            return Response.status(error.getErrorCode()).entity(error).build();
        }

        return Response.status(201).entity(order).build();
    }
}
