package com.perpustakaan.model;

public class Member {
    private String memberId; 
    private String pin;
    private String fullName;
    private String major; 
    private String email;
    private String gender;
    private String addres;
    private String phoneNumber;

    public Member(String memberId, String pin, String fullName, String major, String email, String gender, String addres, String phoneNumber) {
        this.memberId = memberId;
        this.pin = pin;
        this.fullName = fullName;
        this.major = major;
        this.email = email;
        this.gender = gender;
        this.addres = addres;
        this.phoneNumber = phoneNumber;
    }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGender() { return gender; }
    public void setGender(String gender){this.gender = gender; }
    public String getAddres() { return addres; }
    public void setAddres(String addres) { this.addres = addres; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}