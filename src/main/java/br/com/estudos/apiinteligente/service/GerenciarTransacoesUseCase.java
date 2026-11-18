package br.com.estudos.apiinteligente.service;

import br.com.estudos.apiinteligente.dto.NovaTransacaoRequest;
import br.com.estudos.apiinteligente.dto.ResumoMensalResponse;
import br.com.estudos.apiinteligente.dto.TransacaoResponse;
import br.com.estudos.apiinteligente.infra.persistence.TipoTransacao;
import br.com.estudos.apiinteligente.infra.persistence.Transacao;
import br.com.estudos.apiinteligente.infra.persistence.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class GerenciarTransacoesUseCase {

    private final TransacaoRepository repository;

    public GerenciarTransacoesUseCase(TransacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TransacaoResponse registrar(NovaTransacaoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Os dados da transação são obrigatórios.");
        }
        if (request.descricao() == null || request.descricao().isBlank()) {
            throw new IllegalArgumentException("A descrição é obrigatória.");
        }
        if (request.valor() == null || request.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor precisa ser maior que zero.");
        }
        if (request.tipo() == null) {
            throw new IllegalArgumentException("O tipo precisa ser RECEITA ou DESPESA.");
        }
        if (request.categoria() == null || request.categoria().isBlank()) {
            throw new IllegalArgumentException("A categoria é obrigatória.");
        }
        LocalDate data = request.data() == null ? LocalDate.now() : request.data();
        Transacao transacao = new Transacao(
                request.descricao().trim(), request.valor(), request.tipo(),
                request.categoria().trim(), data
        );
        return TransacaoResponse.from(repository.save(transacao));
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> listar() {
        return repository.findAllByOrderByDataDescIdDesc().stream()
                .map(TransacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> listar(YearMonth mes) {
        return repository.findByDataBetweenOrderByDataDesc(
                        mes.atDay(1), mes.atEndOfMonth())
                .stream()
                .map(TransacaoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumoMensalResponse resumir(YearMonth mes) {
        List<Transacao> transacoes = repository.findByDataBetweenOrderByDataDesc(
                mes.atDay(1), mes.atEndOfMonth());

        BigDecimal receitas = somar(transacoes, TipoTransacao.RECEITA);
        BigDecimal despesas = somar(transacoes, TipoTransacao.DESPESA);

        return new ResumoMensalResponse(mes, receitas, despesas, receitas.subtract(despesas));
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Transação não encontrada: " + id);
        }
        repository.deleteById(id);
    }

    private BigDecimal somar(List<Transacao> transacoes, TipoTransacao tipo) {
        return transacoes.stream()
                .filter(transacao -> transacao.getTipo() == tipo)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
