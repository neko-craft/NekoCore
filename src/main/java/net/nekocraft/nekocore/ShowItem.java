package net.nekocraft.nekocore;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class ShowItem implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {
        if (!(s instanceof Player)) {
            s.sendMessage("§c你不是玩家.");
            return true;
        }
        Player p = (Player) s;
        String name = p.getDisplayName();
        ItemStack i = p.getInventory().getItemInMainHand();
        if (i.getType() == Material.AIR) {
            s.sendMessage("§c你的手里没有物品.");
            return true;
        }
        BaseComponent[] hoverEventComponents = new BaseComponent[]{
            new TextComponent(convertItemStackToJson(i))
        };

        ItemMeta im = i.getItemMeta();
        String sn = "[" + (im.hasDisplayName() ? im.getDisplayName() : i.getI18NDisplayName()) + "]";
        if (i.getAmount() > 1) sn += "x" + i.getAmount();
        TextComponent c3 = new TextComponent(sn);
        c3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents));
        c3.setColor(ChatColor.AQUA);

        TextComponent c1 = new TextComponent(name);

        TextComponent c2 = new TextComponent(" 向你展示了一个物品: ");
        c2.setColor(ChatColor.YELLOW);

        TextComponent c4 = new TextComponent(" ←鼠标移到这里可以查看");
        c4.setColor(ChatColor.GRAY);

        Bukkit.broadcast(c1, c2, c3, c4);
        return true;
    }

    @Nullable
    private String convertItemStackToJson(ItemStack itemStack) {
        Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

        Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
        Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
        Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

        try {
            Object nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
            Object nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            return saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj).toString();
        } catch (Exception t) {
            t.printStackTrace();
            return null;
        }
    }
}
