package cn.itcast.server.handler;

import cn.itcast.message.RpcRequestMessage;
import cn.itcast.message.RpcResponseMessage;
import cn.itcast.server.service.HelloService;
import cn.itcast.server.service.ServicesFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
@Slf4j
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg){
        RpcResponseMessage rpcResp = new RpcResponseMessage();
        rpcResp.setSequenceId(msg.getSequenceId());
        HelloService service = null;
        try {
            service = (HelloService) ServicesFactory.getService(Class.forName(msg.getInterfaceName()));
            Method method = service.getClass().getMethod(msg.getMethodName(),msg.getParameterTypes());
            Object invoke = method.invoke(service, msg.getParameterValue());
            rpcResp.setReturnValue(invoke);
        } catch (Exception e) {
            rpcResp.setExceptionValue(new Exception("远程调用失败"+e.getCause().getMessage()));
            e.printStackTrace();
        }
        log.debug("server send msg");
        ctx.writeAndFlush(rpcResp);

    }

    public static void main(String[] args) {
        RpcRequestMessage rpcRequestMessage = new RpcRequestMessage(
                1,
                "cn.itcast.server.service.HelloService",
                "sayHello",
                String.class,
                new Class[]{String.class},
                new Object[]{"张三"});
        try {
            HelloService service = (HelloService)ServicesFactory.getService(Class.forName(rpcRequestMessage.getInterfaceName()));
            Method method = service.getClass().getMethod(rpcRequestMessage.getMethodName(),rpcRequestMessage.getParameterTypes());
            String invoke = (String) method.invoke(service, rpcRequestMessage.getParameterValue());
            System.out.println(invoke);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
