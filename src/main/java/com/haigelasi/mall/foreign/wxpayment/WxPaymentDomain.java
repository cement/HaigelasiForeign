package com.haigelasi.mall.foreign.wxpayment;

import com.haigelasi.mall.foreign.wxpaysdk.IWXPayDomain;
import com.haigelasi.mall.foreign.wxpaysdk.WXPayConfig;
import com.haigelasi.mall.foreign.wxpaysdk.WXPayConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WxPaymentDomain implements IWXPayDomain {
    @Override
    public void report(String domain, long elapsedTimeMillis, Exception ex) {
            log.info("report:domain->",domain);
           if (ex !=null) ex.printStackTrace();
    }

    @Override
    public DomainInfo getDomain(WXPayConfig config) {
        DomainInfo domainInfo = new DomainInfo(WXPayConstants.DOMAIN_API, true);
        return domainInfo;
    }
}
