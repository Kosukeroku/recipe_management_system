package kosukeroku.recipe_management_system.service;

import kosukeroku.recipe_management_system.dto.RegistrationRequestDto;
import kosukeroku.recipe_management_system.dto.RegistrationResponseDto;
import kosukeroku.recipe_management_system.exception.EmailAlreadyExistsException;
import kosukeroku.recipe_management_system.model.User;
import kosukeroku.recipe_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationResponseDto register(RegistrationRequestDto registrationRequestDto) {
        if (userRepository.existsByEmail(registrationRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException(registrationRequestDto.getEmail());
        }

        User newUser = new User();
        newUser.setEmail(registrationRequestDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(registrationRequestDto.getPassword()));
        userRepository.save(newUser);
        return new RegistrationResponseDto(newUser);
    }


}
