package br.com.estudos.apiinteligente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpeechRequest(
        @NotBlank @Size(max = 4000) String text
) {
}
