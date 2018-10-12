package ch.atdit.discord;

import ch.atdit.discord.Commands.DiscordCommands;
import ch.atdit.discord.Events.onChat;
import ch.atdit.discord.Events.onJoin;
import ch.atdit.discord.Events.onLeave;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;

public class Discord extends JavaPlugin {

    private static Discord instance;
    private CommandsManager<CommandSender> commands;

    public static Discord instance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        Long latestStart = System.currentTimeMillis();

        this.commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender sender, String perm) {
                return sender instanceof ConsoleCommandSender || sender.hasPermission(perm);
            }
        };

        CommandsManagerRegistration cmds = new CommandsManagerRegistration(this, this.commands);
        cmds.register(DiscordCommands.class);

        getLogger().info("Discord enabled!");
        saveDefaultConfig();

        if (getConfig().getBoolean("enabled")) {
            String server = getConfig().getString("name");
            String ip = getConfig().getString("server-ip");
            String port = getConfig().getString("port");
            if (server != null && ip != null && port != null) { // If name, server ip and port are defined, send start post
                Bukkit.getServer().getPluginManager().registerEvents(new onChat(this), this);
                Bukkit.getServer().getPluginManager().registerEvents(new onJoin(this), this);
                Bukkit.getServer().getPluginManager().registerEvents(new onLeave(this), this);

                try {
                    Unirest.post(ip + ":" + port + "/started").queryString("server", server).queryString("started", true).asJson();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            HttpResponse<JsonNode> response = Unirest.post(ip + ":" + port + "/getMessages").header("Content-Type", "application/json").asJson();
                            for (int i = 0; i < response.getBody().getObject().length(); i++) {
                                JSONObject res = response.getBody().getArray().getJSONObject(0).getJSONObject(String.valueOf(i));
                                if (res.getLong("timestamp") > latestStart) { // Check if messages were sent after latest start, ignore others
                                    if (res.getString("origin").equalsIgnoreCase("discord")) {
                                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&3[Discord] &f"
                                                + res.getString("author") + ": " + res.getString("message")));
                                    } else if (!res.getString("origin").equalsIgnoreCase(server)) {
                                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&3[" + res.getString("server") + "] &f"
                                                + res.getString("author") + ": " + res.getString("message")));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }.runTaskTimer(this, 0L, 2);
            } else {
                getLogger().warning("Config is missing name, server ip or port.");
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Discord disabled!");

        if (getConfig().getBoolean("enabled")) {
            String server = getConfig().getString("name");
            String ip = getConfig().getString("server-ip");
            String port = getConfig().getString("port");
            if (server != null && ip != null && port != null) { // If name, server ip and port are defined, send start post
                try {
                    Unirest.post(ip + ":" + port + "/stopped").queryString("server", server).queryString("stopped", true).asJson();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                getLogger().warning("Config is missing name, server ip or port.");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        try {
            this.commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permissions.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occured. See console.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }
}
