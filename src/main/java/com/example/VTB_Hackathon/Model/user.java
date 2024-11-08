package com.example.VTB_Hackathon.Model;

import jakarta.persistence.*;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.Arrays;

@Entity
@Table(name = "users")
public class user {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "first_name", nullable = false, length = 50)
    private String first_name;

    @Column(name = "last_name", nullable = false, length = 50)
    private String last_name;

    @Column(name = "birth_date", nullable = false)
    private String birth_date;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "about", nullable = false, length = 255)
    private String about_user;
    // Конструкторы, геттеры и сеттеры

    @Column(name = "gmail_qr_secrete_code")
    private String gmail_qr_secrete_code;

    @Column(name = "visual_pattern",columnDefinition = "BYTEA")
    private byte[] visual_pattern;

    @Column(name = "tapping")
    private String tapping;

    public user() {
    }

    public user(String nickname, String first_name, String last_name, String birth_date, String password, String email, String gmail_qr_secrete_code, byte[] visualPattern) {
        this.nickname = nickname;
        this.first_name = first_name;
        this.last_name = last_name;
        this.birth_date = birth_date;
        this.password = password;
        this.email = email;
        this.gmail_qr_secrete_code = gmail_qr_secrete_code;
        this.visual_pattern = visualPattern;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(String birth_date) {
        this.birth_date = birth_date;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAbout_user() {
        return about_user;
    }

    public void setAbout_user(String about_user) {
        this.about_user = about_user;
    }

    public String getGmail_qr_secrete_code() {
        return gmail_qr_secrete_code;
    }

    public void setGmail_qr_secrete_code(String gmail_qr_secrete_code) {
        this.gmail_qr_secrete_code = gmail_qr_secrete_code;
    }

    public byte[] getVisual_pattern() {
        return visual_pattern;
    }

    public void setVisual_pattern(byte[] visual_pattern) {
        this.visual_pattern = visual_pattern;
    }

    public String getTapping() {
        return tapping;
    }

    public void setTapping(String tapping) {
        this.tapping = tapping;
    }

    @Override
    public String toString() {
        return "user{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", birth_date='" + birth_date + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", about_user='" + about_user + '\'' +
                '}';
    }
}