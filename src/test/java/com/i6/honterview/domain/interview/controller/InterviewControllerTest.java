package com.i6.honterview.domain.interview.controller;

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
import com.i6.honterview.domain.answer.entity.Answer;
import com.i6.honterview.domain.answer.entity.AnswerType;
import com.i6.honterview.domain.answer.repository.AnswerRepository;
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

class InterviewControllerTest extends IntegrationTest {

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
	@Autowired
	private AnswerRepository answerRepository;

	@Test
	void 면접현황조회_성공() throws Exception {
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
		Interview savedInterview = interviewRepository.save(interview);
		Question secondQuestion = Question.builder()
			.content("second question")
			.categories(categories)
			.createdBy("MEMBER1")
			.build();
		Question savedSecondQuestion = questionRepository.save(secondQuestion);
		interview.addQuestion(savedSecondQuestion);

		// When & Then
		mockMvc.perform(get("/api/v1/interviews/{id}", savedInterview.getId())
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.questionsAndAnswers[0].questionContent").value(firstQuestion.getContent()))
			.andDo(print());
	}

	@Test
	void 첫번째질문에대한_답변_저장() throws Exception {
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
		Interview savedInterview = interviewRepository.save(interview);

		// When & Then
		// API를 호출하여 첫번째 답변 추가
		String firstquestionAndAnswerJson = """
			{
			    "questionContent": "first question",
			    "answerContent": "This is an answer to the first question.",
			    "processingTime": 30
			}
			""";

		mockMvc.perform(post("/api/v1/interviews/{id}", savedInterview.getId())
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(firstquestionAndAnswerJson))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.questionId").value(savedFirstQuestion.getId()))
			.andDo(print());
	}

	@Test
	void 두번째질문_답변_저장() throws Exception {
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
		Interview savedInterview = interviewRepository.save(interview);

		// 첫번째 질문에 대한 답변 저장
		answerRepository.save(new Answer("answer to first Question", 30, savedFirstQuestion, savedInterview));

		// When & Then
		// API를 호출하여 두 번째 질문 추가
		String questionAndAnswerJson = """
			{
			    "questionContent": "second question",
			    "answerContent": "This is an answer to the second question.",
			    "processingTime": 30
			}
			""";

		mockMvc.perform(post("/api/v1/interviews/{id}", savedInterview.getId())
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(questionAndAnswerJson))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.questionId").value(2))
			.andDo(print());
	}
}
