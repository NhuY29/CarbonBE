package com.example.KLTN.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    CustomJWTDecoder customJWTDecoder;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request -> request
                .requestMatchers(HttpMethod.POST, "/user/register", "/auth/**","/wallets/create-wallet","/client/sendmail","/wallet/airdrop","/wallet/transfer","/project/create").permitAll()
                .requestMatchers(HttpMethod.GET, "/user/gmail","/wallet/info","/wallet/transactions","/user/getusername","/project/list","/common-categories").permitAll()
                .requestMatchers(HttpMethod.PUT, "/user/**/status","/common-categories/**","/measurementData/update/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/user/pagination/**","/common-categories/**","/commonParentChild/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/user/delete/**","/project/delete/**").permitAll()
                .anyRequest().authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer ->
                        jwtConfigurer.decoder(customJWTDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())));

        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return converter;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            web.ignoring().requestMatchers(HttpMethod.GET, "/user/gmail","/user/getAll","/user/getAll2","/user/getEmail/**","/user/pagination/**","/user/pagination2","/wallets/get/**","/project/download/**","/image/get-by-url/**");
            web.ignoring().requestMatchers(HttpMethod.POST, "/user/register", "/auth/","/client/sendmail","/seller/create","/buyer/create","/wallet/create","/wallet/airdrop");
            web.ignoring().requestMatchers(HttpMethod.PUT,"/user/**","/project/update/**","common-categories/**","/commonParentChild/**","/measurementData/update/**");
            web.ignoring().requestMatchers(HttpMethod.DELETE,"/user/delete/**");

        };
    }
}
