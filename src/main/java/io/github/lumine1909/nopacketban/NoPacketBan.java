package io.github.lumine1909.nopacketban;

import io.github.lumine1909.nopacketban.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NoPacketBan extends JavaPlugin {

    public static int PRESERVED_PACKET_SIZE;
    public static int MAX_NORMAL_PACKET_SIZE = 2097151;
    public static int MAX_PACKET_SIZE = 8388608;
    public static boolean hasVia = true;

    private Metrics metrics;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        metrics = new Metrics(this, 27531);
        PRESERVED_PACKET_SIZE = getConfig().getInt("preserved-packet-size", 0);
        if (Bukkit.getPluginManager().getPlugin("ViaVersion") == null) {
            PRESERVED_PACKET_SIZE = 0;
            hasVia = false;
        } else {
            hasVia = true;
        }
        MAX_NORMAL_PACKET_SIZE = 2097151 - PRESERVED_PACKET_SIZE;
        MAX_PACKET_SIZE = 8388608 - PRESERVED_PACKET_SIZE;

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerListener.injectPlayer(player);
        }
    }

    @Override
    public void onDisable() {
        if (metrics != null) {
            metrics.shutdown();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerListener.uninjectPlayer(player);
        }
        PlayerListener.injectedPlayer.clear();
    }
}