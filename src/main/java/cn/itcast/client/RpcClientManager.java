package cn.itcast.client;

import cn.itcast.message.RpcRequestMessage;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import cn.itcast.protocol.SequenceIdGenerator;
import cn.itcast.server.handler.RpcResponseMessageHandler;
import cn.itcast.server.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static cn.itcast.server.handler.RpcResponseMessageHandler.PROMISES;


@Slf4j
public class RpcClientManager {
    private static Channel channel;
    private static final Object LOCK = new Object();
    public static void main(String[] args) {
        HelloService proxyService = getProxyService(HelloService.class);
        proxyService.sayHello("刘航必赢");
        proxyService.sayHello("全力冲刺");
    }

    public static <T> T getProxyService(Class<T> serviceClass){
        ClassLoader classLoader = serviceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceClass};

        Object proxyInstance = Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                int sid = SequenceIdGenerator.nextId();
                getChannle().writeAndFlush(new RpcRequestMessage(
                        sid,
                        serviceClass.getName(),
                        method.getName(),
                        method.getReturnType(),
                        method.getParameterTypes(),
                        args));
                // 接受信息是nioEventLoop在接受，这里线程要想获取结果通过promise.
                DefaultPromise<Object> promise = new DefaultPromise<>(getChannle().eventLoop());//第二个参数是执行异步处理处理的线程。这里是同步的方式主线程await等待。
                PROMISES.put(sid, promise); // 将「空书包promise」放进map中，供另一个线程放结果。
                // 等待返回结果
                promise.await();
                if (promise.isSuccess()){
                    Object resp = promise.getNow();
                    return resp;
                }else{
                    throw new RuntimeException(promise.cause());
                }


            }
        });
        return (T)proxyInstance;
    }
    // 单例获取channel
    private static Channel getChannle(){
        if (channel != null){
            return channel;
        }
        synchronized (LOCK) { //  t2等待t1的锁
            if (channel != null) { // t1拿到锁后去执行init,channel有值。 t2拿到锁后进入这里已经有锁了，所以应该直接返回。
                return channel;
            }
            initChannel();
            return channel;
        }

    }
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        // rpc 响应消息处理器，待实现
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProcotolFrameDecoder());
                ch.pipeline().addLast(LOGGING_HANDLER);
                ch.pipeline().addLast(MESSAGE_CODEC);
                ch.pipeline().addLast(RPC_HANDLER);
            }
        });
        try {
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().addListener(future->{
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }
}