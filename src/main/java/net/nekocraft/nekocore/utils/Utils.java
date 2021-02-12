package net.nekocraft.nekocore.utils;

import net.nekocraft.nekocore.Constants;
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

@SuppressWarnings("ConstantConditions")
public final class Utils {
    private static final Class<?> craftItemStackClass = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
    private static final Class<?> craftPlayerClass = ReflectionUtil.getOBCClass("entity.CraftPlayer");
    private static final Class<?> nmsItemStackClass = ReflectionUtil.getNMSClass("ItemStack");
    private static final Class<?> nbtTagCompoundClass = ReflectionUtil.getNMSClass("NBTTagCompound");
    private static final Class<?> nmsEntityPlayerClass = ReflectionUtil.getNMSClass("EntityPlayer");
    private static final Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
    private static final Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClass, "save", nbtTagCompoundClass);
    private static final Method craftPlayerGetHandle = ReflectionUtil.getMethod(craftPlayerClass, "getHandle");
    private static final Field craftItemStackHandleField = ReflectionUtil.getField(craftItemStackClass, "handle", true);
    private static final Field nmsEntityPlayerPingField = ReflectionUtil.getField(nmsEntityPlayerClass, "ping", false);
    private static final BlockFace[] blockFaces = BlockFace.values();

    private Utils() {}

    public static String convertItemStackToJson(final ItemStack itemStack) {
        try {
            final Object nmsNbtTagCompoundObj = nbtTagCompoundClass.newInstance();
            return saveNmsItemStackMethod.invoke(getNMSItemStack(itemStack), nmsNbtTagCompoundObj).toString();
        } catch (Exception t) {
            t.printStackTrace();
            return null;
        }
    }

    private static Object getNMSItemStack(final ItemStack itemStack) throws InvocationTargetException, IllegalAccessException {
        Object nms = null;
        if (craftItemStackClass.isInstance(itemStack)) try {
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

    public static int getPlayerPing(final Player player) {
        try {
            return (int) nmsEntityPlayerPingField.get(craftPlayerGetHandle.invoke(player));
        } catch (final Exception e) {
            return -1;
        }
    }

    public static String getDisplayName(final Player p) {
        switch (p.getUniqueId().toString()) {
            case "18c7d817-3ad3-4b0f-9106-6eb471dfd530": return "¡ìfÂ¶Â¶";
            case "c0a5ed47-a171-49ba-bd69-cb5b217ae0f2": return "¡ìfBB";
            case "8c33f169-44f1-4a2d-ad9b-9d6b37b363da": return "¡ìf¾õ¾õ";
            case "70d557ce-c6fc-43b2-b356-d2957d92c700": return "¡ìf±âÜ½Fulandre_Scarlet";
            case "e59cd3bc-5c8a-485a-b7a6-0dad75da74c6": return "¡ìf¹ÄÜ½Flandern_S";
            case "a2bf5901-8cd1-44cd-af49-cb7b839d8076": return "¡ìfÂ¶ç÷ÑÇÐ¡ÅóÓÑ";
            case "0936c888-85d4-424d-8194-f6dceab8ec57": return "¡ìfÃ¨ÄïYtonE";
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

    public static void giveAdvancement(final Advancement ad, final Player p) {
        if (ad == null) return;
        final AdvancementProgress ap = p.getAdvancementProgress(ad);
        if (!ap.isDone()) ap.awardCriteria("trigger");
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

    public static boolean isConductive(final ItemStack item) {
        return item != null && isConductive(item.getType());
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
            case HOPPER_MINECART:
            case CHEST_MINECART:
            case FURNACE_MINECART:
            case TNT_MINECART:
            case CHAIN:
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_LEGGINGS:
            case HOPPER:
            case ANVIL:
            case CHIPPED_ANVIL:
            case DAMAGED_ANVIL:
            case SHEARS:
            case CAULDRON:
                return true;
            default: return false;
        }
    }
}
