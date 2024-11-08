package com.example.VTB_Hackathon.Repository;

import com.example.VTB_Hackathon.Model.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface userRepository extends JpaRepository<user,Integer> {
    Optional<user> findByEmail(String email);
}
