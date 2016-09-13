package co.reasondev.prison;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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
            return msg(sender, "&7Try &6/tokens &7plus one of the following: " +
                    "\n&6balance " + (sender.isOp() || sender.hasPermission("tokens.admin") ? "<player> " : "") + "- &7get your current Tokens" +
                    "\n&6claim - &7claim your daily Token allowance every 24 hours" +
                    "\n&6deposit <amount> - &7deposit your physical Tokens" +
                    "\n&6withdraw <amount> - &7withdraw Tokens to physical form" +
                    "\n&6shop - &7warp to Tokens shop");
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
                return msg(sender, "&6" + toCheck.getName() + "'s Balance: &7" + PrisonTokens.getTokens(toCheck));
            }
            if(sender instanceof ConsoleCommandSender) {
                return err(sender, "Error! This command cannot be run from the Console!");
            }
            return msg(sender, "&6Tokens Balance: &7" + PrisonTokens.getTokens((OfflinePlayer) sender));
        }
        //Claim Sub-Command
        if(args[0].equalsIgnoreCase("claim")) {
            if(sender instanceof ConsoleCommandSender) {
                return err(sender, "Error! This command cannot be run from the Console!");
            }
            long lastClaim = PrisonTokens.getLastClaim((OfflinePlayer) sender);
            if(System.currentTimeMillis() - lastClaim < 86400000) {
                return err(sender, "Error! You must wait 24 hours in between Token claims!");
            }
            Player p = (Player) sender;
            PrisonTokens.setTokens(p, PrisonTokens.getTokens(p) + 10);
            PrisonTokens.setLastClaim(p);
            return msg(sender, "&aYou have successfully claimed your &610 Tokens");
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
                return err(sender, "Error! You do not have that many Tokens to withdraw!");
            }
            ItemStack item = new ItemStack(Material.DOUBLE_PLANT, amount);
            p.getInventory().addItem(item);
            PrisonTokens.setTokens(p, PrisonTokens.getTokens(p) - amount);
            return msg(sender, "&aSuccessfully withdrew &6" + amount + " Tokens");
        }
        //Deposit Sub-Command
        if(args[0].equalsIgnoreCase("deposit")) {
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
            int iAmount = 0;
            for(ItemStack i : p.getInventory().getContents()) {
                if(i != null && i.getType() == Material.DOUBLE_PLANT) {
                    iAmount += i.getAmount();
                }
            }
            if(iAmount < amount) {
                return err(sender, "Error! You do not have that many Tokens in your inventory!");
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
            return msg(sender, "&aSuccessfully deposited &6" + args[1] + " Tokens");
        }
        //Shop Sub-Command
        if(args[0].equalsIgnoreCase("shop")) {
            if(sender instanceof ConsoleCommandSender) {
                return err(sender, "Error! This command cannot be run from the Console!");
            }
            Bukkit.dispatchCommand(sender, "warp tokens");
            return msg(sender, "&aWarping to Token Shop...");
        }
        return msg(sender, "&7Try &6/tokens &7plus one of the following: " +
                "\n&6balance " + (sender.isOp() || sender.hasPermission("tokens.admin") ? "<player> " : "") + "- &7get your current Tokens" +
                "\n&6claim - &7claim your daily Token allowance every 24 hours" +
                "\n&6deposit <amount> - &7deposit your physical item Tokens" +
                "\n&6withdraw <amount> - &7withdraw Tokens to item form" +
                "\n&6shop - &7warp to Tokens shop");
    }

    private boolean msg(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        return true;
    }

    private boolean err(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.RED + message));
        return true;
    }
}
