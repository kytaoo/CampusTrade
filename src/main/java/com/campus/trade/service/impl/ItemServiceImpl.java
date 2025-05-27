package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.dto.ItemPublishReqDTO;
import com.campus.trade.dto.ItemUpdateReqDTO;
import com.campus.trade.entity.Item;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.ItemMapper;
import com.campus.trade.mapper.UserMapper; // 需要注入 UserMapper 获取卖家信息
import com.campus.trade.service.FileService;
import com.campus.trade.service.IItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils; // 引入 CollectionUtils
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private UserMapper userMapper; // 注入 UserMapper

    @Autowired
    private FileService fileService; // <<-- 注入 FileService

    // 注入商品图片相关的配置
    @Value("${file.upload.item-base-path}")
    private String itemUploadBasePath;
    @Value("${file.access.item-path-pattern}")
    private String itemAccessPathPattern;

    // 商品状态常量 (推荐使用枚举)
    private static final String STATUS_ON_SALE = "在售"; // 在售
    private static final String STATUS_SOLD = "已售";       // 已售
    private static final String STATUS_OFF_SHELF = "下架"; // 下架
    private static final String STATUS_DELETED = "已删除"; // 逻辑删除状态

    @Override
    @Transactional
    public Item publishItem(ItemPublishReqDTO publishDTO, Integer userId, List<MultipartFile> imageFiles) throws IOException {
        Item item = new Item();
        BeanUtils.copyProperties(publishDTO, item);
        item.setUserId(userId);
        item.setStatus("在售"); // 使用中文常量
        item.setClickCount(0);

        // 处理图片上传
        if (!CollectionUtils.isEmpty(imageFiles)) {
            List<String> imagePaths = new ArrayList<>();
            String accessPrefix = itemAccessPathPattern.replace("/**", ""); // 获取访问前缀 /images/item
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    // 【修改】调用 FileService 保存图片
                    String relativePath = fileService.saveFile(file, itemUploadBasePath, accessPrefix);
                    imagePaths.add(relativePath);
                }
            }
            item.setImages(imagePaths);
        }

        itemMapper.insert(item);
        return item;
    }

    @Override
    public IPage<Item> findItems(Page<Item> page, String category, String keyword) {
        // 调用 Mapper 的自定义分页查询方法
        // 只查询在售商品
        return itemMapper.findItemPage(page, category, keyword, STATUS_ON_SALE);
    }

    @Override
    @Transactional // 包含更新点击量操作
    public Item getItemDetail(Integer itemId) {
        Item item = itemMapper.selectById(itemId);
        log.info("通过 selectById 获取到的 Item (ID: {}): {}", itemId, item); // <<-- 添加这行日志

        if (item != null && !STATUS_DELETED.equals(item.getStatus())) { // 确保商品存在且未被删除
            // 增加点击量 (可以考虑用 Redis 优化高并发场景)
            item.setClickCount(item.getClickCount() + 1);
            itemMapper.updateById(item); // 更新点击量

            // 查询并设置卖家信息
            User seller = userMapper.selectById(item.getUserId());
            if (seller != null) {
                item.setSellerNickname(seller.getNickname());
                item.setSellerAvatar(seller.getAvatar());
            }
            return item;
        }
        return null; // 或抛出未找到异常
    }

    @Override
    @Transactional
    public Item updateItem(ItemUpdateReqDTO updateDTO, Integer userId, List<MultipartFile> imageFiles) throws Exception {
        Item existingItem = itemMapper.selectById(updateDTO.getId());
        if (existingItem == null || STATUS_DELETED.equals(existingItem.getStatus())) {
            throw new Exception("商品不存在");
        }
        // 权限校验：只有发布者才能修改
        if (!existingItem.getUserId().equals(userId)) {
            throw new Exception("无权修改他人发布的商品");
        }
        // 已售商品通常不允许修改核心信息，可以根据业务调整
        if (STATUS_SOLD.equals(existingItem.getStatus())) {
             throw new Exception("已售商品无法修改");
        }

        // 更新允许修改的字段
        BeanUtils.copyProperties(updateDTO, existingItem, "id", "userId", "status", "clickCount", "createdAt", "updatedAt"); // 忽略不能修改的字段

        // 处理图片更新
        if (!CollectionUtils.isEmpty(imageFiles)) {
            // 可选：删除旧图片文件 (需要实现 FileService.deleteFile 并传入旧的 relativePaths)
            // deleteOldImages(existingItem.getImages()); // 假设有这个方法

            List<String> newImagePaths = new ArrayList<>();
            String accessPrefix = itemAccessPathPattern.replace("/**", "");
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    // 【修改】调用 FileService 保存新图片
                    String relativePath = fileService.saveFile(file, itemUploadBasePath, accessPrefix);
                    newImagePaths.add(relativePath);
                }
            }
            existingItem.setImages(newImagePaths); // 覆盖旧的图片列表
        }
        // else 分支可以保留原有图片，或者提供删除指定图片的功能

        // updatedAt 由 MP 自动填充
        itemMapper.updateById(existingItem);
        return existingItem;
    }

    @Override
    @Transactional
    public boolean updateItemStatus(Integer itemId, String status, Integer userId) throws Exception {
        Item item = itemMapper.selectById(itemId);
        if (item == null || STATUS_DELETED.equals(item.getStatus())) {
            throw new Exception("商品不存在");
        }
        if (!item.getUserId().equals(userId)) {
            throw new Exception("无权修改他人发布的商品状态");
        }

        // 简单的状态流转校验 (可以根据业务细化)
        if (!STATUS_ON_SALE.equals(status) && !STATUS_OFF_SHELF.equals(status)) {
            throw new Exception("无效的商品状态");
        }
        // 不能将已售商品改为上架或下架 (如果业务允许，可以调整)
        if(STATUS_SOLD.equals(item.getStatus())){
            throw new Exception("已售商品无法修改状态");
        }

        item.setStatus(status);
        // updatedAt 由 MP 自动填充
        return itemMapper.updateById(item) > 0;
    }

    @Override
    @Transactional
    public boolean deleteItem(Integer itemId, Integer userId) throws Exception {
        Item item = itemMapper.selectById(itemId);
        if (item == null || STATUS_DELETED.equals(item.getStatus())) {
            throw new Exception("商品不存在");
        }
        if (!item.getUserId().equals(userId)) {
            throw new Exception("无权删除他人发布的商品");
        }
        // 只有下架状态的商品才能删除 (根据设计书)
        if (!STATUS_OFF_SHELF.equals(item.getStatus())) {
            throw new Exception("请先下架商品再删除");
        }

        // 物理删除
        // int result = itemMapper.deleteById(itemId);
        // return result > 0;

        // 逻辑删除 (如果配置了全局逻辑删除或使用 update)
         item.setStatus(STATUS_DELETED); // 设置为删除状态
         return itemMapper.updateById(item) > 0;
        // 或者如果配置了 @TableLogic, 直接调用 deleteById 即可
        // int result = itemMapper.deleteById(itemId);
        // return result > 0;

        // 注意：删除商品时，关联的图片文件也应该被清理，这里暂未实现文件清理逻辑
    }

    @Override
    public IPage<Item> findMyItems(Page<Item> page, Integer userId) {
        // 查询指定用户发布的所有未被逻辑删除的商品
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getUserId, userId);
        queryWrapper.ne(Item::getStatus, STATUS_DELETED); // 排除逻辑删除的
        queryWrapper.orderByDesc(Item::getCreatedAt);
        // 这里直接使用 MP 的分页查询，不包含卖家信息（因为就是用户自己）
        return itemMapper.selectPage(page, queryWrapper);
    }


    // (可选) 删除旧图片文件的辅助方法
    // private void deleteOldImages(List<String> relativePaths) {
    //     if (!CollectionUtils.isEmpty(relativePaths)) {
    //         String relativePathPrefix = accessPathPattern.replace("/**", ""); // 例如 /images
    //         for (String relativePath : relativePaths) {
    //             try {
    //                  // 从相对路径构造物理路径
    //                  String filename = relativePath.substring(relativePathPrefix.length() + 1);
    //                  Path filePath = Paths.get(uploadBasePath, filename);
    //                  Files.deleteIfExists(filePath);
    //                  log.info("Deleted old image file: {}", filePath);
    //             } catch (IOException e) {
    //                  log.error("Failed to delete old image file: {}", relativePath, e);
    //             }
    //         }
    //     }
    // }

    @Override
    public List<Item> listItemsByIds(List<Integer> itemIds) {
        if (CollectionUtils.isEmpty(itemIds)) {
            return new ArrayList<>();
        }
        return itemMapper.selectBatchIds(itemIds);
    }
}