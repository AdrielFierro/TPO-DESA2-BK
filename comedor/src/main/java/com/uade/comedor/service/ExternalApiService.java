package com.uade.comedor.service;

import com.uade.comedor.dto.ParametroDTO;
import com.uade.comedor.dto.SedeDTO;
import com.uade.comedor.entity.Location;
import com.uade.comedor.repository.LocationRepository;
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

  // Método para obtener el costo de la reserva desde backoffice
  public java.math.BigDecimal getReservationCost(String jwtToken) {
    try {
      // ID del parámetro "Coste Reserva" en el backoffice
      String parametroId = "35814d0d-692c-4b68-b524-068414560b6b";
      String backofficeUrl = "https://backoffice-production-df78.up.railway.app/api/v1/parametros/" + parametroId;
      
      System.out.println("========================================");
      System.out.println("Llamando a backoffice para obtener COSTO_RESERVA");
      System.out.println("URL: " + backofficeUrl);
      System.out.println("Token JWT: " + (jwtToken != null ? jwtToken.substring(0, Math.min(20, jwtToken.length())) + "..." : "null"));
      System.out.println("========================================");
      
      // Llamar al backoffice pasando el token JWT
      ParametroResponseDTO parametro = webClient.get()
          .uri(backofficeUrl)
          .header("Authorization", "Bearer " + jwtToken)
          .retrieve()
          .bodyToMono(ParametroResponseDTO.class)
          .block();
      
      if (parametro == null) {
        System.err.println("✗ El backoffice no retornó datos");
        System.err.println("========================================");
        throw new RuntimeException("No se pudo obtener el costo de la reserva desde el backoffice. Por favor, intente realizar la reserva más tarde.");
      }
      
      System.out.println("Respuesta recibida del backoffice:");
      System.out.println("ID: " + parametro.getId_parametro());
      System.out.println("Nombre: " + parametro.getNombre());
      System.out.println("Tipo: " + parametro.getTipo());
      System.out.println("Valor numérico: " + parametro.getValor_numerico());
      System.out.println("Status: " + parametro.isStatus());
      
      if (!parametro.isStatus()) {
        System.err.println("✗ El parámetro está inactivo (status=false)");
        System.err.println("========================================");
        throw new RuntimeException("El parámetro de costo de reserva está desactivado. Por favor, intente realizar la reserva más tarde.");
      }
      
      if (parametro.getValor_numerico() == null || parametro.getValor_numerico().trim().isEmpty()) {
        System.err.println("✗ El parámetro no tiene valor numérico");
        System.err.println("========================================");
        throw new RuntimeException("El costo de reserva no está configurado correctamente. Por favor, intente realizar la reserva más tarde.");
      }
      
      java.math.BigDecimal costo = new java.math.BigDecimal(parametro.getValor_numerico());
      System.out.println("✓ COSTO_RESERVA obtenido: " + costo);
      System.out.println("========================================");
      return costo;
      
    } catch (RuntimeException e) {
      // Re-lanzar excepciones de tipo RuntimeException (incluyendo las que acabamos de crear)
      throw e;
    } catch (Exception e) {
      // En caso de error de comunicación, lanzar excepción
      System.err.println("========================================");
      System.err.println("✗ ERROR comunicándose con el backoffice:");
      System.err.println("Tipo de error: " + e.getClass().getName());
      System.err.println("Mensaje: " + e.getMessage());
      e.printStackTrace();
      System.err.println("========================================");
      throw new RuntimeException("Error al comunicarse con el sistema de precios. Por favor, intente realizar la reserva más tarde.", e);
    }
  }
  
  // DTO para mapear la respuesta del backoffice
  public static class ParametroResponseDTO {
    private String id_parametro;
    private String nombre;
    private String tipo;
    private String valor_numerico;
    private String valor_texto;
    private boolean status;
    
    public String getId_parametro() {
      return id_parametro;
    }
    
    public void setId_parametro(String id_parametro) {
      this.id_parametro = id_parametro;
    }
    
    public String getNombre() {
      return nombre;
    }
    
    public void setNombre(String nombre) {
      this.nombre = nombre;
    }
    
    public String getTipo() {
      return tipo;
    }
    
    public void setTipo(String tipo) {
      this.tipo = tipo;
    }
    
    public String getValor_numerico() {
      return valor_numerico;
    }
    
    public void setValor_numerico(String valor_numerico) {
      this.valor_numerico = valor_numerico;
    }
    
    public String getValor_texto() {
      return valor_texto;
    }
    
    public void setValor_texto(String valor_texto) {
      this.valor_texto = valor_texto;
    }
    
    public boolean isStatus() {
      return status;
    }
    
    public void setStatus(boolean status) {
      this.status = status;
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

  /**
   * Obtiene las sedes desde el módulo de Backoffice.
   * Lanza excepción si no puede obtener las sedes.
   */
  public List<Location> getLocationsFromBackoffice() {
    try {
      String sedesUrl = "https://backoffice-production-df78.up.railway.app/api/v1/sedes/?skip=0&limit=100";
      log.info("Obteniendo sedes desde: {}", sedesUrl);
      
      WebClient backofficeClient = WebClient.builder().build();
      
      List<SedeDTO> sedes = backofficeClient.get()
          .uri(sedesUrl)
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<List<SedeDTO>>() {})
          .block();
      
      if (sedes == null || sedes.isEmpty()) {
        log.error("No se pudieron obtener sedes del backoffice");
        throw new RuntimeException("No se pudieron obtener las sedes: respuesta vacía del servicio");
      }
      
      log.info("Se obtuvieron {} sedes del backoffice", sedes.size());
      
      // Convertir SedeDTO a Location (solo las activas)
      List<Location> locations = new java.util.ArrayList<>();
      for (SedeDTO sedeDTO : sedes) {
        if (Boolean.TRUE.equals(sedeDTO.getStatus())) {
          Location location = new Location();
          location.setId(sedeDTO.getIdSede());
          location.setName(sedeDTO.getNombre());
          location.setAddress(sedeDTO.getUbicacion());
          location.setCapacity(10); // Capacidad por defecto
          locations.add(location);
        }
      }
      
      if (locations.isEmpty()) {
        log.error("No se encontraron sedes activas en el backoffice");
        throw new RuntimeException("No se pudieron obtener las sedes: no hay sedes activas disponibles");
      }
      
      log.info("Se obtuvieron {} sedes activas", locations.size());
      return locations;
      
    } catch (RuntimeException e) {
      // Re-lanzar las excepciones de negocio
      throw e;
    } catch (Exception e) {
      log.error("Error al conectar con el servicio de backoffice: {}", e.getMessage());
      throw new RuntimeException("No se pudieron obtener las sedes: error de conexión con el servicio externo", e);
    }
  }
}
