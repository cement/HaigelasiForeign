package com.haigelasi.mall.foreign.entity;

import lombok.Data;

/**
 * @author ：ycsh
 * @date ：Created in 20200222
 */
@Data
public class WxUser {
    private Long id;
    private String unionid;
    private String openid;
    private String nickname;
    private String sex;
    private String province;
    private String city;
    private String country;
    private String headimgurl;
    private String privilege;
}
