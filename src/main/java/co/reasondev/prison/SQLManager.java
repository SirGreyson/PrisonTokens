package co.reasondev.prison;

import org.bukkit.OfflinePlayer;

import java.sql.*;

public class SQLManager {

    private PrisonTokens plugin;

    private Connection connection;

    public SQLManager(PrisonTokens plugin) {
        this.plugin = plugin;
    }

    public int getTokens(OfflinePlayer player) {
        try {
            ResultSet res = querySQL("SELECT * FROM player_data WHERE player_id = '" + player.getUniqueId() + "';");
            if (res.next()) {
                return res.getInt("tokens");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error! Could not get Tokens! " + e.getMessage());
        }
        return 0;
    }

    public long getLastClaim(OfflinePlayer player) {
        try {
            ResultSet res = querySQL("SELECT * FROM player_data WHERE player_id = '" + player.getUniqueId() + "';");
            if (res.next()) {
                return res.getLong("last_claim");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error! Could not get Tokens! " + e.getMessage());
        }
        return 0;
    }

    public void setTokenData(OfflinePlayer player, int tokens, long lastClaim) {
        try {
            updateSQL("INSERT INTO player_data (player_id, tokens, last_claim) VALUES (" +
                    "'" + player.getUniqueId() + "', " +
                    "'" + tokens + "', " +
                    "'" + lastClaim + "') " +
                    "ON DUPLICATE KEY UPDATE tokens = VALUES(tokens), last_claim = VALUES(last_claim);");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error! Could not save Tokens! " + e.getMessage());
        }
    }

    //MySQL Database Management

    public boolean checkConnection() throws SQLException {
        return connection != null && !connection.isClosed();
    }

    public Connection openConnection() {
        try {
            if (checkConnection()) {
                return connection;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" +
                            plugin.getConfig().getString("mySQL.HOST") + ":" +
                            plugin.getConfig().getString("mySQL.PORT") + "/" +
                            plugin.getConfig().getString("mySQL.DATABASE"),
                    plugin.getConfig().getString("mySQL.USERNAME"), plugin.getConfig().getString("mySQL.PASSWORD"));
            //Create PlayerData Table in Schema
            updateSQL("CREATE TABLE IF NOT EXISTS " + plugin.getConfig().getString("mySQL.DATABASE") + ".player_data (" +
                    "player_id CHAR(36) NOT NULL, " +
                    "tokens INT NULL DEFAULT 0, " +
                    "last_claim BIGINT NULL DEFAULT 0, " +
                    "PRIMARY KEY (player_id));");
            plugin.getLogger().info("Successfully connected to MySQL database!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error! Could not open MySQL Connection! " + e.getMessage());
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("Error! MySQL Driver not found! Please install MySQL on this machine!");
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (!checkConnection()) {
                return;
            }
            connection.close();
            connection = null;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error! Could not close MySQL Connection! " + e.getMessage());
        }
    }

    public ResultSet querySQL(String query) throws SQLException {
        if (!checkConnection()) {
            openConnection();
        }
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public int updateSQL(String update) throws SQLException {
        if (!checkConnection()) {
            openConnection();
        }
        Statement statement = connection.createStatement();
        return statement.executeUpdate(update);
    }
}
