package com.anjosflow.subject;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {
    @Mock private SubjectRepository repository;
    @InjectMocks private SubjectService service;

    @Test
    void createsNormalizedSubjectWithDefaultColor() {
        when(repository.saveAndFlush(any(Subject.class))).thenAnswer(call -> call.getArgument(0));
        SubjectResponse response = service.create(new SubjectRequest("  Matemática  ", null, null));
        assertThat(response.name()).isEqualTo("Matemática");
        assertThat(response.color()).isEqualTo("#A3B18A");
        assertThat(response.description()).isNull();
    }

    @Test
    void preservesProvidedDescriptionAndColor() {
        when(repository.saveAndFlush(any(Subject.class))).thenAnswer(call -> call.getArgument(0));
        var response = service.create(new SubjectRequest("Java", "Orientação a objetos", "#aBc123"));
        assertThat(response.description()).isEqualTo("Orientação a objetos");
        assertThat(response.color()).isEqualTo("#aBc123");
    }

    @Test
    void listsWithStableNameAndIdSort() {
        Sort sort = Sort.by("name", "id");
        when(repository.findAll(sort)).thenReturn(List.of(new Subject("Java", null, null)));
        assertThat(service.list()).extracting(SubjectResponse::name).containsExactly("Java");
        verify(repository).findAll(sort);
    }

    @Test
    void returnsEmptyListWhenThereAreNoSubjects() {
        when(repository.findAll(Sort.by("name", "id"))).thenReturn(List.of());
        assertThat(service.list()).isEmpty();
    }

    @Test
    void findsSubjectById() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Subject("Java", null, null)));
        assertThat(service.findById(1L).name()).isEqualTo("Java");
    }

    @Test
    void updatesExistingSubjectAndResetsOmittedOptionals() {
        Subject subject = new Subject("Java", "Descrição antiga", "#123456");
        when(repository.findById(1L)).thenReturn(Optional.of(subject));
        when(repository.saveAndFlush(subject)).thenReturn(subject);
        var response = service.update(1L, new SubjectRequest("  Banco de dados  ", null, null));
        assertThat(response.name()).isEqualTo("Banco de dados");
        assertThat(response.description()).isNull();
        assertThat(response.color()).isEqualTo(Subject.DEFAULT_COLOR);
    }

    @Test
    void deletesExistingSubject() {
        Subject subject = new Subject("Java", null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(subject));
        service.delete(1L);
        verify(repository).delete(subject);
    }

    @Test
    void missingSubjectFailsForFindUpdateAndDelete() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(SubjectNotFoundException.class);
        assertThatThrownBy(() -> service.update(99L, new SubjectRequest("Java", null, null)))
                .isInstanceOf(SubjectNotFoundException.class);
        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(SubjectNotFoundException.class);
        verify(repository, never()).saveAndFlush(any());
        verify(repository, never()).delete(any(Subject.class));
    }
}