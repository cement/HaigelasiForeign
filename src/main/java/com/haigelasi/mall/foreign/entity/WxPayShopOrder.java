package com.haigelasi.mall.foreign.entity;


import lombok.Data;

/**
 * @author ：ycsh
 * @date ：Created in 20200222
 */
@Data
public class WxPayShopOrder {
  private Long id;
  private String message;
  private String orderSn;
  private String realPrice;
  private String openid;
  private int status;
//  private String unionid;
}
