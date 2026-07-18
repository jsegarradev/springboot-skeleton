package dev.jsegarra.skeleton.adapter.in;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.jsegarra.skeleton.port.in.E2e;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Safe-by-default: with {@code e2e.secret} unset the endpoint is invisible — 404 even when a header is supplied
 * (springboot.md §10.1).
 */
@WebMvcTest(E2eController.class)
class E2eControllerDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private E2e e2e;

    @Test
    void returns_404_when_the_secret_is_unset() throws Exception {
        mockMvc.perform(post("/internal/e2e").header("x-e2e-secret", "anything")).andExpect(status().isNotFound());
    }
}
