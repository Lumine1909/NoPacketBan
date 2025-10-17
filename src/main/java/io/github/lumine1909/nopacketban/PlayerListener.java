package io.github.lumine1909.nopacketban;

import io.github.lumine1909.nopacketban.handler.DecodeChecker;
import io.github.lumine1909.nopacketban.handler.EncodeChecker;
import io.github.lumine1909.nopacketban.handler.PrependChecker;
import io.github.lumine1909.nopacketban.handler.SplitChecker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
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
        addOutboundChecker(channel, "encoder", encoder -> new EncodeChecker<>(player, (PacketEncoder) encoder));
        addInboundChecker(channel, "decoder", decoder -> new DecodeChecker<>(player, (PacketDecoder) decoder));
        addOutboundChecker(channel, "prepender", prepender -> new PrependChecker(player));
        //addInboundChecker(channel, "splitter", splitter -> new SplitChecker(player));
    }

    private void addOutboundChecker(Channel channel, String id, Function<ChannelHandler, ChannelHandler> creator) {
        ChannelHandler handler = channel.pipeline().get(id);
        if (handler == null) {
            throw new RuntimeException("No handler found for id " + id);
        }
        channel.pipeline().addAfter(id, id + "_checker", creator.apply(handler));
    }

    private void addInboundChecker(Channel channel, String id, Function<ChannelHandler, ChannelHandler> creator) {
        ChannelHandler handler = channel.pipeline().get(id);
        if (handler == null) {
            throw new RuntimeException("No handler found for id " + id);
        }
        channel.pipeline().addBefore(id, id + "_checker", creator.apply(handler));
    }
}
