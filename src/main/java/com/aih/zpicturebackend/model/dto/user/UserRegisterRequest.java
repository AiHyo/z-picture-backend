package com.aih.zpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1316599223492120910L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
