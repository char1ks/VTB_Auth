package com.example.VTB_Hackathon.Service;

import com.example.VTB_Hackathon.Model.UserInfoAbout;
import com.example.VTB_Hackathon.Repository.IPUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IPUsers_Service {

    @Autowired
    private IPUsersRepository operations;

    public UserInfoAbout getByUserId(int userId){
        return operations.findByUserId(userId).orElse(null);
    }
    public void saveIp(UserInfoAbout userInfoAbout){
        operations.save(userInfoAbout);
    }
    public void updateIp(int id, UserInfoAbout userInfoAbout){
        userInfoAbout.setId(id);
        operations.save(userInfoAbout);
    }
}
