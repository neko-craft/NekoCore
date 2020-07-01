package net.nekocraft.nekocore;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.nekocraft.nekocore.utils.Utils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
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
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.bukkit.Material.*;
import static net.nekocraft.nekocore.utils.Utils.registerCommand;

@Plugin(name = "NekoCore", version = "1.0")
@Description("An basic plugin used in NekoCraft.")
@Author("Shirasawa")
@Website("https://apisium.cn")
@ApiVersion(ApiVersion.Target.v1_13)
@Permission(name = "neko.show", defaultValue = PermissionDefault.TRUE)
@Permission(name = "neko.explode")
@Permission(name = "neko.rsd")
@Permission(name = "neko.notdeatheffect")
@Command(name = "show", permission = "neko.show")
@Command(name = "explode", permission = "neko.explode")
@Command(name = "rsd", permission = "neko.rsd")
@Command(name = "acceptrule")
@Command(name = "denyrule")
@SuppressWarnings("unused")
public final class Main extends JavaPlugin implements Listener {
    private int i = 0;
    private Thread thread;
    private static final HashMap<String, Object[]> deathRecords = new HashMap<>();
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
                    final double tps = s.getTPS()[0];
                    if (tps < 8) i++;
                    else i = 0;
                    if (i > 20) {
                        getServer().broadcastMessage("��c������ TPS ��, ����������Զ�����!");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getServer().shutdown();
                        return;
                    }
                    s.getOnlinePlayers().forEach(it -> it.setPlayerListFooter("\n��a��ǰ TPS: ��7" + df.format(tps) +
                        "\n��b��m                                      "));
                    Thread.sleep(2000);
                }
            } catch (InterruptedException ignored) { }
        });

        thread.start();
        s.getScheduler().runTaskTimerAsynchronously(this, () -> getServer().getWorlds().forEach(it -> {
            final Chunk[] ch = it.getLoadedChunks();
            for (final Chunk c : ch) if (c.getEntities().length > 500) {
                getServer().getScheduler().runTask(this, () -> {
                    final Entity[] es = c.getEntities();
                    for (final Entity e : es) if (e instanceof Item || (e instanceof FallingBlock && !(e instanceof TNTPrimed)))
                        e.remove();
                    if (c.getEntities().length < 200) getServer().broadcastMessage("��c���λ�� ��7(" + c.getWorld().getName() + ", " +
                        (c.getX() << 4) + ", " + (c.getZ() << 4) + ") ��c��һ���ʵ��, �ѱ����.");
                });
            }
        }), 0, 200);
    }

    @Override
    public void onDisable() {
        deathRecords.clear();
        if (thread == null) return;
        thread.interrupt();
        thread = null;
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        e.setQuitMessage("��c- " + Utils.getDisplayName(e.getPlayer()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("��a+ " + Utils.getDisplayName(e.getPlayer()));
        final Player p = e.getPlayer();
        final Server s = getServer();
        p.setPlayerListHeader(Constants.PLAYER_HEADER);
        p.sendMessage(Constants.JOIN_MESSAGE_HEADER);
        p.sendMessage("  ��a��ǰ�������: ��7" + s.getOnlinePlayers().size() +
                "                     ��a��ǰTPS: " + (int) s.getTPS()[0]);
        p.sendMessage(Constants.JOIN_MESSAGES);
        p.sendMessage(Constants.JOIN_MESSAGE1);
        p.sendMessage(Constants.JOIN_MESSAGE_FOOTER);
    }

    @EventHandler
    public void onKill(final EntityDeathEvent e) {
        final List<ItemStack> drops = e.getDrops();
        switch (e.getEntityType()) {
            case TURTLE:
                final Player killer = e.getEntity().getKiller();
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
    public void onJump(final PlayerInteractEvent e) {
        if (e.getAction() != Action.PHYSICAL) return;
        final Block b = e.getClickedBlock();
        if (b != null && b.getType() == Material.FARMLAND) e.setCancelled(true);
    }

    @EventHandler
    public void onMobJump(final EntityInteractEvent e) {
        if (e.getEntityType() != EntityType.PLAYER &&
            e.getBlock().getType() == Material.FARMLAND) e.setCancelled(true);
    }

    @EventHandler
    public void onLightingStrike(final LightningStrikeEvent e) {
        if (e.getCause() == LightningStrikeEvent.Cause.TRIDENT ||
            e.getCause() == LightningStrikeEvent.Cause.COMMAND) return;
        for (final Entity it : e.getLightning().getNearbyEntities(5, 5, 5)) {
            if (it.getType() == EntityType.VILLAGER) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(final EntitySpawnEvent e) {
        switch (e.getEntityType()) {
            case BAT:
                e.setCancelled(true);
                break;
            case VILLAGER:
                int i = 0;
                final Location l = e.getLocation();
                for (final Entity it : l.getNearbyEntities(48, 48, 48)) {
                    if (it.getType() == EntityType.VILLAGER) i++;
                }
                if (i > 50) {
                   Bukkit.broadcastMessage("��c������ ��7" + l.getBlockX() + "," + l.getBlockY() + "," +
                       l.getBlockZ() + " ��c������ֳ����.");
                    e.setCancelled(true);
                }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(final AsyncPlayerChatEvent e) {
        e.setFormat("%1$s��7: %2$s");
        final StringBuilder sb = new StringBuilder();
        for (final String s : e.getMessage().split(" ")) {
            final Player p = getServer().getPlayerExact(s);
            if (p != null) {
                sb.append("��a@").append(s).append("��7");
                p.sendMessage("��aһλ�� ��f" + e.getPlayer().getDisplayName() + " ��a��С����@����.");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else sb.append(s);
            sb.append(' ');
        }
        e.setMessage(sb.toString());
    }


    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e) {
        final Player p = e.getEntity();
        if (!p.hasPermission("neko.notdeatheffect")) return;
        deathRecords.put(p.getUniqueId().toString(), new Object[] { p.getExhaustion(), p.getSaturation(), p.getFoodLevel() });
    }

    @EventHandler
    public void onPlayerPostRespawn(final PlayerPostRespawnEvent e) {
        final Player p = e.getPlayer();
        if (!p.hasPermission("neko.notdeatheffect")) return;
        final String id = p.getUniqueId().toString();
        final Object[] obj = deathRecords.get(id);
        if (obj != null) {
            p.setExhaustion((float) obj[0]);
            p.setSaturation((float) obj[1]);
            p.setFoodLevel((int) obj[2]);
            deathRecords.remove(id);
        }
        p.setHealth(Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue() / 2);
        p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20 * 30, 1, true, false));
    }

    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent e) {
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
    public void onDamageByEntity(final EntityDamageByEntityEvent e) {
        if (e.getDamager().getType() == EntityType.CREEPER && !(e.getEntity() instanceof Monster)) e.setCancelled(true);
    }

    @EventHandler
    public void onBlockIgnite(final BlockIgniteEvent e) {
        switch (e.getCause()) {
            case SPREAD:
            case LAVA:
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(final BlockBurnEvent e) {
        e.setCancelled(true);
    }

    private boolean isDangerCommand(final String cmd) {
        for (final Pattern c : Constants.DANGER_COMMANDS) if (c.matcher(cmd).matches()) return true;
        return false;
    }

    @EventHandler
    public void onPlayerCommand(final PlayerCommandPreprocessEvent e) {
        if (isDangerCommand(e.getMessage())) {
            e.getPlayer().sendMessage("��cΣ�յ�ָ���ѱ��ܾ�ִ��!");
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onServerCommand(final ServerCommandEvent e) {
        if (isDangerCommand(e.getCommand())) {
            e.getSender().sendMessage("��cΣ�յ�ָ���ѱ��ܾ�ִ��!");
            e.setCancelled(true);
        }
    }
}
