package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.repository.EmailVerificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Test
    void registerLoginAndMe() throws Exception {
        String email = "victor@dinix.test";
        enviarEVerificarEmail(email);

        String registerJson = """
                {"nome":"Victor","email":"victor@dinix.test","senha":"senha1234"}
                """;
        mockMvc.perform(post("/api/v1/autenticacao/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo_token").value("Bearer"))
                .andExpect(jsonPath("$.nome").value("Victor"));

        MvcResult login = mockMvc.perform(post("/api/v1/autenticacao/entrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"victor@dinix.test","senha":"senha1234"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String body = login.getResponse().getContentAsString();
        String token = body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        mockMvc.perform(get("/api/v1/usuarios/eu").header("autorizacao", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("victor@dinix.test"))
                .andExpect(jsonPath("$.nome").value("Victor"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        String email = "ana@dinix.test";
        enviarEVerificarEmail(email);

        String registerJson = """
                {"nome":"Ana","email":"ana@dinix.test","senha":"senha1234"}
                """;
        mockMvc.perform(post("/api/v1/autenticacao/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/autenticacao/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isConflict());
    }

    @Test
    void registerWithoutVerificationFails() throws Exception {
        mockMvc.perform(post("/api/v1/autenticacao/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Sem Verificacao","email":"semverif@dinix.test","senha":"senha1234"}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    private void enviarEVerificarEmail(String email) throws Exception {
        mockMvc.perform(post("/api/v1/autenticacao/enviar-codigo-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());

        String code = emailVerificationRepository
                .findTopByEmailIgnoreCaseOrderByCreatedAtDesc(email)
                .orElseThrow()
                .getCode();

        mockMvc.perform(post("/api/v1/autenticacao/verificar-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","codigo":"%s"}
                                """.formatted(email, code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificado").value(true));
    }
}
