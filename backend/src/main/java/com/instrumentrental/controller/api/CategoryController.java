package com.instrumentrental.controller.api;

import com.instrumentrental.domain.model.Category;
import com.instrumentrental.domain.repository.CategoryRepository;
import com.instrumentrental.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ApiResponse<List<Category>> getCategories() {
        return ApiResponse.success(categoryRepository.findByParentIsNullOrderBySortOrder());
    }
}