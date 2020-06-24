package net.nekocraft.nekocore;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public final class Constants {
    private Constants() {}
    public static final String WRONG_USAGE = "§c错误的命令用法!";
    public static final String NO_PERMISSION = "§c你没有权限来执行这个指令!";

    public static final String PLAYER_HEADER = "§b§m          §r §a[§eNekoCraft§a] §b§m          \n§aTelegream 群组: §7t.me/NekoCraft\n§aQQ 群: §77923309\n§r";

    public static final String JOIN_MESSAGE_HEADER = "§b§m                       §r §a[§eNekoCraft§a] §b§m                      §r";
    public static final String JOIN_MESSAGE1 = "  §c由于服务器没有领地插件, 请不要随意拿取他人物品, 否则会直接封禁!";
    public static final String JOIN_MESSAGE_FOOTER = "§b§m                                                          §r\n\n\n";
    public static final TextComponent[] JOIN_MESSAGES = new TextComponent[9];

    public static final TextComponent[] RULES = new TextComponent[6];

    static {
        (JOIN_MESSAGES[0] = new TextComponent("  QQ 群: ")).setColor(ChatColor.GREEN);

        TextComponent c = JOIN_MESSAGES[1] = new TextComponent("7923309");
        c.setColor(ChatColor.GRAY);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://jq.qq.com/?k=5AzDYNC"));

        JOIN_MESSAGES[2] = new TextComponent("      ");

        (JOIN_MESSAGES[3] = new TextComponent("Telegram 群组: ")).setColor(ChatColor.GREEN);

        c = JOIN_MESSAGES[4] = new TextComponent("@NekoCraft");
        c.setColor(ChatColor.GRAY);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://t.me/NekoCraft"));

        (JOIN_MESSAGES[5] = new TextComponent("\n  用户中心 & 大地图: ")).setColor(ChatColor.GREEN);

        JOIN_MESSAGES[6] = c = new TextComponent("user.n.apisium.cn");
        c.setColor(ChatColor.GRAY);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://user.n.apisium.cn"));

        (JOIN_MESSAGES[7] = new TextComponent("\n  服务器地址 & 官网: ")).setColor(ChatColor.GREEN);

        JOIN_MESSAGES[8] = c = new TextComponent("n.apisium.cn");
        c.setColor(ChatColor.GRAY);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://n.apisium.cn/index.html"));

        RULES[0] = new TextComponent("  §b欢迎您第一次来到 §eNekoCraft §a!\n  §e您需要扫描您手中的二维码或点击 ");

        c = RULES[1] = new TextComponent("[这里]");
        c.setColor(ChatColor.BLUE);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://user.n.apisium.cn/about"));

        RULES[2] = new TextComponent("  §e来阅读服务器规定\n  §7点击确认后则默认您已阅读并遵守服务器规定!\n       ");

        c = RULES[3] = new TextComponent(" [我已阅读并遵守服务器规定] ");
        c.setColor(ChatColor.GREEN);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptrule"));

        RULES[4] = new TextComponent("      ");

        c = RULES[5] = new TextComponent("[拒绝]");
        c.setColor(ChatColor.RED);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/denyrule"));
    }
}
