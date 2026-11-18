package br.com.estudos.apiinteligente.controller;

import br.com.estudos.apiinteligente.dto.SpeechRequest;
import br.com.estudos.apiinteligente.service.AudioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/audio")
public class AudioController {

    private static final long MAX_AUDIO_BYTES = 25L * 1024 * 1024;
    private final AudioService audioService;

    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    @PostMapping(value = "/transcriptions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TranscriptionResponse transcrever(@RequestPart("file") MultipartFile file) throws IOException {
        validarArquivo(file);
        String texto = audioService.transcrever(file.getBytes(), file.getOriginalFilename());
        return new TranscriptionResponse(texto);
    }

    @PostMapping(value = "/speech", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "audio/mpeg")
    public ResponseEntity<byte[]> sintetizar(@Valid @RequestBody SpeechRequest request) {
        byte[] audio = audioService.sintetizar(request.text());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=resposta.mp3")
                .body(audio);
    }

    private void validarArquivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Envie um arquivo de áudio no campo 'file'.");
        }
        if (file.getSize() > MAX_AUDIO_BYTES) {
            throw new IllegalArgumentException("O áudio deve ter no máximo 25 MB.");
        }
        String nome = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        boolean extensaoAceita = nome.matches(".*\\.(mp3|mp4|mpeg|mpga|m4a|wav|webm|ogg)$");
        if (!extensaoAceita) {
            throw new IllegalArgumentException("Formato não reconhecido. Envie MP3, MP4, M4A, WAV, WEBM ou OGG.");
        }
    }

    public record TranscriptionResponse(String transcription) {
    }
}
