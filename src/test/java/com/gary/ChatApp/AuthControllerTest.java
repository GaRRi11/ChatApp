//package com.gary.ChatApp;
//
//import com.gary.ChatApp.domain.model.user.User;
//import com.gary.ChatApp.domain.repository.UserRepository;
//import com.gary.ChatApp.domain.service.user.UserService;
//import com.gary.ChatApp.web.controller.AuthController;
//import com.gary.ChatApp.web.dto.AuthDto;
//import com.gary.ChatApp.web.dto.LoginResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.util.Optional;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//public class AuthControllerTest {
//
//    private MockMvc mockMvc;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private UserService userService;
//
//    @InjectMocks
//    private AuthController authController;
//
//    private ObjectMapper objectMapper = new ObjectMapper();
//
//    @BeforeEach
//    void setup() {
//        MockitoAnnotations.openMocks(this);
//        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
//    }
//
//    @Test
//    void registerSuccess() throws Exception {
//        AuthDto request = new AuthDto("testuser", "password");
//
//        when(userRepository.findByName("testuser")).thenReturn(Optional.empty());
//        when(userService.register("testuser", "password")).thenReturn("User registered");
//
//        mockMvc.perform(post("/api/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(content().string("User registered"));
//
//        verify(userRepository).findByName("testuser");
//        verify(userService).register("testuser", "password");
//    }
//
//    @Test
//    void registerDuplicateUsername() throws Exception {
//        AuthDto request = new AuthDto("testuser", "password");
//
//        when(userRepository.findByName("testuser")).thenReturn(Optional.of(new User()));
//
//        mockMvc.perform(post("/api/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isConflict());
//    }
//
//    @Test
//    void loginSuccess() throws Exception {
//        AuthDto request = new AuthDto("testuser", "password");
//        User user = new User();
//        user.setUsername("testuser");
//        user.setPassword("encodedPassword");
//
//        when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));
//        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
//        when(userService.login("testuser", "password")).thenReturn(new LoginResponse("token", "refreshToken"));
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").value("token"))
//                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
//    }
//
//    @Test
//    void loginInvalidCredentials() throws Exception {
//        AuthDto request = new AuthDto("testuser", "wrongpassword");
//        User user = new User();
//        user.setUsername("testuser");
//        user.setPassword("encodedPassword");
//
//        when(userRepository.findByName("testuser")).thenReturn(Optional.of(user));
//        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);
//
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isUnauthorized());
//    }
//}
