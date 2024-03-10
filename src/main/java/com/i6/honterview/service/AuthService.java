package com.i6.honterview.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.i6.honterview.domain.Member;
import com.i6.honterview.dto.request.LoginRequest;
import com.i6.honterview.dto.response.TokenResponse;
import com.i6.honterview.exception.CustomException;
import com.i6.honterview.exception.ErrorCode;
import com.i6.honterview.exception.SecurityCustomException;
import com.i6.honterview.exception.SecurityErrorCode;
import com.i6.honterview.repository.MemberRepository;
import com.i6.honterview.repository.RedisRepository;
import com.i6.honterview.security.auth.UserDetailsImpl;
import com.i6.honterview.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

	private final MemberRepository memberRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisRepository redisRepository;
	private final AuthenticationManager authenticationManager;

	public TokenResponse reissue(String token) {
		Object memberIdObj = redisRepository.get(token);
		if (memberIdObj == null) {
			throw new SecurityCustomException(SecurityErrorCode.REFRESH_TOKEN_EXPIRED);
		}
		Long memberId = Long.parseLong(redisRepository.get(token).toString());
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		UserDetailsImpl userDetails = UserDetailsImpl.from(member);
		String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
		String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

		redisRepository.delete(token);
		redisRepository.saveRefreshToken(refreshToken, memberId);
		return new TokenResponse(accessToken, refreshToken);
	}

	public void logout(String refreshToken, String authorizationToken, Long id) {
		if (redisRepository.hasKey(refreshToken)) {
			Long userIdByRefresh = Long.valueOf(redisRepository.get(refreshToken).toString());
			if (userIdByRefresh.equals(id)) {
				String accessToken = jwtTokenProvider.getTokenBearer(authorizationToken);
				redisRepository.delete(refreshToken);
				redisRepository.saveBlackList(accessToken, "accessToken");
			} else {
				throw new SecurityCustomException(SecurityErrorCode.LOGOUT_FORBIDDEN);
			}
		} else {
			throw new SecurityCustomException(SecurityErrorCode.REFRESH_TOKEN_EXPIRED);
		}
	}

	public TokenResponse adminLogin(LoginRequest request) {
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			request.email(),
			request.password()
		);

		Authentication authenticate = authenticationManager.authenticate(authentication);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl)authenticate.getPrincipal();
		String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
		String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
		redisRepository.saveRefreshToken(refreshToken, userDetails.getId());

		return new TokenResponse(accessToken, refreshToken);
	}
}
