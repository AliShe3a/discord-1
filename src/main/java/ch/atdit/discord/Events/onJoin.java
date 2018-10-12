package ch.atdit.discord.Events;

import ch.atdit.discord.Discord;
import com.mashape.unirest.http.Unirest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class onJoin implements Listener {

    private Discord instance;

    public onJoin(Discord main) {
        this.instance = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        String server = instance.getConfig().getString("name");
        String ip = instance.getConfig().getString("server-ip");
        String port = instance.getConfig().getString("port");

        try {
            Unirest.post(ip + ":" + port + "/join").header("Content-Type", "application/json")
                    .queryString("server", server).queryString("user", p.getName()).asJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
