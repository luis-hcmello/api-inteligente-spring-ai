package br.com.estudos.apiinteligente.tool;

import br.com.estudos.apiinteligente.dto.NovaTransacaoRequest;
import br.com.estudos.apiinteligente.dto.ResumoMensalResponse;
import br.com.estudos.apiinteligente.dto.TransacaoResponse;
import br.com.estudos.apiinteligente.infra.persistence.TipoTransacao;
import br.com.estudos.apiinteligente.service.GerenciarTransacoesUseCase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Component
public class FerramentasOrcamento {

    private final GerenciarTransacoesUseCase transacoes;

    public FerramentasOrcamento(GerenciarTransacoesUseCase transacoes) {
        this.transacoes = transacoes;
    }

    @Tool(name = "registrar_transacao", description = "Registra uma receita ou despesa no orçamento financeiro. Use somente quando a pessoa pedir explicitamente para registrar uma operação. Nunca invente valor, descrição, categoria ou data. Se faltar informação essencial, pergunte antes.")
    public String registrarTransacao(
            @ToolParam(description = "Tipo da operação: RECEITA ou DESPESA") String tipo,
            @ToolParam(description = "Descrição curta da operação") String descricao,
            @ToolParam(description = "Valor positivo em reais, usando ponto como separador decimal") BigDecimal valor,
            @ToolParam(description = "Categoria, por exemplo Alimentação, Transporte ou Salário") String categoria,
            @ToolParam(description = "Data no formato AAAA-MM-DD; opcional, use a data atual quando omitida", required = false) String data
    ) {
        try {
            if (tipo == null || descricao == null || descricao.isBlank()
                    || descricao.length() > 120
                    || valor == null || valor.compareTo(BigDecimal.ZERO) <= 0
                    || categoria == null || categoria.isBlank() || categoria.length() > 60) {
                return "Não foi possível registrar: informe tipo, descrição (até 120 caracteres), " +
                        "valor positivo e categoria (até 60 caracteres).";
            }
            TipoTransacao tipoTransacao = TipoTransacao.valueOf(tipo.trim().toUpperCase(Locale.ROOT));
            LocalDate dataTransacao = (data == null || data.isBlank())
                    ? LocalDate.now() : LocalDate.parse(data.trim());
            TransacaoResponse registrada = transacoes.registrar(new NovaTransacaoRequest(
                    descricao, valor, tipoTransacao, categoria, dataTransacao));
            return "Transação registrada com sucesso: " + registrada;
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            return "Não foi possível registrar. Confira o tipo (RECEITA ou DESPESA) e a data (AAAA-MM-DD).";
        }
    }

    @Tool(name = "listar_transacoes", description = "Lista as transações financeiras de um mês. Use o formato AAAA-MM para o mês.")
    public List<TransacaoResponse> listarTransacoes(
            @ToolParam(description = "Mês a consultar no formato AAAA-MM") String mes
    ) {
        try {
            return transacoes.listar(YearMonth.parse(mes));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Informe o mês no formato AAAA-MM.");
        }
    }

    @Tool(name = "consultar_resumo_mensal", description = "Consulta receitas, despesas e saldo de um mês. Não estime valores; use os dados do sistema.")
    public ResumoMensalResponse consultarResumoMensal(
            @ToolParam(description = "Mês a consultar no formato AAAA-MM") String mes
    ) {
        try {
            return transacoes.resumir(YearMonth.parse(mes));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Informe o mês no formato AAAA-MM.");
        }
    }
}
