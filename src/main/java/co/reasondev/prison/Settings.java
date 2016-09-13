package co.reasondev.prison;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class Settings {

    private static FileConfiguration c;

    public static void setConfig(FileConfiguration config) {
        c = config;
    }

    public enum General {

        CLAIM_AMOUNT;

        public int val() {
            return c.getInt(name());
        }
    }

    public enum MySQL {

        HOST, PORT, DATABASE, USERNAME, PASSWORD;

        public String val() {
            return c.getString("mySQL." + name());
        }
    }

    public enum Messages {

        PREFIX,

        TOKENS_HELP,

        BALANCE, BALANCE_OTHER,

        TOKENS_CLAIMED, CLAIM_COOLDOWN,

        WITHDRAW_SUCCESS, WITHDRAW_FAILURE,

        DEPOSIT_SUCCESS, DEPOSIT_FAILURE,

        SHOP_MESSAGE;

        public String val() {
            return ChatColor.translateAlternateColorCodes('&', c.getString("messages." + name()).replace("\\n", "\n"));
        }
    }
}
