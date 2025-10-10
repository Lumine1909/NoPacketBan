package io.github.lumine1909.nopacketban;

import io.github.lumine1909.nopacketban.handler.SafeDecoder;
import io.github.lumine1909.nopacketban.handler.SafeEncoder;
import io.github.lumine1909.nopacketban.handler.SafePrepender;
import io.github.lumine1909.nopacketban.handler.SafeSplitter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.function.Function;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        injectPlayer(e.getPlayer());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void injectPlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
        replaceHandler(channel, "encoder", encoder -> new SafeEncoder<>(player, (PacketEncoder) encoder));
        replaceHandler(channel, "decoder", decoder -> new SafeDecoder<>(player, (PacketDecoder) decoder));
        replaceHandler(channel, "prepender", prepender -> new SafePrepender(player, (Varint21LengthFieldPrepender) prepender));
        replaceHandler(channel, "splitter", splitter -> new SafeSplitter(player, (Varint21FrameDecoder) splitter));
    }

    private void replaceHandler(Channel channel, String id, Function<ChannelHandler, ChannelHandler> wrapper) {
        ChannelHandler handler = channel.pipeline().get(id);
        if (handler == null) {
            throw new RuntimeException("No handler found for id " + id);
        }
        channel.pipeline().replace(id, id, wrapper.apply(handler));
    }
}
