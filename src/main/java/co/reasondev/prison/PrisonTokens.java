package co.reasondev.prison;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

    public static boolean isTokenItem(ItemStack i) {
        return i != null && i.getType() == Material.DOUBLE_PLANT && i.hasItemMeta() &&
                i.getItemMeta().hasDisplayName() && i.getItemMeta().getDisplayName().equals(Settings.General.DISPLAY_NAME.toString());
    }

    public static int getBothTokens(Player player) {
        return getTokens(player) + getTokenItems(player);
    }

    public static void takeBothTokens(Player player, int amount) {
        int left = takeTokenItems(player, amount);
        if (left > 0) {
            setTokens(player, getTokens(player) - left);
        }
    }

    public static int getTokenItems(Player player) {
        int counter = 0;
        for (ItemStack i : player.getInventory().getContents()) {
            if (!isTokenItem(i)) {
                continue;
            }
            counter += i.getAmount();
        }
        return counter;
    }

    public static HashMap<Integer, ItemStack> giveTokenItems(Player player, int amount) {
        ItemStack i = new ItemStack(Material.DOUBLE_PLANT, amount);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(Settings.General.DISPLAY_NAME.toString());
        i.setItemMeta(meta);
        return player.getInventory().addItem(i);
    }

    public static int takeTokenItems(Player player, int amount) {
        for (int i = 0; i < player.getInventory().getSize() && amount > 0; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (!isTokenItem(item)) {
                continue;
            }
            final int count = item.getAmount();
            if (item.getAmount() <= amount) {
                player.getInventory().setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - amount);
            }
            amount -= count;
        }
        return amount;
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
