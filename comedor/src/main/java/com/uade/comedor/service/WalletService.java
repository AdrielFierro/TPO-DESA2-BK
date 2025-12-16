package com.uade.comedor.service;

import com.uade.comedor.dto.WalletTransferRequest;
import com.uade.comedor.dto.WalletTransferResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;

/**
 * Servicio para integrar con la API de Wallet
 */
@Service
public class WalletService {

    private final WebClient webClient;

    @Value("${wallet.api.url}")
    private String walletApiUrl;

    @Value("${wallet.system.account}")
    private String systemAccount;

    public WalletService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Realiza una transferencia desde la wallet del usuario hacia el sistema
     * @param userWalletId UUID de la wallet del usuario
     * @param amount Monto a transferir
     * @param description Descripción de la transferencia
     * @param jwtToken Token JWT para autenticación con la wallet API
     * @return Respuesta de la API de Wallet
     * @throws RuntimeException si la transferencia falla
     */
    public WalletTransferResponse chargeUser(String userWalletId, BigDecimal amount, String description, String jwtToken) {
        if (userWalletId == null || userWalletId.trim().isEmpty()) {
            throw new RuntimeException("El usuario no tiene una wallet asociada");
        }

    // Transferencia desde el usuario hacia el sistema (pago)
    WalletTransferRequest request = new WalletTransferRequest(
        userWalletId,      // from: user
        systemAccount,     // to: system
        "ARG",
        amount,
        "RESERVA",
        description
    );

        try {
            WalletTransferResponse response = webClient
                    .post()
                    .uri(walletApiUrl + "/api/transfers")
                    .header("Authorization", "Bearer " + jwtToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(WalletTransferResponse.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("No se recibió respuesta de la API de Wallet");
            }

            return response;
        } catch (WebClientResponseException e) {
            String errorMessage = String.format(
                    "Error al realizar el cobro en la wallet. Status: %d, Response: %s",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con la API de Wallet: " + e.getMessage(), e);
        }
    }

    /**
     * Realiza un cobro para una reserva
     */
    public WalletTransferResponse chargeReservation(String userWalletId, BigDecimal amount, Long reservationId, String jwtToken) {
        String description = reservationId != null
                ? String.format("Pago de reserva #%d", reservationId)
                : "Pago de reserva";
        return transferFromUserToSystem(userWalletId, amount, "RESERVA", description, jwtToken);
    }

    /**
     * Realiza un cobro para un pedido de comida
     */
    public WalletTransferResponse chargeOrder(String userWalletId, BigDecimal amount, Long billId, String jwtToken) {
        String description = billId != null
                ? String.format("Pago de pedido #%d", billId)
                : "Pago de pedido";
        // Transferencia para compra de comida en cajero
        WalletTransferRequest request = new WalletTransferRequest(
                userWalletId,
                systemAccount,
                "ARG",
                amount,
                "COMPRA",
                description
        );

        try {
            WalletTransferResponse response = webClient
                    .post()
                    .uri(walletApiUrl + "/api/transfers")
                    .header("Authorization", "Bearer " + jwtToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(WalletTransferResponse.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("No se recibió respuesta de la API de Wallet");
            }

            return response;
        } catch (WebClientResponseException e) {
            String errorMessage = String.format(
                    "Error al realizar el cobro en la wallet. Status: %d, Response: %s",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con la API de Wallet: " + e.getMessage(), e);
        }
    }

    /**
     * Realiza una transferencia desde la wallet del usuario hacia el sistema (pago de reserva)
     */
    public WalletTransferResponse transferFromUserToSystem(String userWalletId, BigDecimal amount, String type, String description, String jwtToken) {
        if (userWalletId == null || userWalletId.trim().isEmpty()) {
            throw new RuntimeException("El usuario no tiene una wallet asociada");
        }

        WalletTransferRequest request = new WalletTransferRequest(
                userWalletId,
                systemAccount,
                "ARG",
                amount,
                type,
                description
        );

        try {
            WalletTransferResponse response = webClient
                    .post()
                    .uri(walletApiUrl + "/api/transfers")
                    .header("Authorization", "Bearer " + jwtToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(WalletTransferResponse.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("No se recibió respuesta de la API de Wallet");
            }

            return response;
        } catch (WebClientResponseException e) {
            String errorMessage = String.format(
                    "Error al realizar la transferencia en la wallet. Status: %d, Response: %s",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con la API de Wallet: " + e.getMessage(), e);
        }
    }

    /**
     * Realiza una transferencia desde el sistema hacia la wallet del usuario (reembolso)
     */
    public WalletTransferResponse transferFromSystemToUser(String userWalletId, BigDecimal amount, String type, String description, String jwtToken) {
        if (userWalletId == null || userWalletId.trim().isEmpty()) {
            throw new RuntimeException("El usuario no tiene una wallet asociada");
        }

        WalletTransferRequest request = new WalletTransferRequest(
                systemAccount,
                userWalletId,
                "ARG",
                amount,
                type,
                description
        );

        try {
            WalletTransferResponse response = webClient
                    .post()
                    .uri(walletApiUrl + "/api/transfers")
                    .header("Authorization", "Bearer " + jwtToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(WalletTransferResponse.class)
                    .block();

            if (response == null) {
                throw new RuntimeException("No se recibió respuesta de la API de Wallet");
            }

            return response;
        } catch (WebClientResponseException e) {
            String errorMessage = String.format(
                    "Error al realizar la transferencia en la wallet. Status: %d, Response: %s",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con la API de Wallet: " + e.getMessage(), e);
        }
    }

    /**
     * Reembolsa el costo de una reserva: transferencia desde el sistema hacia el usuario
     */
    public WalletTransferResponse refundReservation(String userWalletId, BigDecimal amount, Long reservationId, String jwtToken) {
        String description = reservationId != null
                ? String.format("Devolución por cancelación de reserva #%d", reservationId)
                : "Devolución por cancelación de reserva";
        return transferFromSystemToUser(userWalletId, amount, "CANCELACION_RESERVA", description, jwtToken);
    }
}
