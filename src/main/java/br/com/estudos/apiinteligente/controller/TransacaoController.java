package br.com.estudos.apiinteligente.controller;

import br.com.estudos.apiinteligente.dto.NovaTransacaoRequest;
import br.com.estudos.apiinteligente.dto.ResumoMensalResponse;
import br.com.estudos.apiinteligente.dto.TransacaoResponse;
import br.com.estudos.apiinteligente.service.GerenciarTransacoesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransacaoController {

    private final GerenciarTransacoesUseCase useCase;

    public TransacaoController(GerenciarTransacoesUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<TransacaoResponse> criar(@Valid @RequestBody NovaTransacaoRequest request) {
        TransacaoResponse criada = useCase.registrar(request);
        return ResponseEntity.created(URI.create("/api/v1/transactions/" + criada.id())).body(criada);
    }

    @GetMapping
    public List<TransacaoResponse> listar() {
        return useCase.listar();
    }

    @GetMapping("/summary")
    public ResumoMensalResponse resumo(@RequestParam String month) {
        try {
            return useCase.resumir(YearMonth.parse(month));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("O parâmetro month deve estar no formato AAAA-MM.");
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        useCase.excluir(id);
    }
}
