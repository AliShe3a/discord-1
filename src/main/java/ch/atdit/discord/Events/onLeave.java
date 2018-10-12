package ch.atdit.discord.Events;

import ch.atdit.discord.Discord;
import com.mashape.unirest.http.Unirest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class onLeave implements Listener {

    private Discord instance;

    public onLeave(Discord main) {
        this.instance = main;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        String server = instance.getConfig().getString("name");
        String ip = instance.getConfig().getString("server-ip");
        String port = instance.getConfig().getString("port");

        try {
            Unirest.post(ip + ":" + port + "/leave").header("Content-Type", "application/json")
                    .queryString("server", server).queryString("user", p.getName()).asJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
