package portal.expenses.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import portal.expenses.config.ApplicationConfig;
import portal.expenses.config.CustomAuthenticationEntryPoint;
import portal.expenses.config.JwtAuthFilter;
import portal.expenses.config.WebSecurityConfig;
import portal.expenses.dto.LoginRequest;
import portal.expenses.dto.LoginResponse;
import portal.expenses.entity.AppUser;
import portal.expenses.repository.UserRepository;
import portal.expenses.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import portal.expenses.util.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class) // Test AuthController
@Import({WebSecurityConfig.class, ApplicationConfig.class, JwtAuthFilter.class, CustomAuthenticationEntryPoint.class}) // Explicitly import your security configurations
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthController authController;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    // We must mock UserDetailsServiceImpl because JwtAuthFilter (a web component)
    // depends on it, and it's not loaded in a @WebMvcTest slice.
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    // AuthController depends on UserRepository directly, so it must be mocked
    // for the WebMvcTest application context to start.
    @MockBean
    private UserRepository userRepository;

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user", "password");
        UserDetails userDetails = new User("user", "password", new ArrayList<>());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        AppUser appUser = new AppUser();
        appUser.setUsername("user");
        appUser.setName("Test User");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("mock-jwt-token");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(appUser));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("user", "wrong-password");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validate_shouldReturnBadRequest_whenAuthorizationHeaderIsMissingBearerPrefix() throws Exception {
        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", "not-a-bearer-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false));
    }

    /*

    public static void main(String args[]){
        System.out.println("display in test");
    }

     */


    @Test
    void validate_shouldReturnOkAndValid_whenTokenIsValid() throws Exception {
        // Arrange
        UserDetails userDetails = new User("user", "password", new ArrayList<>());

        when(jwtUtil.extractUsername("valid-token")).thenReturn("user");
        when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(jwtUtil.validateToken("valid-token", userDetails)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("user"));
    }

    /*

    public static void main(String args[]){
        System.out.println("welcome");
    }

     */

    @Test
    void login_shouldThrowRuntimeException_whenUserNotFoundInRepository() {
        LoginRequest loginRequest = new LoginRequest("unknown", "password");
        UserDetails userDetails = new User("unknown", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("mock-jwt-token");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authController.login(loginRequest));
    }

    @Test
    void validateToken_shouldReturnOkAndInvalid_whenTokenValidationFails() {
        UserDetails userDetails = new User("user", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtUtil.extractUsername("invalid-token")).thenReturn("user");
        when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(jwtUtil.validateToken("invalid-token", userDetails)).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = authController.validateToken("Bearer invalid-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("valid"));
        assertEquals("user", response.getBody().get("username"));
    }

    @Test
    void validateToken_shouldReturnBadRequest_whenHeaderIsNull() {
        ResponseEntity<Map<String, Object>> response = authController.validateToken(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("valid"));
        assertEquals("Missing or invalid Authorization header", response.getBody().get("error"));
    }

    @Test
    void validateToken_shouldReturnBadRequest_whenExceptionOccurs() {
        when(jwtUtil.extractUsername("broken-token")).thenThrow(new RuntimeException("Token parse failed"));

        ResponseEntity<Map<String, Object>> response = authController.validateToken("Bearer broken-token");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("valid"));
        assertEquals("Token parse failed", response.getBody().get("error"));
    }
}