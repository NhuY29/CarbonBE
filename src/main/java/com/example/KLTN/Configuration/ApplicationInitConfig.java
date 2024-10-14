package com.example.KLTN.Configuration;

import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Reponsitory.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("superadmin").isEmpty()) {
                UserEntity user = UserEntity.builder()
                        .username("superadmin")
                        .password(passwordEncoder.encode("superadmin"))
                        .roles("ROLE_SUPERADMIN")
                        .firstname("Ý")
                        .lastname("Nguyễn")
                        .status(true)
                        .build();

                userRepository.save(user);
            }
        };
    }
}
