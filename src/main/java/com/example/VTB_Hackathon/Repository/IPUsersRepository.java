package com.example.VTB_Hackathon.Repository;

import com.example.VTB_Hackathon.Model.UserInfoAbout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPUsersRepository extends JpaRepository<UserInfoAbout, Integer> {
    Optional<UserInfoAbout> findByUserId(int userId); // Изменено с findByUser_id на findByUserId
}