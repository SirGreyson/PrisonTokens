package co.reasondev.prison;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PrisonTokens extends JavaPlugin {

    protected static SQLManager sqlManager;

    private static Map<UUID, Integer> TOKENS = new HashMap<>();
    private static Map<UUID, Long> LAST_CLAIM = new HashMap<>();

    public void onEnable() {
        saveDefaultConfig();
        Settings.setConfig(getConfig());
        if(getSQLManager().openConnection() == null) {
            getLogger().severe("Error! Could not connect to MySQL Database! Plugin disabling...");
            setEnabled(false);
        } else {
            registerPlaceholder();
            getServer().getPluginManager().registerEvents(new PlayerListener(), this);
            getCommand("tokens").setExecutor(new TokensCommand(this));
            getLogger().info("has been enabled");
        }
    }

    public void onDisable() {
        for(Player p : getServer().getOnlinePlayers()) {
            sqlManager.setTokenData(p, TOKENS.get(p.getUniqueId()), LAST_CLAIM.get(p.getUniqueId()));
        }
        getSQLManager().closeConnection();
        getLogger().info("has been disabled");
    }

    private void registerPlaceholder() {
        if (getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            PlaceholderAPI.registerPlaceholder(this, "tokens", new PlaceholderReplacer() {
                @Override
                public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
                    if (e.isOnline()) {
                        return String.valueOf(getTokens(e.getPlayer()));
                    }
                    return String.valueOf(getTokens(e.getOfflinePlayer()));
                }
            });
        }
    }

    public SQLManager getSQLManager() {
        if(sqlManager == null) {
            sqlManager = new SQLManager(this);
        }
        return sqlManager;
    }

    public static int getTokens(OfflinePlayer player) {
        if(!TOKENS.containsKey(player.getUniqueId())) {
            TOKENS.put(player.getUniqueId(), sqlManager.getTokens(player));
        }
        return TOKENS.get(player.getUniqueId());
    }

    public static void setTokens(OfflinePlayer player, int amount) {
        TOKENS.put(player.getUniqueId(), amount);
    }

    public static long getLastClaim(OfflinePlayer player) {
        if(!LAST_CLAIM.containsKey(player.getUniqueId())) {
            LAST_CLAIM.put(player.getUniqueId(), sqlManager.getLastClaim(player));
        }
        return LAST_CLAIM.get(player.getUniqueId());
    }

    public static void setLastClaim(OfflinePlayer player) {
        LAST_CLAIM.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
