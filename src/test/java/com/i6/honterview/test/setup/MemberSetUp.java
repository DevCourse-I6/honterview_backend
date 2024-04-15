package com.i6.honterview.test.setup;

import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.i6.honterview.common.security.auth.UserDetailsImpl;
import com.i6.honterview.common.security.jwt.JwtTokenProvider;
import com.i6.honterview.domain.user.entity.Member;
import com.i6.honterview.domain.user.entity.Provider;
import com.i6.honterview.domain.user.entity.Role;
import com.i6.honterview.domain.user.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Component
@ActiveProfiles(profiles = "test")
@RequiredArgsConstructor
public class MemberSetUp {

	private final MemberRepository memberRepository;
	private final JwtTokenProvider jwtTokenProvider;

	public Member save() {
		Member member = build();
		return memberRepository.save(member);
	}

	public String generateAccessToken(Member member) {
		UserDetailsImpl userDetails = UserDetailsImpl.from(member);
		return jwtTokenProvider.generateAccessToken(userDetails);
	}

	private Member build() {
		return Member.builder()
			.provider(Provider.KAKAO)
			.nickname("TESTER")
			.email("test@test.com")
			.role(Role.ROLE_USER)
			.build();
	}
}
