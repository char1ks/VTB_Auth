package com.example.VTB_Hackathon.Controller;

import com.example.VTB_Hackathon.DTO.google_qr;
import com.example.VTB_Hackathon.Model.Auth_type;
import com.example.VTB_Hackathon.Model.UserInfoAbout;
import com.example.VTB_Hackathon.Model.Utils;
import com.example.VTB_Hackathon.Model.user;
import com.example.VTB_Hackathon.Security.userDetails;
import com.example.VTB_Hackathon.Service.IPUsers_Service;
import com.example.VTB_Hackathon.Service.auth_type_service;
import com.example.VTB_Hackathon.Service.tappingService;
import com.example.VTB_Hackathon.Service.userService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/FA")
public class FA_authenticated {

    @Autowired
    private userService operations;
    @Autowired
    private auth_type_service operationsAuth;
    @Autowired
    private IPUsers_Service operationsIp;
    @Autowired
    private tappingService operationsTapping;

    private final WebClient webClient;

    public FA_authenticated(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://otp-authenticator.p.rapidapi.com").build();
    }

    @PostMapping("/googleAuthenticated")
    public Mono<ResponseEntity<String>> getUrlWithPage(@RequestBody google_qr account) {
        String apiKey = "bd9d960bf9mshd30c2d87659a793p1fb9b6jsn078d41efcb71";
        return webClient.post()
                .uri("/new_v2/")
                .header("X-RapidAPI-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("{}")) // Пустое тело запроса
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody -> {
                    user user = operations.getByEmail(account.getAccount());
                    user.setGmail_qr_secrete_code(responseBody);
                    operations.saveUser(user);
                    if (operationsAuth.getByEmail(user.getEmail()) == null) {
                        Auth_type authType = new Auth_type();
                        authType.setEmail(account.getAccount());
                        authType.setQr_code("QR-code");
                        byte[] visualPattern = new byte[0]; // Или установите его в нужное значение
                        authType.setVisual_pattern(visualPattern);
                        operationsAuth.saveAuth(authType);
                    } else {
                        Auth_type authType = operationsAuth.getByEmail(account.getAccount());
                        authType.setQr_code("QR-code");
                        operationsAuth.saveAuth(authType);
                    }
                    return webClient.post()
                            .uri("/enroll/")
                            .header("X-RapidAPI-Key", apiKey)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .body(BodyInserters.fromFormData("secret", responseBody)
                                    .with("account", account.getAccount())
                                    .with("issuer", "VTB_HACK"))
                            .retrieve()
                            .bodyToMono(String.class);
                })
                .map(response2 -> ResponseEntity.ok(response2))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body("Ошибка: " + e.getMessage())));
    }

    @PostMapping("/check_correct")
    private boolean responseToCheckCorrect(@RequestBody String auth_code) {
        RestTemplate restTemplate = new RestTemplate();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        user user = new user();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof DefaultOidcUser) {
                DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
                String email = oidcUser.getEmail();
                user authUser = operations.getByEmail(email);
                user = authUser;
            } else if (authentication.getPrincipal() instanceof userDetails) {
                userDetails userDetails = (userDetails) authentication.getPrincipal();
                user = userDetails.getUser();
            }
        }
        String secret = user.getGmail_qr_secrete_code();
        if (auth_code.replaceAll("\"", "").equals(Utils.getTOTPCode(secret))) {
            int userId = Math.toIntExact(user.getId());
            UserInfoAbout current = operationsIp.getByUserId(userId);
            current.setIp(restTemplate.getForObject("http://localhost:80/page/real-ip", String.class));
            operationsIp.updateIp(current.getId(), current);
            return true;
        } else {
            return false;
        }
    }

    @PostMapping("/visual_pattern_check")
    public ResponseEntity<HttpStatus> visualPatternCheck(@RequestBody byte[] picture) {
        RestTemplate restTemplate = new RestTemplate();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;
        user user;
        if (authentication.getPrincipal() instanceof DefaultOidcUser) {
            DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
            email = oidcUser.getAttribute("email");
            user = operations.getByEmail(email);
        } else if (authentication.getPrincipal() instanceof userDetails) {
            userDetails details = (userDetails) authentication.getPrincipal();
            user = details.getUser();
            email = user.getEmail();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            long inputImageHash = ImageUtils.averageHash(picture);
            long userPatternHash = ImageUtils.averageHash(user.getVisual_pattern());
            double similarity = ImageUtils.compareHashes(inputImageHash, userPatternHash);
            System.out.println(similarity);
            if (similarity > 0.8) {
                int userId = Math.toIntExact(user.getId());
                UserInfoAbout current = operationsIp.getByUserId(userId);
                current.setIp(restTemplate.getForObject("http://localhost:80/page/real-ip", String.class));
                operationsIp.updateIp(current.getId(), current);
                return ResponseEntity.ok(HttpStatus.OK);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (IOException e) {
            return ResponseEntity.ok(HttpStatus.BAD_REQUEST);
        }
    }

    public static class ImageUtils {
        public static long averageHash(byte[] imageBytes) throws IOException {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            BufferedImage resizedImg = resizeImage(img, 8, 8);
            long hash = 0;
            for (int y = 0; y < resizedImg.getHeight(); y++) {
                for (int x = 0; x < resizedImg.getWidth(); x++) {
                    int pixel = resizedImg.getRGB(x, y);
                    int gray = (int) (0.2126 * ((pixel >> 16) & 0xff) + 0.7152 * ((pixel >> 8) & 0xff) + 0.0722 * (pixel & 0xff));
                    if (gray > 128) {
                        hash |= (1L << (x + y * resizedImg.getWidth()));
                    }
                }
            }
            return hash;
        }

        private static BufferedImage resizeImage(BufferedImage img, int newW, int newH) {
            BufferedImage resizedImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImg.createGraphics();
            g.drawImage(img, 0, 0, newW, newH, null);
            g.dispose();
            return resizedImg;
        }

        public static double compareHashes(long hash1, long hash2) {
            long xor = hash1 ^ hash2;
            int differingBits = Long.bitCount(xor);
            return 1.0 - (differingBits / 64.0);

        }
    }


    @PostMapping("/visual_pattern_auth")
    public ResponseEntity<HttpStatus> visualPatternAuth(@RequestBody byte[] picture) {
        if (picture == null || picture.length == 0) {
            return ResponseEntity.badRequest().body(HttpStatus.BAD_REQUEST);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;
        user user;
        if (authentication.getPrincipal() instanceof DefaultOidcUser) {
            DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
            email = oidcUser.getAttribute("email");
            user authUser = operations.getByEmail(email);
            user = authUser;
        } else if (authentication.getPrincipal() instanceof userDetails) {
            userDetails details = (userDetails) authentication.getPrincipal();
            user = details.getUser();
            email = user.getEmail();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Auth_type authType = operationsAuth.getByEmail(email);
        if (authType == null) {
            authType = new Auth_type();
            authType.setEmail(email);
        }
        authType.setVisual_pattern(picture);
        user.setVisual_pattern(authType.getVisual_pattern());

        operations.saveUser(user);
        operationsAuth.saveAuth(authType);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/tapping_auth")
    public ResponseEntity<HttpStatus> tapping_auth(@RequestBody byte[] audio) {
        System.out.println(123);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;
        user user;

        if (authentication.getPrincipal() instanceof DefaultOidcUser) {
            DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
            email = oidcUser.getAttribute("email");
            user = operations.getByEmail(email);
        } else if (authentication.getPrincipal() instanceof userDetails) {
            userDetails details = (userDetails) authentication.getPrincipal();
            user = details.getUser();
            email = user.getEmail();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String tapping = operationsTapping.analyzeSound(audio);
        Auth_type authType = operationsAuth.getByEmail(email);
        if (authType == null) {
            authType = new Auth_type();
            authType.setEmail(email);
        }
        authType.setTapping(tapping);
        user.setTapping(tapping);
        operations.saveUser(user);
        operationsAuth.saveAuth(authType);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/tapping_check")
    public ResponseEntity<HttpStatus> tapping(@RequestBody byte[] audio) {
        RestTemplate restTemplate = new RestTemplate();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        user user = new user();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof DefaultOidcUser) {
                DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
                String email = oidcUser.getEmail();
                user authUser = operations.getByEmail(email);
                user = authUser;
            } else if (authentication.getPrincipal() instanceof userDetails) {
                userDetails userDetails = (userDetails) authentication.getPrincipal();
                user = userDetails.getUser();
            }
        }
        String current = operationsTapping.analyzeSound(audio);
        String tapping = user.getTapping();
        System.out.println("Что получилось:" + current + ".Что надо:" + tapping);
        int similarity = 0;
        double similarityPercentage = 0;
        if (current != null && tapping != null) {
            int maxLength = Math.max(current.length(), tapping.length());

            for (int i = 0; i < maxLength; i++) {
                // Проверяем, что индекс в пределах длины обеих строк
                char currentChar = (i < current.length()) ? current.charAt(i) : '\0'; // Используем '\0' или другой символ по умолчанию
                char tappingChar = (i < tapping.length()) ? tapping.charAt(i) : '\0'; // Используем '\0' или другой символ по умолчанию
                System.out.println("Символы:" + currentChar + " " + tappingChar);
                if (currentChar == tappingChar) {
                    similarity++;
                }
            }

            System.out.println("Схожесть " + similarity + ".Максимальная длина " + maxLength);
            similarityPercentage = (double) similarity / maxLength * 100;
            System.out.println("Вероятность схожести: " + similarityPercentage + "%");
        } else {
            System.out.println("Одна из строк равна null");
        }
        if (similarityPercentage >= 65) {
            int userId = Math.toIntExact(user.getId());
            UserInfoAbout currentIP = operationsIp.getByUserId(userId);
            currentIP.setIp(restTemplate.getForObject("http://localhost:80/page/real-ip", String.class));
            operationsIp.updateIp(currentIP.getId(), currentIP);
            return ResponseEntity.ok(HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}