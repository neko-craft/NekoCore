package net.nekocraft.nekocore;

import net.nekocraft.nekocore.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public final class Network implements Listener, CommandExecutor {
    private final HashMap<String, Integer> map = new HashMap<>();
    private final HashMap<Player, String> playerToAddress = new HashMap<>();
    @SuppressWarnings("ConstantConditions")
    public Network(final Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginCommand("network").setExecutor(this);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        sender.sendMessage("§b连接信息:");
        map.forEach((k, v) -> sender.sendMessage(k + "§7: " + v));
        if (sender instanceof Player) sender.sendMessage("Ping: §7" + Utils.getPlayerPing((Player) sender) + "ms");
        return true;
    }

    @EventHandler
    public void onPlayerLogin(final PlayerLoginEvent e) {
        String ip = e.getHostname().replace("\00FML2\00", "")
                .replace(".:", ":").replace("apisium.cn", "neko-craft.com").replace(":25565", "");
        playerToAddress.put(e.getPlayer(), ip);
        map.put(ip, map.getOrDefault(ip, 0) + 1);
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        String ip = playerToAddress.get(e.getPlayer());
        if (ip == null) return;
        Integer it = map.get(ip);
        if (it == null) return;
        if (--it == 0) map.remove(ip);
        else map.put(ip, it);
    }
}
