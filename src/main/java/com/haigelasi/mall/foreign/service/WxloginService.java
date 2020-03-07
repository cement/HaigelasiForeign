package com.haigelasi.mall.foreign.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.haigelasi.mall.foreign.config.ApplicationConfig;
import com.haigelasi.mall.foreign.util.MD5Util;
import com.haigelasi.mall.foreign.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.haigelasi.mall.foreign.config.ApplicationConfig.redirectUrl;

/**
 * @author ：ysh
 * @date ：Created in 20200222
 */
@Slf4j
@Service
public class WxloginService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RestTemplate restTemplate;


    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");

    public String  createWxlonginUrl() throws IOException {
//        String redirectUrl = "http://cement.imwork.net/wxLogin/callback";
        String wxloginUrl = "https://open.weixin.qq.com/connect/oauth2/authorize" +
                "?appid=" + ApplicationConfig.appid +
                "&redirect_uri=" + URLEncoder.encode(redirectUrl, "utf-8") +
                "&response_type=" + "code" +
                "&scope=" + "snsapi_userinfo" +
                "&state=STATE" +
                "#wechat_redirect";
        return wxloginUrl;
      }

      public JSONObject getAccesstokenJson(String code,String state){
          String tokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                  "?appid=" + ApplicationConfig.appid +
                  "&secret=" + ApplicationConfig.secret +
                  "&code=" + code +
                  "&grant_type=" + "authorization_code" ;
          String accessString = restTemplate.getForObject(tokenUrl, String.class);
          JSONObject accessJson = JSON.parseObject(accessString);
          return accessJson;
      }
      public JSONObject getunionidJson(String accessToken,String openid){
          String unionidUrl = "https://api.weixin.qq.com/sns/userinfo" +
                  "?access_token=" + accessToken +
                  "&openid=" + openid;
          String unionidString = restTemplate.getForObject(unionidUrl, String.class);

          JSONObject unionidJson = JSON.parseObject(unionidString);
          return unionidJson;
      }
      public Long getLocalShopuserId(String openIdOrUnionId){
          String sql = "SELECT shopuser.id FROM t_shop_user as shopuser left join t_wx_user as wxuser on shopuser.wxid = wxuser.id  WHERE wxuser.openid =? or unionid=?  LIMIT 1 OFFSET 0";
          Long localuserId = jdbcTemplate.queryForObject(sql, new String[]{openIdOrUnionId,openIdOrUnionId},Long.class);
          return localuserId;
      }

      @Transactional
      public Long insertUserInfo(JSONObject wxuserMap){
          wxuserMap.put("privilege", JSON.toJSONString(wxuserMap.remove("privilege")));

          List<String> keyList = wxuserMap.keySet().stream().collect(Collectors.toList());
          String colums = keyList.stream().collect(Collectors.joining(","));
          String holders = Stream.generate(() -> "?").limit(keyList.size()).collect(Collectors.joining(","));
          String sql  = "insert into t_wx_user("+colums+")values("+holders+")";
          KeyHolder wxuserKsyholder = new GeneratedKeyHolder();
          jdbcTemplate.update(connection -> {
              PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
              for (int i = 0; i < keyList.size(); i++) {
                  Object o = wxuserMap.get(keyList.get(i));
                  ps.setObject(i+1, o);
              }
             return ps;
          },wxuserKsyholder);

          Long wxuserid = wxuserKsyholder.getKey().longValue();

          KeyHolder shopuserKeyholder = new GeneratedKeyHolder();
          if (!Objects.isNull(wxuserid)){
              String sopuserSql  = "insert into t_shop_user(nick_name,mobile,password,avatar,salt,wxid,gender,create_time)values(?,?,?,?,?,?,?,?)";

              jdbcTemplate.update(connection -> {
                  PreparedStatement ps = connection.prepareStatement(sopuserSql, Statement.RETURN_GENERATED_KEYS);
                  String nickname  = String.valueOf(wxuserMap.get("nickname"));
                  ps.setObject(1,nickname);
                  String mobiletmp  = String.valueOf(wxuserMap.get("openid")).substring(0,16);
                  ps.setObject(2,mobiletmp);
                  String password  = MD5Util.md5(String.valueOf(wxuserMap.get("openid")),String.valueOf(wxuserMap.get("salt")));
                  ps.setObject(3,password);
                  String avatar  = String.valueOf(wxuserMap.get("headimgurl"));
                  ps.setObject(4,avatar);
                  String salt  = RandomUtil.getRandomString(5);
                  ps.setObject(5,salt);
                  ps.setObject(6,wxuserid);
                  String gender = wxuserMap.getString("sex")=="0"?"female":"male";
                  ps.setObject(7,gender);
                  Date createtime  = new Date();
                  ps.setObject(8,createtime);
                  return ps;
              },shopuserKeyholder);
          }
          Long shopuserId = shopuserKeyholder.getKey().longValue();
          return shopuserId;
      }

}
