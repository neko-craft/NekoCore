package net.nekocraft.nekocore.utils;

import net.nekocraft.nekocore.Constants;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

@SuppressWarnings("ConstantConditions")
public final class Utils {
    private static final Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
    private static final Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
    private static final Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
    private static final Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);
    private static final Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);
    private static final Field craftItemStackHandleField = ReflectionUtil.getField(craftItemStackClazz, "handle", true);
    private static final BlockFace[] blockFaces = BlockFace.values();

    private Utils() {}

    public static String convertItemStackToJson(final ItemStack itemStack) {
        try {
            final Object nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
            return saveNmsItemStackMethod.invoke(getNMSItemStack(itemStack), nmsNbtTagCompoundObj).toString();
        } catch (Exception t) {
            t.printStackTrace();
            return null;
        }
    }

    private static Object getNMSItemStack(final ItemStack itemStack) throws InvocationTargetException, IllegalAccessException {
        Object nms = null;
        if (craftItemStackClazz.isInstance(itemStack)) try {
            nms = craftItemStackHandleField.get(itemStack);
        } catch (Exception ignored) { }
        return nms == null ? asNMSCopyMethod.invoke(null, itemStack) : nms;
    }

    public static void registerCommand(final String name, final CommandExecutor e) {
        final PluginCommand cmd = Bukkit.getPluginCommand(name);
        assert cmd != null;
        cmd.setUsage(Constants.WRONG_USAGE);
        cmd.setPermissionMessage(Constants.NO_PERMISSION);
        cmd.setDescription(Constants.COMMAND_DESCRIPTION);
        cmd.setExecutor(e);
    }

    public static String getDisplayName(final Player p) {
        switch (p.getUniqueId().toString()) {
            case "18c7d817-3ad3-4b0f-9106-6eb471dfd530": return "¡ìaÂ¶Â¶";
            case "c0a5ed47-a171-49ba-bd69-cb5b217ae0f2": return "¡ìaBB";
            case "8c33f169-44f1-4a2d-ad9b-9d6b37b363da": return "¡ìa¾õ¾õ";
            case "3de49e85-2e7c-43f9-8ff2-4cea43da4655": return "¡ìaÜ½Ü½";
            default: return "¡ìf" + p.getDisplayName();
        }
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

    public static boolean isLeaves(final Material type) {
        switch (type) {
            case ACACIA_LEAVES:
            case BIRCH_LEAVES:
            case DARK_OAK_LEAVES:
            case JUNGLE_LEAVES:
            case OAK_LEAVES:
            case SPRUCE_LEAVES:
                return true;
            default: return false;
        }
    }

    public static boolean isLog(final Material type) {
        switch (type) {
            case ACACIA_LOG:
            case BIRCH_LOG:
            case DARK_OAK_LOG:
            case JUNGLE_LOG:
            case OAK_LOG:
            case SPRUCE_LOG:
                return true;
            default: return false;
        }
    }

    public static boolean isConductive(final Material type) {
        switch (type) {
            case IRON_AXE:
            case IRON_BARS:
            case IRON_BLOCK:
            case IRON_BOOTS:
            case IRON_CHESTPLATE:
            case IRON_DOOR:
            case IRON_HELMET:
            case IRON_HOE:
            case IRON_HORSE_ARMOR:
            case IRON_INGOT:
            case IRON_LEGGINGS:
            case IRON_NUGGET:
            case IRON_PICKAXE:
            case IRON_SHOVEL:
            case IRON_SWORD:
            case IRON_TRAPDOOR:
            case NETHERITE_AXE:
            case NETHERITE_BLOCK:
            case NETHERITE_BOOTS:
            case NETHERITE_CHESTPLATE:
            case NETHERITE_HELMET:
            case NETHERITE_HOE:
            case NETHERITE_INGOT:
            case NETHERITE_LEGGINGS:
            case NETHERITE_PICKAXE:
            case NETHERITE_SCRAP:
            case NETHERITE_SHOVEL:
            case NETHERITE_SWORD:
            case ANCIENT_DEBRIS:
            case GOLD_BLOCK:
            case GOLD_INGOT:
            case GOLDEN_AXE:
            case GOLDEN_BOOTS:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_HELMET:
            case GOLDEN_HOE:
            case GOLDEN_HORSE_ARMOR:
            case GOLDEN_LEGGINGS:
            case GOLDEN_SWORD:
            case GOLDEN_PICKAXE:
            case GOLDEN_SHOVEL:
            case BUCKET:
            case FLINT_AND_STEEL:
            case MINECART:
            case CHAIN:
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_LEGGINGS:
                return true;
            default: return false;
        }
    }
}
