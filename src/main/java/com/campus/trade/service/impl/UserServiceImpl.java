package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.trade.constant.RedisKeys;
import com.campus.trade.dto.UserLoginReqDTO;
import com.campus.trade.dto.UserRegisterReqDTO;
import com.campus.trade.entity.User;
import com.campus.trade.enums.UserStatusEnum;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.security.JwtTokenProvider;
import com.campus.trade.service.IUserService;
import com.campus.trade.service.MailService;
import com.campus.trade.utils.CommonUtils;
import com.campus.trade.vo.LoginSuccessVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 引入事务注解

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder; // 注入密码编码器

    @Autowired
    private StringRedisTemplate redisTemplate; // 注入 Redis 操作模板

    @Autowired
    private MailService mailService; // 注入邮件服务

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // 注入 JWT 工具类

    @Override
    @Transactional // 添加事务注解，保证注册操作的原子性
    public void register(UserRegisterReqDTO registerDTO) throws Exception {
        // 1. 检查学号是否已存在
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStudentId, registerDTO.getStudentId())) > 0) {
            throw new Exception("学号已被注册");
        }
        // 2. 检查邮箱是否已存在
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, registerDTO.getEmail())) > 0) {
            throw new Exception("邮箱已被注册");
        }

        // 3. 创建用户对象
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user); // 属性拷贝
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword())); // 加密密码
        user.setStatus(UserStatusEnum.PENDING_VERIFICATION); // 设置初始状态为待验证
        user.setBalance(BigDecimal.ZERO); // 设置初始余额
        user.setNickname("用户_" + CommonUtils.generateRandomCode(6)); // 生成默认昵称
        // createdAt 和 updatedAt 由 MP 自动填充

        // 4. 保存用户到数据库
        userMapper.insert(user); // 使用 BaseMapper 的 insert 方法

        // 5. 生成邮箱验证码
        String verifyCode = CommonUtils.generateRandomCode(6);

        // 6. 将验证码存入 Redis，设置过期时间（例如 5 分钟）
        String redisKey = RedisKeys.VERIFY_CODE_EMAIL_PREFIX + registerDTO.getEmail();
        redisTemplate.opsForValue().set(redisKey, verifyCode, 5, TimeUnit.MINUTES);

        // 7. 异步发送验证码邮件
        mailService.sendVerificationCode(registerDTO.getEmail(), verifyCode);
    }

    @Override
    @Transactional // 验证并更新状态，也应在一个事务中
    public boolean verifyEmail(String email, String code) {
        String redisKey = RedisKeys.VERIFY_CODE_EMAIL_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (code != null && code.equalsIgnoreCase(storedCode)) { // 验证码匹配 (忽略大小写)
            // 查找用户
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
            if (user != null && user.getStatus() == UserStatusEnum.PENDING_VERIFICATION) {
                // 更新用户状态为已激活
                user.setStatus(UserStatusEnum.ACTIVATED);
                userMapper.updateById(user);
                // 删除 Redis 中的验证码
                redisTemplate.delete(redisKey);
                return true;
            }
        }
        return false;
    }

    @Override
    public LoginSuccessVO login(UserLoginReqDTO loginDTO) throws Exception {
        // 1. 根据学号或邮箱查找用户
        User user = findUserByIdentifier(loginDTO.getIdentifier());

        // 2. 校验用户是否存在
        if (user == null) {
            throw new Exception("用户不存在");
        }

        // 3. 校验用户状态是否已激活
        if (user.getStatus() != UserStatusEnum.ACTIVATED) {
            if (user.getStatus() == UserStatusEnum.PENDING_VERIFICATION) {
                throw new Exception("账号尚未激活，请检查邮箱完成验证");
            } else if (user.getStatus() == UserStatusEnum.FROZEN) {
                throw new Exception("账号已被冻结");
            } else {
                throw new Exception("账号状态异常");
            }
        }

        // 4. 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new Exception("密码错误");
        }

        // 5. 生成 JWT
        String token = jwtTokenProvider.generateToken(user.getStudentId(), user.getId().toString()); // 使用学号和ID生成Token

        // 6. 构建返回结果
        LoginSuccessVO vo = new LoginSuccessVO();
        vo.setToken(token);
        vo.setTokenHead(jwtTokenProvider.getTokenHead()); // 从配置中获取 Token 前缀
        // 可以选择性地返回一些用户信息
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());

        return vo;
    }

    @Override
    public User findUserByIdentifier(String identifier) {
        // 尝试按学号查找，如果找不到再按邮箱查找
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 判断 identifier 格式是否像邮箱
        if (identifier != null && identifier.contains("@")) {
            queryWrapper.eq(User::getEmail, identifier);
        } else {
            queryWrapper.eq(User::getStudentId, identifier);
        }
        return userMapper.selectOne(queryWrapper);
    }
}