package co.reasondev.prison;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemRename(InventoryClickEvent e) {
        if (e.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }
        if (e.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }
        if (e.getCurrentItem().getType() != Material.DOUBLE_PLANT) {
            return;
        }
        if (e.getWhoClicked().isOp()) {
            return;
        }
        e.setCancelled(true);
        e.getWhoClicked().sendMessage(ChatColor.RED + "You cannot rename Sun Flowers!");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTokenDeposit(InventoryClickEvent e) {
        if (!e.getInventory().getTitle().equals(Settings.General.DEPOSIT_TITLE.toString())) {
            return;
        }
        if (e.getCurrentItem().getType() != Material.AIR && !PrisonTokens.isTokenItem(e.getCurrentItem())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getInventory().getSize() != 9) {
            return;
        }
        if (!e.getInventory().getTitle().equals(Settings.General.DEPOSIT_TITLE.toString())) {
            return;
        }
        int amount = 0;
        for (ItemStack i : e.getInventory().getContents()) {
            if (PrisonTokens.isTokenItem(i)) {
                amount += i.getAmount();
            }
        }
        PrisonTokens.setTokens((OfflinePlayer) e.getPlayer(), PrisonTokens.getTokens((OfflinePlayer) e.getPlayer()) + amount);
        e.getPlayer().sendMessage(Settings.Messages.PREFIX.val() + " " + String.format(Settings.Messages.DEPOSIT_SUCCESS.val(), amount));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!(e.getClickedBlock().getState() instanceof Sign)) {
            return;
        }
        Sign sign = (Sign) e.getClickedBlock().getState();
        if (sign.getLine(1).equals(Settings.General.DEPOSIT_SIGN.toString())) {
            Inventory gui = Bukkit.createInventory(e.getPlayer(), 9, Settings.General.DEPOSIT_TITLE.toString());
            e.getPlayer().openInventory(gui);
        } else if (sign.getLine(1).equals(Settings.General.WITHDRAW_SIGN.toString())) {
            String s = ChatColor.stripColor(sign.getLine(2));
            int amount = Integer.parseInt(s.split(" Tokens")[0]);
            if (PrisonTokens.getTokens(e.getPlayer()) < amount) {
                e.getPlayer().sendMessage(Settings.Messages.PREFIX.val() + " " + Settings.Messages.WITHDRAW_FAILURE.val());
            } else {
                PrisonTokens.setTokens(e.getPlayer(), PrisonTokens.getTokens(e.getPlayer()) - amount);
                PrisonTokens.giveTokenItems(e.getPlayer(), amount);
                e.getPlayer().sendMessage(Settings.Messages.PREFIX.val() + " " + String.format(Settings.Messages.WITHDRAW_SUCCESS.val(), amount));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent e) {
        if (e.getLine(0).equalsIgnoreCase("[Deposit]")) {
            if (!e.getPlayer().isOp() && !e.getPlayer().hasPermission("tokens.admin")) {
                e.getBlock().breakNaturally();
                e.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to place this Sign!");
            } else {
                e.setLine(0, "");
                e.setLine(1, Settings.General.DEPOSIT_SIGN.toString());
            }
        } else if (e.getLine(0).equalsIgnoreCase("[Withdraw]")) {
            if (!e.getPlayer().isOp() && !e.getPlayer().hasPermission("tokens.admin")) {
                e.getBlock().breakNaturally();
                e.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to place this Sign!");
            } else {
                try {
                    int amount = Integer.parseInt(e.getLine(1));
                    e.setLine(0, "");
                    e.setLine(1, Settings.General.WITHDRAW_SIGN.toString());
                    e.setLine(2, ChatColor.GOLD + "" + amount + " Tokens");
                } catch (Exception exception) {
                    e.getBlock().breakNaturally();
                    e.getPlayer().sendMessage(ChatColor.RED + "You did not enter a valid Token amount!");
                }
            }
        }
    }
}
