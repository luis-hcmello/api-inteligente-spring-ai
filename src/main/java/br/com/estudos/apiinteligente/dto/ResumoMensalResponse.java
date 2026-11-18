package br.com.estudos.apiinteligente.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ResumoMensalResponse(
        YearMonth mes,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo
) {
}
