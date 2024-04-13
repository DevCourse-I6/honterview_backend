package com.i6.honterview.test.setup;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.i6.honterview.domain.question.entity.Category;
import com.i6.honterview.domain.question.entity.Question;
import com.i6.honterview.domain.question.repository.QuestionRepository;
import com.i6.honterview.domain.user.entity.Member;

import lombok.RequiredArgsConstructor;

@Component
@ActiveProfiles(profiles = "test")
@RequiredArgsConstructor
public class QuestionSetUp {

	private final QuestionRepository questionRepository;

	public Question save(String content, List<Category> categories, Member member) {
		return questionRepository.save(Question.builder()
			.content(content)
			.categories(categories)
			.createdBy("MEMBER" + member.getId())
			.build());
	}
}
