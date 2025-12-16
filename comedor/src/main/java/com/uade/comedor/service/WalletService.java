package com.uade.comedor.service;

import com.uade.comedor.dto.WalletApiResponse;
import com.uade.comedor.dto.WalletDTO;
import com.uade.comedor.dto.WalletTransferRequest;
import com.uade.comedor.dto.WalletTransferResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.List;

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
        System.out.println("📝 [WalletService.chargeOrder] Preparando transferencia para pedido");
        System.out.println("   From (User Wallet): " + userWalletId);
        System.out.println("   To (System): " + systemAccount);
        System.out.println("   Amount: " + amount);
        System.out.println("   Bill ID: " + billId);
        
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
        
        System.out.println("   Type: COMPRA");
        System.out.println("   Description: " + description);

        try {
            System.out.println("🌐 [WalletService.chargeOrder] Enviando request a Wallet API: " + walletApiUrl + "/api/transfers");
            
            WalletTransferResponse response = webClient
                    .post()
                    .uri(walletApiUrl + "/api/transfers")
                    .header("Authorization", "Bearer " + jwtToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(WalletTransferResponse.class)
                    .block();

            if (response == null) {
                System.err.println("❌ [WalletService.chargeOrder] No se recibió respuesta de la API de Wallet");
                throw new RuntimeException("No se recibió respuesta de la API de Wallet");
            }

            System.out.println("✅ [WalletService.chargeOrder] Transferencia completada exitosamente");
            System.out.println("   Transfer ID: " + (response.getId() != null ? response.getId() : "N/A"));
            System.out.println("   Status: " + (response.getStatus() != null ? response.getStatus() : "N/A"));
            
            return response;
        } catch (WebClientResponseException e) {
            System.err.println("❌ [WalletService.chargeOrder] Error HTTP en Wallet API");
            System.err.println("   Status Code: " + e.getStatusCode().value());
            System.err.println("   Response Body: " + e.getResponseBodyAsString());
            
            String errorMessage = String.format(
                    "Error al realizar el cobro en la wallet. Status: %d, Response: %s",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            System.err.println("❌ [WalletService.chargeOrder] Error general al conectar con Wallet API: " + e.getMessage());
            e.printStackTrace();
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

    /**
     * Obtiene el walletId de un usuario a partir de su userId
     * @param userId UUID del usuario
     * @return walletId (UUID de la wallet)
     * @throws RuntimeException si no se encuentra la wallet o hay error en la comunicación
     */
    public String getWalletIdByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new RuntimeException("El userId no puede estar vacío");
        }

        String walletsByUserUrl = "https://jtseq9puk0.execute-api.us-east-1.amazonaws.com/api/wallets/" + userId + "/user";

        System.out.println("🔍 [WalletService.getWalletIdByUserId] Buscando wallet para userId: " + userId);
        System.out.println("   URL: " + walletsByUserUrl);

        try {
            WalletApiResponse response = webClient
                    .get()
                    .uri(walletsByUserUrl)
                    .retrieve()
                    .bodyToMono(WalletApiResponse.class)
                    .block();

            if (response == null || !response.isSuccess() || response.getData() == null || response.getData().isEmpty()) {
                System.err.println("❌ [WalletService.getWalletIdByUserId] No se encontró wallet para userId: " + userId);
                throw new RuntimeException("No se encontró una wallet activa para el usuario: " + userId);
            }

            // Obtener la primera wallet (asumiendo que un usuario tiene una sola wallet)
            WalletDTO wallet = response.getData().get(0);
            
            if (wallet.getUuid() == null || wallet.getUuid().trim().isEmpty()) {
                System.err.println("❌ [WalletService.getWalletIdByUserId] Wallet sin UUID válido para userId: " + userId);
                throw new RuntimeException("La wallet del usuario no tiene un UUID válido");
            }

            System.out.println("✅ [WalletService.getWalletIdByUserId] Wallet encontrada");
            System.out.println("   WalletId: " + wallet.getUuid());
            System.out.println("   Balance: " + wallet.getBalance());
            System.out.println("   Currency: " + wallet.getCurrency());

            return wallet.getUuid();
        } catch (WebClientResponseException e) {
            System.err.println("❌ [WalletService.getWalletIdByUserId] Error HTTP al buscar wallet");
            System.err.println("   Status Code: " + e.getStatusCode().value());
            System.err.println("   Response: " + e.getResponseBodyAsString());
            
            String errorMessage = String.format(
                    "Error al consultar la wallet del usuario. Status: %d, Response: %s",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            System.err.println("❌ [WalletService.getWalletIdByUserId] Error general: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al conectar con la API de Wallet: " + e.getMessage(), e);
        }
    }
}
