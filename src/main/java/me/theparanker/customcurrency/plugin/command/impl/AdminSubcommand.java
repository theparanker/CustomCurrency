package me.theparanker.customcurrency.plugin.command.impl;

import me.theparanker.customcurrency.CustomCurrency;
import me.theparanker.customcurrency.application.currency.CurrencyManager;
import me.theparanker.customcurrency.core.domain.currency.Currency;
import me.theparanker.customcurrency.plugin.command.strucuture.SubcommandExecutor;
import me.theparanker.utilslib.number.NumberUtil;
import me.theparanker.utilslib.string.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminSubcommand implements SubcommandExecutor {

    @Override
    public boolean execute(CustomCurrency plugin, CommandSender sender, String[] args, Currency currency) {
        if (args.length == 0) {
            sender.sendMessage(CC.translate(currency.subCommands().get("admin").usage()));
            return true;
        }

        String subcommand = args[0].toLowerCase();
        CurrencyManager currencyManager = plugin.getService().getManager(CurrencyManager.class);

        switch (subcommand) {
            case "check":
                return handleCheck(sender, args, currency, currencyManager);
            case "give":
                return handleGive(sender, args, currency, currencyManager);
            case "giveall":
                return handleGiveAll(sender, args, currency, currencyManager);
            case "remove":
                return handleRemove(sender, args, currency, currencyManager);
            case "set":
                return handleSet(sender, args, currency, currencyManager);
            default:
                return true;
        }
    }

    private boolean handleCheck(CommandSender sender, String[] args, Currency currency, CurrencyManager currencyManager) {
        if (args.length < 2) {
            sender.sendMessage(currency.getMessage("prefix") + "§cUsa: /" + currency.commandName() + " admin check <player>");
            return true;
        }

        String playerName = args[1];
        Double balance = currencyManager.getBalance(currency, playerName);

        if (balance == null) {
            sender.sendMessage(currency.getMessage("player_not_found"));
        } else {
            String message = currency.getMessage("balance_other")
                    .replace("{balance}", NumberUtil.format(balance)
            );
            sender.sendMessage(message);
        }
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args, Currency currency, CurrencyManager currencyManager) {
        if (args.length < 3) {
            sender.sendMessage(currency.getMessage("prefix") + "§cUsa: /" + currency.commandName() + " admin give <player> <amount>");
            return true;
        }

        String playerName = args[1];
        String amountStr = args[2];

        try {
            double amount = NumberUtil.deformat(amountStr);
            if (amount <= 0) {
                sender.sendMessage(currency.getMessage("invalid_number"));
                return true;
            }

            Double currentBalance = currencyManager.getBalance(currency, playerName);
            if (currentBalance == null) {
                sender.sendMessage(currency.getMessage("player_not_found"));
                return true;
            }

            currencyManager.addBalance(currency, playerName, amount);

            String message = currency.getMessage("balance_added")
                    .replace("{amount}", NumberUtil.format(amount))
                    .replace("{player}", playerName);

            if (!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(message);
            }

            Player onlinePlayer = Bukkit.getPlayer(playerName);
            if (onlinePlayer != null) {
                String receivedMessage = currency.getMessage("currency_received")
                        .replace("{amount}", NumberUtil.format(amount))
                        .replace("{player}", sender.getName());
                onlinePlayer.sendMessage(receivedMessage);
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(currency.getMessage("invalid_number"));
        }
        return true;
    }

    private boolean handleGiveAll(CommandSender sender, String[] args, Currency currency, CurrencyManager currencyManager) {
        if (args.length < 2) {
            sender.sendMessage(currency.getMessage("prefix") + "§cUsa: /" + currency.commandName() + " admin giveall <amount>");
            return true;
        }

        String amountStr = args[1];

        try {
            double amount = NumberUtil.deformat(amountStr);
            if (amount <= 0) {
                sender.sendMessage(currency.getMessage("invalid_number"));
                return true;
            }

            int playersCount = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                currencyManager.addBalance(currency, player.getName(), amount);

                String receivedMessage = currency.getMessage("currency_received")
                        .replace("{amount}", NumberUtil.format(amount))
                        .replace("{player}", sender.getName());
                player.sendMessage(receivedMessage);
                playersCount++;
            }

            String message = currency.getMessage("balance_added_all")
                    .replace("{amount}", NumberUtil.format(amount))
                    .replace("{players}", String.valueOf(playersCount));

            if (!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(message);
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(currency.getMessage("invalid_number"));
        }
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args, Currency currency, CurrencyManager currencyManager) {
        if (args.length < 3) {
            sender.sendMessage(currency.getMessage("prefix") + "§cUsa: /" + currency.commandName() + " admin remove <player> <amount>");
            return true;
        }

        String playerName = args[1];
        String amountStr = args[2];

        try {
            double amount = NumberUtil.deformat(amountStr);
            if (amount <= 0) {
                sender.sendMessage(currency.getMessage("invalid_number"));
                return true;
            }

            Double currentBalance = currencyManager.getBalance(currency, playerName);
            if (currentBalance == null) {
                sender.sendMessage(currency.getMessage("player_not_found"));
                return true;
            }

            currencyManager.removeBalance(currency, playerName, amount);

            String message = currency.getMessage("balance_taken")
                    .replace("{amount}", NumberUtil.format(amount))
                    .replace("{player}", playerName);

            if (!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(message);
            }

            Player onlinePlayer = Bukkit.getPlayer(playerName);
            if (onlinePlayer != null) {
                String takenMessage = currency.getMessage("currency_removed")
                        .replace("{amount}", NumberUtil.format(amount));
                onlinePlayer.sendMessage(takenMessage);
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(currency.getMessage("invalid_number"));
        }
        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args, Currency currency, CurrencyManager currencyManager) {
        if (args.length < 3) {
            sender.sendMessage(currency.getMessage("prefix") + "§cUsa: /" + currency.commandName() + " admin set <player> <amount>");
            return true;
        }

        String playerName = args[1];
        String amountStr = args[2];

        try {
            double amount = NumberUtil.deformat(amountStr);
            if (amount < 0) {
                sender.sendMessage(currency.getMessage("invalid_number"));
                return true;
            }

            Double currentBalance = currencyManager.getBalance(currency, playerName);
            if (currentBalance == null) {
                sender.sendMessage(currency.getMessage("player_not_found"));
                return true;
            }

            currencyManager.setBalance(currency, playerName, amount);

            String message = currency.getMessage("balance_set")
                    .replace("{amount}", NumberUtil.format(amount))
                    .replace("{player}", playerName);

            if (!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(message);
            }

            Player onlinePlayer = Bukkit.getPlayer(playerName);
            if (onlinePlayer != null) {
                String setMessage = currency.getMessage("currency_set_notification")
                        .replace("{amount}", NumberUtil.format(amount));
                onlinePlayer.sendMessage(setMessage);
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(currency.getMessage("invalid_number"));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CustomCurrency plugin, CommandSender sender, String[] args, Currency currency) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("check", "give", "giveall", "remove", "set"));
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();

            if (subcommand.equals("check") || subcommand.equals("give") ||
                    subcommand.equals("remove") || subcommand.equals("set")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (subcommand.equals("giveall")) {
                completions.addAll(Arrays.asList("100", "1k", "5k", "10k"));
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            String subcommand = args[0].toLowerCase();

            if (subcommand.equals("give") || subcommand.equals("remove") || subcommand.equals("set")) {
                completions.addAll(Arrays.asList("100", "1k", "5k", "10k"));
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}