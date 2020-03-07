package com.haigelasi.mall.foreign.wxpayment;

import com.haigelasi.mall.foreign.config.WxpaymentConfig;
import com.haigelasi.mall.foreign.wxpaysdk.WXPay;
import com.haigelasi.mall.foreign.wxpaysdk.WXPayConfig;
import com.haigelasi.mall.foreign.wxpaysdk.WXPayConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class WxPayment extends WXPay {

    public WxPayment(WXPayConfig config, String notifyUrl, boolean autoReport, boolean useSandbox) throws Exception {
        super(config, notifyUrl, autoReport, useSandbox);
    }
}
