package cn.itcast.server.handler;

import cn.itcast.message.RpcResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    public static final ConcurrentHashMap<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        Exception exceptionValue = msg.getExceptionValue();
        Object returnValue = msg.getReturnValue();
        Promise<Object> promise = PROMISES.remove(msg.getSequenceId()); // 获取到后，一定要移除PROMISE。不然MAP里面promise越来越多。
        if (exceptionValue != null){
            promise.setFailure(msg.getExceptionValue());
        }else{
            promise.setSuccess(returnValue);
        }
    }
}
