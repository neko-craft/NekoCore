package net.nekocraft.nekocore;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.nekocraft.nekocore.utils.Utils;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
@Command(name = "bedrock", aliases = "be")
@Command(name = "acceptrule")
@Command(name = "denyrule")
@SuppressWarnings({"unused", "deprecation"})
public final class Main extends JavaPlugin implements Listener {
    private int i = 0;
    private Thread thread;
    private static final HashMap<String, Object[]> deathRecords = new HashMap<>();
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final Random RANDOM = new Random();
    private static final JsonParser PARSER = new JsonParser();
    private World nether, world;
    private final Set<Player> beList = Collections.newSetFromMap(new WeakHashMap<>());

    private final Advancement DEATH = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/death")),
            STRIKE = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/death_strike")),
            HUNGRY = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/death_hungry")),
            EXPLOSION = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/death_explosion")),
            STABBED = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/death_stabbed")),
            FIRST_STEP = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/first_step")),
            CHAT = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/chat")),
            QUESTION = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/chat_question")),
            AT = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/chat_at")),
            HOME = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/home"));

    @SuppressWarnings({"BusyWait", "ResultOfMethodCallIgnored", "ConstantConditions"})
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
        registerCommand("bedrock", this);

        world = s.getWorld("world");
        nether = s.getWorld("world_nether");
        final Location spawn = world.getSpawnLocation();

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
                    final ArrayList<Player> list = new ArrayList<>();
                    s.getOnlinePlayers().forEach(it -> {
                        it.setPlayerListFooter(footer);
                        final Location loc = it.getLocation();
                        if (loc.getWorld() == world && loc.distanceSquared(spawn) > 400) list.add(it);
                    });
                    if (!list.isEmpty()) s.getScheduler().runTask(this, () -> list.forEach(it -> Utils.giveAdvancement(FIRST_STEP, it)));
                    try {
                        s.getWorlds().forEach(it -> {
                            final Chunk[] ch = it.getLoadedChunks();
                            for (final Chunk c : ch) if (c.getEntities().length > 500) {
                                s.getScheduler().runTask(this, () -> {
                                    final Entity[] es = c.getEntities();
                                    for (final Entity e : es) if (e instanceof Item || (e instanceof FallingBlock && !(e instanceof TNTPrimed)))
                                        e.remove();
                                    if (c.getEntities().length < 200) s.broadcastMessage("§c这个位置 §7(" + c.getWorld().getName() + ", " +
                                            (c.getX() << 4) + ", " + (c.getZ() << 4) + ") §c有一大堆实体, 已被清除.");
                                });
                            }
                        });
                    } catch (final Exception ignored) { }
                    if (world.isThundering() && world.hasStorm()) {
                        world.getPlayers().forEach(it -> {
                            if (it.getGameMode() != GameMode.SURVIVAL || RANDOM.nextInt(17) != 0) return;
                            final Location loc = it.getLocation();
                            if (it.isInRain() && RANDOM.nextInt(3) == 0) {
                                final PlayerInventory inv = it.getInventory();
                                if (Utils.isConductive(inv.getItemInMainHand()) ||
                                        Utils.isConductive(inv.getItemInOffHand()) ||
                                        Utils.isConductive(inv.getBoots()) ||
                                        Utils.isConductive(inv.getChestplate()) ||
                                        Utils.isConductive(inv.getLeggings()) ||
                                        Utils.isConductive(inv.getHelmet())) {
                                    s.getScheduler().runTask(this, () -> world.strikeLightning(loc));
                                    return;
                                }
                            }
                            Block block = loc.toHighestLocation().getBlock();
                            if (Utils.isLeaves(block.getType()) && block.getHumidity() > 0 && block.getTemperature() > 0) {
                                final Leaves data = (Leaves) block.getBlockData();
                                if (data.isPersistent()) return;
                                int y = block.getY(), endY = loc.getBlockY() + 3;
                                while (y-- > endY) {
                                    block = block.getRelative(0, -1, 0);
                                    final Material type = block.getType();
                                    if (!(type == Material.AIR || Utils.isLeaves(type) || Utils.isLog(type))) return;
                                }
                                s.getScheduler().runTask(this, () -> world.strikeLightning(loc));
                            }
                        });
                        world.getEntities().forEach(it -> {
                            if (it.getType() == EntityType.DROPPED_ITEM) {
                                if (!Utils.isConductive(((Item) it).getItemStack().getType())) return;
                            } else if (it instanceof Monster) {
                                EntityEquipment inv = ((LivingEntity) it).getEquipment();
                                if (!(Utils.isConductive(inv.getItemInMainHand()) ||
                                        Utils.isConductive(inv.getItemInOffHand()) ||
                                        Utils.isConductive(inv.getBoots()) ||
                                        Utils.isConductive(inv.getChestplate()) ||
                                        Utils.isConductive(inv.getLeggings()) ||
                                        Utils.isConductive(inv.getHelmet()))) return;
                            } else return;
                            if (!it.isInRain() || RANDOM.nextInt(14) != 0) return;
                            final Location loc = it.getLocation();
                            getServer().getScheduler().runTask(this, () -> world.strikeLightning(loc));
                        });
                    }
                    Thread.sleep(2000);
                }
            } catch (final InterruptedException ignored) { } catch (final Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    @Override
    public void onDisable() {
        deathRecords.clear();
        if (thread == null) return;
        thread.interrupt();
        thread = null;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command, final String label, final String[] args) {
        if (!command.getName().equalsIgnoreCase("bedrock") || !(sender instanceof Player)) return false;
        if (beList.contains(sender)) {
            beList.remove(sender);
            sender.sendMessage("§a您当前已离开了 Bedrock 模式!");
        } else {
            beList.add((Player) sender);
            sender.sendMessage("§a您已进入 Bedrock 模式!");
        }
        return true;
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


    @EventHandler(ignoreCancelled = true)
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
                    int bound = Math.round(((float) killer
                            .getInventory()
                            .getItemInMainHand()
                            .getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)) / 2);
                    if (bound > 0) count += RANDOM.nextInt(bound);
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

    @EventHandler(ignoreCancelled = true)
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

    @EventHandler(ignoreCancelled = true)
    public void onMobJump(final EntityInteractEvent e) {
        if (e.getEntityType() != EntityType.PLAYER &&
            e.getBlock().getType() == Material.FARMLAND) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLightingStrike(final LightningStrikeEvent e) {
        Location loc = e.getLightning().getLocation();
        final double y = loc.getY();
        final int x = loc.getBlockX() - 16, z = loc.getBlockZ() - 16;
        loop: for (int i = 0; i < 32; i++) for (int j = 0; j < 32; j++) {
            final Block block = world.getHighestBlockAt(x + i, z + j);
            final Location loc2 = block.getLocation();
            if (loc2.getY() >= y && Utils.isConductive(block.getType())) {
                loc = loc2.toCenterLocation();
                e.getLightning().teleport(loc);
                final Block b2 = block.getRelative(0, -1, 0);
                if (Utils.isLog(b2.getType())) b2.setType(Material.COAL_BLOCK);
                break loop;
            }
        }
        if (e.getCause() != LightningStrikeEvent.Cause.TRIDENT &&
                e.getCause() != LightningStrikeEvent.Cause.COMMAND) for (final Entity it : loc.getNearbyEntities(5, 5, 5)) {
            if (it.getType() == EntityType.VILLAGER) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntitySpawn(final EntitySpawnEvent e) {
        switch (e.getEntityType()) {
            case CREEPER: return;
            case BAT:
                e.setCancelled(true);
                return;
            case HUSK:
            case ZOMBIE:
            case DROWNED:
            case ZOMBIE_VILLAGER:
                if (RANDOM.nextBoolean()) ((Zombie) e.getEntity()).setShouldBurnInDay(false);
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
                return;
        }
        if (!(e.getEntity() instanceof Monster)) return;
        final Monster entity = (Monster) e.getEntity();
        while (RANDOM.nextInt(10) >= 7) entity.addPotionEffect(
                new PotionEffect(Constants.EFFECTS[RANDOM.nextInt(Constants.EFFECTS.length - 1)], 144000, RANDOM.nextBoolean() ? 1 : 2));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(final AsyncPlayerChatEvent e) {
        final StringBuilder sb = new StringBuilder();
        final String n = e.getPlayer().getName();
        boolean isAt = false;
        for (String s : e.getMessage().split(" ")) {
            if (s.startsWith("@")) s = s.replaceAll("^@+", "");
            if (n.equalsIgnoreCase(s)) {
                sb.append(s).append(' ');
                continue;
            }
            final Player p = getServer().getPlayerExact(s);
            if (p != null) {
                sb.append("§a@").append(s).append("§7");
                p.sendMessage("§a一位叫 §f" + n + " §a的小朋友@了你.");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                isAt = true;
            } else sb.append(s);
            sb.append(' ');
        }
        final String value = sb.toString();
        final String be = n + "§7: " + value;
        final TextComponent name = new TextComponent(n),
            text = new TextComponent(": " + value);
        name.setHoverEvent(Constants.AT);
        name.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, n + " "));
        text.setColor(ChatColor.GRAY);
        text.setHoverEvent(Constants.TPA);
        text.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value.trim()));
        e.getRecipients().forEach(it -> {
            if (beList.contains(it)) it.sendMessage(be);
            else it.sendMessage(name, text);
        });
        e.getRecipients().clear();
        final boolean flag = isAt;
        getServer().getScheduler().runTask(this, () -> {
            Utils.giveAdvancement(CHAT, e.getPlayer());
            if (flag) Utils.giveAdvancement(AT, e.getPlayer());
            if (value.contains("\u00BF")) Utils.giveAdvancement(QUESTION, e.getPlayer());
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent e) {
        final Player p = e.getEntity();
        Utils.giveAdvancement(DEATH, p);
        final EntityDamageEvent dmg = p.getLastDamageCause();
        if (dmg != null) {
            Advancement ad = null;
            switch (dmg.getCause()) {
                case ENTITY_EXPLOSION:
                case BLOCK_EXPLOSION:
                    ad = EXPLOSION;
                    break;
                case STARVATION:
                    ad = HUNGRY;
                    break;
                case LIGHTNING:
                    ad = STRIKE;
                    break;
                default: if (dmg instanceof EntityDamageByBlockEvent) {
                    final Block block = ((EntityDamageByBlockEvent) dmg).getDamager();
                    if (block != null) {
                        final Material type = block.getType();
                        if (type == Material.CACTUS || type == Material.SWEET_BERRY_BUSH) ad = STABBED;
                    }
                }
            }
            Utils.giveAdvancement(ad, p);
        }
        if (p.hasPermission("neko.notdeatheffect")) return;
        deathRecords.put(p.getUniqueId().toString(), new Object[] { p.getExhaustion(), p.getSaturation(), p.getFoodLevel() });
    }

    @EventHandler
    public void onPlayerPostRespawn(final PlayerPostRespawnEvent e) {
        final Player p = e.getPlayer();
        if (p.hasPermission("neko.notdeatheffect")) return;
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

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        if (checkTrapChest(e.getPlayer(), e.getBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final HangingBreakByEntityEvent e) {
        if (e.getRemover() != null && e.getRemover().getType() == EntityType.CREEPER) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getWhoClicked().isOp()) return;
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
                if (e.getClickedInventory() == e.getView().getTopInventory())
                    checkTrapChest((Player) e.getWhoClicked(), ((Chest) holder).getLocation());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(final EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Zombie) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageByEntity(final EntityDamageByEntityEvent e) {
        switch (e.getDamager().getType()) {
            case CREEPER:
            case ENDER_CRYSTAL:
            case LIGHTNING:
            case FIREBALL:
                break;
            default: return;
        }
        if (e.getEntity() instanceof Monster || e.getEntityType() == EntityType.PLAYER) return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(final BlockIgniteEvent e) {
        switch (e.getCause()) {
            case SPREAD:
            case LAVA:
                e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(final BlockBurnEvent e) {
        e.setCancelled(true);
    }

    private boolean isDangerCommand(final String cmd) {
        for (final Pattern c : Constants.DANGER_COMMANDS) if (c.matcher(cmd).matches()) return true;
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent e) {
        if (isDangerCommand(e.getMessage())) {
            e.getPlayer().sendMessage("§c危险的指令已被拒绝执行!");
            e.setCancelled(true);
        } else if (e.getMessage().startsWith("sethome") || e.getMessage().startsWith("/sethome")) Utils.giveAdvancement(HOME, e.getPlayer());
    }
    @EventHandler(ignoreCancelled = true)
    public void onServerCommand(final ServerCommandEvent e) {
        if (isDangerCommand(e.getCommand())) {
            e.getSender().sendMessage("§c危险的指令已被拒绝执行!");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
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
            player.kickPlayer("§c请立即将拿取的物品放回箱子内!");
            return true;
        } else return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockSpread(final BlockSpreadEvent e) {
        Block block = e.getSource();
        if (block.getType() != e.getNewState().getType()) return;
        final Material material;
        switch (block.getType()) {
            case KELP:
                material = Material.KELP_PLANT;
                break;
            case BAMBOO:
                material = Material.BAMBOO;
                break;
            default: return;
        }
        int height = 0;
        do {
            if (height++ >= 15) {
                e.setCancelled(true);
                return;
            }
            block = block.getRelative(BlockFace.DOWN);
        } while (block.getType() == material);
        if (height >= 2 + new Random(block.getLocation().add(0, 1, 0).toBlockKey()).nextInt(13)) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent e) {
        if (e.getBlock().getType() == Material.WET_SPONGE) Utils.absorbLava(e.getBlock(), null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpongeAbsorb(final SpongeAbsorbEvent e) {
        if (!e.getBlocks().isEmpty()) Utils.absorbLava(e.getBlock(), this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent e) {
        if (e.getEntityType() != EntityType.ENDERMAN) return;
        e.setCancelled(true);
        ((Enderman) e.getEntity()).setCarriedBlock(null);
    }
}
