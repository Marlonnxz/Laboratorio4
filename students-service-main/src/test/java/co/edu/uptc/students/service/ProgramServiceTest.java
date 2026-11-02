package co.edu.uptc.students.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.dto.ProgramRequest;
import co.edu.uptc.students.dto.ProgramResponse;
import co.edu.uptc.students.entity.Program;
import co.edu.uptc.students.exception.DuplicateResourceException;
import co.edu.uptc.students.exception.ResourceNotFoundException;
import co.edu.uptc.students.repository.ProgramRepository;

@ExtendWith(MockitoExtension.class)
class ProgramServiceTest {

    @Mock
    private ProgramRepository programRepository;

    private ProgramService programService;

    private Program program;

    @BeforeEach
    void setUp() {
        programService = new ProgramService(programRepository);
        program = new Program(1L, "Ingeniería de Sistemas", "IS-01");
    }

    @Test
    void getAll_shouldReturnPageResponse_whenProgramsExist() {
        Page<Program> page = new PageImpl<>(List.of(program));
        when(programRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<ProgramResponse> response = programService.getAll(0, 10);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).code()).isEqualTo("IS-01");
    }

    @Test
    void getById_shouldReturnProgram_whenExists() {
        when(programRepository.findById(1L)).thenReturn(Optional.of(program));

        ProgramResponse response = programService.getById(1L);

        assertThat(response.name()).isEqualTo("Ingeniería de Sistemas");
    }

    @Test
    void getById_shouldThrowResourceNotFound_whenProgramDoesNotExist() {
        when(programRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldSaveProgram_whenCodeIsUnique() {
        ProgramRequest request = new ProgramRequest("Ingeniería de Sistemas", "IS-01");
        when(programRepository.existsByCode("IS-01")).thenReturn(false);
        when(programRepository.save(any(Program.class))).thenReturn(program);

        ProgramResponse response = programService.create(request);

        assertThat(response.code()).isEqualTo("IS-01");
        verify(programRepository, times(1)).save(any(Program.class));
    }

    @Test
    void create_shouldThrowDuplicateResource_whenCodeAlreadyExists() {
        ProgramRequest request = new ProgramRequest("Ingeniería de Sistemas", "IS-01");
        when(programRepository.existsByCode("IS-01")).thenReturn(true);

        assertThatThrownBy(() -> programService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(programRepository, never()).save(any(Program.class));
    }

    @Test
    void update_shouldUpdateProgram_whenCodeIsUnchanged() {
        ProgramRequest request = new ProgramRequest("Ingeniería de Sistemas Actualizada", "IS-01");
        when(programRepository.findById(1L)).thenReturn(Optional.of(program));
        when(programRepository.save(any(Program.class))).thenReturn(program);

        ProgramResponse response = programService.update(1L, request);

        assertThat(response).isNotNull();
        verify(programRepository, never()).existsByCode("IS-01");
        verify(programRepository, times(1)).save(any(Program.class));
    }

    @Test
    void update_shouldThrowDuplicateResource_whenNewCodeAlreadyExists() {
        ProgramRequest request = new ProgramRequest("Ingeniería de Sistemas", "IS-02");
        when(programRepository.findById(1L)).thenReturn(Optional.of(program));
        when(programRepository.existsByCode("IS-02")).thenReturn(true);

        assertThatThrownBy(() -> programService.update(1L, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(programRepository, never()).save(any(Program.class));
    }

    @Test
    void delete_shouldRemoveProgram_whenExists() {
        when(programRepository.findById(1L)).thenReturn(Optional.of(program));

        programService.delete(1L);

        verify(programRepository, times(1)).delete(program);
    }

    @Test
    void delete_shouldThrowResourceNotFound_whenProgramDoesNotExist() {
        when(programRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}