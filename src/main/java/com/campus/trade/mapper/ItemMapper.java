package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {

    /**
     * 分页查询商品列表（包含卖家部分信息）
     *
     * @param page 分页对象, xml中可以从 parameter 对象中直接获取分页参数
     * @param category 可选的分类筛选
     * @param keyword 可选的关键词筛选 (标题或描述)
     * @param status 可选的状态筛选
     * @return 分页结果 IPage<Item>
     */
    // 使用注解方式实现连表查询（也可以使用 XML）
    IPage<Item> findItemPage(Page<Item> page,
                           @Param("category") String category,
                           @Param("keyword") String keyword,
                           @Param("status") String status);


     /**
     * 获取商品详情，包含卖家信息，并增加点击量
     *
     * @param itemId 商品ID
     * @return Item 详情，包含卖家信息
     */
    // 可以在 Service 层分两步查询，或者也用连表查询
    // 这里示例在 Service 层处理
}