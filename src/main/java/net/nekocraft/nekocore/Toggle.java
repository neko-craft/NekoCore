package net.nekocraft.nekocore;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

@SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
final class Toggle implements CommandExecutor {
    private final File db;
    Toggle(Main plugin) {
        db = new File(plugin.getDataFolder(), "toggle");
        db.mkdirs();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player)) return false;
        Player p = (Player) s;
        File file = new File(db, p.getUniqueId().toString() + ".json");
        switch (p.getGameMode()) {
            case SURVIVAL:
                if (file.exists()) file.delete();
                try (FileWriter fw = new FileWriter(file)) {
                    JSONObject.writeJSONString(p.getLocation().serialize(), fw);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                p.setGameMode(GameMode.SPECTATOR);
                break;
            case SPECTATOR:
                if (file.exists()) {
                    try (FileReader fr = new FileReader(file)) {
                        p.teleport(Location.deserialize((Map<String, Object>) new JSONParser().parse(fr)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                p.setGameMode(GameMode.SURVIVAL);
                break;
        }
        p.sendActionBar("§e成功切换模式!");
        return true;
    }
}
