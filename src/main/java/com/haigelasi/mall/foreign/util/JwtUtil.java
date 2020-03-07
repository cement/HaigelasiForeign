package com.haigelasi.mall.foreign.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;

/**
 * @author ：ysh
 * @date ：Created in 20200222
 */
public class JwtUtil {
    public static String signToken(String passsword,String userName,String id) throws UnsupportedEncodingException {
        Algorithm algorithm = Algorithm.HMAC256(passsword);
        String token = JWT.create()
                .withClaim("username", userName)
                .withClaim("userId", id)
                .withClaim("uuid", UUID.randomUUID().toString())
                .withExpiresAt(new Date(System.currentTimeMillis()+60*60*1000))
                .sign(algorithm);
        return token;
    }
}
