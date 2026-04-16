package com.cauecalil.personalfinance.application.usecase;

import com.cauecalil.personalfinance.application.port.FinancialGateway;
import com.cauecalil.personalfinance.domain.model.Category;
import com.cauecalil.personalfinance.domain.model.UserCredential;
import com.cauecalil.personalfinance.domain.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncCategoriesUseCaseTest {

    @Mock
    private FinancialGateway financialGateway;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private SyncCategoriesUseCase useCase;

    @Test
    void shouldAssignRootCategoryIdAcrossHierarchyWhenCategoriesHaveParents() {
        UserCredential credential = UserCredential.builder().clientId("id").clientSecret("secret").build();

        Category root = Category.builder().id("100").description("Root").parentId(null).build();
        Category child = Category.builder().id("110").description("Child").parentId("100").build();
        Category grandChild = Category.builder().id("111").description("Grandchild").parentId("110").build();

        when(financialGateway.fetchCategories(credential)).thenReturn(List.of(root, child, grandChild));
        when(categoryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Category> result = useCase.execute(credential);

        assertThat(result)
                .extracting(Category::getId, Category::getRootCategoryId)
                .containsExactlyInAnyOrder(
                        tuple("100", "100"),
                        tuple("110", "100"),
                        tuple("111", "100")
                );

        verify(financialGateway).fetchCategories(credential);
        verify(categoryRepository).saveAll(anyList());
    }

    @Test
    void shouldFallbackToSelfRootCategoryWhenParentDoesNotExist() {
        UserCredential credential = UserCredential.builder().clientId("id").clientSecret("secret").build();

        Category orphan = Category.builder()
                .id("200")
                .description("Orphan")
                .parentId("does-not-exist")
                .build();

        when(financialGateway.fetchCategories(credential)).thenReturn(List.of(orphan));
        when(categoryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Category> result = useCase.execute(credential);

        assertThat(result).singleElement().satisfies(category -> {
            assertThat(category.getId()).isEqualTo("200");
            assertThat(category.getRootCategoryId()).isEqualTo("200");
        });
    }

    @Test
    void shouldResolveCycleWithoutRecursionOverflowWhenCategoryHierarchyIsCyclic() {
        UserCredential credential = UserCredential.builder().clientId("id").clientSecret("secret").build();

        Category first = Category.builder().id("c1").description("First").parentId("c2").build();
        Category second = Category.builder().id("c2").description("Second").parentId("c1").build();

        when(financialGateway.fetchCategories(credential)).thenReturn(List.of(first, second));
        when(categoryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Category> result = useCase.execute(credential);

        Map<String, String> rootById = result.stream()
                .collect(Collectors.toMap(Category::getId, Category::getRootCategoryId));

        assertThat(rootById.get("c1")).isEqualTo("c1");
        assertThat(rootById.get("c2")).isIn("c1", "c2");
    }
}
