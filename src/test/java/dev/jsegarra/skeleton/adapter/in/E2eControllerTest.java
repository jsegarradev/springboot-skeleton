package dev.jsegarra.skeleton.adapter.in;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.jsegarra.skeleton.port.in.E2e;
import dev.jsegarra.skeleton.port.in.result.E2eResult;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Live-verify surface with the secret configured: the correct secret runs the journey and returns the asserted fields;
 * a wrong secret keeps the endpoint invisible (404).
 */
@WebMvcTest(E2eController.class)
@TestPropertySource(properties = "e2e.secret=s3cr3t")
class E2eControllerTest {

    private static final String SECRET_HEADER = "x-e2e-secret";
    private static final String SECRET = "s3cr3t";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private E2e e2e;

    @Test
    void runs_the_journey_with_the_correct_secret() throws Exception {
        when(e2e.execute()).thenReturn(new E2eResult(true, Map.of("dummy", "walking-skeleton")));

        mockMvc.perform(post("/internal/e2e").header(SECRET_HEADER, SECRET)).andExpect(status().isOk())
                .andExpect(jsonPath("$.ran").value(true))
                .andExpect(jsonPath("$.fields.dummy").value("walking-skeleton"));
    }

    @Test
    void hides_the_endpoint_when_the_secret_is_wrong() throws Exception {
        mockMvc.perform(post("/internal/e2e").header(SECRET_HEADER, "wrong")).andExpect(status().isNotFound());
    }
}
