package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.dto.UserProfileDTO; // 需要创建这个 DTO
import com.campus.trade.entity.Trade;
import com.campus.trade.entity.User; // 引入 User
import com.campus.trade.service.FileService;
import com.campus.trade.service.IUserService;
import com.campus.trade.service.ITradeService;
import com.campus.trade.utils.Result;
import com.campus.trade.vo.TradeVO;
import com.campus.trade.vo.UserBalanceVO;
import com.campus.trade.vo.UserInfoVO; // 需要创建这个 VO
import lombok.extern.slf4j.Slf4j; // 引入 Slf4j
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 引入 MultipartFile
import org.springframework.beans.factory.annotation.Value; // 引入 Value

import java.io.IOException; // 引入 IOException
import java.math.BigDecimal;

@Slf4j // 添加日志注解
@RestController
@RequestMapping("/user") // 统一前缀 /user
public class UserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private ITradeService tradeService;
    @Autowired
    private FileService fileService; // <<-- 注入 FileService

    // 注入头像图片相关的配置
    @Value("${file.upload.avatar-base-path}")
    private String avatarUploadBasePath;
    @Value("${file.access.avatar-path-pattern}")
    private String avatarAccessPathPattern;

    /**
     * 获取当前用户信息
     * @return 用户信息VO
     */
    @GetMapping("/profile")
    public Result<UserInfoVO> getUserProfile() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
             return Result.unauthorized("用户未登录");
        }
        User user = userService.getById(userId); // IService 提供的根据ID查询方法
        if (user != null) {
            UserInfoVO vo = new UserInfoVO();
            BeanUtils.copyProperties(user, vo, "password"); // 复制属性，忽略密码
            return Result.success(vo);
        } else {
            log.warn("获取用户信息失败，未找到 userId={}", userId);
            return Result.notFound("用户不存在");
        }
    }

    /**
     * 更新当前用户信息 (昵称、手机号等)
     * @param profileDTO 包含要更新信息 DTO
     * @return 更新后的用户信息VO
     */
    @PutMapping("/profile")
    public Result<UserInfoVO> updateUserProfile(@Validated @RequestBody UserProfileDTO profileDTO) {
         Integer userId = getCurrentUserId();
         if (userId == null) {
             return Result.unauthorized("用户未登录");
        }
         try {
             User userToUpdate = userService.getById(userId);
             if (userToUpdate == null) {
                 return Result.notFound("用户不存在");
             }
             // 只更新允许用户修改的字段
             if (profileDTO.getNickname() != null) {
                 userToUpdate.setNickname(profileDTO.getNickname());
             }
             if (profileDTO.getPhone() != null) {
                 // 可以添加手机号格式或唯一性校验
                 userToUpdate.setPhone(profileDTO.getPhone());
             }
             // 头像通过单独接口上传更新
             // ... 其他允许更新的字段

             boolean success = userService.updateById(userToUpdate); // IService 提供的方法
             if (success) {
                 UserInfoVO vo = new UserInfoVO();
                 BeanUtils.copyProperties(userToUpdate, vo, "password");
                 return Result.success("用户信息更新成功", vo);
             } else {
                  log.error("更新用户信息失败, userId={}", userId);
                  return Result.internalError("更新失败，请稍后再试");
             }
         } catch (Exception e) {
              log.error("更新用户信息异常, userId={}", userId, e);
              return Result.internalError("更新用户信息时发生错误");
         }
    }


    /**
     * 更新用户头像
     * @param avatarFile 头像文件
     * @return 新的头像访问 URL (相对路径)
     */
    @PostMapping("/avatar")
    public Result<String> updateUserAvatar(@RequestParam("avatar") MultipartFile avatarFile) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        if (avatarFile == null || avatarFile.isEmpty()) {
            return Result.badRequest("请选择要上传的头像文件");
        }
        // 可选：校验文件类型、大小等...

        try {
            // 【修改】调用 FileService 保存头像文件
            String accessPrefix = avatarAccessPathPattern.replace("/**", ""); // 获取访问前缀 /images/avatar
            String avatarUrl = fileService.saveFile(avatarFile, avatarUploadBasePath, accessPrefix);

            // 更新用户信息
            User user = userService.getById(userId);
            if(user != null) {
                // 可选：删除旧头像文件
                // if (StringUtils.hasText(user.getAvatar())) {
                //     fileService.deleteFile(user.getAvatar()); // 需要实现 deleteFile
                // }
                user.setAvatar(avatarUrl); // 更新头像 URL
                boolean success = userService.updateById(user);
                if(success) {
                    return Result.success("头像更新成功", avatarUrl); // 返回相对路径
                } else {
                    log.error("更新用户头像 URL 到数据库失败, userId={}", userId);
                    return Result.internalError("更新头像失败，请稍后再试");
                }
            } else {
                return Result.notFound("用户不存在");
            }
        } catch (IOException e) {
            log.error("上传头像文件失败, userId={}", userId, e);
            return Result.internalError("头像上传失败，请稍后再试");
        } catch (Exception e) { // 捕获更广泛的异常
            log.error("更新头像时发生未知错误, userId={}", userId, e);
            return Result.internalError("更新头像时发生错误");
        }
    }


    /**
     * 获取用户余额
     * @return 余额信息VO
     */
    @GetMapping("/balance")
    public Result<UserBalanceVO> getUserBalance() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
             return Result.unauthorized("用户未登录");
        }
        try {
            BigDecimal balance = userService.getUserBalance(userId);
            UserBalanceVO vo = new UserBalanceVO();
            vo.setBalance(balance);
            return Result.success(vo);
        } catch (Exception e) {
             log.error("获取用户余额失败, userId={}", userId, e);
             return Result.internalError("获取余额失败");
        }
    }

    /**
     * 获取用户交易记录 (分页)
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 交易记录分页VO
     */
    @GetMapping("/trades")
    public Result<IPage<TradeVO>> listUserTrades(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
             return Result.unauthorized("用户未登录");
        }
        try {
            Page<Trade> page = new Page<>(pageNum, pageSize);
            IPage<Trade> tradePage = tradeService.findUserTrades(page, userId);
            // 转换成 VO Page
            IPage<TradeVO> voPage = tradePage.convert(trade -> {
                TradeVO vo = new TradeVO();
                BeanUtils.copyProperties(trade, vo);
                return vo;
            });
            return Result.success(voPage);
         } catch (Exception e) {
              log.error("获取交易记录失败, userId={}", userId, e);
              return Result.internalError("获取交易记录失败");
         }
    }


    /**
     * 辅助方法获取当前登录用户的 User ID
     * @return 用户 ID，如果未登录或无法解析则返回 null
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof String) {
            try {
                return Integer.parseInt((String) authentication.getPrincipal());
            } catch (NumberFormatException e) {
                log.warn("无法将 Principal '{}' 解析为用户 ID (Integer)", authentication.getPrincipal(), e);
                return null;
            }
        }
        log.warn("无法获取认证信息或 Principal 不是 String 类型: {}", authentication);
        return null; // 未认证或 Principal 类型不符
    }
}