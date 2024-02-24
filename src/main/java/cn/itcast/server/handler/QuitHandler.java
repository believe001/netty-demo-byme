package cn.itcast.server.handler;

import cn.itcast.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class QuitHandler extends ChannelInboundHandlerAdapter{
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 正常断开连接触发, 将channel从session中移除
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{} 断开连接", ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 捕获到异常触发
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{} 断开连接，异常:{},{}", ctx.channel(), cause.getMessage(),cause);

    }
}
