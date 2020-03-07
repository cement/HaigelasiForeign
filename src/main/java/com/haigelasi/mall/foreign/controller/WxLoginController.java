package com.haigelasi.mall.foreign.controller;

import com.alibaba.fastjson.JSONObject;
import com.haigelasi.mall.foreign.config.ApplicationConfig;
import com.haigelasi.mall.foreign.service.WxloginService;
import com.haigelasi.mall.foreign.util.WxLoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import static com.haigelasi.mall.foreign.config.ApplicationConfig.loginUrl;
import static com.haigelasi.mall.foreign.config.ApplicationConfig.revertUrl;

/**
 * @author ：ysh
 * @date ：20200218
 */
@Slf4j
@RestController
@RequestMapping("/wxLogin")
public class WxLoginController {

    @Autowired
    private WxloginService wxloginService;
    @RequestMapping("/wxCheck")
    public String  wxCheck(HttpServletRequest request, HttpServletResponse response){
        String echostr = request.getParameter("echostr");
        log.info("echostr:{}",echostr);
        return echostr;
//        boolean isGet = request.getMethod().toLowerCase().equals("get");
//        if (isGet) {
//            // 微信加密签名
//            String signature = request.getParameter("signature");
//            // 时间戳
//            String timestamp = request.getParameter("timestamp");
//            // 随机数
//            String nonce = request.getParameter("nonce");
//            // 随机字符串
//            String echostr = request.getParameter("echostr");
//            // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
//            log.info("signature:{},timestamp:{},nonce:{},echostr:{}",signature,timestamp,nonce,echostr);
//            if (signature != null && WxLoginUtil.checkSignature(signature, timestamp, nonce)) {
//                try {
//                    PrintWriter print = response.getWriter();
//                    print.write(echostr);
//                    print.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }


    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public void  wxLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("======>>>:login......");
        String wxlonginUrl = wxloginService.createWxlonginUrl();
        log.info("======>>>:redirect->{}",wxlonginUrl);
        response.sendRedirect(wxlonginUrl);
    }

    @RequestMapping(value = "/callback", method = {RequestMethod.GET, RequestMethod.POST})
    public void  wxLoginCallback(@RequestParam("code") String code, @RequestParam("state") String state, HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException {
        log.info("callback======>>>:code:{},state:{}",code,state);
        JSONObject accessJson = wxloginService.getAccesstokenJson(code, state);

        String accessToken = accessJson.getString("access_token");
        String openid = accessJson.getString("openid");
        String unionid = accessJson.getString("unionid");
        if (StringUtils.isEmpty(openid)){
            log.error("获取登录信息失败！",accessJson);
            response.sendRedirect(loginUrl);
        }
        log.info("openid：{},unionid:{}",openid,unionid);
        try {
            Long localShopuserId = wxloginService.getLocalShopuserId(openid);
            String format = String.format(revertUrl, localShopuserId);
            System.out.println(format);
            response.sendRedirect(format);
             return;
            }catch (Exception e) {
                log.error("====={}",e.getMessage());
           }
        JSONObject unionidJson = wxloginService.getunionidJson(accessToken, openid);
        log.info("unionidJson:{}",unionidJson);
        if (StringUtils.isEmpty(unionidJson.get("openid"))){
            throw new RuntimeException("登录失败！"+unionidJson.getString("errcode")+unionidJson.getString("errmsg"));
        }
        Long shopuserId = wxloginService.insertUserInfo(unionidJson);
        log.info("======>>>:revert->{}",revertUrl);
        response.sendRedirect(String.format(revertUrl, shopuserId));

    }


}




