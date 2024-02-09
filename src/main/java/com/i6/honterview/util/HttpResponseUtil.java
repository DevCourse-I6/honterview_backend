package com.i6.honterview.util;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.i6.honterview.response.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;

public class HttpResponseUtil {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static void setSuccessResponse(HttpServletResponse response, HttpStatus httpStatus, Object body)
		throws IOException {
		String responseBody = objectMapper.writeValueAsString(ApiResponse.ok(body));
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(httpStatus.value());
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(responseBody);
	}

	public static void setErrorResponse(HttpServletResponse response, HttpStatus httpStatus, Object body)
		throws IOException {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(httpStatus.value());
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getOutputStream(), body);
	}
}
