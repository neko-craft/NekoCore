package net.nekocraft.nekocore;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;

import static org.bukkit.Material.*;

public class Main extends JavaPlugin implements Listener {
    private TextComponent c1, c2, c3, c4, c5, c6, c7, c8, c9, c20;
    private RedStoneDetection redStoneDetection = new RedStoneDetection(this);
    private int i = 0;
    private Thread thread;
    private HashSet<Player> notAccepts = new HashSet<>();
    private File acceptsFile = new File(getDataFolder(), "accepts.txt");
    private String accepts = "";
    Player op;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onEnable() {
        c1 = new TextComponent("  QQ 群: ");
        c1.setColor(ChatColor.GREEN);

        c2 = new TextComponent("7923309");
        c2.setColor(ChatColor.GRAY);
        c2.setUnderlined(true);
        c2.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://jq.qq.com/?k=5AzDYNC"));

        c3 = new TextComponent("      ");

        c4 = new TextComponent("Telegram 群组: ");
        c4.setColor(ChatColor.GREEN);

        c5 = new TextComponent("@NekoCraft");
        c5.setColor(ChatColor.GRAY);
        c5.setUnderlined(true);
        c5.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://t.me/NekoCraft"));

        c6 = new TextComponent("  用户中心 & 大地图: ");
        c6.setColor(ChatColor.GREEN);

        c7 = new TextComponent("portal.nekocraft.net");
        c7.setColor(ChatColor.GRAY);
        c7.setUnderlined(true);
        c7.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://portal.nekocraft.net"));

        c8 = new TextComponent("  服务器地址 & 官网: ");
        c8.setColor(ChatColor.GREEN);

        c9 = new TextComponent("n.apisium.cn");
        c9.setColor(ChatColor.GRAY);
        c9.setUnderlined(true);
        c9.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://n.apisium.cn"));

        c20 = new TextComponent("http://portal.nekocraft.net/about");
        c20.setColor(ChatColor.GREEN);
        c20.setUnderlined(true);
        c20.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://portal.nekocraft.net/about"));

        try {
            if (acceptsFile.exists()) accepts = new String(Files.readAllBytes(acceptsFile.toPath()));
            else if (!acceptsFile.createNewFile()) throw new IOException("Failed to create new file");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Server s = getServer();
        AntiExplode antiExplode = new AntiExplode();
        s.getPluginManager().registerEvents(antiExplode, this);
        s.getPluginManager().registerEvents(new TimeToSleep(this), this);
        s.getPluginManager().registerEvents(this, this);
        s.getPluginCommand("explode").setExecutor(antiExplode);
        s.getPluginCommand("show").setExecutor(new ShowItem());
        s.getPluginCommand("toggle").setExecutor(new Toggle(this));
        s.getPluginCommand("redstonedetect").setExecutor((sender, c, l, a) -> {
            redStoneDetection.toggle();
            return true;
        });
        s.getPluginCommand("acceptrule").setExecutor((sender, c, l, a) -> {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                String uuid = p.getUniqueId().toString();
                if (!accepts.contains(uuid)) {
                    notAccepts.remove(p);
                    String text = "," + uuid;
                    accepts += text;
                    try {
                        FileWriter writer = new FileWriter(acceptsFile, true);
                        writer.write(text);
                        writer.close();
                    } catch (IOException e) { e.printStackTrace(); }
                    p.sendMessage("§a感谢您接受了服务器的规定, §e同时也希望您能一直遵守规定!");
                }
            }
            return true;
        });

        DecimalFormat df = new DecimalFormat("0.00");

        thread = new Thread(() -> {
            try {
                while (true) {
                    double tps = s.getTPS()[0];
                    if (tps < 8) i++;
                    else i = 0;
                    if (i > 20) {
                        getServer().broadcastMessage("§c服务器 TPS 低, 将在五秒后自动重启!");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getServer().shutdown();
                        return;
                    }
                    if (!notAccepts.isEmpty()) notAccepts.forEach(it -> {
                        it.sendMessage("§e请点击下方链接打开服务器规定, 并仔细阅读以获取解除移动限制的方法:");
                        it.sendMessage(c20);
                    });
                    s.getOnlinePlayers().forEach(it -> it.setPlayerListFooter("\n§a当前 TPS: §7" + df.format(tps) +
                        "\n§b§m                                      "));
                    Thread.sleep(2000);
                }
            } catch (InterruptedException ignored) { }
        });

        thread.start();
        s.getScheduler().runTaskTimerAsynchronously(this, () -> getServer().getWorlds().forEach(it -> {
            Chunk[] ch = it.getLoadedChunks();
            for (Chunk c : ch) if (c.getEntities().length > 500) {
                getServer().getScheduler().runTask(this, () -> {
                    Entity[] es = c.getEntities();
                    for (Entity e : es) if (e instanceof Item || (e instanceof FallingBlock && !(e instanceof TNTPrimed)))
                        e.remove();
                    if (c.getEntities().length < 200) getServer().broadcastMessage("§c这个位置 §7(" + c.getWorld().getName() + ", " +
                        (c.getX() << 4) + ", " + (c.getZ() << 4) + ") §c有一大堆实体, 已被清除.");
                });
            }
        }), 0, 200);
    }

    @Override
    public void onDisable() {
        notAccepts.clear();
        thread.interrupt();
        thread = null;
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
        if (p.getName().equals("ShirasawaSama")) op = p;

        if (!accepts.contains(p.getUniqueId().toString())) notAccepts.add(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (e.getPlayer().getName().equals("ShirasawaSama")) {
            redStoneDetection.stop();
            op = null;
        }
        notAccepts.remove(e.getPlayer());
    }

    @EventHandler
    public void onKill(EntityDeathEvent e) {
        List<ItemStack> drops = e.getDrops();
        switch (e.getEntityType()) {
            case TURTLE:
                Player killer = e.getEntity().getKiller();
                int count = 2;
                if (killer != null) {
                    count += Math.round(((float) killer
                        .getInventory()
                        .getItemInMainHand()
                        .getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)) / 2);
                }
                drops.add(new ItemStack(Material.SCUTE, count));
                break;
            case RAVAGER:
                drops.clear();
                drops.add(new ItemStack(Material.LEATHER, 4));
                break;
            case VINDICATOR:
                drops.removeIf(it -> it.getType() == WHITE_BANNER || it.getType() == IRON_AXE);
                break;
            case EVOKER:
                drops.removeIf(is -> is.getType() == WHITE_BANNER);
                break;
            case PILLAGER:
                drops.removeIf(it -> it.getType() == WHITE_BANNER || it.getType() == CROSSBOW);
                break;
            case WITCH:
                drops.removeIf(is -> is.getType() == POTION);
                break;
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

    @EventHandler
    public void onPlayMove(PlayerMoveEvent e) {
        if (notAccepts.contains(e.getPlayer())) e.setCancelled(true);
    }
}
