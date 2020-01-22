package net.nekocraft.nekocore;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

class RedStoneDetection implements Listener {
    final private Main plugin;
    private BukkitTask task;
    private boolean started = false;
    final private HashMap<Location, Integer> redStoneRecord = new HashMap<>();
    RedStoneDetection(Main plugin) {
        this.plugin = plugin;
    }
    @SuppressWarnings("WeakerAccess")
    void start() {
        if (started) return;
        if (plugin.op != null) plugin.op.sendMessage("¡ìeHigh frequency redstone detection ¡ìastarted.");
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, redStoneRecord::clear, 5 * 20L, 5 * 20L);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        started = true;
    }
    void stop() {
        if (!started) return;
        if (plugin.op != null) plugin.op.sendMessage("¡ìaHigh frequency redstone detection ¡ìcstoped.");
        BlockRedstoneEvent.getHandlerList().unregister(this);
        task.cancel();
        started = false;
    }
    void toggle() {
        if (started) stop(); else start();
    }

    @EventHandler
    void onBlockRedStone(BlockRedstoneEvent e) {
        final Location loc = e.getBlock().getLocation();
        int i = 0;
        try {
            i = redStoneRecord.get(loc);
        } catch (Exception ignored) { }
        redStoneRecord.put(loc, i == 0 ? (i = 1) : ++i);
        if (i >= 20) {
            if (plugin.op != null) plugin.op.sendMessage("¡ìcHigh frequency redstone signal detected: ¡ìeIn (" +
                loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," +
                loc.getBlockZ() + ")");
            redStoneRecord.remove(loc);
        }
    }
}
