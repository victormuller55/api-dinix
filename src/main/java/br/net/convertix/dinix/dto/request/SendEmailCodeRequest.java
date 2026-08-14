package br.net.convertix.dinix.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendEmailCodeRequest(
        @NotBlank @Email @Size(max = 180) String email
) {
}
