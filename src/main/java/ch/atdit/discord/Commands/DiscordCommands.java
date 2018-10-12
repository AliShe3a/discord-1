package ch.atdit.discord.Commands;

import ch.atdit.discord.Discord;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Random;

public class DiscordCommands {

    private static final Random random = new Random();
    private Discord instance;

    public DiscordCommands(Discord main) {
        this.instance = main;
    }

    @Command(aliases = "discord", desc = "Manage the Discord plugin", min = 2, max = 2, usage = "<config|setting> [value|on|off]")
    public static void discord(CommandContext cmd, CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "This command is in development.");
    }
}
