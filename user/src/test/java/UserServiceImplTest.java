import com.gary.application.user.UserServiceImpl;
import com.gary.domain.model.user.User;
import com.gary.domain.repository.user.UserRepository;
import com.gary.domain.service.token.RefreshTokenService;
import com.gary.infrastructure.security.JwtTokenUtil;
import com.gary.web.dto.loginResponse.LoginResponseDto;
import com.gary.web.dto.user.UserRequest;
import com.gary.web.dto.user.UserResponse;
import com.gary.web.exception.DuplicateResourceException;
import com.gary.web.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldCreateUser() {
        UserRequest request = new UserRequest("john", "password");
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.register(request);

        assertEquals("john", response.username());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowIfUserExists() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateResourceException.class, () ->
                userService.register(new UserRequest("john", "pass")));
    }

    @Test
    void login_shouldReturnTokens() {
        User user = User.builder()
                .id(1L)
                .username("john")
                .password("encoded")
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(jwtTokenUtil.generateAccessToken(1L, "john")).thenReturn("access");
        when(jwtTokenUtil.generateRefreshToken(1L, "john")).thenReturn("refresh");

        LoginResponseDto response = userService.login(new UserRequest("john", "password"));

        assertEquals("access", response.token());
        assertEquals("refresh", response.refreshToken());
        verify(refreshTokenService).save(1L, "refresh");
    }

    @Test
    void login_shouldThrowForInvalidUsername() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () ->
                userService.login(new UserRequest("john", "password")));
    }

    @Test
    void login_shouldThrowForInvalidPassword() {
        User user = User.builder().username("john").password("encoded").build();
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () ->
                userService.login(new UserRequest("john", "wrong")));
    }

    @Test
    void logout_shouldRevokeAllTokens() {
        User user = User.builder().id(1L).username("john").build();
        userService.logout(user);
        verify(refreshTokenService).revokeAll(1L);
    }

    @Test
    void refreshToken_shouldReturnNewTokens() {
        String oldToken = "refresh";

        when(jwtTokenUtil.validateToken(oldToken)).thenReturn(true);
        when(refreshTokenService.isValid(oldToken)).thenReturn(true);
        when(jwtTokenUtil.extractUserId(oldToken)).thenReturn(1L);
        when(jwtTokenUtil.extractUsername(oldToken)).thenReturn("john");
        when(jwtTokenUtil.generateAccessToken(1L, "john")).thenReturn("newAccess");
        when(jwtTokenUtil.generateRefreshToken(1L, "john")).thenReturn("newRefresh");

        LoginResponseDto response = userService.refreshToken(oldToken);

        assertEquals("newAccess", response.token());
        assertEquals("newRefresh", response.refreshToken());
        verify(refreshTokenService).revoke(oldToken);
        verify(refreshTokenService).save(1L, "newRefresh");
    }

    @Test
    void refreshToken_shouldThrowIfInvalidJwt() {
        when(jwtTokenUtil.validateToken("bad")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.refreshToken("bad"));
    }

    @Test
    void refreshToken_shouldThrowIfTokenRevokedOrExpired() {
        when(jwtTokenUtil.validateToken("refresh")).thenReturn(true);
        when(refreshTokenService.isValid("refresh")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.refreshToken("refresh"));
    }

    @Test
    void searchByUsername_shouldReturnOtherUsers() {
        User user1 = User.builder().id(1L).username("john").build();
        User user2 = User.builder().id(2L).username("johnny").build();

        when(userRepository.findByUsernameContainingIgnoreCase("john"))
                .thenReturn(List.of(user1, user2));

        List<UserResponse> results = userService.searchByUsername("john", 1L);

        assertEquals(1, results.size());
        assertEquals("johnny", results.get(0).username());
    }

    @Test
    void getById_shouldReturnUser() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertTrue(userService.getById(1L).isPresent());
    }

    @Test
    void findAllById_shouldReturnUsers() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAllById(List.of(1L, 2L))).thenReturn(users);
        assertEquals(2, userService.findAllById(List.of(1L, 2L)).size());
    }
}
