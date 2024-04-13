package com.i6.honterview.domain.question.service;

import static com.i6.honterview.domain.user.entity.Role.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.i6.honterview.common.dto.PageRequest;
import com.i6.honterview.common.dto.PageResponse;
import com.i6.honterview.common.exception.CustomException;
import com.i6.honterview.common.exception.ErrorCode;
import com.i6.honterview.common.security.auth.UserDetailsImpl;
import com.i6.honterview.domain.answer.dto.response.AnswerResponse;
import com.i6.honterview.domain.answer.entity.Answer;
import com.i6.honterview.domain.answer.service.AnswerService;
import com.i6.honterview.domain.question.dto.request.QuestionCreateRequest;
import com.i6.honterview.domain.question.dto.request.QuestionPageRequest;
import com.i6.honterview.domain.question.dto.request.QuestionUpdateRequest;
import com.i6.honterview.domain.question.dto.request.TailQuestionSaveRequest;
import com.i6.honterview.domain.question.dto.response.QuestionDetailResponse;
import com.i6.honterview.domain.question.dto.response.QuestionResponse;
import com.i6.honterview.domain.question.dto.response.QuestionWithCategoriesResponse;
import com.i6.honterview.domain.question.entity.Category;
import com.i6.honterview.domain.question.entity.Question;
import com.i6.honterview.domain.question.repository.QuestionBookmarkRepository;
import com.i6.honterview.domain.question.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class QuestionService {// TODO: 멤버&관리자 연동

	private final QuestionRepository questionRepository;
	private final QuestionBookmarkRepository questionBookmarkRepository;
	private final AnswerService answerService;
	private final CategoryService categoryService;

	@Transactional(readOnly = true)
	public PageResponse<QuestionWithCategoriesResponse> getQuestions(QuestionPageRequest request) {
		Page<Question> questions = questionRepository.
			findQuestionsByKeywordAndCategoryNamesWithPage(request);
		return PageResponse.of(questions, QuestionWithCategoriesResponse::from);
	}

	@Transactional(readOnly = true)
	public QuestionDetailResponse getQuestionById(Long questionId, PageRequest pageRequest) {
		// 현재 로그인한 사용자 조회
		UserDetailsImpl currentUserDetails = getCurrentUserDetails().orElse(null);

		// id로 질문 조회
		Question question = questionRepository.findQuestionByIdWithCategoriesAndHearts(questionId)
			.orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));

		// 로그인한 사용자의 경우, 질문에 대한 좋아요 및 북마크 여부를 확인
		boolean isHeartedByCurrentMember = isQuestionHeartedByMember(question, currentUserDetails);
		boolean isBookmarkedByCurrentMember = isQuestionBookmarkedByMember(question, currentUserDetails);

		// 답변 목록 조회
		PageResponse<AnswerResponse> answerResponse = getAnswerResponse(questionId, pageRequest, currentUserDetails);

		return QuestionDetailResponse.of(question, answerResponse, isHeartedByCurrentMember,
			isBookmarkedByCurrentMember);
	}

	private PageResponse<AnswerResponse> getAnswerResponse(Long questionId, PageRequest pageRequest,
		UserDetailsImpl currentUserDetails) {
		Pageable pageable = pageRequest.getPageable();
		Page<Answer> answers = answerService.findByQuestionIdWithMemberAndHearts(questionId, pageable);

		// 조회된 답변을 DTO로 변환, 로그인한 사용자는 각 답변에 대한 좋아요 여부를 확인
		return PageResponse.of(answers, answer -> AnswerResponse.from(answer, currentUserDetails));
	}

	private Optional<UserDetailsImpl> getCurrentUserDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			boolean isRoleUser = authentication.getAuthorities().stream()
				.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(ROLE_USER.name()));
			if (isRoleUser) {
				return Optional.of((UserDetailsImpl)authentication.getPrincipal());
			}
		}
		return Optional.empty();
	}

	private boolean isQuestionHeartedByMember(Question question, UserDetailsImpl userDetails) {
		if (userDetails == null) {
			return false;
		}
		return question.findQuestionHeartByMemberId(userDetails.getId()).isPresent();
	}

	private boolean isQuestionBookmarkedByMember(Question question, UserDetailsImpl userDetails) {
		if (userDetails == null) {
			return false;
		}
		return questionBookmarkRepository.existsByQuestionIdAndMemberId(question.getId(), userDetails.getId());
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
		Question question = findById(id);

		List<Category> categories = categoryService.validateAndGetCategories(request.categoryIds());
		question.changeContentAndCategories(request.content(), categories);
	}

	public void deleteQuestion(Long id) {
		Question question = findById(id);
		questionRepository.delete(question);
	}

	public List<QuestionResponse> getQuestionsByCategoryNames(List<String> categoryNames) {
		List<Question> questions = questionRepository.findQuestionsByCategoryNames(categoryNames);
		return questions.stream()
			.map(QuestionResponse::from)
			.toList();
	}

	public PageResponse<QuestionWithCategoriesResponse> getBookmarkedQuestionsMypage(Long memberId,
		PageRequest pageRequest) {
		Pageable pageable = pageRequest.getPageable();
		Page<Question> questions = questionRepository.findByMemberIdWithPage(pageable, memberId);
		return PageResponse.of(questions, QuestionWithCategoriesResponse::from);
	}

	public Question findByIdWithHearts(Long questionId) {
		return questionRepository.findByIdWithHearts(questionId)
			.orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));
	}

	public Question findById(Long questionId) {
		return questionRepository.findById(questionId)
			.orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));
	}
}
