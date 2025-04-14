package com.campus.trade.security;

import com.campus.trade.entity.User;
import com.campus.trade.enums.UserStatusEnum;
import com.campus.trade.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Spring Security 需要的 UserDetailsService 实现类
 * 用于根据用户名加载用户信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private IUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Spring Security 的 username 在这里对应我们的学号或邮箱
        User user = userService.findUserByIdentifier(username);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 检查用户状态，非激活状态不允许登录认证
        if (user.getStatus() != UserStatusEnum.ACTIVATED) {
            // 实际项目中可以根据不同状态抛出更具体的异常或返回不同的 UserDetails 实现
            // 例如：DisabledException, LockedException
            // 这里简化处理，也抛出 UsernameNotFoundException，让后续认证失败
             throw new UsernameNotFoundException("用户状态异常: " + username);
        }

        // 设置用户权限 (这里简化处理，所有普通用户都赋予 'ROLE_USER' 权限)
        // 如果有管理员等角色，需要根据 user.getRole() 等字段来设置
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        // 返回 Spring Security 的 UserDetails 对象
        return new org.springframework.security.core.userdetails.User(
                user.getStudentId(), // 使用学号作为 Spring Security 的 username
                user.getPassword(),
                authorities
        );
    }
}