package com.example.meetingminutesgenerator;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ---------------- REGISTER PAGE ----------------
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // ---------------- REGISTER USER ----------------
    @PostMapping("/register")
    public String registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session) {

        email = email.trim().toLowerCase();

        User existingUser = userRepository.findByEmail(email);

        if (existingUser != null) {
            return "redirect:/register?exists=true";
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email);
        user.setPassword(password.trim());

        userRepository.saveAndFlush(user);

        // Auto login after registration
        session.setAttribute("loggedInUser", user);

        return "redirect:/";
    }

    // ---------------- LOGIN USER ----------------
    @PostMapping("/login")
    public String loginUser(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session) {

        email = email.trim().toLowerCase();
        password = password.trim();

        User user = userRepository.findByEmail(email);

        if (user != null && password.equals(user.getPassword())) {
            session.setAttribute("loggedInUser", user);
            return "redirect:/";
        }

        return "redirect:/login?error=true";
    }

    // ---------------- LOGOUT ----------------
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}