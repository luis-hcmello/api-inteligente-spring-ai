package br.com.estudos.apiinteligente.controller;

import br.com.estudos.apiinteligente.service.AssistenteFinanceiroService;
import br.com.estudos.apiinteligente.service.AudioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/assistant")
public class VoiceAssistantController {

    private final AudioService audioService;
    private final AssistenteFinanceiroService assistente;

    public VoiceAssistantController(AudioService audioService,
                                    AssistenteFinanceiroService assistente) {
        this.audioService = audioService;
        this.assistente = assistente;
    }

    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = "audio/mpeg")
    public ResponseEntity<byte[]> conversarPorVoz(@RequestPart("file") MultipartFile file)
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Envie um arquivo de áudio no campo 'file'.");
        }
        if (file.getSize() > 25L * 1024 * 1024) {
            throw new IllegalArgumentException("O áudio deve ter no máximo 25 MB.");
        }
        String nomeArquivo = file.getOriginalFilename() == null
                ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!nomeArquivo.matches(".*\\.(mp3|mp4|mpeg|mpga|m4a|wav|webm|ogg)$")) {
            throw new IllegalArgumentException("Formato não reconhecido. Envie MP3, MP4, M4A, WAV, WEBM ou OGG.");
        }

        String transcricao = audioService.transcrever(file.getBytes(), file.getOriginalFilename());
        String resposta = assistente.conversar(transcricao);
        byte[] respostaEmAudio = audioService.sintetizar(resposta);

        String transcricaoBase64 = Base64.getEncoder().encodeToString(
                transcricao.getBytes(StandardCharsets.UTF_8));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header("X-Transcription-Base64", transcricaoBase64)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=resposta-assistente.mp3")
                .body(respostaEmAudio);
    }
}
