package com.haigelasi.mall.foreign.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

@Slf4j
@RestController
public class WxVerifyController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/MP_verify_cTGCf8YtQPF77eql.txt")
    public String verify(HttpServletRequest request, HttpServletResponse response){
//        InputStream resourceAsStream = null;
//        OutputStream outputStream = null;
//        try {
//         resourceAsStream = getClass().getResourceAsStream("/MP_verify_cTGCf8YtQPF77eql.txt");
//         outputStream = response.getOutputStream();
//         StreamUtils.copy(resourceAsStream,outputStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            if (Objects.nonNull(resourceAsStream)){
//                try {
//                    resourceAsStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (Objects.nonNull(resourceAsStream)){
//                try {
//                    outputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        return "cTGCf8YtQPF77eql";
    }
}
