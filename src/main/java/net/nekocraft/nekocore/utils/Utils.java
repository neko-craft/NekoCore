package net.nekocraft.nekocore.utils;

import net.nekocraft.nekocore.Constants;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("ConstantConditions")
public final class Utils {
    private static final Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
    private static final Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
    private static final Class<?> nmsItemClazz = ReflectionUtil.getNMSClass("Item");
    private static final Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
    private static final Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);
    private static final Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);
    private static final Method getItemNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "getItem");
    private static final Method getNameNmsItemMethod = ReflectionUtil.getMethod(nmsItemClazz, "getName");
    private static final Field craftItemStackHandleField = ReflectionUtil.getField(craftItemStackClazz, "handle", true);

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

    public static String getItemName(final ItemStack itemStack) {
        try {
            return (String) getNameNmsItemMethod.invoke(getItemNmsItemStackMethod.invoke(getNMSItemStack(itemStack)));
        } catch (Exception t) {
            t.printStackTrace();
            return null;
        }
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
}
