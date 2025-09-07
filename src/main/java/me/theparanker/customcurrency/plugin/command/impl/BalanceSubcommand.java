package me.theparanker.customcurrency.plugin.command.impl;

import me.theparanker.customcurrency.CustomCurrency;
import me.theparanker.customcurrency.application.currency.CurrencyManager;
import me.theparanker.customcurrency.core.domain.currency.Currency;
import me.theparanker.customcurrency.plugin.command.strucuture.SubcommandExecutor;
import me.theparanker.utilslib.number.NumberUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class BalanceSubcommand implements SubcommandExecutor {

    @Override
    public boolean execute(CustomCurrency plugin, CommandSender sender, String[] args, Currency currency) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }
        Player player = (Player) sender;
        CurrencyManager currencyManager = plugin.getService().getManager(CurrencyManager.class);

        if (args.length == 0) {
            Double balance = currencyManager.getBalance(currency, player.getName());
            player.sendMessage(currency.getMessage("balance")
                    .replace("{balance}", NumberUtil.format(balance))
            );
        } else if (args.length == 1) {
            if (!currency.setting().playerCanSeesOtherBalances()) {
                sender.sendMessage(currency.getMessage("player-cant-see-other-balances"));
                return true;
            };
            String targetName = args[0];
            Double balance = currencyManager.getBalance(currency, targetName);
            if (balance == null) {
                player.sendMessage(currency.getMessage("player_not_found"));
                return true;
            }
            player.sendMessage(currency.getMessage("balance_other")
                    .replace("{balance}", NumberUtil.format(balance))
            );
        } else {
            sender.sendMessage(currency.getMessage("too-many-arguments"));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CustomCurrency plugin, CommandSender sender, String[] args, Currency currency) {
        String senderName = sender instanceof Player ? sender.getName() : "";
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> !name.equalsIgnoreCase(senderName))
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }

}
