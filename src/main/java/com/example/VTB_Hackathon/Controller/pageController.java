package com.example.VTB_Hackathon.Controller;

import com.example.VTB_Hackathon.DTO.google_qr;
import com.example.VTB_Hackathon.Model.Auth_type;
import com.example.VTB_Hackathon.Model.UserInfoAbout;
import com.example.VTB_Hackathon.Model.user;
import com.example.VTB_Hackathon.Security.userDetails;
import com.example.VTB_Hackathon.Service.IPUsers_Service;
import com.example.VTB_Hackathon.Service.auth_type_service;
import com.example.VTB_Hackathon.Service.userService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequestMapping("/page")
@CrossOrigin
public class pageController {

    @Autowired
    private userService operations;

    private RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private auth_type_service operationsAuth;

    @Autowired
    private IPUsers_Service operationsIP;

    @GetMapping("/auth")
    private String authPage() {
        return "authPage";
    }

    @GetMapping("/log_in_by_socials")
    private String log_in_by_socials(HttpServletResponse response) {
        return "loginBySocials";
    }

    @GetMapping("/writeNeedDatas")
    private String writeDatas(Model model,
                              @RequestParam String first_name,
                              @RequestParam String last_name,
                              @RequestParam String email,
                              @RequestParam String birthdate,
                              @RequestParam String nickname) {
        user user = new user();
        user.setFirst_name(first_name);
        user.setLast_name(last_name);
        user.setEmail(email);
        user.setBirth_date(birthdate); // Преобразуйте строку в LocalDate
        user.setNickname(nickname);
        user.setAbout_user("");

        model.addAttribute("first_name", user.getFirst_name());
        model.addAttribute("last_name", user.getLast_name());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("birth_date", user.getBirth_date());
        model.addAttribute("nickname", user.getNickname());
        model.addAttribute("about_user", user.getAbout_user());

        return "writeNeedDatas";
    }

    @ResponseBody
    @GetMapping("/real-ip")
    public String getRealIP(HttpServletRequest request) {
        Map<String, Object> response = restTemplate.getForObject("http://ip-api.com/json/", Map.class);
        return (String) response.get("query");
    }

    @GetMapping("/mainPage")
    private String mainPage() {
        Map<String, Object> response = restTemplate.getForObject("http://ip-api.com/json/", Map.class);


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = null;
        user authUser = null;
        if (authentication.getPrincipal() instanceof DefaultOidcUser) {
            DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
            email = oidcUser.getEmail();
            authUser = operations.getByEmail(email);
        } else if (authentication.getPrincipal() instanceof userDetails) {
            userDetails userDetails = (userDetails) authentication.getPrincipal();
            email = userDetails.getUser().getEmail();
            authUser = userDetails.getUser();
        }


        UserInfoAbout infoAbout = operationsIP.getByUserId(Math.toIntExact(authUser.getId()));
        infoAbout.setIp((String) response.get("query"));
        infoAbout.setGeolocation((String) response.get("regionName"));
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = currentTime.format(formatter);
        infoAbout.setLast_in(formattedTime);

        operationsIP.saveIp(infoAbout);

        return "UserAlsoHere";
    }

    @GetMapping("/personal_account")
    private String personal_account(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = null;
            user authUser = null;
            Auth_type authType = null;
            if (authentication.getPrincipal() instanceof DefaultOidcUser) {
                DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
                email = oidcUser.getEmail();
                authUser = operations.getByEmail(email);
            } else if (authentication.getPrincipal() instanceof userDetails) {
                userDetails userDetails = (userDetails) authentication.getPrincipal();
                email = userDetails.getUser().getEmail();
                authUser = userDetails.getUser();
            }
            if (authUser != null) {
                model.addAttribute("user", authUser);
                authType = operationsAuth.getByEmail(authUser.getEmail());
                if (authType != null) {
                    model.addAttribute("QrCodeEnabled", "QR-code".equals(authType.getQr_code()));
                    model.addAttribute("VisualPatternEnabled", authType.getVisual_pattern().length != 0);
                    model.addAttribute("TappingEnabled", authType.getTapping() != null && !authType.getTapping().isEmpty());
                } else {
                    model.addAttribute("QrCodeEnabled", false);
                    model.addAttribute("VisualPatternEnabled", false);
                    model.addAttribute("TappingEnabled", false);
                    System.out.println("Auth type not found.");
                }
            }
        }
        return "personal_account"; // Убедитесь, что возвращаете правильное имя шаблона
    }

    @GetMapping("/qr-code")
    private String qrCode(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        google_qr account = new google_qr();
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:80/FA/googleAuthenticated"; // Замените на нужный URL

        if (authentication != null && authentication.isAuthenticated()) {
            String email = null;

            if (authentication.getPrincipal() instanceof DefaultOidcUser) {
                DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
                email = oidcUser.getEmail();
            } else if (authentication.getPrincipal() instanceof userDetails) {
                userDetails userDetails = (userDetails) authentication.getPrincipal();
                email = userDetails.getUser().getEmail();
            }

            if (email != null) {
                account.setAccount(email);
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, account, String.class);
                String response = responseEntity.getBody();
                model.addAttribute("urlToQR", response);
            }
        }
        return "QR_code_page"; // Возвращаем сообщение об ошибке
    }

    @GetMapping("/visual_pattern")
    private String visual_pattern() {
        return "visual_pattern_page";
    }

    @GetMapping("/tapping")
    private String tapping() {
        return "tappingPage";
    }
}