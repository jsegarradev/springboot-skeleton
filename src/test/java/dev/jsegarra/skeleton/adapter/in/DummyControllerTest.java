package dev.jsegarra.skeleton.adapter.in;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.jsegarra.skeleton.domain.value.Dummy;
import dev.jsegarra.skeleton.port.in.GetDummy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web slice: the controller maps the domain output to the response contract. The generated MapStruct impl is imported
 * explicitly because {@code @WebMvcTest} does not scan {@code @Component} mappers.
 */
@WebMvcTest(DummyController.class)
@Import(DummyResponseMapperImpl.class)
class DummyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetDummy getDummy;

    @Test
    void get_dummy_returns_the_value() throws Exception {
        when(getDummy.execute()).thenReturn(new Dummy(1L, "walking-skeleton"));

        mockMvc.perform(get("/dummy")).andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("walking-skeleton"));
    }
}
