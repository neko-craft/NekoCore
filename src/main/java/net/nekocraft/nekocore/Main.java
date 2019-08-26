package net.nekocraft.nekocore;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;

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

        Server s = getServer();
        s.getPluginManager().registerEvents(new TimeToSleep(this), this);
        s.getPluginManager().registerEvents(this, this);
        s.getPluginCommand("show").setExecutor(new ShowItem());
        s.getPluginCommand("toggle").setExecutor(new Toggle(this));

        DecimalFormat df = new DecimalFormat("0.00");

        s.getScheduler().runTaskTimerAsynchronously(this, () -> {
            String text = "\n§a当前 TPS: §7" + df.format(s.getTPS()[0]) +
                "\n§b§m                                      ";
            s.getOnlinePlayers().forEach(it -> it.setPlayerListFooter(text));
        }, 0, 3 * 20);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Server s = getServer();
        p.setPlayerListHeader("§b§m          §r §a[§eNekoCraft§a] §b§m          \n§aTelegream 群组: §7t.me/NekoCraft\n§aQQ 群: §77923309\n§r");
        p.sendMessage("§b§m                   §r §a[§eNekoCraft§a] §b§m                  §r");
        p.sendMessage("  §a当前在线玩家: §7" + s.getOnlinePlayers().size() + "                     §a当前TPS: " + (int) s.getTPS()[0]);
        p.sendMessage(c1, c2, c3, c4, c5);
        p.sendMessage(c6, c7);
        p.sendMessage(c8, c9);
        p.sendMessage("  §c由于服务器没有领地插件, 请不要随意拿取他人物品, 否则会直接封禁!");
        p.sendMessage("§b§m                                                      §r\n\n\n");
    }

    @EventHandler
    public void onKill(EntityDeathEvent e) {
        if (e.getEntityType() == EntityType.TURTLE) {
            Player killer = e.getEntity().getKiller();
            int count = 2;
            if (killer != null) {
                count += Math.round(((float) killer
                    .getInventory()
                    .getItemInMainHand()
                    .getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)) / 2);
            }
            e.getDrops().add(new ItemStack(Material.SCUTE, count));
        }
    }

    @EventHandler
    public void onJump(PlayerInteractEvent e) {
        if (e.getAction() != Action.PHYSICAL) return;
        Block b = e.getClickedBlock();
        if (b != null && b.getType() == Material.FARMLAND) e.setCancelled(true);
    }

    @EventHandler
    public void onMobJump(EntityInteractEvent e) {
        if (e.getEntityType() != EntityType.PLAYER &&
            e.getBlock().getType() == Material.FARMLAND) e.setCancelled(true);
    }

    @EventHandler
    public void onLightingStrike(LightningStrikeEvent e) {
        if (e.getCause() == LightningStrikeEvent.Cause.TRIDENT ||
            e.getCause() == LightningStrikeEvent.Cause.COMMAND) return;
        for (Entity it : e.getLightning().getNearbyEntities(5, 5, 5)) {
            if (it.getType() == EntityType.VILLAGER) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onVillagerSpawn(EntitySpawnEvent e) {
        if (e.getEntityType() == EntityType.VILLAGER) {
            int i = 0;
            Location l = e.getLocation();
            for (Entity it : l.getNearbyEntities(48, 48, 48)) {
                if (it.getType() == EntityType.VILLAGER) i++;
            }
            if (i > 50) {
               Bukkit.broadcastMessage("§c有人在 §7" + l.getBlockX() + "," + l.getBlockY() + "," +
                   l.getBlockZ() + " §c大量繁殖村民.");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        StringBuilder sb = new StringBuilder();
        for (String s : e.getMessage().split(" ")) {
            Player p = Bukkit.getPlayer(s);
            if (p != null) {
                sb.append("§a@").append(s).append("§7");
                p.sendMessage("§a一位叫 §f" + e.getPlayer().getDisplayName() + " §a的小朋友@了你.");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else sb.append(s);
            sb.append(' ');
        }
        e.setMessage(sb.toString());
    }
}
