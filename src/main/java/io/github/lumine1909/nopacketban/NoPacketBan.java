package io.github.lumine1909.nopacketban;

import io.github.lumine1909.nopacketban.metrics.Metrics;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NoPacketBan extends JavaPlugin {

    private static final Key LISTENER_KEY = Key.key("nopacketban:listener");

    public static NoPacketBan plugin;
    public static int RESERVED_PACKET_SIZE;
    public static int MAX_PACKET_SIZE = 2097151;
    public static boolean LOG_PACKET_EXCEPTIONS = false;

    private Metrics metrics;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        ChannelInitializeListenerHolder.addListener(LISTENER_KEY, Injector::inject);
        metrics = new Metrics(this, 27531);
        RESERVED_PACKET_SIZE = getConfig().getInt("reserved-packet-size", getConfig().getInt("preserved-packet-size", 262144));
        LOG_PACKET_EXCEPTIONS = getConfig().getBoolean("log-packet-exceptions", false);
        if (Bukkit.getPluginManager().getPlugin("ViaVersion") == null) {
            RESERVED_PACKET_SIZE = 0;
        }
        MAX_PACKET_SIZE = 2097151 - RESERVED_PACKET_SIZE;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Injector.injectPlayer(player);
        }
    }

    @Override
    public void onDisable() {
        if (metrics != null) {
            metrics.shutdown();
        }
        ChannelInitializeListenerHolder.removeListener(LISTENER_KEY);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Injector.uninjectPlayer(player);
        }
    }
}