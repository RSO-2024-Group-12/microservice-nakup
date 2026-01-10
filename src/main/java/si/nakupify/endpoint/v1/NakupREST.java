package si.nakupify.endpoint.v1;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
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
    NakupService nakupService;

    private Logger log = Logger.getLogger(NakupREST.class.getName());

    public ErrorDTO validacija(PaymentOrderDTO paymentOrderDTO, int mode) {
        if (paymentOrderDTO == null) {
            log.info("Validation fail: PaymentOrderDTO ne sme biti null");
            String msg = "Mora biti podan PaymentOrderDTO!";
            return new ErrorDTO(400, msg);
        }

        if (paymentOrderDTO.getId_buyer() == null || paymentOrderDTO.getId_seller() == null ||
                paymentOrderDTO.getRecipient() == null || paymentOrderDTO.getRecipient().isBlank() ||
                paymentOrderDTO.getRecipient_email() == null || paymentOrderDTO.getRecipient_email().isBlank() ||
                paymentOrderDTO.getStore() == null || paymentOrderDTO.getStore().isBlank()) {
            log.info("Validation fail: PaymentOrderDTO mora imeti podana polja: id_buyer, id_seller, recipient, recipient_mail, store");
            String msg = "Polji id_buyer, id_seller, store, recipient_mail, recipient morata biti podana!";
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

    @POST
    @Path("/start")
    @Operation(
            summary="Izvedi začetek plačila",
            description="Izvede začetek plačila v odgovoru poda redirect url za izvedbo plačila.<br>" +
                    "V primeru napake vrne objekt ErrorDTO z opisom napake."
    )
    @APIResponses({
            @APIResponse(
                    responseCode="201",
                    description="(CREATED) Uspešno izveden začetek plačila.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentOrderDTO.class)
                    )),
            @APIResponse(
                    responseCode="400",
                    description="(BAD REQUEST) Podana nepravilna oblika vhodnega objekta PaymentOrderDTO.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(responseCode="401",
                    description="(UNAUTHORIZED) Težava pri avtorizaciji za PayPal.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(responseCode="422",
                    description="(UNPROCESSABLE CONTENT) Podanega zahtevka za PayPal ni bilo mogoče procesirati.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(responseCode="503",
                    description="(SERVICE UNAVALIABLE) Težava pri komunikaciji z drugo mikrostoritvijo ali zunanjim API.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    ))
    })
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
    @Operation(
            summary="Preveri status plačila",
            description="Preveri status plačila in pošlje račun na e-poštni naslov kupca.<br>" +
                    "V primeru napake vrne objekt ErrorDTO z opisom napake."
    )
    @APIResponses({
            @APIResponse(
                    responseCode="201",
                    description="(CREATED) Uspešno izvedeno preverjanje plačila.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentOrderDTO.class)
                    )),
            @APIResponse(
                    responseCode="400",
                    description="(BAD REQUEST) Podana nepravilna oblika vhodnega objekta PaymentOrderDTO.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(responseCode="403",
                    description="(FORBIDDEN) Težava pri avtorizaciji za PayPal.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(responseCode="422",
                    description="(UNPROCESSABLE CONTENT) Podanega zahtevka za PayPal ni bilo mogoče procesirati.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    )),
            @APIResponse(responseCode="503",
                    description="(SERVICE UNAVALIABLE) Težava pri komunikaciji z drugo mikrostoritvijo ali zunanjim API.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDTO.class)
                    ))
    })
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
