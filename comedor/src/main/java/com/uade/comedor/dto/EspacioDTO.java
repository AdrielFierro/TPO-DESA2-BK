package com.uade.comedor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EspacioDTO {
    
    @JsonProperty("id_espacio")
    private String idEspacio;
    
    @JsonProperty("nombre")
    private String nombre;
    
    @JsonProperty("tipo")
    private String tipo;
    
    @JsonProperty("capacidad")
    private Integer capacidad;
    
    @JsonProperty("ubicacion")
    private String ubicacion;
    
    @JsonProperty("estado")
    private String estado;
    
    @JsonProperty("id_sede")
    private String idSede;
    
    @JsonProperty("status")
    private Boolean status;
}
