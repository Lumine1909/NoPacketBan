package io.github.lumine1909.nopacketban.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.Varint21FrameDecoder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
        public <T> T invoke(Object obj, Object... args) {
            try {
                return (T) method.invoke(obj, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
