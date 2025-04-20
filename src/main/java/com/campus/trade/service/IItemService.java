package com.campus.trade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.dto.ItemPublishReqDTO;
import com.campus.trade.dto.ItemUpdateReqDTO;
import com.campus.trade.entity.Item;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IItemService extends IService<Item> {

    /**
     * 发布新商品
     * @param publishDTO 商品信息
     * @param userId 发布者用户ID
     * @param imageFiles 上传的图片文件列表
     * @return 创建的 Item 对象
     * @throws IOException 文件保存异常
     */
    Item publishItem(ItemPublishReqDTO publishDTO, Integer userId, List<MultipartFile> imageFiles) throws IOException;

    /**
     * 分页查询商品列表
     * @param page 分页对象
     * @param category 分类
     * @param keyword 关键词
     * @return 分页结果
     */
    IPage<Item> findItems(Page<Item> page, String category, String keyword);

    /**
     * 获取商品详情
     * @param itemId 商品ID
     * @return 商品详情，包含卖家信息；如果商品不存在或非在售，可能返回 null 或抛异常
     */
    Item getItemDetail(Integer itemId);

    /**
     * 更新商品信息
     * @param updateDTO 更新的商品信息
     * @param userId 操作者用户ID (用于权限校验)
     * @param imageFiles 新上传的图片文件 (可选)
     * @return 更新后的 Item 对象
     * @throws Exception 可能的异常，如权限不足、商品不存在、文件保存错误等
     */
    Item updateItem(ItemUpdateReqDTO updateDTO, Integer userId, List<MultipartFile> imageFiles) throws Exception;

    /**
     * 更新商品状态 (上架/下架)
     * @param itemId 商品ID
     * @param status 新的状态 ("ON_SALE", "OFF_SHELF")
     * @param userId 操作者用户ID
     * @return 是否成功
     * @throws Exception 权限不足或商品不存在
     */
    boolean updateItemStatus(Integer itemId, String status, Integer userId) throws Exception;

    /**
     * 删除商品 (逻辑删除或物理删除)
     * @param itemId 商品ID
     * @param userId 操作者用户ID
     * @return 是否成功
     * @throws Exception 权限不足或商品状态不允许删除
     */
    boolean deleteItem(Integer itemId, Integer userId) throws Exception;

    /**
     * 分页查询指定用户发布的商品
     * @param page 分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<Item> findMyItems(Page<Item> page, Integer userId);

    /**
     * 保存上传的图片文件
     * @param file MultipartFile 文件对象
     * @return 文件保存后的相对访问路径 (例如 /images/xxxxx.jpg)
     * @throws IOException 保存失败
     */
    String saveImage(MultipartFile file) throws IOException;
}