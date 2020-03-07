package com.haigelasi.mall.foreign.controller;

import com.alibaba.fastjson.JSON;
import com.haigelasi.mall.foreign.config.WxpaymentConfig;
import com.haigelasi.mall.foreign.service.WxpayService;
import com.haigelasi.mall.foreign.util.WxLoginUtil;
import com.haigelasi.mall.foreign.wxpayment.WxPayment;
import com.haigelasi.mall.foreign.wxpaysdk.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.haigelasi.mall.foreign.wxpaysdk.WXPayConstants.SANDBOX_GETSIGNKEY_SUFFIX;

/**
 * @author ：ysh
 * @date ：20200218
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/wxpay")
public class WxpayController {


    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private WxpayService wxpayService;
    @Autowired
    private WxpaymentConfig wxPayConfig;
    @Autowired
    private WxPayment wxPayment;

    @RequestMapping("/wxCheck")
    public void wxCheck(HttpServletRequest request, HttpServletResponse response) {
//        String echostr = request.getParameter("echostr");
//        log.info("echostr:{}",echostr);
//        return echostr;
        boolean isGet = request.getMethod().toLowerCase().equals("get");
        if (isGet) {
            // 微信加密签名
            String signature = request.getParameter("signature");
            // 时间戳
            String timestamp = request.getParameter("timestamp");
            // 随机数
            String nonce = request.getParameter("nonce");
            // 随机字符串
            String echostr = request.getParameter("echostr");
            // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
            log.info("signature:{},timestamp:{},nonce:{},echostr:{}", signature, timestamp, nonce, echostr);
            if (signature != null && WxLoginUtil.checkSignature(signature, timestamp, nonce)) {
                try {
                    PrintWriter print = response.getWriter();
                    print.write(echostr);
                    print.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequestMapping("/unifiedOrder/{orderSn}")
    public ResponseEntity unifiedOrder(@PathVariable("orderSn") String orderSn) throws Exception {
        Map<String, Object> resultMap = wxpayService.JsapiUnifiedOrder(orderSn);
        return ResponseEntity.ok(resultMap);
    }

    @RequestMapping("/notify")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuffer xmlBuffer = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = request.getReader();
            String line = null;
            while ((line = reader.readLine()) != null) {
                xmlBuffer.append(line);
            }
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.info("requesetReader 关闭错误,{}", e.getMessage(), e);
            }
        }
        String notifyXml = xmlBuffer.toString();
        log.info(">>>收到微信支付通知：{}", notifyXml);
        String resultXml = wxpayService.payNotify(notifyXml);
        log.info(">>>返回微信支付信息：{}", resultXml);
        if (StringUtils.isEmpty(resultXml)) {
            /*如果更新不成功，不返回,等待微信再次通知*/
            return;
        }
        ByteArrayInputStream byteInStream = null;
        ServletOutputStream outputStream = null;
        try {
            byteInStream = new ByteArrayInputStream(resultXml.getBytes());
            outputStream = response.getOutputStream();
            StreamUtils.copy(byteInStream, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            log.info("写出流信息错误,{}", e.getMessage(), e);
        } finally {
            if (Objects.isNull(byteInStream)) {
                try {
                    byteInStream.close();
                } catch (IOException e) {
                    log.info("byteInStream 关闭错误,{}", e.getMessage(), e);
                }
            }
            if (Objects.isNull(outputStream)) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.info("byteInStream 关闭错误,{}", e.getMessage(), e);
                }
            }

        }
    }


    @RequestMapping("/getSandboxKey")
    public String getSandboxKey() {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("mch_id", wxPayConfig.getMchID());
            params.put("nonce_str", WXPayUtil.generateNonceStr());
            params.put("sign", WXPayUtil.generateSignature(params, wxPayConfig.getKey()));
            String strXML = wxPayment.requestWithoutCert(SANDBOX_GETSIGNKEY_SUFFIX, params);
            if (StringUtils.isEmpty(strXML)) {
                return null;
            }
            Map<String, String> result = WXPayUtil.xmlToMap(strXML);
            log.info("retrieveSandboxSignKey:" + result);
            if ("SUCCESS".equals(result.get("return_code"))) {
                return result.get("sandbox_signkey");
            }
            return null;
        } catch (Exception e) {
            log.error("获取sandbox_signkey异常", e);
            return null;
        }
    }


    @RequestMapping("/payOrderQuery/{orderSn}")
    public ResponseEntity payOrderQuery(@PathVariable("orderSn") String orderSn) throws Exception {
        LinkedHashMap<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put("out_trade_no", orderSn);
        Map<String, String> resultMap = wxPayment.orderQuery(paramMap);
        log.info("查询订单返回结果:\r\n{}", JSON.toJSONString(resultMap, true));
        String returnCode = resultMap.get("return_code");

        String resultCode = resultMap.get("result_code");
        String tradeState = resultMap.get("trade_state");
        if ("SUCCESS".equals(returnCode)) {
            if ("SUCCESS".equals(resultCode)) {
                if ("SUCCESS".equals(tradeState)) {
                    String outTradeNo = resultMap.get("out_trade_no");
                    String transactionId = resultMap.get("transaction_id");
                    int i = wxpayService.updatePaySuccess(outTradeNo, transactionId);
                    if (i > 0) {
                        HashMap<String, Object> secuessMap = new HashMap<>();
                        secuessMap.put("order_sn", outTradeNo);
                        secuessMap.put("order_status", "2");
                        secuessMap.put("order_msg", wxPayConfig.tradeStateMap.get(tradeState));
                        Map<String, Object> resultWarp = new LinkedHashMap<>();
                        resultWarp.put("code", 20000);
                        resultWarp.put("data", secuessMap);
                        return ResponseEntity.ok(resultWarp);
                    }
                } else {
                    String outTradeNo = resultMap.get("out_trade_no");
                    HashMap<String, Object> tradeStateFailMap = new HashMap<>();
                    tradeStateFailMap.put("order_sn", outTradeNo);
                    tradeStateFailMap.put("order_msg", wxPayConfig.tradeStateMap.get(tradeState));
                    Map<String, Object> resultWarp = new LinkedHashMap<>();
                    resultWarp.put("code", 20000);
                    resultWarp.put("data", tradeStateFailMap);
                    return ResponseEntity.ok(resultWarp);
                }
            } else {
                String errCodeDes = resultMap.get("err_code_des");
                HashMap<Object, Object> resultCodeFailMap = new HashMap<>();
                resultCodeFailMap.put("order_msg", errCodeDes);
                Map<String, Object> resultWarp = new LinkedHashMap<>();
                resultWarp.put("code", 20000);
                resultWarp.put("data", resultCodeFailMap);
                return ResponseEntity.ok(resultWarp);
            }
        } else {
            String returnMsg = resultMap.get("return_msg");
            HashMap<Object, Object> returnCodeFailMap = new HashMap<>();
            returnCodeFailMap.put("order_msg", returnMsg);
            Map<String, Object> resultWarp = new LinkedHashMap<>();
            resultWarp.put("code", 20000);
            resultWarp.put("data", returnCodeFailMap);
            return ResponseEntity.ok(resultWarp);
        }

        Map<String, Object> resultWarp = new LinkedHashMap<>();
        resultWarp.put("code", 20000);
        resultWarp.put("data", resultMap);
        return ResponseEntity.ok(resultWarp);
    }

    @RequestMapping("/getOrderPayStatus/{orderSn}")
    public ResponseEntity getOrderPayStatus(@PathVariable("orderSn") String orderSn) throws Exception {
        Map<String, Object> orderPayStatus = wxpayService.getOrderPayStatus(orderSn);
        if (!CollectionUtils.isEmpty(orderPayStatus) && "2".equals(orderPayStatus.get("status"))){
                HashMap<String, Object> hasSecuessMap = new HashMap<>();
                hasSecuessMap.put("order_sn", orderPayStatus.get("order_sn"));
                hasSecuessMap.put("order_status", orderPayStatus.get("status"));
                hasSecuessMap.put("wx_transaction_id", orderPayStatus.get("wx_transaction_id"));
                hasSecuessMap.put("order_msg", "已付款");
                Map<String, Object> resultWarp = new LinkedHashMap<>();
                resultWarp.put("code", 20000);
                resultWarp.put("data", hasSecuessMap);
                return ResponseEntity.ok(resultWarp);
        }else {
            return payOrderQuery(orderSn);
        }
    }

}

