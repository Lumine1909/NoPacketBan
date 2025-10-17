package io.github.lumine1909.nopacketban.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class Reflection {

    public static final FieldAccessor encoderProtocolInfo = FieldAccessor.of(
        PacketEncoder.class, "protocolInfo"
    );
    public static final FieldAccessor decoderProtocolInfo = FieldAccessor.of(
        PacketDecoder.class, "protocolInfo"
    );
    public static final MethodAccessor messageToByteEncode = MethodAccessor.of(
        MessageToByteEncoder.class, "encode",
        ChannelHandlerContext.class, Object.class, ByteBuf.class
    );
    public static final MethodAccessor byteToMessageDecode = MethodAccessor.of(
        ByteToMessageDecoder.class, "decode",
        ChannelHandlerContext.class, ByteBuf.class, List.class
    );

    public record FieldAccessor(Field field) {

        public static FieldAccessor of(Class<?> clazz, String name) {
            try {
                Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                return new FieldAccessor(f);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        public <T> T get(Object obj) {
            try {
                return (T) field.get(obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void set(Object obj, Object value) {
            try {
                field.set(obj, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public record MethodAccessor(Method method) {

        public static MethodAccessor of(Class<?> clazz, String name, Class<?>... parameterTypes) {
            try {
                Method m = clazz.getDeclaredMethod(name, parameterTypes);
                m.setAccessible(true);
                return new MethodAccessor(m);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        public <T> T invoke(Object obj, Object... args) throws InvocationTargetException {
            try {
                return (T) method.invoke(obj, args);
            } catch (InvocationTargetException e) {
                throw e;
            } catch (Exception other) {
                throw new RuntimeException(other);
            }
        }
    }
}
