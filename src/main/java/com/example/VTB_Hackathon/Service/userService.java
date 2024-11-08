package com.example.VTB_Hackathon.Service;

import com.example.VTB_Hackathon.Model.user;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import com.example.VTB_Hackathon.Repository.userRepository;

@Service
@Transactional
public class userService {


    private final userRepository userRepository;

    public userService(com.example.VTB_Hackathon.Repository.userRepository userRepository) {
        this.userRepository = userRepository;
    }


    public user getByEmail(String email){
        return userRepository.findByEmail(email).orElse(null);
    }
    public void saveUser(user user){
        userRepository.save(user);
    }

    public user addUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oAuth2User = oauthToken.getPrincipal();

            if (oAuth2User != null) {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                System.out.println("here4");
                String email= (String) attributes.get("email");
                Optional<user> findOrNo = userRepository.findByEmail(email);
                if(findOrNo.isEmpty()) {
                    user user = new user();
                    user.setFirst_name((String) attributes.get("given_name"));
                    user.setLast_name((String) attributes.get("family_name"));
                    user.setEmail((String) attributes.get("email"));// Установите значение по умолчанию, если это необходимо
                    return user;
                }
                return findOrNo.get();
            }
        }
        return new user();
    }
}