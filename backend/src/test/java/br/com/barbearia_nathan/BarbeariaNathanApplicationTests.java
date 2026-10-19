package br.com.barbearia_nathan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(BarbeariaNathanApplicationTests.TestApiConfiguration.class)
class BarbeariaNathanApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void restControllersUseApiPrefix() throws Exception {
		mockMvc.perform(get("/api/_test/ping"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/_test/ping"))
				.andExpect(status().isNotFound());
	}

	@Test
	void validationErrorsUseStandardResponse() throws Exception {
		mockMvc.perform(post("/api/_test/validation")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"name\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.name").exists());
	}

	@Test
	void malformedJsonAndInvalidParametersReturnBadRequest() throws Exception {
		mockMvc.perform(post("/api/_test/validation")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(get("/api/_test/parameters").param("count", "invalid"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(get("/api/_test/parameters"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void unexpectedErrorsDoNotExposeInternalDetails() throws Exception {
		mockMvc.perform(get("/api/_test/failure"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.message").value("Ocorreu um erro interno no servidor."));
	}

	@TestConfiguration
	static class TestApiConfiguration {

		@Bean
		TestApiController testApiController() {
			return new TestApiController();
		}
	}

	@RestController
	static class TestApiController {

		@GetMapping("/_test/ping")
		String ping() {
			return "ok";
		}

		@PostMapping("/_test/validation")
		void validate(@Valid @RequestBody TestRequest request) {
		}

		@GetMapping("/_test/parameters")
		int parameters(@RequestParam int count) {
			return count;
		}

		@GetMapping("/_test/failure")
		void failure() {
			throw new IllegalStateException("internal detail");
		}
	}

	record TestRequest(@NotBlank String name) {
	}
}
