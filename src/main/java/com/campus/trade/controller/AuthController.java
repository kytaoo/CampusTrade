package com.campus.trade.controller;

import com.campus.trade.dto.EmailVerifyReqDTO;
import com.campus.trade.dto.UserLoginReqDTO;
import com.campus.trade.dto.UserRegisterReqDTO;
import com.campus.trade.service.IUserService;
import com.campus.trade.utils.Result;
import com.campus.trade.vo.LoginSuccessVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth") // 给 Controller 设置统一路径前缀
public class AuthController {

    @Autowired
    private IUserService userService;

    /**
     * 用户注册接口
     * @param registerDTO 注册信息，使用 @Validated 启用 DTO 中的校验注解
     * @return Result 对象
     */
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody UserRegisterReqDTO registerDTO) {
        try {
            userService.register(registerDTO);
            return Result.success("注册成功，请检查邮箱完成验证");
        } catch (Exception e) {
            // 实际项目中应该定义更具体的业务异常，并在全局异常处理器中处理
            return Result.error(400, "注册失败: " + e.getMessage());
        }
    }

    /**
     * 邮箱验证接口
     * @param verifyReqDTO 邮箱和验证码
     * @return Result 对象
     */
    @PostMapping("/verify-email")
    public Result<Void> verifyEmail(@Validated @RequestBody EmailVerifyReqDTO verifyReqDTO) {
        boolean success = userService.verifyEmail(verifyReqDTO.getEmail(), verifyReqDTO.getCode());
        if (success) {
            return Result.success("邮箱验证成功，账号已激活");
        } else {
            return Result.error(400, "验证失败，邮箱或验证码错误，或验证码已过期");
        }
    }

    /**
     * 用户登录接口
     * @param loginDTO 登录信息
     * @return 包含 Token 的 Result 对象
     */
    @PostMapping("/login")
    public Result<LoginSuccessVO> login(@Validated @RequestBody UserLoginReqDTO loginDTO) {
        try {
            LoginSuccessVO loginResult = userService.login(loginDTO);
            return Result.success("登录成功", loginResult);
        } catch (Exception e) {
            return Result.error(401, "登录失败: " + e.getMessage()); // 登录失败通常返回 401
        }
    }
}