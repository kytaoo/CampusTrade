package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 或者在启动类上使用 @MapperScan
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper 已提供常用 CRUD 方法
    // 如果有特殊 SQL 需求，可以在这里定义方法，并在 XML 文件或使用注解实现
}