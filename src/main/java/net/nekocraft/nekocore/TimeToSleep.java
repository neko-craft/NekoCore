package net.nekocraft.nekocore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import java.util.Objects;
import java.util.Random;

public final class TimeToSleep implements Listener {
    private World w;
    private int current = 0;
    private Main plugin;

    TimeToSleep(Main plugin) {
        this.plugin = plugin;
        w = Objects.requireNonNull(Bukkit.getWorld("world"));
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent e) {
        if (w.isDayTime() || e.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        current++;
        int all = getAll();
        String str = (e.getPlayer().getName().startsWith("Lulu")
            ? "¡ìaÂ¶Â¶" : e.getPlayer().getDisplayName()) +
            " ¡ìbº°ÄãË¯¾õ¾õÀ²! ¡ì7(¡ìf" + current + "¡ì7 / ¡ìf" + all + "¡ì7)";
        w.getPlayers().forEach(p -> p.sendMessage(str));
        if (all <= current) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                w.setTime(new Random().nextInt(2000));
                current = 0;
                w.getPlayers().forEach(p -> p.sendActionBar("¡ìeÃþÁË, Ë¬µ½!"));
            }, 20 * 5);
        }
    }

    @EventHandler
    public void onLeave(PlayerBedLeaveEvent e) {
        if (w.isDayTime()) return;
        if (current > 0) current--;
        String str = e.getPlayer().getDisplayName() + " ¡ìe´¹ËÀ²¡ÖÐ¾ª×øÆð! ¡ì7(¡ìf" + current + "¡ì7 / ¡ìf" + getAll() + "¡ì7)";
        w.getPlayers().forEach(p -> p.sendMessage(str));
    }

    private int getAll() {
        int all = 0;
        for (Player p : w.getPlayers()) {
            if (p.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD &&
                !p.getDisplayName().startsWith("¡ì7")) all++;
        }
        return (int) Math.floor((float) all / 2);
    }
}
