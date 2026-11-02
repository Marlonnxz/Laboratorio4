package co.edu.uptc.students.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uptc.students.dto.PageResponse;
import co.edu.uptc.students.dto.ProgramRequest;
import co.edu.uptc.students.dto.ProgramResponse;
import co.edu.uptc.students.entity.Program;
import co.edu.uptc.students.exception.DuplicateResourceException;
import co.edu.uptc.students.exception.ResourceNotFoundException;
import co.edu.uptc.students.repository.ProgramRepository;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;

    public ProgramService(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProgramResponse> getAll(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<Program> page = programRepository.findAll(pageable);

        List<ProgramResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ProgramResponse getById(Long id) {
        return toResponse(findProgramOrThrow(id));
    }

    @Transactional
    public ProgramResponse create(ProgramRequest request) {
        if (programRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Program with code " + request.code() + " already exists");
        }

        Program program = new Program(null, request.name(), request.code());
        return toResponse(programRepository.save(program));
    }

    @Transactional
    public ProgramResponse update(Long id, ProgramRequest request) {
        Program program = findProgramOrThrow(id);

        if (!program.getCode().equals(request.code()) && programRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Program with code " + request.code() + " already exists");
        }

        program.setName(request.name());
        program.setCode(request.code());

        return toResponse(programRepository.save(program));
    }

    @Transactional
    public void delete(Long id) {
        programRepository.delete(findProgramOrThrow(id));
    }

    private Program findProgramOrThrow(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program with id " + id + " was not found"));
    }

    private ProgramResponse toResponse(Program program) {
        return new ProgramResponse(program.getId(), program.getName(), program.getCode());
    }
}