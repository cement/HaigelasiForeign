package com.haigelasi.mall.foreign.service;

import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson.JSON;
import com.haigelasi.mall.foreign.wxpayment.WxPayment;
import com.haigelasi.mall.foreign.wxpaysdk.WXPayConstants;
import com.haigelasi.mall.foreign.wxpaysdk.WXPayUtil;
import com.haigelasi.mall.foreign.config.WxpaymentConfig;
import com.haigelasi.mall.foreign.entity.WxPayShopOrder;
import com.haigelasi.mall.foreign.mapper.ShopOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.haigelasi.mall.foreign.config.WxpaymentConfig.*;

/**
 * @author ：ysh
 * @date ：Created in 20200222
 */
@Slf4j
@Service
public class WxpayService {

    @Autowired
    private WxPayment wxPayment;
    @Autowired
    private WxpaymentConfig wxPayConfig;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Resource
    private ShopOrderMapper shopOrderMapper;


    public static String PAY_TYPE_NATIVE = "NATIVE";
    public static String PAY_TYPE_APP = "APP";
    public static String PAY_TYPE_H5 = "MWEB";
    public static String PAY_TYPE_JSAPI = "JSAPI";
    public static String PAY_FEE_TYPE = "CNY";
    public static String PAY_DEVICE_INFO = "WEB";


    public Map<String, Object> JsapiUnifiedOrder(String orderNo) throws Exception {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String clientIP = ServletUtil.getClientIP(request);
        WxPayShopOrder shopOrder = shopOrderMapper.getPayOrderByNo(orderNo);
        log.info("sqlResult:{}", JSON.toJSONString(shopOrder, true));

        String timeStart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String timeExpire = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        LinkedHashMap<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put("notify_url", wxPayConfig.notifyUrl);//接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数。
        paramMap.put("device_info", PAY_DEVICE_INFO);//终端设备号(门店号或收银设备ID)，注意：PC网页或公众号内支付请传"WEB"
        paramMap.put("trade_type", PAY_TYPE_JSAPI);  // 此处指定为JSAPI
        paramMap.put("fee_type", PAY_FEE_TYPE); //默认人民币：CNY
        paramMap.put("product_id", String.valueOf(shopOrder.getId()));//trade_type=NATIVE，此参数必传。此id为二维码中包含的商品ID，商户自行定义。
        paramMap.put("out_trade_no", shopOrder.getOrderSn()); //自己的订单号
        //TODO 测试时商品总价
//            params.put("total_fee",shopOrder.getRealPrice());//总价款(实际付款金额)
        paramMap.put("total_fee", "1");//总价款(实际付款金额)
        paramMap.put("openid", shopOrder.getOpenid());//JSAPI必须传
        paramMap.put("body", shopOrder.getMessage());//商品简单描述，该字段须严格按照规范传递，具体请见参数规定
        paramMap.put("spbill_create_ip", clientIP);//发起支付客户端ip

//            params.put("time_start",timeStart);//订单生成时间，格式为yyyyMMddHHmmss
//            params.put("time_expire",timeExpire);//订单失效时间，格式为yyyyMMddHHmmss
//            params.put("receipt","Y");//Y，传入Y时，支付成功消息和支付详情页将出现开票入口。需要在微信支付商户平台或微信公众平台开通电子发票功能，传此字段才可生效
//            params.put("scene_info","Y");//Y，传入Y时，支付成功消息和支付详情页将出现开票入口。需要在微信支付商户平台或微信公众平台开通电子发票功能，传此字段才可生效

        log.info("请求参数:{}", JSON.toJSONString(paramMap,true));

        Map<String, String> responseMap = wxPayment.unifiedOrder(paramMap);

        log.info("返回结果:{}", JSON.toJSONString(responseMap,true));
        /*返回结果的处理*/
        if ("FAIL".equals(responseMap.get("return_code"))) {
            log.error("returnCode==fail:{}", responseMap);
            Map<String, Object> resultWarp = new LinkedHashMap<>();
            resultWarp.put("code", 20000);
            resultWarp.put("result", responseMap);
            return resultWarp;
        }
        if ("FAIL".equals(responseMap.get("resut_code"))) {
            log.error("resut_code==fail:{}", responseMap);
            Map<String, Object> resultWarp = new LinkedHashMap<>();
            resultWarp.put("code", 20000);
            resultWarp.put("result", responseMap);
            return resultWarp;
        }
        boolean responseSignatureValid = wxPayment.isResponseSignatureValid(responseMap);
        if (!responseSignatureValid) {
            log.error("返回验证失败！");
            Map<String, Object> resultWarp = new LinkedHashMap<>();
            resultWarp.put("code", 20000);
            resultWarp.put("result", "返回验证失败！");
            return resultWarp;
        }

        Map<String, String> resultMap = new LinkedHashMap<>();
        resultMap.put("appId", wxPayConfig.appid);
        resultMap.put("timeStamp", String.valueOf(WXPayUtil.getCurrentTimestamp()));
        resultMap.put("package", "prepay_id=" + responseMap.get("prepay_id"));
        resultMap.put("nonceStr", WXPayUtil.generateNonceStr());
        resultMap.put("signType", wxPayConfig.signType);
        String signature = WXPayUtil.generateSignature(resultMap, wxPayConfig.getKey(), WXPayConstants.SignType.MD5);
        resultMap.put("paySign", signature);

        log.info("=== unifiedOrder result==={}", resultMap);

        Map<String, Object> resultWarp = new LinkedHashMap<>();
        resultWarp.put("code", 20000);
        resultWarp.put("data", resultMap);
        return resultWarp;
    }

    /**
     * 微信支付完成通知(并不一定成功)
     *
     * @param notifyXml
     * @return
     * @throws Exception
     */
    public String payNotify(String notifyXml) throws Exception {
        Map<String, String> notifyMap = WXPayUtil.xmlToMap(notifyXml);
        /*签名验证*/
        boolean signatureValid = wxPayment.isPayResultNotifySignatureValid(notifyMap);
//        boolean signatureValid = WXPayUtil.isSignatureValid(notifyMap,wxPayConfig.getKey(), WXPayConstants.SignType.MD5);
        if (!signatureValid) {
            String signatureValidXml = createXmlResult("签名验证错误！");
            return signatureValidXml;
        }
        /*返回值result_code验证*/
        String resultCode = notifyMap.get("result_code");
        if (!"SUCCESS".equals(resultCode)) {
            String resultCodeFailXml = createXmlResult("result_code FAIL !!!");
            return resultCodeFailXml;
        }

        /*验证订单金额*/
        String outTradeNo = notifyMap.get("out_trade_no");
        String transactionId = notifyMap.get("transaction_id");
        String totalFee = notifyMap.get("total_fee");
        WxPayShopOrder payOrder = shopOrderMapper.getPayOrderByNo(outTradeNo);
        //todo 测试不比较金额
//        if (Objects.isNull(payOrder)||!payOrder.getRealPrice().equals(totalFee)){
//            String totalFeeFailXml = createXmlResult("total_fee FAIL !!!");
//            return totalFeeFailXml;
//        }
        /*如果已经更新，直接返回成功*/
        if (payOrder.getStatus() == 2) {
            String successXml = createXmlResult("OK");
            return successXml;
        }
        /*更新订单状态表 (此处微信团队提示进行并发控制,mysql更新有行锁,并且重复更新也是更新订单status值为2,所以此处不加锁。如需要，下面有带锁方法)*/
        int count = shopOrderMapper.updatePaySuccess(outTradeNo,transactionId);
        if (count > 0) {
            String successXml = createXmlResult("OK");
            return successXml;
        }
        /*如果更新不成功，返回null,等待微信再次通知*/
        return null;
    }


    private synchronized int updatePaySuccessStatusSync(String orderNo,String transactionId) {
        int count = shopOrderMapper.updatePaySuccess(orderNo,transactionId);
        return count;
    }
    public int updatePaySuccess(String orderNo,String transactionId) {
        int count = shopOrderMapper.updatePaySuccess(orderNo,transactionId);
        return count;
    }

    /**
     * 创建微信支付回复
     *
     * @param returnMsg
     * @return
     * @throws Exception
     */
    private String createXmlResult(String returnMsg) throws Exception {
        Map<String, String> successMap = new HashMap<>();
        successMap.put("return_code", "SUCCESS");
        successMap.put("return_msg", returnMsg);
        String successXml = WXPayUtil.mapToCDATAXml(successMap);
        return successXml;
    }


    /**
     * 获取订单支付状态
     *
     * @param orderNo
     * @return
     */
    public Map<String, Object> getOrderPayStatus(String orderNo) {
        Map<String, Object> shoporderStatus = shopOrderMapper.getShoporderStatus(orderNo);
        return shoporderStatus;
    }
}
