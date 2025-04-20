package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.dto.ItemPublishReqDTO;
import com.campus.trade.dto.ItemStatusUpdateDTO;
import com.campus.trade.dto.ItemUpdateReqDTO;
import com.campus.trade.entity.Item;
import com.campus.trade.service.IItemService;
import com.campus.trade.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication; // 用于获取当前用户信息
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private IItemService itemService;

    /**
     * 获取商品列表 (分页、筛选、排序) - 匿名可访问
     * @param pageNum 当前页码, 默认为 1
     * @param pageSize 每页数量, 默认为 10
     * @param category 分类筛选 (可选)
     * @param keyword 关键词筛选 (可选)
     * @return 商品分页结果
     */
    @GetMapping
    public Result<IPage<Item>> listItems(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        Page<Item> page = new Page<>(pageNum, pageSize);
        IPage<Item> itemPage = itemService.findItems(page, category, keyword);
        return Result.success(itemPage);
    }

    /**
     * 获取商品详情 - 匿名可访问
     * @param itemId 商品ID
     * @return 商品详情
     */
    @GetMapping("/{itemId}")
    public Result<Item> getItemDetail(@PathVariable Integer itemId) {
        Item item = itemService.getItemDetail(itemId);
        if (item != null) {
            return Result.success(item);
        } else {
            return Result.notFound("商品未找到或已下架");
        }
    }

    /**
     * 获取我发布的商品列表 (需要登录)
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return 我的商品分页结果
     */
    @GetMapping("/my")
    public Result<IPage<Item>> listMyItems(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 从 SecurityContext 获取当前用户ID
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录"); // 理论上会被 Spring Security 拦截，这里做个双重保险
        }

        Page<Item> page = new Page<>(pageNum, pageSize);
        IPage<Item> itemPage = itemService.findMyItems(page, userId);
        return Result.success(itemPage);
    }

    /**
     * 发布新商品 (需要登录)
     * 使用 consumes = MediaType.MULTIPART_FORM_DATA_VALUE 支持文件上传
     * @param publishDTO 商品信息 (作为 form-data 的一部分)
     * @param imageFiles 图片文件列表 (使用 @RequestPart)
     * @return 发布成功的商品信息
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Item> publishItem(
            @Validated @RequestPart("itemInfo") ItemPublishReqDTO publishDTO, // 商品信息用 @RequestPart 接收 JSON 字符串或对象
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles) { // 图片文件用 @RequestPart 接收

        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }

        try {
            Item newItem = itemService.publishItem(publishDTO, userId, imageFiles);
            return Result.success("商品发布成功", newItem);
        } catch (IOException e) {
            log.error("商品图片保存失败", e); // 需要添加日志记录
            return Result.internalError("商品图片上传失败，请稍后再试");
        } catch (Exception e) {
             log.error("商品发布失败", e);
             return Result.error(400, "商品发布失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品信息 (需要登录)
     * @param itemId 商品路径参数 ID
     * @param updateDTO 更新信息
     * @param imageFiles 新图片文件 (可选)
     * @return 更新后的商品信息
     */
    @PutMapping(value = "/{itemId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Item> updateItem(
            @PathVariable Integer itemId,
            @Validated @RequestPart("itemInfo") ItemUpdateReqDTO updateDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles) {

        // 校验路径参数 ID 和 DTO 中的 ID 是否一致
        if (!itemId.equals(updateDTO.getId())) {
             return Result.badRequest("路径参数 ID 与请求体 ID 不一致");
        }

        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }

        try {
            Item updatedItem = itemService.updateItem(updateDTO, userId, imageFiles);
            return Result.success("商品更新成功", updatedItem);
        } catch (IOException e) {
             log.error("商品图片更新失败", e);
             return Result.internalError("商品图片上传失败，请稍后再试");
        } catch (Exception e) {
             log.error("商品更新失败", e);
             return Result.error(400, "商品更新失败: " + e.getMessage());
        }
    }

     /**
     * 更新商品状态 (上架/下架) (需要登录)
     * @param itemId 商品ID
     * @param statusUpdateDTO 包含新状态 ("在售", "下架")
     * @return 操作结果
     */
    @PutMapping("/{itemId}/status")
    public Result<Void> updateItemStatus(
            @PathVariable Integer itemId,
            @Validated @RequestBody ItemStatusUpdateDTO statusUpdateDTO) {

        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }

        try {
            boolean success = itemService.updateItemStatus(itemId, statusUpdateDTO.getStatus(), userId);
            if (success) {
                return Result.success("商品状态更新成功");
            } else {
                // 这种情况理论上应该抛异常，而不是返回 false
                return Result.error(500, "商品状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新商品状态失败, itemId={}, status={}", itemId, statusUpdateDTO.getStatus(), e);
            return Result.error(400, "更新商品状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品 (需要登录)
     * @param itemId 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/{itemId}")
    public Result<Void> deleteItem(@PathVariable Integer itemId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }

        try {
            boolean success = itemService.deleteItem(itemId, userId);
            if (success) {
                return Result.success("商品删除成功");
            } else {
                 return Result.error(500, "商品删除失败");
            }
        } catch (Exception e) {
             log.error("删除商品失败, itemId={}", itemId, e);
             return Result.error(400, "删除商品失败: " + e.getMessage());
        }
    }


    /**
     * 获取当前登录用户的 User ID
     * @return 用户 ID，如果未登录则返回 null
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() != null) {
             Object principal = authentication.getPrincipal();
             if (principal instanceof String) { // 假设 principal 直接存了 userId 字符串
                 try {
                     // 将 String 类型的 userId 转换为 Integer
                     return Integer.parseInt((String) principal);
                 } catch (NumberFormatException e) {
                     log.warn("无法将 Principal '{}' 解析为用户 ID (Integer)", principal, e);
                     return null;
                 }
             } else  {
                 // 如果 Principal 不是预期的 String 类型，记录警告
                 log.warn("预期的 Principal 类型是 String (userId)，但实际类型是: {}", principal.getClass().getName());
                 return null;
             }
        }
        return null; // 未认证
    }

     // 添加日志记录器
     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ItemController.class);

}