package cn.itcast.server.service;

/**
 * 用户管理接口, 返回用户登录结果
 */
public interface UserService {

    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回 true, 否则返回 false
     */
    boolean login(String username, String password);
}
