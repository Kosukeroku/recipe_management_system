package kosukeroku.recipe_management_system.controller;

import jakarta.validation.Valid;
import kosukeroku.recipe_management_system.dto.LoginJwtResponseDto;
import kosukeroku.recipe_management_system.dto.LoginRequestDto;
import kosukeroku.recipe_management_system.dto.RegistrationRequestDto;
import kosukeroku.recipe_management_system.dto.RegistrationResponseDto;
import kosukeroku.recipe_management_system.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDto> register(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {
        RegistrationResponseDto registrationResponseDto = authenticationService.register(registrationRequestDto);
        return ResponseEntity.ok(registrationResponseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginJwtResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginJwtResponseDto loginJwtResponseDto = authenticationService.login(loginRequestDto);
        return ResponseEntity.ok(loginJwtResponseDto);
    }
}
