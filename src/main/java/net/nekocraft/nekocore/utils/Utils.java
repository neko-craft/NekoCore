package net.nekocraft.nekocore.utils;

import net.nekocraft.nekocore.Constants;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public final class Utils {
    private Utils() {}

    public static String convertItemStackToJson(ItemStack itemStack) {
        Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

        Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
        Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
        Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

        try {
            Object nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
            assert asNMSCopyMethod != null;
            Object nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            assert saveNmsItemStackMethod != null;
            return saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj).toString();
        } catch (Exception t) {
            t.printStackTrace();
            return null;
        }
    }

    public static void registerCommand(String name, CommandExecutor e) {
        final PluginCommand cmd = Bukkit.getPluginCommand(name);
        assert cmd != null;
        cmd.setUsage(Constants.WRONG_USAGE);
        cmd.setPermissionMessage(Constants.NO_PERMISSION);
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
