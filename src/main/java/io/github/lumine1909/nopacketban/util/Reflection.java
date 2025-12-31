package io.github.lumine1909.nopacketban.util;

import io.github.lumine1909.reflexion.Field;
import io.github.lumine1909.reflexion.Method;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.ProtocolInfo;

import java.util.List;

public class Reflection {

    public static final Field<ProtocolInfo<?>> encoderProtocolInfo = Field.of(
        PacketEncoder.class, "protocolInfo"
    );
    public static final Field<ProtocolInfo<?>> decoderProtocolInfo = Field.of(
        PacketDecoder.class, "protocolInfo"
    );
    public static final Method<Void> messageToByteEncode = Method.of(
        MessageToByteEncoder.class, "encode",
        void.class,
        ChannelHandlerContext.class, Object.class, ByteBuf.class
    );
    public static final Method<Void> byteToMessageDecode = Method.of(
        ByteToMessageDecoder.class, "decode",
        void.class,
        ChannelHandlerContext.class, ByteBuf.class, List.class
    );
}
