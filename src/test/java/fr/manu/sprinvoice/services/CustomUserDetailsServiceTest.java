package fr.manu.sprinvoice.services;

import fr.manu.sprinvoice.models.Role;
import fr.manu.sprinvoice.models.User;
import fr.manu.sprinvoice.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * TYPE : Test unitaire de service (Mockito, aucun contexte Spring)
 *
 * Vérifie que CustomUserDetailsService charge correctement un utilisateur
 * depuis la base de données et lève l'exception appropriée si introuvable.
 * Ce service est utilisé par Spring Security pour l'authentification.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    /**
     * loadUserByUsername() avec un nom d'utilisateur existant doit retourner
     * l'objet User complet (qui implémente UserDetails).
     */
    @Test
    void loadUserByUsername_found_returnsUser() {
        Role role = new Role();
        role.setName("ROLE_ADMIN");

        User user = new User();
        user.setUsername("admin");
        user.setPassword("{bcrypt}hashed");
        user.setRole(role);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        var result = customUserDetailsService.loadUserByUsername("admin");

        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_ADMIN");
    }

    /**
     * loadUserByUsername() avec un nom d'utilisateur inconnu doit lever
     * UsernameNotFoundException (requis par l'interface UserDetailsService).
     */
    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("inconnu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("inconnu"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
