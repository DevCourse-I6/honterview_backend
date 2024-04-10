package com.i6.honterview.domain.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.i6.honterview.common.security.auth.UserDetailsImpl;
import com.i6.honterview.common.security.jwt.JwtTokenProvider;
import com.i6.honterview.domain.IntegrationTest;
import com.i6.honterview.domain.answer.entity.AnswerType;
import com.i6.honterview.domain.interview.entity.Interview;
import com.i6.honterview.domain.interview.repository.InterviewRepository;
import com.i6.honterview.domain.question.entity.Category;
import com.i6.honterview.domain.question.entity.Question;
import com.i6.honterview.domain.question.repository.CategoryRepository;
import com.i6.honterview.domain.question.repository.QuestionRepository;
import com.i6.honterview.domain.user.entity.Member;
import com.i6.honterview.domain.user.entity.Provider;
import com.i6.honterview.domain.user.entity.Role;
import com.i6.honterview.domain.user.repository.MemberRepository;

class MypageControllerTest extends IntegrationTest {

	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private InterviewRepository interviewRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Test
	void 마이페이지_연습목록_조회시_각_연습의_첫번째질문내용_목록이_조회된다() throws Exception {
		// Given
		Member member = memberRepository.save(
			Member.builder()
				.provider(Provider.KAKAO)
				.nickname("TESTER")
				.email("test@test.com")
				.role(Role.ROLE_USER)
				.build()
		);
		UserDetailsImpl userDetails = UserDetailsImpl.from(member);
		String token = jwtTokenProvider.generateAccessToken(userDetails);

		Category category = new Category("testcategory");
		categoryRepository.save(category);
		List<Category> categories = List.of(category);
		Question firstQuestion = Question.builder()
			.content("first question")
			.categories(categories)
			.createdBy("MEMBER1")
			.build();
		Question savedFirstQuestion = questionRepository.save(firstQuestion);
		Interview interview = new Interview(AnswerType.TEXT, 2, 30, member, savedFirstQuestion);
		interviewRepository.save(interview);
		Question secondQuestion = Question.builder()
			.content("second question")
			.categories(categories)
			.createdBy("MEMBER1")
			.build();
		Question savedSecondQuestion = questionRepository.save(secondQuestion);
		interview.addQuestion(savedSecondQuestion);
		interviewRepository.save(interview);

		// When & Then
		mockMvc.perform(get("/api/v1/mypage/interviews")
				.header("Authorization", "Bearer " + token)
				.param("page", "1")
				.param("size", "5")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.data[0].firstQuestionContent").value(firstQuestion.getContent()))
			.andDo(print());
	}

	// @Test
	// void 마이페이지_연습목록_조회시_각_연습의_첫번째질문내용_목록이_조회된다_실제데이터세팅방식을_따름() throws Exception {
	// 	// Given
	// 	Member member = memberRepository.save(
	// 		Member.builder()
	// 			.provider(Provider.KAKAO)
	// 			.nickname("TESTER")
	// 			.email("test@test.com")
	// 			.role(Role.ROLE_USER)
	// 			.build()
	// 	);
	// 	UserDetailsImpl userDetails = UserDetailsImpl.from(member);
	// 	String token = jwtTokenProvider.generateAccessToken(userDetails);
	//
	// 	Category category = new Category("testcategory");
	// 	categoryRepository.save(category);
	//
	// 	// 질문 생성 요청 JSON 준비
	// 	String questionCreateJson = """
	// 		{
	// 		    "content": "first question",
	// 		    "categoryIds": [1]
	// 		}
	// 		""";
	//
	// 	// 질문 생성 API 호출 및 응답으로부터 질문 ID 추출
	// 	MvcResult questionResult = mockMvc.perform(post("/api/v1/questions")
	// 			.header("Authorization", "Bearer " + token)
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.content(questionCreateJson))
	// 		.andExpect(status().isCreated())
	// 		.andReturn();
	//
	// 	Integer questionId = JsonPath.read(questionResult.getResponse().getContentAsString(), "$.data.id");
	//
	// 	// API를 호출하여 인터뷰 생성
	// 	String interviewCreateJson = String.format("""
	// 		{
	// 		    "answerType": "TEXT",
	// 		    "questionCount": 2,
	// 		    "timer": 30,
	// 		    "questionId": %d
	// 		}
	// 		""", questionId);
	//
	// 	// 인터뷰 생성 API 호출
	// 	MvcResult result = mockMvc.perform(post("/api/v1/interviews")
	// 			.header("Authorization", "Bearer " + token)
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.content(interviewCreateJson))
	// 		.andExpect(status().isCreated())
	// 		.andReturn();
	//
	// 	// 인터뷰 ID 추출
	// 	Integer interviewId = JsonPath.read(result.getResponse().getContentAsString(), "$.data");
	//
	// 	// API를 호출하여 첫번째 답변 추가
	// 	String firstquestionAndAnswerJson = """
	// 		{
	// 		    "questionContent": "first question",
	// 		    "answerContent": "This is an answer to the first question.",
	// 		    "processingTime": 30
	// 		}
	// 		""";
	//
	// 	mockMvc.perform(post("/api/v1/interviews/{id}", interviewId)
	// 			.header("Authorization", "Bearer " + token)
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.content(firstquestionAndAnswerJson))
	// 		.andExpect(status().isOk())
	// 		.andDo(print());
	//
	// 	// API를 호출하여 두 번째 질문 추가
	// 	String questionAndAnswerJson = """
	// 		{
	// 		    "questionContent": "second question",
	// 		    "answerContent": "This is an answer to the second question.",
	// 		    "processingTime": 30
	// 		}
	// 		""";
	//
	// 	mockMvc.perform(post("/api/v1/interviews/{id}", interviewId)
	// 			.header("Authorization", "Bearer " + token)
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.content(questionAndAnswerJson))
	// 		.andExpect(status().isOk())
	// 		.andDo(print());
	//
	// 	// when & then
	// 	mockMvc.perform(get("/api/v1/mypage/interviews")
	// 			.header("Authorization", "Bearer " + token)
	// 			.param("page", "1")
	// 			.param("size", "5")
	// 			.contentType(MediaType.APPLICATION_JSON))
	// 		.andExpect(status().isOk())
	// 		.andExpect(jsonPath("$.data.data[0].firstQuestionContent").value("first question"))
	// 		.andDo(print());
	// }

}
