// src/main/java/com/campus/trade/mapper/AddressMapper.java
package com.campus.trade.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {
    // 将用户的所有地址设置为非默认
    @Update("UPDATE address SET is_default = 0 WHERE user_id = #{userId}")
    void setAllNotDefault(@Param("userId") Integer userId);
}