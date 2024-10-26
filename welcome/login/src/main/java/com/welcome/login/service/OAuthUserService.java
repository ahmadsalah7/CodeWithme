package com.welcome.login.service;

import com.welcome.login.entity.User;
import com.welcome.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OAuthUserService extends DefaultOAuth2UserService {

    private UserRepository userRepository;

    @Value("${github.email.api}")
    private  String githubEmailApi;

    @Value("${github.user.api}")
    private  String githubUserApi;

    public OAuthUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("login");
        String providerId = oAuth2User.getAttribute("sub");
        String providerType = userRequest.getClientRegistration().getRegistrationId();

        if (providerType.equals("github")) {
            if (email == null) {
                email = fetchGitHubEmail(userRequest);
            }
            if (username == null) {
                username = fetchGitHubUsername(userRequest);
            }
        }
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setProviderId(providerId);
            newUser.setProviderType(providerType);

            userRepository.save(newUser);
            System.out.println("New user saved: " + email);
        }else {
            System.out.println("User already exists: " + email);
        }
        return oAuth2User ;
    }


    public  String fetchGitHubEmail(OAuth2UserRequest userRequest) {
        RestTemplate restTemplate = new RestTemplate();
        String email = null;

        String uri = githubEmailApi;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "token " + userRequest.getAccessToken().getTokenValue());
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                uri, HttpMethod.GET, entity, new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> emails = response.getBody();
        if (emails != null) {
            for (Map<String, Object> emailEntry : emails) {
                if (Boolean.TRUE.equals(emailEntry.get("primary")) && Boolean.TRUE.equals(emailEntry.get("verified"))) {
                    email = (String) emailEntry.get("email");
                    break;
                }
            }
        }
        return email;
    }

    private String fetchGitHubUsername(OAuth2UserRequest userRequest) {
        RestTemplate restTemplate = new RestTemplate();

        String uri = githubUserApi;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "token " + userRequest.getAccessToken().getTokenValue());
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        Map<String, Object> userProfile = restTemplate.exchange(
                uri, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String, Object>>() {}
        ).getBody();

        return userProfile != null ? (String) userProfile.get("login") : null;
    }

}
