package net.nekocraft.nekocore;

import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;

public final class Utils {
    private static final World world = Bukkit.getWorld("world");
    private static final Object nmsWorld;
    private static Method getX, getY, getZ, toRodLightingLocation;
    private static Constructor<?> blockPositionConstructor;

    static {
        Object nmsWorld0 = null;
        try {
            assert world != null;
            final Class<?> CraftWorld = world.getClass();
            nmsWorld0 = CraftWorld.getMethod("getHandle").invoke(world);
            toRodLightingLocation = nmsWorld0.getClass().getDeclaredMethod("a", Class.forName("net.minecraft.core.BlockPosition"));
            toRodLightingLocation.setAccessible(true);
            var bp = Class.forName("net.minecraft.core.BlockPosition");
            getX = bp.getMethod("getX");
            getY = bp.getMethod("getY");
            getZ = bp.getMethod("getZ");
            blockPositionConstructor = bp.getConstructor(double.class, double.class, double.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        nmsWorld = nmsWorld0;
    }

    private static final BlockFace[] blockFaces = BlockFace.values();

    private Utils() {}

    public static void registerCommand(final String name, final CommandExecutor e) {
        final PluginCommand cmd = Bukkit.getPluginCommand(name);
        assert cmd != null;
        cmd.setUsage(Constants.WRONG_USAGE);
        cmd.setPermissionMessage(Constants.NO_PERMISSION);
        cmd.setDescription(Constants.COMMAND_DESCRIPTION);
        cmd.setExecutor(e);
    }

    @SuppressWarnings("deprecation")
    public static String getDisplayName(final Player p) {
        return switch (p.getUniqueId().toString()) {
            case "18c7d817-3ad3-4b0f-9106-6eb471dfd530" -> "§f露露";
            case "c0a5ed47-a171-49ba-bd69-cb5b217ae0f2" -> "§f晴猫";
            case "8c33f169-44f1-4a2d-ad9b-9d6b37b363da" -> "§f觉觉";
            case "70d557ce-c6fc-43b2-b356-d2957d92c700" -> "§f扁芙Fulandre_Scarlet";
            case "e59cd3bc-5c8a-485a-b7a6-0dad75da74c6" -> "§f泡芙Flandern_S";
            case "a2bf5901-8cd1-44cd-af49-cb7b839d8076" -> "§f露琪亚小朋友";
            case "0936c888-85d4-424d-8194-f6dceab8ec57" -> "§f猫娘YtonE";
            case "89d9e62f-0e37-412d-a4a4-3a124317f851" -> "§f本服最帅小动物tobell";
            default -> "§f" + p.getDisplayName();
        };
    }

    public static void absorbLava(final Block initBlock, final Plugin plugin) {
        final LinkedList<Object[]> queue = new LinkedList<>();
        queue.add(new Object[] { initBlock, 0 });
        int i = 0;
        while (!queue.isEmpty()) {
            Object[] pair = queue.poll();
            Block sourceBlock = (Block) pair[0];
            int j = (int) pair[1];
            for (final BlockFace it : blockFaces) {
                Block block = sourceBlock.getRelative(it);
                if (block.getType() == Material.LAVA) {
                    block.setType(Material.AIR);
                    ++i;
                    if (j < 6) queue.add(new Object[] { block, j + 1 });
                }
            }
            if (i > 64) break;
        }
        if (i > 0) {
            initBlock.getWorld().playEffect(initBlock.getLocation(), Effect.EXTINGUISH, null);
            initBlock.getWorld().spawnParticle(Particle.SMOKE_LARGE, initBlock.getLocation().add(0.5, 1, 0.5), 10, 0.2, 0.5, 0.2, 0);
            if (plugin == null) initBlock.setType(Material.CRYING_OBSIDIAN);
            else plugin.getServer().getScheduler().runTask(plugin, () -> initBlock.setType(Material.CRYING_OBSIDIAN));
        }
    }

    public static void giveAdvancement(final Advancement ad, final Player p) {
        if (ad == null) return;
        final AdvancementProgress ap = p.getAdvancementProgress(ad);
        if (!ap.isDone()) ap.awardCriteria("trigger");
    }

    public static boolean isLeaves(final Material type) {
        return switch (type) {
            case ACACIA_LEAVES, BIRCH_LEAVES, DARK_OAK_LEAVES, JUNGLE_LEAVES, OAK_LEAVES, SPRUCE_LEAVES -> true;
            default -> false;
        };
    }

    public static boolean isLog(final Material type) {
        return switch (type) {
            case ACACIA_LOG, BIRCH_LOG, DARK_OAK_LOG, JUNGLE_LOG, OAK_LOG, SPRUCE_LOG -> true;
            default -> false;
        };
    }

    @SuppressWarnings("ConstantConditions")
    public static void strikeLightning(Location loc) {
        if (loc.getWorld() != world) return;
        try {
            var newLoc = toRodLightingLocation.invoke(nmsWorld,
                    blockPositionConstructor.newInstance(loc.getX(), loc.getY(), loc.getZ()));
            loc = new Location(loc.getWorld(), (int) getX.invoke(newLoc), (int) getY.invoke(newLoc),
                    (int) getZ.invoke(newLoc));
        } catch (Exception e) { e.printStackTrace(); }
        world.strikeLightning(loc);
    }

    public static boolean isConductive(final ItemStack item) {
        return item != null && isConductive(item.getType());
    }
    public static boolean isConductive(final Material type) {
        return switch (type) {
            case IRON_AXE, IRON_BARS, IRON_BLOCK, IRON_BOOTS, IRON_CHESTPLATE, IRON_DOOR, IRON_HELMET, IRON_HOE,
                    IRON_HORSE_ARMOR, IRON_INGOT, IRON_LEGGINGS, IRON_NUGGET, IRON_PICKAXE, IRON_SHOVEL, IRON_SWORD,
                    IRON_TRAPDOOR, NETHERITE_AXE, NETHERITE_BLOCK, NETHERITE_BOOTS, NETHERITE_CHESTPLATE,
                    NETHERITE_HELMET, NETHERITE_HOE, NETHERITE_INGOT, NETHERITE_LEGGINGS, NETHERITE_PICKAXE,
                    NETHERITE_SCRAP, NETHERITE_SHOVEL, NETHERITE_SWORD, ANCIENT_DEBRIS, GOLD_BLOCK, GOLD_INGOT,
                    GOLDEN_AXE, GOLDEN_BOOTS, GOLDEN_CHESTPLATE, GOLDEN_HELMET, GOLDEN_HOE, GOLDEN_HORSE_ARMOR,
                    GOLDEN_LEGGINGS, GOLDEN_SWORD, GOLDEN_PICKAXE, GOLDEN_SHOVEL, BUCKET, FLINT_AND_STEEL, MINECART,
                    HOPPER_MINECART, CHEST_MINECART, FURNACE_MINECART, TNT_MINECART, CHAIN, CHAINMAIL_BOOTS,
                    CHAINMAIL_CHESTPLATE, CHAINMAIL_HELMET, CHAINMAIL_LEGGINGS, HOPPER, ANVIL, CHIPPED_ANVIL,
                    DAMAGED_ANVIL, SHEARS, CAULDRON, COPPER_BLOCK, COPPER_INGOT, CUT_COPPER, CUT_COPPER_SLAB,
                    CUT_COPPER_STAIRS, EXPOSED_CUT_COPPER_STAIRS, EXPOSED_COPPER, EXPOSED_CUT_COPPER,
                    EXPOSED_CUT_COPPER_SLAB, RAW_COPPER, RAW_COPPER_BLOCK, OXIDIZED_COPPER, OXIDIZED_CUT_COPPER,
                    OXIDIZED_CUT_COPPER_SLAB, OXIDIZED_CUT_COPPER_STAIRS, WEATHERED_COPPER, WEATHERED_CUT_COPPER_SLAB,
                    WEATHERED_CUT_COPPER_STAIRS, LIGHTNING_ROD -> true;
            default -> false;
        };
    }
}
