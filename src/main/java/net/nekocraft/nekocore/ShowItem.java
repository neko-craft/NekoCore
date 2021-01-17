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
    private final static TextComponent TEXT_C = new TextComponent(" 向你展示了一个物品: ");
    static { TEXT_C.setColor(ChatColor.GRAY); }

    @SuppressWarnings({ "NullableProblems", "deprecation" })
    @Override
    public boolean onCommand(final CommandSender s, final Command command, final String label, final String[] args) {
        if (!(s instanceof Player)) return false;
        final Player p = (Player) s;
        final String name = p.getDisplayName();
        final ItemStack i = p.getInventory().getItemInMainHand();
        if (i.getType() == Material.AIR) {
            s.sendMessage("§c你的手里没有物品.");
            return true;
        }

        final ItemMeta im = i.getItemMeta();
        final String itemName = (i.getType().isBlock() ? "block." : "item.") + i.getType().getKey()
            .toString().replace(':', '.');
        final BaseComponent c3;
        if (im.hasDisplayName()) {
            c3 = new TextComponent("[");
            final TextComponent t = new TextComponent(im.hasDisplayName() ? im.getDisplayName() : i.getI18NDisplayName());
            t.setItalic(true);
            c3.addExtra(t);
            c3.addExtra("]");
            if (i.getAmount() > 1) c3.addExtra("x" + i.getAmount());
        } else {
            c3 = new TextComponent("[");
            c3.addExtra(new TranslatableComponent(itemName));
            c3.addExtra("]");
            if (i.getAmount() > 1) c3.addExtra("x" + i.getAmount());
        }
        c3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new TextComponent[] {
                new TextComponent(Utils.convertItemStackToJson(i))
        }));
        c3.setColor(ChatColor.AQUA);

        Bukkit.broadcast(new TextComponent(name), TEXT_C, c3);
        return true;
    }
}
