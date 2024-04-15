package com.i6.honterview.domain.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.i6.honterview.domain.interview.entity.Interview;
import com.i6.honterview.domain.user.entity.Member;
import com.i6.honterview.test.IntegrationTest;
import com.i6.honterview.test.setup.InterviewSetUp;
import com.i6.honterview.test.setup.MemberSetUp;

class MypageIntegrationTest extends IntegrationTest {

	@Autowired
	private MemberSetUp memberSetUp;

	@Autowired
	private InterviewSetUp interviewSetUp;

	@Test
	void 마이페이지_연습목록_조회시_인터뷰의_첫번째질문내용이_조회된다() throws Exception {
		// Given
		Member member = memberSetUp.save();
		String token = memberSetUp.generateAccessToken(member);
		Interview interview = interviewSetUp.save(member);

		// When & Then
		mockMvc.perform(get("/api/v1/mypage/interviews")
				.header("Authorization", "Bearer " + token)
				.param("page", "1")
				.param("size", "5")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(
				jsonPath("$.data.data[0].firstQuestionContent").value(interview.findFirstQuestion().getContent()))
			.andDo(print());
	}
}
