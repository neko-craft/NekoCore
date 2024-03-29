package net.nekocraft.nekocore;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.*;
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

import static net.nekocraft.nekocore.Utils.registerCommand;

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
@Command(name = "toggle")
@Command(name = "acceptrule")
@Command(name = "denyrule")
@SuppressWarnings({"unused", "deprecation"})
public final class Main extends JavaPlugin implements Listener {
    private int i = 0;
    private Thread thread;
    private static final HashMap<UUID, Object[]> deathRecords = new HashMap<>();
    private static final DecimalFormat df = new DecimalFormat("0.0");
    private static final Random RANDOM = new Random();
    private static final JsonParser PARSER = new JsonParser();
    private World nether, world, theEnd;
    private final Set<Player> beList = Collections.newSetFromMap(new WeakHashMap<>());
    private final Set<Player> warning = Collections.newSetFromMap(new WeakHashMap<>());

    private final Advancement DEATH = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/death")),
            STRIKE = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/death_strike")),
            HUNGRY = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/death_hungry")),
            EXPLOSION = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/death_explosion")),
            STABBED = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/death_stabbed")),
            STONECUTTER = Bukkit.getAdvancement(new NamespacedKey("nekocraft", "nekocraft/death_stonecutter")),
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
        m.registerEvents(this, this);
        registerCommand("explode", antiExplode);
        registerCommand("show", new ShowItem());
        registerCommand("rsd", new RedStoneDetection(this));
        registerCommand("acceptrule", rules);
        registerCommand("welcome", new Welcome());
        registerCommand("bedrock", this);
        registerCommand("toggle", (a, b, c, d) -> {
            a.sendMessage("§c模式切换命令已更改为 §e/gamemode§c. 也可使用 §eF3 + N §c进行切换.");
            return true;
        });

        world = s.getWorld("world");
        nether = s.getWorld("world_nether");
        theEnd = s.getWorld("world_the_end");
        final Location spawn = world.getSpawnLocation();

        thread = new Thread(() -> {
            try {
                while (true) {
                    final double tps = s.getTPS()[0];
                    if (tps < 4.5) i++;
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
                    final String footer = "\n§aTPS: §7" + df.format(tps) + " §aMSPT: §7" +
                            df.format(s.getTickTimes()[0] / 1000000.0) + "\n§b§m                                      ";
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
                                    s.getScheduler().runTask(this, () -> Utils.strikeLightning(loc));
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
                                s.getScheduler().runTask(this, () -> Utils.strikeLightning(loc));
                            }
                        });
                        getServer().getScheduler().runTask(this, () -> world.getEntities().forEach(it -> {
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
                            Utils.strikeLightning(it.getLocation());
                        }));
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
    public void onPlayerQuit(final PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        e.setQuitMessage("§c- " + Utils.getDisplayName(player));
        beList.remove(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
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
        if (warning.remove(p)) p.sendMessage("§e注意: 您当前进入服务器所使用的域名将会被废弃! 请使用 neko-craft.com 进入服务器!");
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerPreLogin(final AsyncPlayerPreLoginEvent e) {
        boolean needKick = true;
        for (final ProfileProperty it : e.getPlayerProfile().getProperties()) if (it.getName().equals("textures")) try {
            JsonElement urlElement = PARSER.parse(new String(Base64.getDecoder().decode(it.getValue()))).getAsJsonObject()
                    .getAsJsonObject("textures").getAsJsonObject("SKIN").get("url");
            if (urlElement != null) {
                String url = urlElement.getAsString();
                if (!url.endsWith("1a4af718455d4aab528e7a61f86fa25e6a369d1768dcb13f7df319a713eb810b") &&
                        !url.endsWith("3b60a1f6d562f52aaebbf1434f1de147933a3affe0e764fa49ea057536623cd3")) needKick = false;
            }
        } catch (final Exception ignored) { }
        if (needKick) e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§c请先给您的游戏账户设置一个皮肤再尝试进入服务器!");
    }

    @EventHandler
    public void onSmithItem(final SmithItemEvent e) {
        ItemStack is = e.getInventory().getResult();
        if (is == null || is.getType() != Material.DIAMOND) return;
        e.getInventory().setResult(new ItemStack(Material.DIAMOND, is.getAmount()));
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent e) {
        final List<ItemStack> drops = e.getDrops();
        switch (e.getEntityType()) {
            case TURTLE -> {
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
            }
            case RAVAGER -> {
                drops.clear();
                drops.add(new ItemStack(Material.LEATHER, 4));
            }
            case VINDICATOR -> drops.removeIf(it -> it.getType() == Material.WHITE_BANNER || it.getType() == Material.IRON_AXE);
            case EVOKER -> drops.removeIf(is -> is.getType() == Material.WHITE_BANNER);
            case PILLAGER -> drops.removeIf(it -> it.getType() == Material.WHITE_BANNER || it.getType() == Material.CROSSBOW);
            case WITCH -> drops.removeIf(is -> is.getType() == Material.POTION);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTransform(final EntityTransformEvent e) {
        if (e.getEntityType() != EntityType.VILLAGER) return;
        for (final Entity it : e.getTransformedEntities()) if (it.getType() == EntityType.WITCH) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL && e.getClickedBlock() != null &&
                e.getClickedBlock().getType() == Material.FARMLAND) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobJump(final EntityInteractEvent e) {
        if (e.getEntityType() != EntityType.PLAYER &&
            e.getBlock().getType() == Material.FARMLAND) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLightingStrike(final LightningStrikeEvent e) {
        var block = e.getLightning().getLocation().getBlock();
        if (block.getY() < 2) return;
        block = block.getRelative(0, -1, 0);
        if (block.getType() != Material.LIGHTNING_ROD) return;
        var b2 = block.getRelative(0, -1, 0);
        if (Utils.isLog(b2.getType())) b2.setType(Material.COAL_BLOCK);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortalCreate(final PortalCreateEvent e) {
        if (e.getReason() == PortalCreateEvent.CreateReason.END_PLATFORM) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRaidTrigger(final RaidTriggerEvent e) {
        if (getServer().getTPS()[0] < 16.0) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntitySpawn(final EntitySpawnEvent e) {
        switch (e.getEntityType()) {
            case CREEPER: return;
            case WITHER:
            case PILLAGER:
            case RAVAGER:
            case EVOKER:
            case EVOKER_FANGS:
            case VINDICATOR:
            case ENDERMAN:
                if (getServer().getTPS()[0] >= 16.0) break;
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
                CreatureSpawnEvent.SpawnReason reason = e.getEntity().getEntitySpawnReason();
                if (reason == CreatureSpawnEvent.SpawnReason.CUSTOM ||
                        reason == CreatureSpawnEvent.SpawnReason.COMMAND) return;
                final Location l = e.getLocation();
                if (l.getNearbyEntitiesByType(Villager.class, 48).size() > 50) {
                    Bukkit.broadcastMessage("§c有人在 §7" + l.getBlockX() + "," + l.getBlockY() + "," +
                            l.getBlockZ() + " §c大量繁殖村民.");
                }
                return;
        }
        if (!(e.getEntity() instanceof final Monster entity)) return;
        for (int i = 0; i < 2 && RANDOM.nextInt(10) >= 7; i++) {
            PotionEffectType type = Constants.EFFECTS[RANDOM.nextInt(Constants.EFFECTS.length - 1)];
            entity.addPotionEffect(new PotionEffect(type, 144000,
                            type == PotionEffectType.DAMAGE_RESISTANCE || RANDOM.nextBoolean() ? 1 : 2));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerLogin(final PlayerLoginEvent e) {
        if (e.getResult() == PlayerLoginEvent.Result.ALLOWED && e.getHostname().contains("apisium.cn")) warning.add(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent e) {
        final Player p = e.getEntity();
        Utils.giveAdvancement(DEATH, p);
        final EntityDamageEvent dmg = p.getLastDamageCause();
        if (dmg != null) {
            Advancement ad = null;
            switch (dmg.getCause()) {
                case MAGIC:
                    if (dmg instanceof EntityDamageByBlockEvent) {
                        final Block block = ((EntityDamageByBlockEvent) dmg).getDamager();
                        if (block != null && block.getType() == Material.STONECUTTER) {
                            e.setDeathMessage(p.getName() + "裂开了");
                            ad = STONECUTTER;
                        }
                    }
                    break;
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
            if (ad != null) Utils.giveAdvancement(ad, p);
        }
        if (p.hasPermission("neko.notdeatheffect") || p.getStatistic(Statistic.PLAY_ONE_MINUTE) < 20 * 60 * 40) return;
        deathRecords.put(p.getUniqueId(), new Object[] { p.getExhaustion(), p.getSaturation(), p.getFoodLevel() });
    }

    @EventHandler
    public void onPlayerPostRespawn(final PlayerPostRespawnEvent e) {
        final Player p = e.getPlayer();
        if (p.hasPermission("neko.notdeatheffect")) return;
        final UUID id = p.getUniqueId();
        final Object[] obj = deathRecords.get(id);
        if (obj != null) {
            p.setExhaustion((float) obj[0]);
            p.setSaturation((float) obj[1]);
            p.setFoodLevel((int) obj[2]);
            p.setHealth(Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue() / 2);
            p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20 * 30, 1, true, false));
            deathRecords.remove(id);
        }
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
                break;
            default:
                Location loc = e.getLocation();
                if (loc.getWorld() != world || loc.distanceSquared(world.getSpawnLocation()) > 12544) return;
        }
        e.blockList().clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        if (e.getPlayer().isOp()) return;
        if (checkTrapChestExact(e.getBlock().getLocation())) {
            Player player = e.getPlayer();
            getServer().broadcastMessage("§c玩家 §f" + player.getName() + " §c正在尝试从出生点钻石箱中取出物品!!");
            player.banPlayer("§c不要尝试偷盗! 解封请进入QQ群: 7923309");
            e.setCancelled(true);
        } else if (checkTrapChest(e.getBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreakByEntity(final HangingBreakByEntityEvent e) {
        if (e.getRemover() != null && e.getRemover().getType() == EntityType.CREEPER) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getWhoClicked().isOp()) return;
        final InventoryHolder holder = e.getView().getTopInventory().getHolder();
        if (holder instanceof Chest && e.getWhoClicked() instanceof Player) {
            if (e.getClickedInventory() == e.getView().getTopInventory() && checkTrapChestExact(((Chest) holder).getLocation())) {
                Player player = (Player) e.getWhoClicked();
                getServer().broadcastMessage("§c玩家 §f" + player.getName() + " §c正在尝试从出生点钻石箱中取出物品!!");
                player.banPlayer("§c不要尝试偷盗! 解封请进入QQ群: 7923309");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(final EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Zombie) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
        if (e.getEntityType() == EntityType.VILLAGER && !((Villager) e.getEntity()).hasAI()) e.setCancelled(true);
        switch (e.getDamager().getType()) {
            case PLAYER:
                if (e.getEntityType() == EntityType.VILLAGER &&
                        ((Player) e.getDamager()).getInventory().getItemInMainHand().getType() == Material.BLAZE_ROD) {
                    var villager = (Villager) e.getEntity();
                    villager.setAI(!villager.hasAI());
                    break;
                } else return;
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
    public void onEntityDamageByBlock(final EntityDamageByBlockEvent e) {
        if (e.getEntityType() == EntityType.VILLAGER && ((e.getDamager() != null &&
                e.getDamager().getType() == Material.STONECUTTER) || !((Villager) e.getEntity()).hasAI())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(final BlockIgniteEvent e) {
        switch (e.getCause()) {
            case SPREAD, LAVA -> e.setCancelled(true);
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

    private boolean checkTrapChest(final Location loc) {
        return loc.getWorld() == world && (Math.pow(loc.getBlockX() + 202, 2) +
                Math.pow(loc.getBlockY() - 65, 2) + Math.pow(loc.getBlockZ() - 219, 2) <= 4);
    }

    private boolean checkTrapChestExact(final Location loc) {
        return loc != null && loc.getWorld() == world && loc.getBlockX() == -202 && loc.getBlockY() == 65 &&loc.getBlockZ() == 219;
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
        if (!e.getPlayer().isOp() && checkTrapChest(e.getBlock().getLocation())) e.setCancelled(true);
        else if (e.getBlock().getType() == Material.WET_SPONGE) Utils.absorbLava(e.getBlock(), null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpongeAbsorb(final SpongeAbsorbEvent e) {
        if (!e.getBlocks().isEmpty()) Utils.absorbLava(e.getBlock(), this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(final BlockPistonExtendEvent e) {
        for (final Block block : e.getBlocks()) if (checkTrapChest(block.getLocation())) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(final BlockPistonRetractEvent e) {
        for (final Block block : e.getBlocks()) if (checkTrapChest(block.getLocation())) {
            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent e) {
        if (e.getEntityType() != EntityType.ENDERMAN) return;
        final Enderman entity = (Enderman) e.getEntity();
        if (entity.getWorld() == theEnd) switch (e.getBlock().getType()) {
            case MELON:
            case PUMPKIN:
                return;
        }
        e.setCancelled(true);
        entity.setCarriedBlock(null);
    }
}
