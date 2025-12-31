package io.github.lumine1909.nopacketban.util;

import net.minecraft.network.BandwidthDebugMonitor;

public class DummyMonitor extends BandwidthDebugMonitor {

    public static final DummyMonitor INSTANCE = new DummyMonitor();

    @SuppressWarnings("DataFlowIssue")
    public DummyMonitor() {
        super(null);
    }

    @Override
    public void tick() {
    }

    @Override
    public void onReceive(int amount) {
    }
}