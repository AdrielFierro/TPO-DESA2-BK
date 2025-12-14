package com.uade.comedor.service;

import com.uade.comedor.dto.EspacioDTO;
import com.uade.comedor.entity.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
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
    }
  }

  /**
   * Obtiene el costo de la reserva sin autenticacion (para consultas internas)
   * Nota: Este endpoint del backoffice debe ser publico o deberias implementar autenticacion de servicio
   */
  public java.math.BigDecimal getReservationCost() {
    // Por ahora, lanzamos una excepcion indicando que se debe usar la version con token
    throw new RuntimeException("Para obtener el costo de reserva se requiere autenticacion. Use el metodo con token JWT.");
  }

  // Metodo para obtener el costo de la reserva desde backoffice con autenticacion
  public java.math.BigDecimal getReservationCost(String jwtToken) {
    try {
      // ID del parametro "Coste Reserva" en el backoffice
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
        System.err.println("El backoffice no retorno datos");
        System.err.println("========================================");
        throw new RuntimeException("No se pudo obtener el costo de la reserva desde el backoffice. Por favor, intente realizar la reserva mas tarde.");
      }
      
      System.out.println("Respuesta recibida del backoffice:");
      System.out.println("ID: " + parametro.getId_parametro());
      System.out.println("Nombre: " + parametro.getNombre());
      System.out.println("Tipo: " + parametro.getTipo());
      System.out.println("Valor numerico: " + parametro.getValor_numerico());
      System.out.println("Status: " + parametro.isStatus());
      
      if (!parametro.isStatus()) {
        System.err.println("El parametro esta inactivo (status=false)");
        System.err.println("========================================");
        throw new RuntimeException("El parametro de costo de reserva esta desactivado. Por favor, intente realizar la reserva mas tarde.");
      }
      
      if (parametro.getValor_numerico() == null || parametro.getValor_numerico().trim().isEmpty()) {
        System.err.println("El parametro no tiene valor numerico");
        System.err.println("========================================");
        throw new RuntimeException("El costo de reserva no esta configurado correctamente. Por favor, intente realizar la reserva mas tarde.");
      }
      
      java.math.BigDecimal costo = new java.math.BigDecimal(parametro.getValor_numerico());
      System.out.println("COSTO_RESERVA obtenido: " + costo);
      System.out.println("========================================");
      return costo;
      
    } catch (RuntimeException e) {
      // Re-lanzar excepciones de tipo RuntimeException (incluyendo las que acabamos de crear)
      throw e;
    } catch (Exception e) {
      // En caso de error de comunicacion, lanzar excepcion
      System.err.println("========================================");
      System.err.println("ERROR comunicandose con el backoffice:");
      System.err.println("Tipo de error: " + e.getClass().getName());
      System.err.println("Mensaje: " + e.getMessage());
      e.printStackTrace();
      System.err.println("========================================");
      throw new RuntimeException("Error al comunicarse con el sistema de precios. Por favor, intente realizar la reserva mas tarde.", e);
    }
  }

  /**
   * Obtiene los espacios tipo COMEDOR desde el módulo de Backoffice.
   * Lanza excepción si no puede obtener los espacios.
   */
  public List<Location> getLocationsFromBackoffice() {
    try {
      String espaciosUrl = "https://backoffice-production-df78.up.railway.app/api/v1/espacios/?skip=0&limit=100&param=tipo&value=COMEDOR";
      System.out.println("Obteniendo espacios tipo COMEDOR desde: " + espaciosUrl);
      
      WebClient backofficeClient = WebClient.builder().build();
      
      List<EspacioDTO> espacios = backofficeClient.get()
          .uri(espaciosUrl)
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<List<EspacioDTO>>() {})
          .block();
      
      if (espacios == null || espacios.isEmpty()) {
        System.err.println("No se pudieron obtener espacios del backoffice");
        throw new RuntimeException("No se pudieron obtener los espacios: respuesta vacia del servicio");
      }
      
      System.out.println("Se obtuvieron " + espacios.size() + " espacios del backoffice");
      
      // Convertir EspacioDTO a Location (solo los activos y disponibles)
      List<Location> locations = new java.util.ArrayList<>();
      for (EspacioDTO espacio : espacios) {
        if (Boolean.TRUE.equals(espacio.getStatus()) && "DISPONIBLE".equals(espacio.getEstado())) {
          Location location = new Location();
          location.setId(espacio.getIdEspacio());
          location.setName(espacio.getNombre());
          location.setAddress(espacio.getUbicacion());
          
          // Usar la capacidad del espacio, sin fallback
          if (espacio.getCapacidad() == null) {
            System.err.println("Espacio " + espacio.getIdEspacio() + " no tiene capacidad definida");
            throw new RuntimeException("El espacio " + espacio.getNombre() + " no tiene capacidad configurada en el backoffice");
          }
          location.setCapacity(espacio.getCapacidad());
          
          locations.add(location);
        }
      }
      
      if (locations.isEmpty()) {
        System.err.println("No se encontraron espacios activos y disponibles en el backoffice");
        throw new RuntimeException("No se pudieron obtener los espacios: no hay espacios de tipo COMEDOR activos y disponibles");
      }
      
      System.out.println("Se obtuvieron " + locations.size() + " espacios activos y disponibles");
      return locations;
      
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      System.err.println("Error al conectar con el servicio de backoffice: " + e.getMessage());
      throw new RuntimeException("No se pudieron obtener los espacios del backoffice", e);
    }
  }
}
