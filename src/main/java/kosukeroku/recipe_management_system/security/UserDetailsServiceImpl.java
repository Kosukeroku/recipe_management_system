package kosukeroku.recipe_management_system.security;

import kosukeroku.recipe_management_system.exception.InvalidCredentialsException;
import kosukeroku.recipe_management_system.model.User;
import kosukeroku.recipe_management_system.repository.UserRepository;
import kosukeroku.recipe_management_system.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        return new UserPrincipal(user);
    }
}