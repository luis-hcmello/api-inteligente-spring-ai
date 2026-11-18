package br.com.estudos.apiinteligente.service;

import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AudioService {

    private final TranscriptionModel transcriptionModel;
    private final TextToSpeechModel textToSpeechModel;

    public AudioService(TranscriptionModel transcriptionModel,
                        TextToSpeechModel textToSpeechModel) {
        this.transcriptionModel = transcriptionModel;
        this.textToSpeechModel = textToSpeechModel;
    }

    public String transcrever(byte[] audio, String nomeArquivo) {
        if (audio == null || audio.length == 0) {
            throw new IllegalArgumentException("O arquivo de áudio está vazio.");
        }
        ByteArrayResource resource = new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return StringUtils.hasText(nomeArquivo) ? nomeArquivo : "audio.mp3";
            }
        };
        return transcriptionModel.transcribe(resource);
    }

    public byte[] sintetizar(String texto) {
        if (!StringUtils.hasText(texto)) {
            throw new IllegalArgumentException("O texto para sintetizar não pode ficar vazio.");
        }
        return textToSpeechModel.call(texto.trim());
    }
}
