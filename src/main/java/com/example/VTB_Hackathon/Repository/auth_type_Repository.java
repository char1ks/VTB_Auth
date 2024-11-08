package com.example.VTB_Hackathon.Repository;

import com.example.VTB_Hackathon.Model.Auth_type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface auth_type_Repository extends JpaRepository<Auth_type,Integer> {
    Optional<Auth_type> findByEmail(String email);
}
