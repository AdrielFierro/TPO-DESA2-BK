package com.uade.comedor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SedeDTO {
    
    @JsonProperty("id_sede")
    private String idSede;
    
    @JsonProperty("nombre")
    private String nombre;
    
    @JsonProperty("ubicacion")
    private String ubicacion;
    
    @JsonProperty("status")
    private Boolean status;
}
