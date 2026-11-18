# API Inteligente de Orçamento — Spring Boot + Spring AI

Projeto de estudo que integra uma aplicação de orçamento financeiro a um assistente de IA. A pessoa pode consultar receitas/despesas por texto ou voz; o assistente pode acionar ferramentas Java para consultar o PostgreSQL e registrar uma transação quando houver um pedido explícito.

## Tecnologias

- Java 25
- Spring Boot 4.1.2
- Spring AI 2.0.2
- Spring MVC (endpoints REST)
- Spring Data JPA + PostgreSQL
- OpenAI Chat, Audio Transcription e Text-to-Speech
- Maven
- Docker Compose

As versões seguem a matriz atual de compatibilidade: Spring AI 2.0.x funciona com Spring Boot 4.0.x e 4.1.x; Spring Boot 4.1.2 aceita Java até 27. Consulte a documentação oficial antes de atualizar versões.

## Arquitetura

```text
src/main/java/br/com/estudos/apiinteligente
├── config/                 # reservado para configurações adicionais
├── controller/             # endpoints REST
├── dto/                    # contratos de entrada e saída
├── exception/               # tratamento centralizado de erros
├── infra/
│   ├── audit/               # log de método, rota, status e duração (sem payload)
│   └── persistence/         # entidade, enum e repositório JPA
├── service/                 # casos de uso, orquestração e áudio
└── tool/                    # funções reais expostas ao modelo via @Tool
```

### Fluxo de texto

1. `POST /api/v1/assistant/chat` recebe uma mensagem.
2. `AssistenteFinanceiroService` usa `ChatClient`.
3. O modelo escolhe, quando necessário, uma ferramenta de `FerramentasOrcamento`.
4. A ferramenta chama `GerenciarTransacoesUseCase`.
5. O caso de uso persiste ou consulta transações via `TransacaoRepository`.
6. O resultado real volta ao modelo, que formula a resposta.

### Fluxo de voz

`POST /api/v1/assistant/voice` recebe áudio multipart, chama a Transcription API, envia o texto ao mesmo assistente, sintetiza a resposta com Text-to-Speech e devolve MP3. A transcrição é incluída no cabeçalho `X-Transcription-Base64`; a resposta é reproduzida no próprio MP3.

## 1. Pré-requisitos

- JDK 25 instalado e configurado no IntelliJ.
- Maven 3.6.3 ou superior (o ambiente de estudo já usa Maven 3.10.0).
- Docker Desktop em execução.
- Uma chave de API da OpenAI para usar o chat, a transcrição e a síntese de voz.

A chave da API é configurada separadamente da aplicação ChatGPT. Não coloque a chave diretamente no código, no `application.yml` ou no Git.

## 2. Configuração no Windows (PowerShell)

Abra o terminal na pasta raiz do projeto.

### Iniciar o PostgreSQL

```powershell
docker compose up -d
```

O banco ficará disponível em `localhost:5432` com os valores padrão do `docker-compose.yml`.

### Configurar a chave da OpenAI

```powershell
$env:OPENAI_API_KEY="sua-chave-da-api"
```

Opcionalmente, escolha outros modelos:

```powershell
$env:OPENAI_CHAT_MODEL="gpt-5-mini"
$env:OPENAI_TRANSCRIPTION_MODEL="whisper-1"
$env:OPENAI_TTS_MODEL="gpt-4o-mini-tts"
$env:OPENAI_TTS_VOICE="nova"
```

Essas variáveis duram apenas naquela janela do PowerShell. No IntelliJ, configure-as em **Run > Edit Configurations > Environment variables**.

O arquivo `.env.example` documenta as variáveis disponíveis. Ele não é carregado automaticamente pelo IntelliJ; configure as variáveis na sessão ou na configuração de execução.

### Executar a API

```powershell
mvn clean test
mvn spring-boot:run
```

Ou gere o JAR:

```powershell
mvn clean package
java -jar target/api-inteligente-spring-ai-0.0.1-SNAPSHOT.jar
```

A API inicia em `http://localhost:8080`. O PostgreSQL precisa estar ativo. Os recursos de IA precisam de uma chave válida e de acesso habilitado à API.

## 3. Endpoints

| Método | Endpoint | Função |
|---|---|---|
| `POST` | `/api/v1/transactions` | Registrar receita ou despesa |
| `GET` | `/api/v1/transactions` | Listar transações |
| `GET` | `/api/v1/transactions/summary?month=2026-11` | Consultar resumo mensal |
| `DELETE` | `/api/v1/transactions/{id}` | Excluir transação |
| `POST` | `/api/v1/assistant/chat` | Conversar com o assistente e permitir Tool Calling |
| `POST` | `/api/v1/audio/transcriptions` | Converter arquivo de áudio em texto |
| `POST` | `/api/v1/audio/speech` | Converter texto em MP3 |
| `POST` | `/api/v1/assistant/voice` | Áudio → texto → assistente/ferramentas → áudio |
| `GET` | `/actuator/health` | Verificar a saúde da aplicação |

### Registrar uma transação

```json
{
  "descricao": "Compra no mercado",
  "valor": 125.90,
  "tipo": "DESPESA",
  "categoria": "Alimentação",
  "data": "2026-11-18"
}
```

`data` é opcional; quando omitida, o sistema usa a data atual. `valor` deve ser positivo.

### Conversar com o assistente

`POST /api/v1/assistant/chat`

```json
{
  "message": "Registre uma despesa de 42,50 reais na categoria Transporte, descrição corrida de aplicativo, hoje."
}
```

Exemplos de consulta:

```json
{
  "message": "Qual foi meu saldo de novembro de 2026?"
}
```

```json
{
  "message": "Liste minhas despesas de novembro de 2026."
}
```

O modelo pode optar por chamar `consultar_resumo_mensal`, `listar_transacoes` ou `registrar_transacao`. O código Java valida e persiste a operação; o modelo não altera o banco diretamente.

### Transcrever um arquivo de áudio

Envie `multipart/form-data` com um campo chamado `file`. Formatos aceitos pelo endpoint: MP3, MP4, M4A, WAV, WEBM e OGG (limite de 25 MB).

Exemplo no PowerShell, usando o `curl.exe` do Windows:

```powershell
curl.exe -X POST http://localhost:8080/api/v1/audio/transcriptions `
  -F "file=@C:\\audios\\comando.mp3"
```

### Gerar áudio a partir de texto

`POST /api/v1/audio/speech`, com corpo JSON:

```json
{
  "text": "Seu saldo do mês está positivo."
}
```

A resposta é um arquivo de áudio `audio/mpeg`. Para salvar usando curl:

```powershell
curl.exe -X POST http://localhost:8080/api/v1/audio/speech `
  -H "Content-Type: application/json" `
  -d '{"text":"Seu saldo do mês está positivo."}' `
  --output resposta.mp3
```

### Conversar por voz

```powershell
curl.exe -X POST http://localhost:8080/api/v1/assistant/voice `
  -F "file=@C:\\audios\\comando.mp3" `
  -D cabecalhos.txt `
  --output resposta-assistente.mp3
```

A transcrição é enviada em Base64 UTF-8 no cabeçalho `X-Transcription-Base64`. Para decodificá-la no PowerShell:

```powershell
$headers = Get-Content .\cabecalhos.txt -Raw
# Copie o valor depois de X-Transcription-Base64:
[Text.Encoding]::UTF8.GetString([Convert]::FromBase64String("VALOR_BASE64"))
```

## 4. Testes rápidos

O teste unitário de `GerenciarTransacoesUseCase` verifica o cálculo de receitas, despesas e saldo mensal sem depender de uma instância real do PostgreSQL.

```powershell
mvn test
```

## 5. Decisões e limites desta versão

- O Hibernate usa `ddl-auto: update` para facilitar o estudo. Em produção, prefira migrações versionadas com Flyway ou Liquibase.
- O filtro de auditoria registra método HTTP, rota, status e duração, sem gravar corpo da requisição, áudio, prompts ou dados financeiros.
- Não há autenticação/autorização nesta primeira versão. **Não exponha esta API publicamente nem a utilize com dados financeiros reais antes de implementar autenticação, autorização, proteção contra abuso, limites de requisição e políticas de retenção.**
- As ferramentas do assistente só devem ser disponibilizadas a usuários autorizados quando a aplicação evoluir para múltiplas contas. A versão atual é um projeto local de estudo.
- Chamadas ao modelo de chat, transcrição e síntese de voz podem gerar custos na conta do provedor.
- O endpoint de voz devolve MP3 e os textos em cabeçalhos Base64. Em uma evolução, pode-se trocar isso por uma resposta JSON com URL temporária de áudio ou por streaming.

## Próximas evoluções sugeridas

1. Adicionar usuários, autenticação e segregação de transações por pessoa.
2. Adicionar Flyway e versionar o esquema do banco.
3. Registrar auditoria de operações de negócio com política explícita de retenção e minimização de dados.
4. Adicionar testes de integração com Testcontainers.
5. Implementar histórico de conversa por usuário e streaming de respostas.
6. Criar documentação OpenAPI e uma interface web/mobile para gravar áudio.
