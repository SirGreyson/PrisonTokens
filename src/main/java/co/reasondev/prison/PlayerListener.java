package co.reasondev.prison;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        PrisonTokens.getTokens(e.getPlayer());
        PrisonTokens.getLastClaim(e.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        int tokens = PrisonTokens.getTokens(e.getPlayer());
        long lastClaim = PrisonTokens.getLastClaim(e.getPlayer());
        PrisonTokens.sqlManager.setTokenData(e.getPlayer(), tokens, lastClaim);
    }
}
