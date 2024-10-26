package com.welcome.login.config;

import com.welcome.login.util.Jwt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import com.welcome.login.service.OAuthUserService;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableWebMvc
public class SecurityConfig {

    private final OAuthUserService oAuthUserService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${compiler.url}")
    private String compilerUrl;

    @Value("${github.email.api}")
    private String githubEmailApi;

    @Value("${allowed.origins}")
    private List<String> allowedOrigins;

    public SecurityConfig(OAuthUserService oAuthUserService, OAuth2AuthorizedClientService authorizedClientService) {
        this.oAuthUserService = oAuthUserService;
        this.authorizedClientService = authorizedClientService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth/github").permitAll()
                        .requestMatchers("/oauth").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuthUserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                            OAuth2User oAuth2User = oauthToken.getPrincipal();

                            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                                    oauthToken.getAuthorizedClientRegistrationId(),
                                    oauthToken.getName()
                            );

                            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                            String email = oAuth2User.getAttribute("email");

                            if (email == null && accessToken != null) {
                                email = fetchGitHubEmail(accessToken.getTokenValue());
                            }

                            if (email != null) {
                                String token = Jwt.generateToken(email);
                                response.setHeader("Authorization", "Bearer " + token);
                                response.sendRedirect(compilerUrl + "?token=" + token);
                            } else {
                                response.sendRedirect(compilerUrl + "?error=email_not_found");
                            }
                        })
                );
        return http.build();
    }

    private String fetchGitHubEmail(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "token " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                githubEmailApi, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {}
        );

        List<Map<String, Object>> emails = response.getBody();
        if (emails != null) {
            for (Map<String, Object> emailEntry : emails) {
                if (Boolean.TRUE.equals(emailEntry.get("primary")) && Boolean.TRUE.equals(emailEntry.get("verified"))) {
                    return (String) emailEntry.get("email");
                }
            }
        }
        return null;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
