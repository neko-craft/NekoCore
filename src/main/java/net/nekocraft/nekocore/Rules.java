package net.nekocraft.nekocore;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

final class Rules implements Listener, CommandExecutor {
    private static final String ITEM_NAME = "§e服务器规则二维码";
    private static final Render render = new Render();
    private final MapView map = Bukkit.createMap(Objects.requireNonNull(Bukkit.getWorld("world")));

    private final HashSet<Player> notAccepts = new HashSet<>();
    private final File acceptsFile;
    private String accepts = "";

    {
        map.getRenderers().forEach(map::removeRenderer);
        map.addRenderer(render);
        map.setLocked(true);
    }

    public Rules(Main main) {
        acceptsFile = new File(main.getDataFolder(), "accepts.txt");

        try {
            if (acceptsFile.exists()) accepts = new String(Files.readAllBytes(acceptsFile.toPath()));
            else if (!acceptsFile.createNewFile()) throw new IOException("Failed to create new file");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(main.getServer().getPluginCommand("denyrule")).setExecutor((sender, c, l, a) -> {
            if (!(sender instanceof Player)) return false;
            final Player p = (Player) sender;
            if (notAccepts.contains(p)) p.kickPlayer("§e[NekoCraft] §c你拒绝遵守服务器规定.");
            else {
                final PlayerInventory i = p.getInventory();
                final ItemStack is = i.getItemInMainHand();
                final List<String> lore = is.getLore();
                if (lore != null && lore.size() == 1 && lore.get(0).equals(ITEM_NAME)) i.remove(is);
                p.sendMessage("§c你已经同意遵守了服务器规定!");
            }
            return true;
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (accepts.contains(p.getUniqueId().toString())) return;
        notAccepts.add(p);
        p.sendMessage(Constants.JOIN_MESSAGE_HEADER);
        p.sendMessage(Constants.RULES);
        p.sendMessage(Constants.JOIN_MESSAGE_FOOTER);
        if (p.getInventory().getItemInMainHand().getType() != Material.AIR) return;
        final ItemStack is = new ItemStack(Material.FILLED_MAP);
        final MapMeta meta = (MapMeta) is.getItemMeta();
        meta.setMapView(map);
        meta.setDisplayName(ITEM_NAME);
        meta.setColor(Color.YELLOW);
        meta.setLocationName("二维码");
        meta.setUnbreakable(true);
        meta.setLore(Lists.newArrayList(ITEM_NAME));
        p.getInventory().setItemInMainHand(is);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        notAccepts.remove(e.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (notAccepts.contains(e.getPlayer())) e.setCancelled(true);
    }
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (notAccepts.contains(e.getPlayer())) e.setCancelled(true);
    }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (notAccepts.contains(e.getPlayer())) e.setCancelled(true);
    }
    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e) {
        if (notAccepts.contains(e.getPlayer())) e.setCancelled(true);
    }
    @EventHandler
    public void onHeld(PlayerItemHeldEvent e) {
        if (notAccepts.contains(e.getPlayer())) e.setCancelled(true);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        String uuid = p.getUniqueId().toString();
        if (!accepts.contains(uuid)) {
            notAccepts.remove(p);
            String text = "," + uuid;
            accepts += text;
            try {
                FileWriter writer = new FileWriter(acceptsFile, true);
                writer.write(text);
                writer.close();
            } catch (IOException e) { e.printStackTrace(); }
            p.sendMessage("§a感谢您接受了服务器的规定, §e同时也希望您能一直遵守规定!");
            Bukkit.broadcastMessage("§b欢迎新玩家 §7" + p.getDisplayName() + " §b加入了服务器!");
        }
        return true;
    }

    private final static class Render extends MapRenderer {
        private BufferedImage buffer;

        {
            try {
                buffer = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("QRCode.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public void render(MapView map, MapCanvas canvas, Player player) {
            canvas.drawImage(0, 0, buffer);
        }
    }
}
