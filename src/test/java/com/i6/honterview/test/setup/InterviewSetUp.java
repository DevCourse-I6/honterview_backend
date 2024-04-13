package com.i6.honterview.test.setup;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.i6.honterview.domain.answer.entity.AnswerType;
import com.i6.honterview.domain.interview.entity.Interview;
import com.i6.honterview.domain.interview.repository.InterviewRepository;
import com.i6.honterview.domain.question.entity.Category;
import com.i6.honterview.domain.question.entity.Question;
import com.i6.honterview.domain.user.entity.Member;

import lombok.RequiredArgsConstructor;

@Component
@ActiveProfiles(profiles = "test")
@RequiredArgsConstructor
public class InterviewSetUp {

	private final InterviewRepository interviewRepository;
	private final CategorySetUp categorySetUp;
	private final QuestionSetUp questionSetUp;

	public Interview save(Member member) {
		// TODO : add answerSetUp
		List<Category> categories = categorySetUp.saveAll();
		Question firstQuestion = questionSetUp.save("first question", categories, member);

		Interview interview = build(member, firstQuestion);
		Question secondQuestion = questionSetUp.save("second question", categories, member);
		interview.addQuestion(secondQuestion);
		return interviewRepository.save(interview);
	}

	private Interview build(Member member, Question firstQuestion) {
		return new Interview(AnswerType.TEXT, 2, 30, member, firstQuestion);
	}

}
