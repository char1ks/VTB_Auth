package com.example.VTB_Hackathon.Controller;

import com.example.VTB_Hackathon.Model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/vk/api/tokens")
@CrossOrigin
public class TokenController {

    @PostMapping
    public ResponseEntity<HttpStatus> receiveTokens(@RequestBody TokenData tokenData) {
        UserActor userActor=new UserActor(tokenData.getUser_id(),tokenData.getAccess_token());
        System.out.println(userActor.getId());
        RestTemplate restTemplate=new RestTemplate();
        String URL="https://api.vk.com/method/users.get?user_ids="+userActor.getId()+"&fields=bdate&access_token="+userActor.getAccessToken()+"&v=5.199 HTTP/1.1";
        String response=restTemplate.postForObject(URL,"", String.class);
        System.out.println(response);
        return ResponseEntity.ok(HttpStatus.OK); // Перенаправление на страницу ошибки или другое действие
    }

}
