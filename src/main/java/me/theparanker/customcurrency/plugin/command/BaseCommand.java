package me.theparanker.customcurrency.plugin.command;

import me.theparanker.customcurrency.CustomCurrency;
import me.theparanker.customcurrency.application.currency.CurrencyManager;
import me.theparanker.customcurrency.core.domain.currency.Currency;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BaseCommand implements CommandExecutor, TabCompleter {

    private final CustomCurrency plugin;
    private final CurrencyManager currencyManager;

    public BaseCommand(CustomCurrency plugin) {
        this.plugin = plugin;
        this.currencyManager = plugin.getService().getManager(CurrencyManager.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Currency currency = currencyManager.getCurrencies().values()
                .stream()
                .filter(c -> c.commandName().equalsIgnoreCase(cmd.getName()))
                .findFirst()
                .orElse(null);

        if (currency != null) {
            if (args.length == 0) {
                sender.sendMessage("§cInvalid command usage.");
                return true;
            }
            var subCommands = currency.subCommands();
            if (subCommands.containsKey(args[0])) {
                boolean permission = sender.hasPermission(subCommands.get(args[0]).permission()) || subCommands.get(args[0]).permission() == "";
                if (permission) {
                    subCommands.get(args[0]).executor().execute(plugin, sender, Arrays.copyOfRange(args, 1, args.length), currency);
                } else {
                    sender.sendMessage(currency.getMessage("no-permission"));
                }
            } else {
                sender.sendMessage("§cThis command does not exist.");
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        Currency currency = currencyManager.getCurrencies().values()
                .stream()
                .filter(c -> c.commandName().equalsIgnoreCase(cmd.getName()))
                .findFirst()
                .orElse(null);
        if (currency != null) {
            if (args.length == 1) {
                return currency.subCommands().values().stream()
                        .filter(c -> sender.hasPermission(c.permission()))
                        .map(c -> c.command())
                        .collect(Collectors.toList());
            }else {
                var subCommands = currency.subCommands();
                if (subCommands.containsKey(args[0])) {
                    return subCommands.get(args[0]).executor().tabComplete(plugin, sender, Arrays.copyOfRange(args, 1, args.length), currency);
                }
            }
        }
        return List.of();
    }

}
