package net.nekocraft.nekocore;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.nekocraft.nekocore.utils.Utils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

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
@Command(name = "welcome", aliases = "w")
@Command(name = "acceptrule")
@Command(name = "denyrule")
@SuppressWarnings("unused")
public final class Main extends JavaPlugin implements Listener {
    private int i = 0;
    private Thread thread;
    private static final HashMap<String, Object[]> deathRecords = new HashMap<>();
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final Random RANDOM = new Random();
    private static final JsonParser PARSER = new JsonParser();
    private final World nether = getServer().getWorld("world_nether");

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
        registerCommand("welcome", new Welcome());

        thread = new Thread(() -> {
            try {
                while (true) {
                    final double tps = s.getTPS()[0];
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
                    final String footer = "\n§aMSPT: §7" + df.format(s.getTickTimes()[0] / 1000000.0) +
                        "  §aTPS: §7" + df.format(tps) + "\n§b§m                                      ";
                    s.getOnlinePlayers().forEach(it -> it.setPlayerListFooter(footer));
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
                    if (c.getEntities().length < 200) getServer().broadcastMessage("§c这个位置 §7(" + c.getWorld().getName() + ", " +
                        (c.getX() << 4) + ", " + (c.getZ() << 4) + ") §c有一大堆实体, 已被清除.");
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
        e.setQuitMessage("§c- " + Utils.getDisplayName(e.getPlayer()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("§a+ " + Utils.getDisplayName(e.getPlayer()));
        final Player p = e.getPlayer();
        final Server s = getServer();
        p.setPlayerListHeader(Constants.PLAYER_HEADER);
        p.sendMessage(Constants.JOIN_MESSAGE_HEADER);
        p.sendMessage("  §a当前在线玩家: §7" + s.getOnlinePlayers().size() +
                "                     §a当前TPS: " + (int) s.getTPS()[0]);
        p.sendMessage(Constants.JOIN_MESSAGES);
        p.sendMessage(Constants.JOIN_MESSAGE1);
        p.sendMessage(Constants.JOIN_MESSAGE_FOOTER);
    }


    @EventHandler
    public void onAsyncPlayerPreLogin(final AsyncPlayerPreLoginEvent e) {
        boolean needKick = true;
        for (final ProfileProperty it : e.getPlayerProfile().getProperties()) if (it.getName().equals("textures")) try {
            if (PARSER.parse(new String(Base64.getDecoder().decode(it.getValue()))).getAsJsonObject()
                .getAsJsonObject("textures").getAsJsonObject("SKIN").has("url")) needKick = false;
        } catch (final Exception ignored) { }
        if (needKick) e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§c请您先给您的游戏账户设置一个皮肤再尝试进入服务器!");
    }

    @EventHandler
    public void onKill(final EntityDeathEvent e) {
        final List<ItemStack> drops = e.getDrops();
        switch (e.getEntityType()) {
            case TURTLE: {
                final Player killer = e.getEntity().getKiller();
                int count = 2;
                if (killer != null) {
                    count += RANDOM.nextInt(Math.round(((float) killer
                        .getInventory()
                        .getItemInMainHand()
                        .getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)) / 2));
                }
                drops.add(new ItemStack(Material.SCUTE, count));
                break;
            }
            case RAVAGER:
                drops.clear();
                drops.add(new ItemStack(Material.LEATHER, 4));
                break;
            case VINDICATOR:
                drops.removeIf(it -> it.getType() == Material.WHITE_BANNER || it.getType() == Material.IRON_AXE);
                break;
            case EVOKER:
                drops.removeIf(is -> is.getType() == Material.WHITE_BANNER);
                break;
            case PILLAGER:
                drops.removeIf(it -> it.getType() == Material.WHITE_BANNER || it.getType() == Material.CROSSBOW);
                break;
            case WITCH:
                drops.removeIf(is -> is.getType() == Material.POTION);
                break;
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        switch (e.getAction()) {
            case PHYSICAL:
                final Block b = e.getClickedBlock();
                if (b != null && b.getType() == Material.FARMLAND) e.setCancelled(true);
                break;
            case RIGHT_CLICK_BLOCK:
                if (e.getItem() != null && e.getClickedBlock() != null &&
                    e.getClickedBlock().getType() == Material.SPAWNER &&
                    e.getItem().getType().name().endsWith("_SPAWN_EGG")) e.setCancelled(true);
        }
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
                   Bukkit.broadcastMessage("§c有人在 §7" + l.getBlockX() + "," + l.getBlockY() + "," +
                       l.getBlockZ() + " §c大量繁殖村民.");
                    e.setCancelled(true);
                }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(final AsyncPlayerChatEvent e) {
        final StringBuilder sb = new StringBuilder();
        final String n = e.getPlayer().getName();
        for (final String s : e.getMessage().split(" ")) {
            final Player p = getServer().getPlayerExact(s);
            if (p != null) {
                sb.append("§a@").append(s).append("§7");
                p.sendMessage("§a一位叫 §f" + n + " §a的小朋友@了你.");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else sb.append(s);
            sb.append(' ');
        }
        final String value = sb.toString();
        final TextComponent name = new TextComponent(n),
            text = new TextComponent(": " + value);
        name.setHoverEvent(Constants.AT);
        name.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, n + " "));
        text.setColor(ChatColor.GRAY);
        text.setHoverEvent(Constants.TPA);
        text.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value.trim()));
        e.getRecipients().forEach(it -> it.sendMessage(name, text));
        e.getRecipients().clear();
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
    public void onBlockBreak(final BlockBreakEvent e) {
        if (checkTrapChest(e.getPlayer(), e.getBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        final InventoryHolder holder = e.getView().getTopInventory().getHolder();
        if (holder instanceof Chest && e.getWhoClicked() instanceof Player) switch (e.getAction()) {
            case PICKUP_ALL:
            case PICKUP_ONE:
            case PICKUP_HALF:
            case PICKUP_SOME:
            case HOTBAR_SWAP:
            case SWAP_WITH_CURSOR:
            case COLLECT_TO_CURSOR:
            case MOVE_TO_OTHER_INVENTORY:
                checkTrapChest((Player) e.getWhoClicked(), ((Chest) holder).getLocation());
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
            e.getPlayer().sendMessage("§c危险的指令已被拒绝执行!");
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onServerCommand(final ServerCommandEvent e) {
        if (isDangerCommand(e.getCommand())) {
            e.getSender().sendMessage("§c危险的指令已被拒绝执行!");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(final BlockExplodeEvent e) {
        final Block b = e.getBlock();
        final Location loc = b.getLocation();
        if (b.getWorld() == nether && b.getType().isAir() &&
            (Math.pow(loc.getBlockX() + 36, 2) + Math.pow(loc.getBlockZ(), 2)) < 12544) {
            e.setCancelled(true);
            loc.getNearbyPlayers(6).forEach(it -> {
                it.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 120, 5, true, false));
                it.sendMessage("§c请不要在距离世界出生点7个区块以内玩爆炸物!");
            });
        }
    }

    private boolean checkTrapChest(final Player player, final Location loc) {
        if (loc.getWorld().getName().equals("world") && loc.getBlockX() == -202 &&
            loc.getBlockY() == 65 && loc.getBlockZ() == 219) {
            getServer().broadcastMessage("§c玩家 §f" +
                player.getName() + " §c正在尝试从出生点钻石箱中取出物品! §b请立即将物品放回箱子内!");
            return true;
        } else return false;
    }
}
