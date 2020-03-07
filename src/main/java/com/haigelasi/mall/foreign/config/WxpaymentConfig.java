package com.haigelasi.mall.foreign.config;

import com.haigelasi.mall.foreign.wxpayment.WxPayment;
import com.haigelasi.mall.foreign.wxpayment.WxPaymentDomain;
import com.haigelasi.mall.foreign.wxpaysdk.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author ：ysh
 * @date ：Created in 20200222
 */


@Configuration
//@ConfigurationProperties(prefix = "wx.pay")
public class WxpaymentConfig extends WXPayConfig {

    @Value("${wx.pay.appid}")
    public  String appid;
    @Value("${wx.pay.mchid}")
    public  String mchid;
    @Value("${wx.pay.key}")
    public  String key;

    @Value("${wx.pay.notifyUrl}")
    public  String notifyUrl;
    @Value("${wx.pay.certPath}")
    public  String certPath;
    @Value("${wx.pay.signType}")
    public  String signType;
    @Value("${wx.pay.autoReport}")
    public  boolean autoReport;
    @Value("${wx.pay.useSandbox}")
    public  boolean useSandbox;


    @Override
    public String getAppID() {
        return appid;
    }

    @Override
    public String getMchID() {
        return mchid;
    }

    @Override
    public String getKey() {
        //todo 沙箱环境
       return key;
    }

    @Override
    protected InputStream getCertStream() throws IOException {
        InputStream inputStream = Files.newInputStream(Paths.get(certPath), StandardOpenOption.READ);
        return inputStream;
    }

    @Override
    protected IWXPayDomain getWXPayDomain() {
        return new WxPaymentDomain();
    }

    @Bean
    public WxPayment wxPayment(WxpaymentConfig config) throws Exception {
        WxPayment wxPayment = new WxPayment(this,notifyUrl,autoReport,useSandbox);
        return wxPayment;
    }






    public  Map<String,String> tradeStateMap = new LinkedHashMap(){{
        put("SUCCESS","支付成功");
        put("REFUND","转入退款");
        put("NOTPAY","未支付");
        put("CLOSED","已关闭");
        put("CLOSED","已关闭");
        put("REVOKED","已撤销");
        put("USERPAYING","支付中");
        put("PAYERROR","支付失败");
    }};
    public  Map<String,String> errCodeDesMap = new LinkedHashMap(){{
        put("ORDERNOTEXIST","此交易订单号不存在");
        put("SYSTEMERROR","系统错误");
    }};


}
