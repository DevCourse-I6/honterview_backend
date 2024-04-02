package com.i6.honterview.domain.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.i6.honterview.domain.IntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@ActiveProfiles("test")
class MypageControllerTest extends IntegrationTest {

	@Test
	@WithUserDetails(value = "user1", userDetailsServiceBeanName = "userDetailsServiceImpl")
	void 마이페이지_내_연습목록_조회() throws Exception {
		mvc.perform(get("/api/v1/mypage/interviews")
				.param("page", "1")
				.param("size", "5")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content").exists());
	}
}
