package com.haigelasi.mall.foreign.mapper;

import com.haigelasi.mall.foreign.entity.ShopUser;
import com.haigelasi.mall.foreign.entity.WxPayShopOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
 * @author ：ysh
 * @date ：Created in 20200222
 */
@Mapper
public interface ShopOrderMapper {

  @Select(
            " SELECT  " +
                    "  shoporder.id" +
                    " ,shoporder.order_sn" +
                    " ,shoporder.message" +
                    " ,shoporder.real_price" +
                    " ,shoporder.status" +
                    " ,wxuser.openid " +
                    " FROM t_shop_order as shoporder " +
                    " LEFT JOIN t_shop_user as shopuser ON shoporder.id_user = shopuser.id " +
                    " LEFT JOIN t_wx_user as wxuser ON  shopuser.wxid = wxuser.id  " +
                    " WHERE shoporder.order_sn = #{orderNo}"
    )
    WxPayShopOrder getPayOrderByNo(String orderNo);
    @Select(
            "SELECT shopuser.id FROM t_shop_user as shopuser left join t_wx_user as wxuser on shopuser.wxid = wxuser.id  WHERE wxuser.openid =? or unionid=?  LIMIT 1 OFFSET 0"
    )
    ShopUser getLoginShopuser(String opennidOrUnionid);

    @Update(
            "UPDATE t_shop_order SET status =2, wx_transaction_id = #{wxTransactionId} WHERE order_sn = #{orderNo} "
    )
    int updatePaySuccess(@Param("orderNo") String orderNo,@Param("wxTransactionId")String wxTransactionId);

    @Select(
            "SELECT id,status,order_sn,wx_transaction_id FROM t_shop_order WHERE order_sn = #{orderNo}"
    )
    Map<String,Object> getShoporderStatus(String orderNo);
}
