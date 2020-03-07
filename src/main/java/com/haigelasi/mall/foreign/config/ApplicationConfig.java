package com.haigelasi.mall.foreign.config;

import com.haigelasi.mall.foreign.wxpayment.WxPayment;
import com.haigelasi.mall.foreign.wxpaysdk.WXPayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static com.haigelasi.mall.foreign.config.WxpaymentConfig.*;


/**
 * @author ：ysh
 * @date ：Created in 20200222
 */

@Configuration
public class ApplicationConfig {


    public static  String appid ;
    public static  String secret;
    public static  String redirectUrl;
    public static  String revertUrl;
    public static  String loginUrl;

    @Value("${wx.login.revertUrl}")
    public  void setRevertUrl(String revertUrl) {
        ApplicationConfig.revertUrl = revertUrl;
    }
    @Value("${wx.login.loginUrl}")
    public void setLoginUrl(String loginUrl) {
        ApplicationConfig.loginUrl = loginUrl;
    }

    @Value("${wx.login.redirectUrl}")
    public  void setRedirectUrl(String redirectUrl) {
        ApplicationConfig.redirectUrl = redirectUrl;
    }

    @Value("${wx.login.appid}")
    public  void setAppid(String appid) {
        ApplicationConfig.appid = appid;
    }

    @Value("${wx.login.secret}")
    public  void setSecret(String secret) {
        ApplicationConfig.secret = secret;
    }

    @Autowired
    private RestTemplateBuilder builder;
    @Bean
    public RestTemplate restTemplate() {
        return builder.build();
    }



}
