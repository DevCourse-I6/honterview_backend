package com.i6.honterview.test;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;

@Ignore
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles(profiles = "test")
public abstract class IntegrationTest {

	private static final String REDIS_VERSION = "redis:7.2.4";
	private static final String MYSQL_VERSION = "mysql:8.0.35";

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Container
	public static GenericContainer<?> redis = new GenericContainer(DockerImageName.parse(REDIS_VERSION))
		.withExposedPorts(6379);

	@Container
	public static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse(MYSQL_VERSION))
		.withDatabaseName("testdb")
		.withUsername("test")
		.withPassword("test");

	@DynamicPropertySource
	static void redisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", redis::getFirstMappedPort);
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
	}

	@BeforeEach
	public void cleanDatabase() {
		System.out.println("cleanDatabase");
		jdbcTemplate.execute("TRUNCATE TABLE admin");
		jdbcTemplate.execute("TRUNCATE TABLE answer");
		jdbcTemplate.execute("TRUNCATE TABLE answer_heart");
		jdbcTemplate.execute("TRUNCATE TABLE category");
		jdbcTemplate.execute("TRUNCATE TABLE interview");
		jdbcTemplate.execute("TRUNCATE TABLE interview_question");
		jdbcTemplate.execute("TRUNCATE TABLE member");
		jdbcTemplate.execute("TRUNCATE TABLE question");
		jdbcTemplate.execute("TRUNCATE TABLE question_bookmark");
		jdbcTemplate.execute("TRUNCATE TABLE question_category");
		jdbcTemplate.execute("TRUNCATE TABLE question_heart");
		jdbcTemplate.execute("TRUNCATE TABLE video");
	}

}
