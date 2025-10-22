package kosukeroku.recipe_management_system.service;

import kosukeroku.recipe_management_system.dto.LoginJwtResponseDto;
import kosukeroku.recipe_management_system.dto.LoginRequestDto;
import kosukeroku.recipe_management_system.dto.RegistrationRequestDto;
import kosukeroku.recipe_management_system.dto.RegistrationResponseDto;
import kosukeroku.recipe_management_system.exception.EmailAlreadyExistsException;
import kosukeroku.recipe_management_system.exception.InvalidCredentialsException;
import kosukeroku.recipe_management_system.model.User;
import kosukeroku.recipe_management_system.repository.UserRepository;
import kosukeroku.recipe_management_system.security.JwtTokenProvider;
import kosukeroku.recipe_management_system.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

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

    public LoginJwtResponseDto login(LoginRequestDto loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword()
                    )
            );

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            String token = jwtTokenProvider.generateToken(userPrincipal);

            return new LoginJwtResponseDto(token, userPrincipal.getUsername());

        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }
    }
}
