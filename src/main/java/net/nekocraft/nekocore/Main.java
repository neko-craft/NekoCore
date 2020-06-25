package net.nekocraft.nekocore;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.nekocraft.nekocore.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Pattern;

import static org.bukkit.Material.*;
import static net.nekocraft.nekocore.utils.Utils.registerCommand;

@Plugin(name = "NekoCore", version = "1.0")
@Description("An basic plugin used in NekoCraft.")
@Author("Shirasawa")
@Website("https://apisium.cn")
@ApiVersion(ApiVersion.Target.v1_13)
@Dependency("NekoEssentials")
@Permission(name = "neko.show", defaultValue = PermissionDefault.TRUE)
@Permission(name = "neko.explode")
@Permission(name = "neko.rsd")
@Permission(name = "neko.notdeatheffect")
@Command(name = "show", permission = "neko.show")
@Command(name = "explode", permission = "neko.explode")
@Command(name = "rsd", permission = "neko.rsd")
@Command(name = "acceptrule")
@Command(name = "denyrule")
public final class Main extends JavaPlugin implements Listener {
    private int i = 0;
    private Thread thread;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @SuppressWarnings({"BusyWait", "ResultOfMethodCallIgnored"})
    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        final Server s = getServer();
        final PluginManager m = s.getPluginManager();
        final AntiExplode antiExplode = new AntiExplode();
        final Rules rules = new Rules(this);
        m.registerEvents(antiExplode, this);
        m.registerEvents(rules, this);
        m.registerEvents(new TimeToSleep(this), this);
        m.registerEvents(this, this);
        registerCommand("explode", antiExplode);
        registerCommand("show", new ShowItem());
        registerCommand("rsd", new RedStoneDetection(this));
        registerCommand("acceptrule", rules);

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
        if (thread == null) return;
        thread.interrupt();
        thread = null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage("§c- " + Utils.getDisplayName(e.getPlayer()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("§a+ " + Utils.getDisplayName(e.getPlayer()));
        Player p = e.getPlayer();
        Server s = getServer();
        p.setPlayerListHeader(Constants.PLAYER_HEADER);
        p.sendMessage(Constants.JOIN_MESSAGE_HEADER);
        p.sendMessage("  §a当前在线玩家: §7" + s.getOnlinePlayers().size() +
                "                     §a当前TPS: " + (int) s.getTPS()[0]);
        p.sendMessage(Constants.JOIN_MESSAGES);
        p.sendMessage(Constants.JOIN_MESSAGE1);
        p.sendMessage(Constants.JOIN_MESSAGE_FOOTER);
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
    public void onEntitySpawn(EntitySpawnEvent e) {
        switch (e.getEntityType()) {
            case BAT:
                e.setCancelled(true);
                break;
            case VILLAGER:
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

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        e.setFormat("%1$s§7: %2$s");
        StringBuilder sb = new StringBuilder();
        for (String s : e.getMessage().split(" ")) {
            Player p = getServer().getPlayerExact(s);
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
    public void onPlayerPostRespawn(PlayerPostRespawnEvent e) {
        final Player p = e.getPlayer();
        if (!p.hasPermission("neko.notdeatheffect")) p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20 * 60 * 3, 8, true, false));
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        switch (e.getEntityType()) {
            case CREEPER:
            case FIREBALL:
            case SMALL_FIREBALL:
            case DRAGON_FIREBALL:
            case ENDER_DRAGON:
            case WITHER_SKULL:
                e.blockList().clear();
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager().getType() == EntityType.CREEPER && !(e.getEntity() instanceof Monster)) e.setCancelled(true);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (e.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        e.setCancelled(true);
    }

    private boolean isDangerCommand(final String cmd) {
        for (final Pattern c : Constants.DANGER_COMMANDS) if (c.matcher(cmd).matches()) return true;
        return false;
    }
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (isDangerCommand(e.getMessage())) {
            e.getPlayer().sendMessage("§c危险的指令已被拒绝执行!");
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onServerCommand(ServerCommandEvent e) {
        if (isDangerCommand(e.getCommand())) {
            e.getSender().sendMessage("§c危险的指令已被拒绝执行!");
            e.setCancelled(true);
        }
    }
}
