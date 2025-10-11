package io.github.lumine1909.nopacketban;

import io.github.lumine1909.nopacketban.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NoPacketBan extends JavaPlugin {

    private Metrics metrics;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        metrics = new Metrics(this, 27531);
    }

    @Override
    public void onDisable() {
        if (metrics != null) {
            metrics.shutdown();
        }
    }
}
