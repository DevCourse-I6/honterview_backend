package com.i6.honterview.test.setup;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import com.i6.honterview.domain.question.entity.Category;
import com.i6.honterview.domain.question.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@ActiveProfiles(profiles = "test")
@RequiredArgsConstructor
public class CategorySetUp {

	private final CategoryRepository categoryRepository;

	public Category save() {
		return categoryRepository.save(build("category"));
	}

	public List<Category> saveAll() {
		Category category1 = build("category1");
		Category category2 = build("category2");
		return categoryRepository.saveAll(List.of(category1, category2));
	}

	private Category build(String name) {
		return new Category(name);
	}
}
