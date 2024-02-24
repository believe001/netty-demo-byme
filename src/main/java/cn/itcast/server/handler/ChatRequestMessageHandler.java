package cn.itcast.server.handler;

import cn.itcast.message.ChatRequestMessage;
import cn.itcast.message.ChatResponseMessage;
import cn.itcast.server.session.Session;
import cn.itcast.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
@ChannelHandler.Sharable
@Slf4j
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        try{
            System.out.println("读chatRequest消息");
            String to = msg.getTo();
            String content = msg.getContent();
            Channel toChannel = SessionFactory.getSession().getChannel(to);
//            log.debug("拿到channel{}",toChannel.toString());
            if (toChannel != null){
                // 在线
                toChannel.writeAndFlush(new ChatResponseMessage(msg.getFrom(), content));

            }else{
                // 不在线，给发送者返回消息
                ctx.channel().writeAndFlush(new ChatResponseMessage(false, "您发送的用户不存在或者不在线"));
            }

        }catch (Exception e){
            log.debug("{}",e);
        }


    }
}
