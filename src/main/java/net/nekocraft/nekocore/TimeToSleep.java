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

public final class TimeToSleep implements Listener {
    private World w;
    private int current = 0;

    TimeToSleep() {
        w = Objects.requireNonNull(Bukkit.getWorld("world"));
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent e) {
        if (w.isDayTime() || e.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        current++;
        int all = 0;
        for (Player p : w.getPlayers()) {
            if (p.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD &&
                !p.getDisplayName().startsWith("¡ì7")) all++;
        }
        all = (int) Math.floor((float) all / 2);
        String str = e.getPlayer().getDisplayName() + " ¡ìbº°ÄãË¯¾õ¾õÀ²! ¡ì7(¡ìf" + current + "¡ì7 / ¡ìf" + all + "¡ì7)";
        if (all > current) {
            w.getPlayers().forEach(p -> {
                if (!p.isSleeping()) p.sendMessage(str);
            });
        } else {
            w.setTime(1000);
            current = 0;
            w.getPlayers().forEach(p -> p.sendActionBar("¡ìeÃşÁË, Ë¬µ½!"));
        }
    }

    @EventHandler
    public void onLeave(PlayerBedLeaveEvent e) {
        if (current > 0) current--;
        String str = e.getPlayer().getDisplayName() + " ¡ìe´¹ËÀ²¡ÖĞ¾ª×øÆğ!";
        w.getPlayers().forEach(p -> {
            if (p.isSleeping()) p.sendMessage(str);
        });
    }
}
