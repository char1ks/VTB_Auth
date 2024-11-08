package com.example.VTB_Hackathon.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "User_info_about")
public class UserInfoAbout {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id")
    private int userId; // Изменено с user_id на userId

    @Column(name = "ip")
    private String ip;

    @Column(name = "geolocation")
    private String geolocation;

    @Column(name = "last_in")
    private String last_in;

    public UserInfoAbout() {}

    public UserInfoAbout(int userId, String ip) {
        this.userId = userId; // Изменено с user_id на userId
        this.ip = ip;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() { // Изменено с getUser_id на getUserId
        return userId;
    }

    public void setUserId(int userId) { // Изменено с setUser_id на setUserId
        this.userId = userId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(String geolocation) {
        this.geolocation = geolocation;
    }

    public String getLast_in() {
        return last_in;
    }

    public void setLast_in(String last_in) {
        this.last_in = last_in;
    }

    @Override
    public String toString() {
        return "UserInfoAbout{" +
                "id=" + id +
                ", userId=" + userId +
                ", ip='" + ip + '\'' +
                ", geolocation='" + geolocation + '\'' +
                ", last_in='" + last_in + '\'' +
                '}';
    }
}