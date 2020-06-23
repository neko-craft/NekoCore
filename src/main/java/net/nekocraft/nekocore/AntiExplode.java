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
    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (flag) {
            flag = false;
            if (sender.isOp()) sender.sendMessage("§a当前服务器的TNT爆炸已开启!");
            else Bukkit.broadcastMessage("§a当前服务器的TNT爆炸已开启!");
        } else {
            flag = true;
            if (sender.isOp()) sender.sendMessage("§e当前服务器的TNT爆炸已关闭!");
            else Bukkit.broadcastMessage("§e当前服务器的TNT爆炸已关闭!");
        }
        return true;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (flag && (e.getEntityType() == EntityType.PRIMED_TNT || e.getEntityType() == EntityType.MINECART_TNT))
            e.blockList().clear();
    }
}
