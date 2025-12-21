package si.nakupify.service.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import jakarta.enterprise.context.ApplicationScoped;
import si.nakupify.proto.*;
import si.nakupify.service.dto.ElementDTO;
import si.nakupify.service.dto.ErrorDTO;
import si.nakupify.service.dto.KosaricaDTO;
import si.nakupify.service.dto.PairDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class KosaricaClient {

    @GrpcClient("kosarica")
    gRPCKosaricaService client;

    private Logger log = Logger.getLogger(KosaricaClient.class.getName());

    public KosaricaDTO toDto(gRPCKosaricaDTO protoKosarica) {
        if (protoKosarica == null) {
            return null;
        }

        List<ElementDTO> elementi = new ArrayList<>();

        for (gRPCElementDTO e : protoKosarica.getKosaricaList()) {
            ElementDTO element = new ElementDTO();
            element.setId_kosarica(e.getIdKosarica() != 0 ? e.getIdKosarica() : null);
            element.setId_izdelek(e.getIdIzdelek() != 0 ? e.getIdIzdelek() : null);
            element.setNaziv(e.getNaziv() != null && !e.getNaziv().isEmpty() ? e.getNaziv() : null);
            element.setCena(e.getCena() != 0 ? e.getCena() : null);
            element.setKolicina(e.getKolicina());
            elementi.add(element);
        }

        return new KosaricaDTO(protoKosarica.getIdUporabnik(), elementi);
    }

    public PairDTO<KosaricaDTO, ErrorDTO> getKosarica(Long id_uporabnik) {
        try {
            gRPCKosaricaDTO kosarica = client.getKosarica(
                    GetKosaricaRequest.newBuilder()
                            .setIdUporabnik(id_uporabnik)
                            .build()
            ).await().indefinitely();

            return new PairDTO<>(toDto(kosarica), null);
        } catch (StatusRuntimeException e) {
            Status.Code code = e.getStatus().getCode();
            ErrorDTO error = new ErrorDTO();

            switch (code) {
                case NOT_FOUND:
                    log.info("gRPC error NOT_FOUND: Določenih podatkov o izdelkih v košarici ni bilo mogoče najti");
                    error.setErrorCode(404);
                    error.setError("Določenih podatkov o izdelkih v košarici ni bilo mogoče najti.");
                    break;
                case UNAVAILABLE:
                    log.info("gRPC error UNAVAILABLE: Napaka pri komunikaciji z microservice-kosarica");
                    error.setErrorCode(503);
                    error.setError("Napaka pri komunikaciji z microservice-kosarica.");
                    break;
                default:
                    log.info("gRPC error: Napaka pri komunikaciji z microservice-kosarica");
                    error.setErrorCode(503);
                    error.setError("Napaka pri komunikaciji z microservice-kosarica.");
                    break;
            }

            return new PairDTO<>(null, error);
        }
    }

    public ErrorDTO deleteKosarica(Long id_uporabnik) {
        try {
            client.deleteKosarica(
                    DeleteKosaricaRequest.newBuilder()
                            .setIdUporabnik(id_uporabnik)
                            .build()
            );

            return null;
        } catch (StatusRuntimeException e) {
            ErrorDTO error = new ErrorDTO();

            log.info("gRPC error: Napaka pri komunikaciji z microservice-kosarica");
            error.setErrorCode(503);
            error.setError("Napaka pri komunikaciji z microservice-kosarica.");

            return error;
        }
    }
}
