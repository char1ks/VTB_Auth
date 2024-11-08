package com.example.VTB_Hackathon.Service;

import com.example.VTB_Hackathon.Model.Auth_type;
import com.example.VTB_Hackathon.Repository.auth_type_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class auth_type_service {

    @Autowired
    private auth_type_Repository operations;

    public Auth_type getByEmail(String email){
        return operations.findByEmail(email).orElse(null);
    }

    public void saveAuth(Auth_type authType){
        operations.save(authType);
    }

}
