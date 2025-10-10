package io.github.lumine1909.nopacketban.util;

import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.Varint21FrameDecoder;

import java.lang.reflect.Field;

public class Reflection {

    public static final FieldAccessor encoderProtocolInfo = FieldAccessor.of(
        PacketEncoder.class, "protocolInfo"
    );
    public static final FieldAccessor decoderProtocolInfo = FieldAccessor.of(
        PacketDecoder.class, "protocolInfo"
    );
    public static final FieldAccessor monitor = FieldAccessor.of(
        Varint21FrameDecoder.class, "monitor"
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
}
