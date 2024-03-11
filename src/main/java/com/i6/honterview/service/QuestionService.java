package com.i6.honterview.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.i6.honterview.domain.Answer;
import com.i6.honterview.domain.Category;
import com.i6.honterview.domain.Question;
import com.i6.honterview.dto.request.QuestionCreateRequest;
import com.i6.honterview.dto.request.QuestionUpdateRequest;
import com.i6.honterview.dto.request.TailQuestionSaveRequest;
import com.i6.honterview.dto.response.AnswerResponse;
import com.i6.honterview.dto.response.PageResponse;
import com.i6.honterview.dto.response.QuestionDetailResponse;
import com.i6.honterview.dto.response.QuestionResponse;
import com.i6.honterview.dto.response.QuestionWithCategoriesResponse;
import com.i6.honterview.exception.CustomException;
import com.i6.honterview.exception.ErrorCode;
import com.i6.honterview.repository.AnswerRepository;
import com.i6.honterview.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class QuestionService {// TODO: 멤버&관리자 연동

	private final QuestionRepository questionRepository;
	private final AnswerRepository answerRepository;
	private final CategoryService categoryService;

	@Transactional(readOnly = true)
	public PageResponse<QuestionWithCategoriesResponse> getQuestions(int page, int size, String query,
		List<String> categoryNames,
		String orderType) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<Question> questions = questionRepository.
			findQuestionsByKeywordAndCategoryNamesWithPage(pageable, query, categoryNames, orderType);
		return PageResponse.of(questions, QuestionWithCategoriesResponse::from);
	}

	@Transactional(readOnly = true)
	public QuestionDetailResponse getQuestionById(Long id, int page, int size) {
		Question question = questionRepository.findQuestionByIdWithCategories(id)
			.orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));

		Pageable pageable = PageRequest.of(page - 1, size);
		Page<Answer> answers = answerRepository.findByQuestionIdWithMember(id, pageable);
		PageResponse<AnswerResponse> answerResponse = PageResponse.of(answers, AnswerResponse::from);

		return QuestionDetailResponse.from(question, answerResponse);
	}

	public List<QuestionResponse> getRandomTailQuestions(Long parentId) {
		List<Question> tailQuestions = questionRepository.findRandomTailQuestionsByParentId(parentId);
		return tailQuestions.stream()
			.map(QuestionResponse::from)
			.toList();
	}

	public QuestionResponse createQuestion(QuestionCreateRequest request, String creator) {
		List<Category> categories = categoryService.validateAndGetCategories(request.categoryIds());
		Question question = questionRepository.save(request.toEntity(categories, creator));
		return QuestionResponse.from(question);
	}

	public Question saveTailQuestion(TailQuestionSaveRequest request) {
		List<Category> categories = categoryService.validateAndGetCategories(request.categoryIds());
		return questionRepository.save(request.toEntity(categories));
	}

	public void updateQuestion(Long id, QuestionUpdateRequest request) {
		Question question = questionRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));

		List<Category> categories = categoryService.validateAndGetCategories(request.categoryIds());
		question.changeContentAndCategories(request.content(), categories);
	}

	public void deleteQuestion(Long id) {
		Question question = questionRepository.findById(id)
			.orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));
		questionRepository.delete(question);
		// TODO : 연관된 답변 삭제
	}

	public List<QuestionResponse> getQuestionsByCategoryNames(List<String> categoryNames) {
		List<Question> questions = questionRepository.findQuestionsByCategoryNames(categoryNames);
		return questions.stream()
			.map(QuestionResponse::from)
			.toList();
	}

	public PageResponse<QuestionWithCategoriesResponse> getBookmarkedQuestionsMypage(int page, int size,
		Long memberId) {
		Pageable pageable = PageRequest.of(page - 1, size);  // TODO : PageRequest 리팩토링
		Page<Question> questions = questionRepository.findByMemberIdWithPage(pageable, memberId);
		return PageResponse.of(questions, QuestionWithCategoriesResponse::from);
	}
}
