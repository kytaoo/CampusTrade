package com.campus.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.trade.dto.UserLoginReqDTO;
import com.campus.trade.dto.UserRegisterReqDTO;
import com.campus.trade.entity.User;
import com.campus.trade.vo.LoginSuccessVO;


public interface IUserService extends IService<User> { // 继承 IService 提供基础 CRUD

    /**
     * 用户注册
     * @param registerDTO 注册信息 DTO
     * @throws Exception 注册过程中可能出现的业务异常
     */
    void register(UserRegisterReqDTO registerDTO) throws Exception;

    /**
     * 校验邮箱验证码并激活用户
     * @param email 邮箱
     * @param code  验证码
     * @return true 如果验证成功并激活，false 否则
     */
    boolean verifyEmail(String email, String code);


    /**
     * 用户登录
     * @param loginDTO 登录信息 DTO
     * @return 登录成功信息，包含 Token
     * @throws Exception 登录过程中可能出现的业务异常 (用户不存在、密码错误、未激活等)
     */
    LoginSuccessVO login(UserLoginReqDTO loginDTO) throws Exception;

    /**
     * 根据学号或邮箱查找用户 (用于登录)
     * @param identifier 学号或邮箱
     * @return 用户实体，或 null 如果未找到
     */
    User findUserByIdentifier(String identifier);
}