package net.nekocraft.nekocore.utils;

import net.nekocraft.nekocore.Constants;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
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
}
