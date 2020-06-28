package net.nekocraft.nekocore;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.nekocraft.nekocore.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

final class ShowItem implements CommandExecutor {
    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {
        if (!(s instanceof Player)) return false;
        Player p = (Player) s;
        String name = p.getDisplayName();
        ItemStack i = p.getInventory().getItemInMainHand();
        if (i.getType() == Material.AIR) {
            s.sendMessage("§c你的手里没有物品.");
            return true;
        }
        BaseComponent[] hoverEventComponents = {
            new TextComponent(Utils.convertItemStackToJson(i))
        };

        ItemMeta im = i.getItemMeta();
        String itemName = Utils.getItemName(i);
        BaseComponent c3;
        if (im.hasDisplayName() || itemName == null) {
            String sn = "[" + (im.hasDisplayName() ? im.getDisplayName() : i.getI18NDisplayName()) + "]";
            if (i.getAmount() > 1) sn += "x" + i.getAmount();
            c3 = new TextComponent(sn);
        } else {
            c3 = new TextComponent("[");
            c3.addExtra(new TranslatableComponent(itemName));
            c3.addExtra("]");
            if (i.getAmount() > 1) c3.addExtra("x" + i.getAmount());
        }
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
}
