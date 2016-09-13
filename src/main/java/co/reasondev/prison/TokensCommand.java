package co.reasondev.prison;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TokensCommand implements CommandExecutor {

    private PrisonTokens plugin;

    public TokensCommand(PrisonTokens plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length < 1 || args[0].equalsIgnoreCase("help")) {
            return msg(sender, Settings.Messages.TOKENS_HELP);
        }
        //Balance Sub-Command
        if(args[0].equalsIgnoreCase("balance")) {
            if(args.length > 1) {
                if(!sender.isOp() && !sender.hasPermission("tokens.admin")) {
                    return err(sender, "You do not have permission to use this command!");
                }
                OfflinePlayer toCheck = Bukkit.getOfflinePlayer(args[1]);
                if(toCheck == null) {
                    return err(sender, "Error! There is no Token data for that Player!");
                }
                return msg(sender, Settings.Messages.BALANCE_OTHER, toCheck.getName(), PrisonTokens.getTokens(toCheck));
            }
            if(sender instanceof ConsoleCommandSender) {
                return err(sender, "Error! This command cannot be run from the Console!");
            }
            return msg(sender, Settings.Messages.BALANCE, PrisonTokens.getTokens((OfflinePlayer) sender));
        }
        //Claim Sub-Command
        if(args[0].equalsIgnoreCase("claim")) {
            if(sender instanceof ConsoleCommandSender) {
                return err(sender, "Error! This command cannot be run from the Console!");
            }
            long lastClaim = PrisonTokens.getLastClaim((OfflinePlayer) sender);
            if(System.currentTimeMillis() - lastClaim < 86400000) {
                return err(sender, Settings.Messages.CLAIM_COOLDOWN.val());
            }
            Player p = (Player) sender;
            PrisonTokens.setTokens(p, PrisonTokens.getTokens(p) + 10);
            PrisonTokens.setLastClaim(p);
            return msg(sender, Settings.Messages.TOKENS_CLAIMED);
        }
        //Withdraw Sub-Command
        if(args[0].equalsIgnoreCase("withdraw")) {
            if(sender instanceof ConsoleCommandSender) {
                return err(sender, "Error! This command cannot be run from the Console!");
            }
            if(args.length < 2) {
                return err(sender, "Invalid arguments! Try &6/tokens withdraw <amount>");
            }
            Player p = (Player) sender;
            int amount = 0;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                return err(sender, "Error! " + args[1] + " is not a number!");
            }
            if(PrisonTokens.getTokens(p) < amount) {
                return err(sender, Settings.Messages.WITHDRAW_FAILURE.val());
            }
            ItemStack item = new ItemStack(Material.DOUBLE_PLANT, amount);
            p.getInventory().addItem(item);
            PrisonTokens.setTokens(p, PrisonTokens.getTokens(p) - amount);
            return msg(sender, Settings.Messages.WITHDRAW_SUCCESS, amount);
        }
        //Deposit Sub-Command
        if(args[0].equalsIgnoreCase("deposit")) {
            if(sender instanceof ConsoleCommandSender) {
                return err(sender, "Error! This command cannot be run from the Console!");
            }
            if(args.length < 2) {
                return err(sender, "Invalid arguments! Try &6/tokens deposit <amount>");
            }
            Player p = (Player) sender;
            int amount = 0;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                return err(sender, "Error! " + args[1] + " is not a number!");
            }
            int iAmount = 0;
            for(ItemStack i : p.getInventory().getContents()) {
                if(i != null && i.getType() == Material.DOUBLE_PLANT) {
                    iAmount += i.getAmount();
                }
            }
            if(iAmount < amount) {
                return err(sender, Settings.Messages.DEPOSIT_FAILURE.val());
            }
            for(int i = 0; i < p.getInventory().getSize() && amount > 0; i++) {
                ItemStack item = p.getInventory().getItem(i);
                if(item == null || item.getType() != Material.DOUBLE_PLANT) {
                    continue;
                }
                if(item.getAmount() <= amount) {
                    p.getInventory().setItem(i, null);
                    amount -= item.getAmount();
                } else {
                    item.setAmount(item.getAmount() - amount);
                    break;
                }
            }
            PrisonTokens.setTokens(p, PrisonTokens.getTokens(p) + Integer.parseInt(args[1]));
            return msg(sender, Settings.Messages.DEPOSIT_SUCCESS, Integer.parseInt(args[1]));
        }
        //Shop Sub-Command
        if(args[0].equalsIgnoreCase("shop")) {
            if(sender instanceof ConsoleCommandSender) {
                return err(sender, "Error! This command cannot be run from the Console!");
            }
            Bukkit.dispatchCommand(sender, "warp tokens");
            return msg(sender, Settings.Messages.SHOP_MESSAGE);
        }
        return msg(sender, Settings.Messages.TOKENS_HELP);
    }

    private boolean msg(CommandSender sender, String message) {
        sender.sendMessage(Settings.Messages.PREFIX.val() + " " + ChatColor.translateAlternateColorCodes('&', message));
        return true;
    }

    private boolean msg(CommandSender sender, Settings.Messages message) {
        return msg(sender, message.val());
    }

    private boolean msg(CommandSender sender, Settings.Messages message, Object... args) {
        return msg(sender, String.format(message.val(), args));
    }

    private boolean err(CommandSender sender, String message) {
        return msg(sender, ChatColor.RED + message);
    }
}
