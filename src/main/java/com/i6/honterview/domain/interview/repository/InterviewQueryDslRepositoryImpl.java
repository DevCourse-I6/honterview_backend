package com.i6.honterview.domain.interview.repository;

import static com.i6.honterview.domain.interview.entity.QInterview.*;
import static com.i6.honterview.domain.interview.entity.QInterviewQuestion.*;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.i6.honterview.domain.interview.entity.Interview;
import com.i6.honterview.domain.interview.entity.InterviewQuestion;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InterviewQueryDslRepositoryImpl implements InterviewQueryDslRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Interview> findByMemberIdWithPage(Pageable pageable, Long memberId) {
		List<Interview> interviews = queryFactory
			.selectFrom(interview)
			.leftJoin(interview.interviewQuestions, interviewQuestion).fetchJoin()
			.where(interview.member.id.eq(memberId))
			.orderBy(interview.id.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		interviews.forEach(interview -> interview.getInterviewQuestions()
			.sort(Comparator.comparing(InterviewQuestion::getId)));

		JPAQuery<Long> countQuery = queryFactory
			.select(interview.count())
			.from(interview)
			.where(interview.member.id.eq(memberId));

		return PageableExecutionUtils.getPage(interviews, pageable, countQuery::fetchOne);
	}
}
