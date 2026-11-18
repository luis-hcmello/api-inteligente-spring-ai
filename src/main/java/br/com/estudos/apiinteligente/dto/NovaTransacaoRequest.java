package br.com.estudos.apiinteligente.dto;

import br.com.estudos.apiinteligente.infra.persistence.TipoTransacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NovaTransacaoRequest(
        @NotBlank @Size(max = 120) String descricao,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @NotNull TipoTransacao tipo,
        @NotBlank @Size(max = 60) String categoria,
        LocalDate data
) {
}
