package com.sec.service.protal;

import com.sec.common.Codes;
import com.sec.common.Const;
import com.sec.common.Resp;
import com.sec.dao.UserMapper;
import com.sec.exception.NoLoginException;
import com.sec.pojo.User;
import com.sec.service.func.MailService;
import com.sec.service.func.RedisService;
import com.sec.util.MD5Util;
import com.sec.util.ThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private RedisService redisService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserMapper userMapper;

    public User getUserByToken() {
        String tokenVal = ThreadLocalUtil.getThreadLocalToken();
        if(tokenVal == null) {
            throw new NoLoginException();
        }

        return redisService.get(RedisService.USER_TOKEN_PREFIX, tokenVal, User.class);
    }

    public Resp sendCode(User user) {
        // 注册密码校验
        if(StringUtils.isBlank(user.getPassword())) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "注册密码不能是空");
        }

        // 获取字段
        String email = user.getEmail();

        // 避免重复发送验证码
        String oldCode = redisService.get(redisService.USER_REGISTER_CODE_PREFIX, email, String.class);
        if(StringUtils.isNotBlank(oldCode)) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "60秒内不要重复发送验证码");
        }

        // 先查询数据库
        User dbUser = userMapper.selectByEmail(email);
        // 数据库里已经存在了用户
        if(dbUser != null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "该用户已经存在了");
        }

        // todo 放入redis, 缓存60秒
        String randomCode = this.randomCode();
        redisService.set(redisService.USER_REGISTER_CODE_PREFIX, email, randomCode, 60);

        // todo 发送验证码
        String title = "欢迎注册";
        String content = String.format("本次验证码: %s   (有效时间60秒)", randomCode);
        mailService.sendHtmlMail(email, title, content);

        return Resp.success(true);
    }

    public Resp register(User user, String code) {
        // 注册密码校验
        if(StringUtils.isBlank(user.getPassword())) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "注册密码不能是空");
        }

        // 验证码校验
        if(StringUtils.isBlank(code)) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "验证码不能是空");
        }

        // 获取字段
        String email = user.getEmail();

        // 先查询数据库
        User dbUser = userMapper.selectByEmail(email);
        // 数据库里已经存在了用户
        if(dbUser != null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "该用户已经存在了");
        }

        // todo 从redis中取出验证码校验
        String redisDbCode = redisService.get(redisService.USER_REGISTER_CODE_PREFIX, email, String.class);
        if(!StringUtils.equalsIgnoreCase(redisDbCode, code)) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "验证码错误");
        }

        // 验证通过, 开始创建新用户
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(MD5Util.getMD5(user.getPassword()));  // md5加密放入数据库
        newUser.setNickname(user.getNickname());
        newUser.setAvatar(user.getAvatar());
        newUser.setPhone(user.getPhone());
        newUser.setLoginCount(0);
        newUser.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newUser.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        newUser.setLastLoginTime(user.getLastLoginTime());

        // 插入数据库
        int result = userMapper.insertSelective(newUser);
        if(result <= 0) {
            return Resp.error(Codes.ERROR.getCode(), "数据库未知错误");
        }

        return Resp.success(true);
    }

    public Resp login(User user) {
        if(user == null || StringUtils.isBlank(user.getEmail()) || StringUtils.isBlank(user.getPassword())) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT);
        }

        // 登录
        String email = user.getEmail();
        String password = user.getPassword();

        User dbUser = userMapper.selectByEmail(email);
        // 校验用户
        if(dbUser == null) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "没有此用户");
        }

        // 校验密码
        if(!MD5Util.verify(password, dbUser.getPassword())) {
            return Resp.error(Codes.ILLEGAL_ARGUMENT.getCode(), "密码错误");
        }

        // 到此处, 登录成功
        // 更新登录时间和登陆次数
        dbUser.setLoginCount(dbUser.getLoginCount() + 1);
        Timestamp curTime = new Timestamp(System.currentTimeMillis());
        dbUser.setLastLoginTime(curTime);
        userMapper.updateByPrimaryKeySelective(dbUser);

        // 去掉密码
        dbUser.setPassword("******");

        // 写入redis, 这里暂时允许多浏览器登录, 但是一个浏览器只保留一个token
        String token = UUID.randomUUID().toString();
        String oldToken = ThreadLocalUtil.getThreadLocalToken();
        if(oldToken != null) {
            redisService.del(RedisService.USER_TOKEN_PREFIX, oldToken);
        }
        redisService.set(RedisService.USER_TOKEN_PREFIX, token, dbUser);

        // 写入Cookie
        Cookie cookie = new Cookie(Const.COOKIE_TOKEN_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(RedisService.DEFAULT_EXPIRE_TIME);
        cookie.setPath("/");
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        response.addCookie(cookie);

        return Resp.success(true);
    }

    public Resp logout() {
        String token = ThreadLocalUtil.getThreadLocalToken();
        if(token != null) {
            // 移除redis缓存
            redisService.del(RedisService.USER_TOKEN_PREFIX, token);

            // 移除Cookie
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            HttpServletResponse response = servletRequestAttributes.getResponse();
            Cookie cookie = new Cookie(Const.COOKIE_TOKEN_NAME, "");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        return Resp.success(true);

    }

    /**
     * 生成6位随机数字的字符串
     * @return
     */
    private String randomCode() {
        StringBuilder stb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            stb.append(random.nextInt(10));
        }
        return stb.toString();
    }

}
