package com.sec.pojo;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;

public class User {
    private Integer id;

    @NotBlank(message = "邮箱不能是空")
    @Email(message = "邮箱格式不对")
    private String email;

    private String password;

    private String nickname;

    private String avatar;

    private String phone;

    private Integer loginCount;

    private Timestamp createTime;

    private Timestamp updateTime;

    private Timestamp lastLoginTime;

    public User(Integer id, String email, String password, String nickname, String avatar, String phone, Integer loginCount, Timestamp createTime, Timestamp updateTime, Timestamp lastLoginTime) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
        this.phone = phone;
        this.loginCount = loginCount;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.lastLoginTime = lastLoginTime;
    }

    public User() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? null : nickname.trim();
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar == null ? null : avatar.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}