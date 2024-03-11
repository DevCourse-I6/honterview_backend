package com.i6.honterview.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.i6.honterview.domain.Interview;
import com.i6.honterview.domain.enums.InterviewStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record InterviewMypageResponse(
	@Schema(description = "인터뷰 ID", example = "123")
	Long interviewId,

	@Schema(description = "첫번째 질문 내용", example = "JVM의 역할에 대해 설명해주세요.")
	String firstQuestionContent,

	@Schema(description = "카테고리 이름 목록")
	List<String> categoryNames,

	@Schema(description = "생성 시각")
	LocalDateTime createdAt,

	@Schema(description = "진행 상태", example = "COMPLETED")
	InterviewStatus status

) {
	public static InterviewMypageResponse from(Interview interview) {
		return new InterviewMypageResponse(
			interview.getId(),
			interview.findFirstQuestion().getContent(),
			interview.findFirstQuestion().getCategoryNames(),
			interview.getCreatedAt(),
			interview.getStatus()
		);
	}
}
