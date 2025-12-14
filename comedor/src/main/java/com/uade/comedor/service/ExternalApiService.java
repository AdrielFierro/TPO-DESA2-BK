package com.uade.comedor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ExternalApiService {

  private final WebClient webClient;

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
    }
  }
}
