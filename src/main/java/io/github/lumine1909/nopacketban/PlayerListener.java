package io.github.lumine1909.nopacketban;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import io.github.lumine1909.nopacketban.handler.DecodeChecker;
import io.github.lumine1909.nopacketban.handler.EncodeChecker;
import io.github.lumine1909.nopacketban.handler.PrependChecker;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PlayerListener implements Listener {

    public static final Map<String, Player> injectedPlayer = new HashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void injectPlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
        addOutboundChecker(channel, "encoder", encoder -> new EncodeChecker<>(player, (PacketEncoder) encoder));
        addInboundChecker(channel, "decoder", decoder -> new DecodeChecker<>(player, (PacketDecoder) decoder));
        addOutboundChecker(channel, "prepender", prepender -> new PrependChecker(player));
        //addInboundChecker(channel, "splitter", splitter -> new SplitChecker(player));
        injectedPlayer.put(player.getName(), player);
    }

    public static void uninjectPlayer(Player player) {
        if (player == null) {
            return;
        }
        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().connection.connection.channel.pipeline();
        try {
            pipeline.remove("encoder_checker");
            pipeline.remove("decoder_checker");
            pipeline.remove("prepender_checker");
            //pipeline.remove("splitter_checker");
        } catch (Exception ignored) {
        }
        injectedPlayer.remove(player.getName());
    }

    private static void addOutboundChecker(Channel channel, String id, Function<ChannelHandler, ChannelHandler> creator) {
        ChannelHandler handler = channel.pipeline().get(id);
        if (handler == null) {
            throw new RuntimeException("No handler found for id " + id);
        }
        if (channel.pipeline().get(id + "_checker") != null) {
            channel.pipeline().remove(id + "_checker");
        }
        channel.pipeline().addAfter(id, id + "_checker", creator.apply(handler));
    }

    private static void addInboundChecker(Channel channel, String id, Function<ChannelHandler, ChannelHandler> creator) {
        ChannelHandler handler = channel.pipeline().get(id);
        if (handler == null) {
            throw new RuntimeException("No handler found for id " + id);
        }
        if (channel.pipeline().get(id + "_checker") != null) {
            channel.pipeline().remove(id + "_checker");
        }
        channel.pipeline().addBefore(id, id + "_checker", creator.apply(handler));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        injectPlayer(e.getPlayer());
    }

    @EventHandler
    public void onDisconnect(PlayerConnectionCloseEvent e) {
        uninjectPlayer(injectedPlayer.get(e.getPlayerName()));
    }
}
