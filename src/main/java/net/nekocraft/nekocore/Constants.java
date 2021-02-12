package net.nekocraft.nekocore;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.potion.PotionEffectType;

import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public final class Constants {
    private Constants() {}
    public static final String COMMAND_DESCRIPTION = "A NekoCraft provided command.";
    public static final String WRONG_USAGE = "��c����������÷�!";
    public static final String NO_PERMISSION = "��c��û��Ȩ����ִ�����ָ��!";

    public static final String PLAYER_HEADER = "��b��m          ��r ��a[��eNekoCraft��a] ��b��m          \n��aTelegram Ⱥ��: ��7t.me/NekoCraft\n��aQQ Ⱥ: ��77923309\n��r";

    public static final String JOIN_MESSAGE_HEADER = "��b��m                       ��r ��a[��eNekoCraft��a] ��b��m                      ��r";
    public static final String JOIN_MESSAGE1 = "  ��c���ڷ�����û����ز��, �벻Ҫ������ȡ������Ʒ, �����ֱ�ӷ��!";
    public static final String JOIN_MESSAGE_FOOTER = "��b��m                                                          ��r\n\n";
    public static final TextComponent[] JOIN_MESSAGES = new TextComponent[9];

    public static final TextComponent[] RULES = new TextComponent[6];
    public static final HoverEvent AT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{ new TextComponent("��f�������ֱ�� ��a@ ��f�����") }),
        TPA = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{ new TextComponent("������Ը����ı�") });

    public static final Pattern[] DANGER_COMMANDS = {
            Pattern.compile("kill @e *$"),
            Pattern.compile("kill @e\\[(?!type=)"),
            Pattern.compile("tp @e *$"),
            Pattern.compile("tp @e\\[(?!type=)"),
            Pattern.compile("teleport @e *$"),
            Pattern.compile("teleport @e\\[(?!type=)")
    };

    public static final PotionEffectType[] EFFECTS = new PotionEffectType[] {
            PotionEffectType.SPEED,
            PotionEffectType.HEALTH_BOOST,
            PotionEffectType.INCREASE_DAMAGE,
            PotionEffectType.JUMP,
            PotionEffectType.REGENERATION,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.ABSORPTION
    };

    static {
        (JOIN_MESSAGES[0] = new TextComponent("  QQ Ⱥ: ")).setColor(ChatColor.GREEN);

        TextComponent c = JOIN_MESSAGES[1] = new TextComponent("7923309");
        c.setColor(ChatColor.GRAY);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://jq.qq.com/?k=5AzDYNC"));

        JOIN_MESSAGES[2] = new TextComponent("      ");

        (JOIN_MESSAGES[3] = new TextComponent("Telegram Ⱥ��: ")).setColor(ChatColor.GREEN);

        c = JOIN_MESSAGES[4] = new TextComponent("@NekoCraft");
        c.setColor(ChatColor.GRAY);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://t.me/NekoCraft"));

        (JOIN_MESSAGES[5] = new TextComponent("\n  �û����� & ���ͼ: ")).setColor(ChatColor.GREEN);

        JOIN_MESSAGES[6] = c = new TextComponent("user.neko-craft.com");
        c.setColor(ChatColor.GRAY);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://user.neko-craft.com"));

        (JOIN_MESSAGES[7] = new TextComponent("\n  ��������ַ & ����: ")).setColor(ChatColor.GREEN);

        JOIN_MESSAGES[8] = c = new TextComponent("neko-craft.com");
        c.setColor(ChatColor.GRAY);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://neko-craft.com"));


        RULES[0] = new TextComponent("  ��b��ӭ������ ��eNekoCraft ��a!\n  ��e����Ҫɨ�������еĶ�ά����� ");

        c = RULES[1] = new TextComponent("[����]");
        c.setColor(ChatColor.BLUE);
        c.setUnderlined(true);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://user.neko-craft.com/#/about"));

        RULES[2] = new TextComponent("  ��e���Ķ��������涨\n  ��7���ȷ�Ϻ���Ĭ�������Ķ������ط������涨!\n       ");

        c = RULES[3] = new TextComponent(" [�����Ķ������ط������涨] ��7������ָ��/acceptrule");
        c.setColor(ChatColor.GREEN);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptrule"));

        RULES[4] = new TextComponent("      ");

        c = RULES[5] = new TextComponent("[�ܾ�]");
        c.setColor(ChatColor.RED);
        c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/denyrule"));
    }
}
