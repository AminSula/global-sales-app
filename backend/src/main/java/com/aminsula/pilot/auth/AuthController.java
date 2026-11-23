package com.aminsula.pilot.auth;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aminsula.pilot.security.AuthUser;
import com.aminsula.pilot.security.JwtService;
import com.aminsula.pilot.user.AppUser;
import com.aminsula.pilot.user.AppUserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AppUserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Optional<AppUser> found = users.findByUsernameIgnoreCase(request.getUsername().trim());

        // Same answer for "no such user" and "wrong password", so usernames can't be guessed.
        if (found.isEmpty()
                || !found.get().isActive()
                || !passwordEncoder.matches(request.getPassword(), found.get().getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AppUser user = found.get();
        UserDto dto = new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getRole());
        return ResponseEntity.ok(new LoginResponse(jwtService.createToken(user), dto));
    }

    /** Who am I? Used by the app to check that its saved token is still accepted. */
    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal AuthUser user) {
        return new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getRole());
    }
}
