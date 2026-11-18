package br.com.estudos.apiinteligente.dto;

import br.com.estudos.apiinteligente.infra.persistence.TipoTransacao;
import br.com.estudos.apiinteligente.infra.persistence.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        TipoTransacao tipo,
        String categoria,
        LocalDate data
) {
    public static TransacaoResponse from(Transacao transacao) {
        return new TransacaoResponse(
                transacao.getId(), transacao.getDescricao(), transacao.getValor(),
                transacao.getTipo(), transacao.getCategoria(), transacao.getData()
        );
    }
}
