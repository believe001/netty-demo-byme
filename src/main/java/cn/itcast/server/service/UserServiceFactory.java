package cn.itcast.server.service;

/**
 * 工厂类，获取一个UserService实例
 */
public abstract class UserServiceFactory {

    private static UserService userService = new UserServiceMemoryImpl();

    public static UserService getUserService() {
        return userService;
    }
}
