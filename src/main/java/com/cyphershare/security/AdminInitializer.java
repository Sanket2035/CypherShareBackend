package com.cyphershare.security;

import com.cyphershare.model.SubscriptionTier;
import com.cyphershare.model.User;
import com.cyphershare.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        String adminPassword = System.getenv("ADMIN_MASTER_PASSWORD");
        if (adminPassword != null && !adminPassword.isEmpty()) {
            String adminEmail = "admin@cyphershare.com";
            User admin = userRepository.findByEmail(adminEmail).orElse(null);
            
            if (admin == null) {
                admin = new User();
                admin.setEmail(adminEmail);
                admin.setTier(SubscriptionTier.BUSINESS);
            }
            
            admin.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);
            System.out.println("Master Admin account synchronized.");
        }
    }
}
