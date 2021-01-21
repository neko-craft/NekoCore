package net.nekocraft.nekocore;

import net.nekocraft.nekocore.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import java.util.Objects;
import java.util.Random;

final class TimeToSleep implements Listener {
    private final World w;
    private int current = 0;
    private final Main plugin;

    TimeToSleep(final Main plugin) {
        this.plugin = plugin;
        w = Objects.requireNonNull(plugin.getServer().getWorld("world"));
    }

    @EventHandler
    public void onSleep(final PlayerBedEnterEvent e) {
        if (w.isDayTime() || e.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        current++;
        int all = getAll();
        final Player p = e.getPlayer();
        final String str = Utils.getDisplayName(p) +
            " ¡ìbº°ÄãË¯¾õ¾õÀ²! ¡ì7(¡ìf" + current + "¡ì7 / ¡ìf" + all + "¡ì7)";
        w.getPlayers().forEach(pl -> pl.sendActionBar(str));
        if (all <= current) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (all > current) return;
                w.setTime(new Random().nextInt(2000));
                w.setThundering(false);
                w.setStorm(false);
                current = 0;
                w.getPlayers().forEach(pl -> p.sendActionBar("¡ìeÃþÁË, Ë¬µ½!"));
            }, 20 * 8);
        }
    }

    @EventHandler
    public void onLeave(final PlayerBedLeaveEvent e) {
        if (w.isDayTime()) return;
        if (current > 0) current--;
        final String str = e.getPlayer().getDisplayName() + " ¡ìe´¹ËÀ²¡ÖÐ¾ª×øÆð! ¡ì7(¡ìf" + current + "¡ì7 / ¡ìf" + getAll() + "¡ì7)";
        w.getPlayers().forEach(p -> p.sendActionBar(str));
    }

    private int getAll() {
        int all = 0;
        for (final Player p : w.getPlayers()) {
            if (p.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD &&
                !p.getPlayerListName().startsWith("¡ì7") && p.getGameMode() == GameMode.SURVIVAL) all++;
        }
        return (int) Math.floor((float) all / 2);
    }
}
