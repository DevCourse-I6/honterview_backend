package com.i6.honterview.domain.interview.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.i6.honterview.domain.answer.entity.AnswerType;
import com.i6.honterview.domain.question.entity.Question;
import com.i6.honterview.domain.user.entity.Member;

class InterviewTest {

	private Interview interview;
	private Question firstQuestion;
	private Question secondQuestion;
	private Member member;

	@BeforeEach
	void setUp() {
		firstQuestion = mock(Question.class);
		secondQuestion = mock(Question.class);
		member = mock(Member.class);

		interview = new Interview(AnswerType.TEXT, 2, 30, member, firstQuestion);
		interview.addQuestion(secondQuestion);
	}

	@Test
	void 인터뷰에_두개이상의_질문이_있을때_첫번째질문을_조회함() {
		// When
		Question result = interview.findFirstQuestion();

		// Then
		assertEquals(firstQuestion, result, "The first question added should be returned by findFirstQuestion");
	}
}
