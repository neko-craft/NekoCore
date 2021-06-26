package net.nekocraft.nekocore;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

final class AntiExplode implements CommandExecutor, Listener {
    private boolean flag = false;
    @SuppressWarnings({"NullableProblems", "deprecation"})
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (flag) {
            flag = false;
            if (sender.isOp()) sender.sendMessage("��a��ǰ��������TNT��ը�ѿ���!");
            else Bukkit.broadcastMessage("��a��ǰ��������TNT��ը�ѿ���!");
        } else {
            flag = true;
            if (sender.isOp()) sender.sendMessage("��e��ǰ��������TNT��ը�ѹر�!");
            else Bukkit.broadcastMessage("��e��ǰ��������TNT��ը�ѹر�!");
        }
        return true;
    }

    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent e) {
        if (flag && (e.getEntityType() == EntityType.PRIMED_TNT || e.getEntityType() == EntityType.MINECART_TNT))
            e.blockList().clear();
    }
}
