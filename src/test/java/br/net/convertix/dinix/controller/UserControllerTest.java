package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.repository.EmailVerificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Test
    void updateProfileChangePasswordEmailAndDelete() throws Exception {
        String prefix = UUID.randomUUID().toString().substring(0, 8);
        String email = "perfil" + prefix + "@dinix.test";
        String token = registrar(email, "Victor Muller", "senha1234");

        mockMvc.perform(patch("/api/v1/usuarios/eu")
                        .header("autorizacao", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Victor Atualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Victor Atualizado"));

        mockMvc.perform(put("/api/v1/usuarios/eu/senha")
                        .header("autorizacao", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha_atual\":\"senha1234\",\"senha_nova\":\"senha5678\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/autenticacao/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"senha5678\"}".formatted(email)))
                .andExpect(status().isOk());

        String novoEmail = "novo" + prefix + "@dinix.test";
        mockMvc.perform(post("/api/v1/autenticacao/enviar-codigo-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\"}".formatted(novoEmail)))
                .andExpect(status().isOk());
        MvcResult troca = mockMvc.perform(put("/api/v1/usuarios/eu/email")
                        .header("autorizacao", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"codigo\":\"%s\"}".formatted(novoEmail, codigoDe(novoEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(novoEmail))
                .andReturn();

        String novoToken = tokenDe(troca.getResponse().getContentAsString());
        mockMvc.perform(get("/api/v1/usuarios/eu").header("autorizacao", "Bearer " + novoToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(novoEmail));

        mockMvc.perform(post("/api/v1/usuarios/eu/excluir")
                        .header("autorizacao", "Bearer " + novoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha\":\"senha5678\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/autenticacao/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"senha5678\"}".formatted(novoEmail)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongCurrentPasswordFails() throws Exception {
        String email = "senha" + UUID.randomUUID().toString().substring(0, 8) + "@dinix.test";
        String token = registrar(email, "Ana", "senha1234");

        mockMvc.perform(put("/api/v1/usuarios/eu/senha")
                        .header("autorizacao", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senha_atual\":\"errada\",\"senha_nova\":\"senha5678\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void uploadAndDeletePhoto() throws Exception {
        String email = "foto" + UUID.randomUUID().toString().substring(0, 8) + "@dinix.test";
        String token = registrar(email, "Foto", "senha1234");
        MockMultipartFile foto = new MockMultipartFile(
                "foto",
                "avatar.png",
                "image/png",
                pngUmPixel());

        MvcResult upload = mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/usuarios/eu/foto")
                        .file(foto)
                        .header("autorizacao", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url_foto").isNotEmpty())
                .andReturn();

        String url = upload.getResponse().getContentAsString()
                .replaceAll(".*\"url_foto\":\"([^\"]+)\".*", "$1");
        mockMvc.perform(get(url)).andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/usuarios/eu/foto")
                        .header("autorizacao", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url_foto").value(nullValue()));
    }

    private byte[] pngUmPixel() {
        return new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D,
                0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08, 0x06,
                0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89, 0x00, 0x00, 0x00, 0x0A,
                0x49, 0x44, 0x41, 0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05,
                0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45,
                0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };
    }

    private String registrar(String email, String nome, String senha) throws Exception {
        enviarEVerificarEmail(email);
        MvcResult result = mockMvc.perform(post("/api/v1/autenticacao/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"%s\",\"email\":\"%s\",\"senha\":\"%s\"}".formatted(nome, email, senha)))
                .andExpect(status().isCreated())
                .andReturn();
        return tokenDe(result.getResponse().getContentAsString());
    }

    private void enviarEVerificarEmail(String email) throws Exception {
        mockMvc.perform(post("/api/v1/autenticacao/enviar-codigo-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\"}".formatted(email)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/autenticacao/verificar-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"codigo\":\"%s\"}".formatted(email, codigoDe(email))))
                .andExpect(status().isOk());
    }

    private String codigoDe(String email) {
        return emailVerificationRepository
                .findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email)
                .orElseThrow()
                .getCode();
    }

    private String tokenDe(String body) {
        return body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }
}
