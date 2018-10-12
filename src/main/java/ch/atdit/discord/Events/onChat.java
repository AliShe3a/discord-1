package ch.atdit.discord.Events;

import ch.atdit.discord.Discord;
import com.mashape.unirest.http.Unirest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class onChat implements Listener {

    private Discord instance;

    public onChat(Discord main) {
        this.instance = main;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();

        String server = instance.getConfig().getString("name");
        String ip = instance.getConfig().getString("server-ip");
        String port = instance.getConfig().getString("port");

        try {
            Unirest.post(ip + ":" + port + "/chat").header("Content-Type", "application/json")
                    .queryString("server", server).queryString("user", p.getName())
                    .queryString("message", event.getMessage()).queryString("timestamp", System.currentTimeMillis()).asJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
