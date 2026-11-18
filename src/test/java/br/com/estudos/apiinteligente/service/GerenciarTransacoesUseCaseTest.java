package br.com.estudos.apiinteligente.service;

import br.com.estudos.apiinteligente.dto.ResumoMensalResponse;
import br.com.estudos.apiinteligente.infra.persistence.TipoTransacao;
import br.com.estudos.apiinteligente.infra.persistence.Transacao;
import br.com.estudos.apiinteligente.infra.persistence.TransacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerenciarTransacoesUseCaseTest {

    @Mock
    private TransacaoRepository repository;

    @InjectMocks
    private GerenciarTransacoesUseCase useCase;

    @Test
    void calculaReceitasDespesasESaldoDoMes() {
        YearMonth mes = YearMonth.of(2026, 11);
        when(repository.findByDataBetweenOrderByDataDesc(mes.atDay(1), mes.atEndOfMonth()))
                .thenReturn(List.of(
                        new Transacao("Salário", new BigDecimal("5000.00"), TipoTransacao.RECEITA,
                                "Salário", LocalDate.of(2026, 11, 5)),
                        new Transacao("Mercado", new BigDecimal("650.50"), TipoTransacao.DESPESA,
                                "Alimentação", LocalDate.of(2026, 11, 8)),
                        new Transacao("Freelance", new BigDecimal("800.00"), TipoTransacao.RECEITA,
                                "Extra", LocalDate.of(2026, 11, 10))
                ));

        ResumoMensalResponse resumo = useCase.resumir(mes);

        assertEquals(new BigDecimal("5800.00"), resumo.totalReceitas());
        assertEquals(new BigDecimal("650.50"), resumo.totalDespesas());
        assertEquals(new BigDecimal("5149.50"), resumo.saldo());
    }
}
