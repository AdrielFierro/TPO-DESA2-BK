package com.uade.comedor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParametroDTO {
    
    @JsonProperty("id_parametro")
    private String idParametro;
    
    @JsonProperty("nombre")
    private String nombre;
    
    @JsonProperty("tipo")
    private String tipo;
    
    @JsonProperty("valor_numerico")
    private String valorNumerico;
    
    @JsonProperty("valor_texto")
    private String valorTexto;
    
    @JsonProperty("status")
    private Boolean status;
    
    /**
     * Convierte el valor_numerico de String a BigDecimal
     */
    public BigDecimal getValorNumericoAsBigDecimal() {
        if (valorNumerico != null && !valorNumerico.isEmpty()) {
            return new BigDecimal(valorNumerico);
        }
        return BigDecimal.ZERO;
    }
}
