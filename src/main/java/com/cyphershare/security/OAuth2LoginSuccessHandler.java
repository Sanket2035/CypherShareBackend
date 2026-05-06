package com.cyphershare.security;

import com.cyphershare.model.SubscriptionTier;
import com.cyphershare.model.User;
import com.cyphershare.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        
        if (email == null) {
            response.sendRedirect("/?error=oauth2_missing_email");
            return;
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setPassword(""); // Empty password since OAuth2 managed
            user.setTier(SubscriptionTier.BASIC);
            userRepository.save(user);
        }

        String token = jwtUtils.generateJwtToken(email);

        // Redirect back to frontend
        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null || frontendUrl.isEmpty()) {
            frontendUrl = "http://localhost:5173";
        }
        
        String targetUrl = frontendUrl + "/oauth2/redirect?token=" + token + 
                           "&email=" + email + "&tier=" + user.getTier().name();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
