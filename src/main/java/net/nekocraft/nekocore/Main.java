package net.nekocraft.nekocore;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    private TextComponent c1, c2, c3, c4, c5, c6, c7, c8, c9;
    @Override
    public void onEnable() {
        c1 = new TextComponent("  QQ 群: ");
        c1.setColor(ChatColor.GREEN);

        c2 = new TextComponent("7923309");
        c2.setColor(ChatColor.GRAY);
        c2.setUnderlined(true);
        c2.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://jq.qq.com/?k=5AzDYNC"));

        c3 = new TextComponent("      ");

        c4 = new TextComponent("Telegream 群组: ");
        c4.setColor(ChatColor.GREEN);

        c5 = new TextComponent("@NekoCraft");
        c5.setColor(ChatColor.GRAY);
        c5.setUnderlined(true);
        c5.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://t.me/NekoCraft"));

        c6 = new TextComponent("  网页地图: ");
        c6.setColor(ChatColor.GREEN);

        c7 = new TextComponent("nekocraft.net/maps");
        c7.setColor(ChatColor.GRAY);
        c7.setUnderlined(true);
        c7.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://nekocraft.net/maps"));

        c8 = new TextComponent("  服务器地址 & 用户中心: ");
        c8.setColor(ChatColor.GREEN);

        c9 = new TextComponent("nekocraft.net");
        c9.setColor(ChatColor.GRAY);
        c9.setUnderlined(true);
        c9.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://nekocraft.net"));

        getServer().getPluginManager().registerEvents(new TimeToSleep(), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginCommand("show").setExecutor(new ShowItem());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Server s = getServer();
        p.sendMessage("§b§m                   §r §a[§eNekoCraft§a] §b§m                  §r\n");
        p.sendMessage("  §a当前在线玩家: §7" + s.getOnlinePlayers().size() + "                     §a当前TPS: " + (int) s.getTPS()[0]);
        p.sendMessage(c1, c2, c3, c4, c5);
        p.sendMessage(c6, c7);
        p.sendMessage(c8, c9);
        p.sendMessage("  §c由于服务器没有领地插件, 请不要随意拿取他人物品, 否则会直接封禁!");
        p.sendMessage("§b§m                                                      §r\n\n\n");
    }
}
