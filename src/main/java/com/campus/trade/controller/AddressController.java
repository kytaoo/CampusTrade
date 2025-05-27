package com.campus.trade.controller;

import com.campus.trade.dto.AddressDTO;
import com.campus.trade.entity.Address;
import com.campus.trade.service.IAddressService;
import com.campus.trade.utils.Result;
import com.campus.trade.vo.AddressVO;
import lombok.extern.slf4j.Slf4j; // 引入 Slf4j
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j // 添加日志注解
@RestController
@RequestMapping("/user/addresses") // 统一前缀
public class AddressController {

    @Autowired
    private IAddressService addressService;

    /**
     * 获取当前用户地址列表
     * @return 地址列表VO
     */
    @GetMapping
    public Result<List<AddressVO>> listMyAddresses() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录"); // 添加校验
        }
        List<Address> addresses = addressService.listAddressesByUserId(userId);
        // 转换成 VO
        List<AddressVO> vos = addresses.stream().map(addr -> {
            AddressVO vo = new AddressVO();
            BeanUtils.copyProperties(addr, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 添加新地址
     * @param addressDTO 地址信息 DTO
     * @return 添加后的地址VO
     */
    @PostMapping
    public Result<AddressVO> addAddress(@Validated @RequestBody AddressDTO addressDTO) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        try {
            Address newAddress = addressService.addAddress(addressDTO, userId);
            AddressVO vo = new AddressVO();
            BeanUtils.copyProperties(newAddress, vo);
            return Result.success("添加地址成功", vo);
        } catch (Exception e) {
            log.error("添加地址失败, userId={}", userId, e);
            return Result.error(400, "添加地址失败: " + e.getMessage());
        }
    }

    /**
     * 更新地址
     * @param addressId 地址ID (路径参数)
     * @param addressDTO 地址信息 DTO
     * @return 更新后的地址VO
     */
    @PutMapping("/{addressId}")
    public Result<AddressVO> updateAddress(@PathVariable Integer addressId, @Validated @RequestBody AddressDTO addressDTO) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        addressDTO.setId(addressId); // 确保 ID 从路径参数获取
        try {
            Address updatedAddress = addressService.updateAddress(addressDTO, userId);
            AddressVO vo = new AddressVO();
            BeanUtils.copyProperties(updatedAddress, vo);
            return Result.success("更新地址成功", vo);
        } catch (Exception e) {
            log.error("更新地址失败, addressId={}, userId={}", addressId, userId, e);
            return Result.error(400, "更新地址失败: " + e.getMessage());
        }
    }

    /**
     * 删除地址
     * @param addressId 地址ID
     * @return 操作结果
     */
    @DeleteMapping("/{addressId}")
    public Result<Void> deleteAddress(@PathVariable Integer addressId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        try {
            boolean success = addressService.deleteAddress(addressId, userId);
            if (success) {
                return Result.success("删除地址成功");
            } else {
                // Service 层应该通过异常而不是 boolean 返回失败原因
                log.warn("删除地址 Service 返回 false, addressId={}, userId={}", addressId, userId);
                return Result.error(404, "地址未找到或删除失败");
            }
        } catch (Exception e) {
            log.error("删除地址失败, addressId={}, userId={}", addressId, userId, e);
            return Result.error(400, "删除地址失败: " + e.getMessage());
        }
    }

    /**
     * 设置默认地址
     * @param addressId 地址ID
     * @return 操作结果
     */
    @PutMapping("/{addressId}/default")
    public Result<Void> setDefaultAddress(@PathVariable Integer addressId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("用户未登录");
        }
        try {
            boolean success = addressService.setDefaultAddress(addressId, userId);
            if (success) {
                return Result.success("设置默认地址成功");
            } else {
                log.warn("设置默认地址 Service 返回 false, addressId={}, userId={}", addressId, userId);
                return Result.error(404, "地址未找到或设置失败");
            }
        } catch (Exception e) {
            log.error("设置默认地址失败, addressId={}, userId={}", addressId, userId, e);
            return Result.error(400, "设置默认地址失败: " + e.getMessage());
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