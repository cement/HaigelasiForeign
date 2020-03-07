package com.haigelasi.mall.foreign.mapper;

import com.haigelasi.mall.foreign.entity.ShopUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author ：ysh
 * @date ：Created in 20200222
 */
@Mapper
public interface ShopUserMapper {
    @Select(
            "SELECT shopuser.id FROM t_shop_user as shopuser left join t_wx_user as wxuser on shopuser.wxid = wxuser.id  WHERE wxuser.openid =#{opennidOrUnionid} or unionid=#{opennidOrUnionid}  LIMIT 1 OFFSET 0"
    )
    ShopUser getLoginShopuser(String opennidOrUnionid);
}
