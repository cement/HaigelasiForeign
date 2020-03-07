package com.haigelasi.mall.foreign.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ：ycsh
 * @date ：Created in 20200222
 */
@Data
public class ShopUser implements Serializable {
    private Long id;
    private Date createTime;
    private String mobile;
    private String salt;
    private String password;
    private String nickName;
    private String avatar;
    private String gender;
    private Date lastLoginTime;
    private Long wxid;
}
