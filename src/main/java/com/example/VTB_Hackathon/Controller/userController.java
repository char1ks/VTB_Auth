package com.example.VTB_Hackathon.Controller;

import com.example.VTB_Hackathon.DTO.QuestionAnswer;
import com.example.VTB_Hackathon.Model.Auth_type;
import com.example.VTB_Hackathon.Model.UserInfoAbout;
import com.example.VTB_Hackathon.Model.user;
import com.example.VTB_Hackathon.Security.userDetails;
import com.example.VTB_Hackathon.Service.IPUsers_Service;
import com.example.VTB_Hackathon.Service.LLMControl;
import com.example.VTB_Hackathon.Service.auth_type_service;
import com.example.VTB_Hackathon.Service.userService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
@CrossOrigin
public class userController {

    @Autowired
    private LLMControl llmControl;

    @Autowired
    private userService operations;

    @Autowired
    private IPUsers_Service operationsIP;

    @Autowired
    private auth_type_service operationsAuth;

    @Autowired
    private IPUsers_Service operationsIps;

    @GetMapping("/gmail/auth")
    private String secured(Model model) {
        user user = operations.addUser();
        String password = user.getPassword();
        if (password == null || password.isEmpty()) {
            try {
                return "redirect:/page/writeNeedDatas?first_name=" + URLEncoder.encode(user.getFirst_name() != null ? user.getFirst_name() : "", StandardCharsets.UTF_8.toString()) +
                        "&last_name=" + URLEncoder.encode(user.getLast_name() != null ? user.getLast_name() : "", StandardCharsets.UTF_8.toString()) +
                        "&email=" + URLEncoder.encode(user.getEmail() != null ? user.getEmail() : "", StandardCharsets.UTF_8.toString()) +
                        "&birthdate=" + URLEncoder.encode(user.getBirth_date() != null ? user.getBirth_date() : "", StandardCharsets.UTF_8.toString()) +
                        "&nickname=" + URLEncoder.encode(user.getNickname() != null ? user.getNickname() : "", StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace(); // Обработка исключения
                return "redirect:/error"; // Перенаправление на страницу ошибки или другое действие
            }
        }
        Auth_type authType = operationsAuth.getByEmail(user.getEmail());
        int userId= Math.toIntExact(user.getId());
        UserInfoAbout ip=operationsIps.getByUserId(userId);
        RestTemplate restTemplate=new RestTemplate();
        String currentIp=restTemplate.getForObject("http://localhost:80/page/real-ip",String.class);
        if (authType != null) {
            // Проверяем наличие QR-кода, совпадение IP и наличие визуального паттерна или tapping
            if ("QR-code".equals(authType.getQr_code()) || !ip.getIp().equals(currentIp) ||
                    (authType.getVisual_pattern() != null || authType.getVisual_pattern().length != 0) ||
                    (authType.getTapping() != null || !authType.getTapping().isEmpty())) { // Добавленная проверка на tapping
                model.addAttribute("Qr_code_exist", authType.getQr_code() != null && !authType.getQr_code().isEmpty());
                model.addAttribute("Visual_pattern_exist", authType.getVisual_pattern() != null && authType.getVisual_pattern().length != 0);
                model.addAttribute("Tapping_exist", authType.getTapping() != null && !authType.getTapping().isEmpty()); // Добавлено для tapping
                return "verifyAccount";
            }
        }
        return "redirect:/page/mainPage";
    }
    //AUTH
    @GetMapping("/QRcode_page")
    private String qrCodePage(){
        return "QR-Code_check";
    }
    @GetMapping("/visual_pattern_auth")
    private String visualPatternAuthPage(){
        return "visual_pattern_auth";
    }
    @GetMapping("/tapping_auth")
    private String tappingAuth(){
        return "tappingPage_auth";
    }

    @GetMapping("/AI_Question")
    private String AI_Question(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;
        user user = null;

        if (authentication.getPrincipal() instanceof DefaultOidcUser) {
            DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
            email = oidcUser.getAttribute("email");
            user = operations.getByEmail(email);
        } else if (authentication.getPrincipal() instanceof userDetails) {
            userDetails details = (userDetails) authentication.getPrincipal();
            user = details.getUser();
            email = user.getEmail();
        }

        UserInfoAbout infoAbout = operationsIP.getByUserId(Math.toIntExact(user.getId()));

        String question=llmControl.generate("Сгенерируй мне 1 вопрос на русском языку,который бы позволял проверить,является ли человек,который входит в аккаунт по-астоящему им.ДАВАЙ МНЕ ТОЛЬКО ВОПРОС И НИ СЛОВА БОЛЬШЕ :"+user+".Но все таки постарайс сделать упор на данные о геолокации(например,задать вопрос из какого города он в прошлый раз заешл,но учитывай,что юзер мог входить с ростова,а написано,что он в краснодаре(допускай небольшую погрешность в локациях).Или спрашивай когда (приблизительно)он в последний раз заходил ,допускай погрешности(ЭТО НЕ ЗНАЧИТ,ЧТО ТЫ ДОЛЛЖЕН ГЕНЕРИРОВАТ ЬВОПРОСЫ ТОЛЬКО СВЯЗАННЫЕ С ТЕМ,ЧТО Я ОПИСАЛ,РЕШАЙ САМ!)-"+infoAbout);
        model.addAttribute("Question_to_person",question);

        return "AI_Question_page";
    }
    @PostMapping("/ai_question_check")
    public ResponseEntity<Map<String, String>> checkAiQuestion(@RequestBody QuestionAnswer questionAnswer) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;
        user user = null;
        if (authentication.getPrincipal() instanceof DefaultOidcUser) {
            DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();
            email = oidcUser.getAttribute("email");
            user = operations.getByEmail(email);
        } else if (authentication.getPrincipal() instanceof userDetails) {
            userDetails details = (userDetails) authentication.getPrincipal();
            user = details.getUser();
            email = user.getEmail();
        }
        String question = questionAnswer.getQuestion();
        String answer = questionAnswer.getAnswer();

        UserInfoAbout infoAbout = operationsIP.getByUserId(Math.toIntExact(user.getId()));
        String total = llmControl.generate("Дай мне ответ ТОЛЬКО ДА ИЛИ НЕТ. Юзер ответил на вопрос: " + question + ". Таким образом: " + answer + ". Насколько он прав? Отвечал он по этим данным: " + user+",а также по этим -"+infoAbout);

        System.out.println(total);
        Map<String, String> response = new HashMap<>();
        if (total.toLowerCase().contains("да")) {
            RestTemplate restTemplate=new RestTemplate();
            int userId= Math.toIntExact(user.getId());
            UserInfoAbout userInfoAbout =operationsIps.getByUserId(userId);
            userInfoAbout.setIp(restTemplate.getForObject("http://localhost:80/page/real-ip",String.class));
            operationsIps.saveIp(userInfoAbout);

            response.put("status", "success");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "failure");
            return ResponseEntity.ok(response);
        }
    }
    //-------
    @PostMapping("/new")
    private String newUser(@RequestBody user user){
        operations.saveUser(user);
        RestTemplate restTemplate=new RestTemplate();
        UserInfoAbout userInfoAbout =new UserInfoAbout();
        int userId= Math.toIntExact(user.getId());
        userInfoAbout.setUserId(userId);
        userInfoAbout.setIp(restTemplate.getForObject("http://localhost:80/page/real-ip",String.class));
        operationsIps.saveIp(userInfoAbout);
        return "authPage";
    }
}