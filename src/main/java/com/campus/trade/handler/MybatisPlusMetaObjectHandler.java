package com.campus.trade.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mybatis Plus 自动填充处理器 (用于 createdAt 和 updatedAt)
 */
@Slf4j
@Component // 需要注册为 Spring Bean
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("start insert fill ....");
        // setFieldValByName(字段名, 字段值, metaObject)
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime::now, LocalDateTime.class); // 起始版本 3.3.0(推荐)
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class); // 起始版本 3.3.0(推荐)

        // 或者 HumpConvert=true 时(默认)，可以使用驼峰命名
        // this.strictInsertFill(metaObject, "createdAt", () -> LocalDateTime.now(), LocalDateTime.class);
        // this.strictInsertFill(metaObject, "updatedAt", () -> LocalDateTime.now(), LocalDateTime.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("start update fill ....");
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class); // 起始版本 3.3.0(推荐)
    }
}