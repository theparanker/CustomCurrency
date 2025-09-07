package me.theparanker.customcurrency.plugin.command.impl;

import me.theparanker.customcurrency.CustomCurrency;
import me.theparanker.customcurrency.application.currency.CurrencyManager;
import me.theparanker.customcurrency.core.domain.currency.Currency;
import me.theparanker.customcurrency.plugin.command.strucutre.SubcommandExecutor;
import me.theparanker.utilslib.number.NumberUtil;
import me.theparanker.utilslib.string.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PaySubcommand implements SubcommandExecutor {

    @Override
    public boolean execute(CustomCurrency plugin, CommandSender sender, String[] args, Currency currency) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(CC.translate(currency.subCommands().get("pay").usage()));
            return true;
        }
        CurrencyManager currencyManager = plugin.getService().getManager(CurrencyManager.class);
        String targetName = args[0];
        if (targetName.equalsIgnoreCase(sender.getName())) {
            sender.sendMessage(currency.getMessage("not_pay_self"));
            return true;
        }
        double amount = NumberUtil.deformat(args[1]);
        if (amount <= 0) {
            sender.sendMessage(currency.getMessage("invalid_number"));
            return true;
        }
        double senderBalance = currencyManager.getBalance(currency, sender.getName());
        if (senderBalance < amount) {
            sender.sendMessage(currency.getMessage("insufficient_funds"));
            return true;
        }

        currencyManager.transfer(currency, sender.getName(), targetName, amount).thenAccept(success -> {
            if (success) {
                sender.sendMessage(currency.getMessage("balance_added")
                        .replace("{amount}", NumberUtil.format(amount))
                        .replace("{player}", targetName)
                );

                Player target = Bukkit.getPlayer(targetName);
                if (target != null) {
                    target.sendMessage(currency.getMessage("currency_received")
                            .replace("{amount}", NumberUtil.format(amount))
                            .replace("{player}", sender.getName())
                    );
                }
            } else {
                sender.sendMessage(currency.getMessage("player_not_found"));
            }
        });
        return false;
    }

    @Override
    public List<String> tabComplete(CustomCurrency plugin, CommandSender sender, String[] args, Currency currency) {
        if (args.length == 1) {
            String senderName = sender instanceof Player ? sender.getName() : "";
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> !name.equalsIgnoreCase(senderName))
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return List.of("100", "1k", "5k", "10k", "100k");
        }
        return List.of();
    }

}
