import com.javacraftacademy.userservice.model.dto.request.LoginRequest;
import com.javacraftacademy.userservice.model.dto.request.RegisterRequest;
import com.javacraftacademy.userservice.model.dto.request.PasswordResetRequest;
import com.javacraftacademy.userservice.model.dto.response.AuthResponse;
import com.javacraftacademy.userservice.model.dto.response.ApiResponse;
import com.javacraftacademy.userservice.service.AuthService;
import com.javacraftacademy.userservice.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse authResponse = authService.register(registerRequest);
            ApiResponse<AuthResponse> response = new ApiResponse<>(
                true, 
                "User registered successfully", 
                authResponse
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.login(loginRequest);
            ApiResponse<AuthResponse> response = new ApiResponse<>(
                true, 
                "Login successful", 
                authResponse
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(HttpServletRequest request) {
        try {
            String refreshToken = extractTokenFromRequest(request);
            if (refreshToken == null) {
                ApiResponse<AuthResponse> response = new ApiResponse<>(
                    false, 
                    "Refresh token is required", 
                    null
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            AuthResponse authResponse = authService.refreshToken(refreshToken);
            ApiResponse<AuthResponse> response = new ApiResponse<>(
                true, 
                "Token refreshed successfully", 
                authResponse
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<AuthResponse> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token != null) {
                authService.logout(token);
            }
            
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "Logout successful", 
                null
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) {
        try {
            authService.forgotPassword(email);
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "Password reset email sent successfully", 
                null
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody PasswordResetRequest resetRequest) {
        try {
            authService.resetPassword(resetRequest);
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "Password reset successfully", 
                null
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            ApiResponse<String> response = new ApiResponse<>(
                true, 
                "Email verified successfully", 
                null
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                ApiResponse<Boolean> response = new ApiResponse<>(
                    false, 
                    "Token is required", 
                    false
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            boolean isValid = jwtService.validateToken(token);
            ApiResponse<Boolean> response = new ApiResponse<>(
                true, 
                isValid ? "Token is valid" : "Token is invalid", 
                isValid
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Boolean> response = new ApiResponse<>(
                false, 
                e.getMessage(), 
                false
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}


