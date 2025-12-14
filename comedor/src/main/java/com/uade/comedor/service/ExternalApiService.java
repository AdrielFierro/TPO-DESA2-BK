package com.uade.comedor.service;

import com.uade.comedor.dto.ParametroDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiService {

  private final WebClient webClient;
  
  @Value("${external.api.backoffice.url}")
  private String backofficeUrl;
  
  @Value("${external.api.backoffice.parametro.coste-reserva}")
  private String costeReservaNombre;

  public String getPing(String url) {
    return webClient.get()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class)
        .block(); // si querés modo síncrono
  }

  public String getPing2(String url) {
    return webClient.post()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class)
        .block(); // si querés modo síncrono
  }

  public <T> T postJson(String url, Object body, Class<T> responseType) {
    return webClient.post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(responseType)
        .block();
  }

  /**
   * Obtiene el costo de la reserva desde el módulo de Backoffice.
   * Busca el parámetro "Coste Reserva" de tipo "reserva" en el endpoint externo.
   * Si no se encuentra o hay error, lanza una excepción.
   * 
   * @return BigDecimal con el costo de la reserva
   * @throws RuntimeException si no se puede recuperar el costo
   */
  public BigDecimal getReservationCost() {
    try {
      String fullUrl = backofficeUrl + "/?skip=0&limit=100";
      log.info("Obteniendo costo de reserva desde: {}", fullUrl);
      
      // Crear WebClient específico para el backoffice
      WebClient backofficeClient = WebClient.builder().build();
      
      // Llamar al endpoint directamente con la URL completa
      List<ParametroDTO> parametros = backofficeClient.get()
          .uri(fullUrl)
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<List<ParametroDTO>>() {})
          .block();
      
      if (parametros == null || parametros.isEmpty()) {
        log.error("No se pudieron obtener parámetros del backoffice");
        throw new RuntimeException("No se pudo recuperar el costo de la reserva: respuesta vacía del servicio");
      }
      
      log.info("Se obtuvieron {} parámetros del backoffice", parametros.size());
      
      // Buscar el parámetro "Coste Reserva"
      ParametroDTO costeReserva = parametros.stream()
          .filter(p -> costeReservaNombre.equals(p.getNombre()))
          .filter(p -> "reserva".equals(p.getTipo()))
          .filter(p -> Boolean.TRUE.equals(p.getStatus()))
          .findFirst()
          .orElse(null);
      
      if (costeReserva == null) {
        log.error("No se encontró el parámetro '{}' de tipo 'reserva' activo", costeReservaNombre);
        throw new RuntimeException("No se pudo recuperar el costo de la reserva: parámetro 'Coste Reserva' no encontrado");
      }
      
      BigDecimal costo = costeReserva.getValorNumericoAsBigDecimal();
      log.info("Costo de reserva obtenido exitosamente: ${}", costo);
      return costo;
      
    } catch (RuntimeException e) {
      // Re-lanzar las excepciones de negocio
      throw e;
    } catch (Exception e) {
      log.error("Error al conectar con el servicio de backoffice: {}", e.getMessage());
      throw new RuntimeException("No se pudo recuperar el costo de la reserva: error de conexión con el servicio externo", e);
    }
  }
}
