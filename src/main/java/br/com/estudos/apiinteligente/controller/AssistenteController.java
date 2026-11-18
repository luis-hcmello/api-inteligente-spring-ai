package br.com.estudos.apiinteligente.controller;

import br.com.estudos.apiinteligente.dto.ChatRequest;
import br.com.estudos.apiinteligente.dto.ChatResponse;
import br.com.estudos.apiinteligente.service.AssistenteFinanceiroService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistenteController {

    private final AssistenteFinanceiroService assistente;

    public AssistenteController(AssistenteFinanceiroService assistente) {
        this.assistente = assistente;
    }

    @PostMapping("/chat")
    public ChatResponse conversar(@Valid @RequestBody ChatRequest request) {
        return new ChatResponse(assistente.conversar(request.message()));
    }
}
