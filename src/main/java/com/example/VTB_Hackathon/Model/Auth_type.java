package com.example.VTB_Hackathon.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.awt.image.BufferedImage;

@Entity
@Table(name = "auth_type")
public class Auth_type {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email")
    private String email;

    @Column(name = "qr_code")
    private String Qr_code;

    @Column(name = "visual_pattern",columnDefinition = "BYTEA")
    private byte[] visual_pattern;

    @Column(name = "tapping")
    private String tapping;

    public Auth_type() {}

    public Auth_type(String email, String qr_code, byte[] visual_pattern) {
        this.email = email;
        Qr_code = qr_code;
        this.visual_pattern = visual_pattern;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQr_code() {
        return Qr_code;
    }

    public void setQr_code(String qr_code) {
        Qr_code = qr_code;
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
}
