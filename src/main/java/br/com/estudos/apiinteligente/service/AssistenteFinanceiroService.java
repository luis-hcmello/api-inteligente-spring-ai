package br.com.estudos.apiinteligente.service;

import br.com.estudos.apiinteligente.tool.FerramentasOrcamento;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AssistenteFinanceiroService {

    private final ChatClient chatClient;

    public AssistenteFinanceiroService(ChatClient.Builder builder,
                                       FerramentasOrcamento ferramentasOrcamento) {
        this.chatClient = builder
                .defaultSystem("""
                        Você é um assistente financeiro pessoal que conversa em português do Brasil.
                        Seja claro, educado e objetivo. Ajude a pessoa a entender receitas, despesas e saldo.
                        Use as ferramentas disponíveis para consultar ou registrar dados reais do orçamento.
                        Só registre uma transação quando a pessoa pedir explicitamente. Nunca invente valores,
                        datas ou operações. Se faltarem descrição, valor, tipo ou categoria, pergunte antes.
                        Valores são em reais (BRL). Ao apresentar cálculos, use os resultados retornados pelas
                        ferramentas. Não diga que salvou algo antes de a ferramenta confirmar.
                        Este assistente oferece organização financeira e não substitui aconselhamento profissional.
                        """)
                .defaultTools(ferramentasOrcamento)
                .build();
    }

    public String conversar(String mensagem) {
        if (!StringUtils.hasText(mensagem)) {
            throw new IllegalArgumentException("A mensagem não pode ficar vazia.");
        }
        return chatClient.prompt()
                .user(mensagem.trim())
                .call()
                .content();
    }
}
