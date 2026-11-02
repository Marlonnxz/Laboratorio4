package co.edu.uptc.auth.service;

import co.edu.uptc.auth.dto.AuthResponse;
import co.edu.uptc.auth.dto.LoginRequest;
import co.edu.uptc.auth.dto.RegisterRequest;
import co.edu.uptc.auth.entity.User;
import co.edu.uptc.auth.exception.BadCredentialsException;
import co.edu.uptc.auth.exception.UserAlreadyExistsException;
import co.edu.uptc.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                jwtService.getExpirationMs(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStudentId()
        );
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("El nombre de usuario ya está registrado");
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                request.getRole(),
                request.getStudentId()
        );

        user = userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                jwtService.getExpirationMs(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStudentId()
        );
    }
}

