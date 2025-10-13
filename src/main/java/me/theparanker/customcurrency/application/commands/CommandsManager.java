package me.theparanker.customcurrency.application.commands;

import me.theparanker.customcurrency.CustomCurrency;
import me.theparanker.customcurrency.application.currency.CurrencyManager;
import me.theparanker.customcurrency.core.domain.currency.Currency;
import me.theparanker.customcurrency.plugin.command.BalanceCommand;
import me.theparanker.customcurrency.plugin.command.BaseCommand;
import me.theparanker.customcurrency.plugin.command.PayCommand;
import me.theparanker.managerservice.Manager;
import me.theparanker.managerservice.ManagerSync;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandsManager extends ManagerSync<CustomCurrency> {

    public CommandsManager(CustomCurrency plugin) {
        super(plugin);
    }

    private Currency defaultCurrency;

    @Override
    public void start() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            getManager(CurrencyManager.class).getCurrencies().forEach((id, currency) -> {
                Command dynamicCommand = new Command(currency.commandName()) {
                    @Override
                    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                        BaseCommand baseCommand = new BaseCommand(plugin);
                        return baseCommand.onCommand(sender, this, commandLabel, args);
                    }

                    @Override
                    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                        BaseCommand baseCommand = new BaseCommand(plugin);
                        return baseCommand.onTabComplete(sender, this, alias, args);
                    }
                };
                debug("Added command: " + plugin.getPlugin().getName().toLowerCase());
                commandMap.register(plugin.getPlugin().getName().toLowerCase(), dynamicCommand);
            });

            if (plugin.getDefaultCurrency() != null) {
                Command payCommand = new Command("pay") {
                    @Override
                    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
                        return new PayCommand(plugin).onCommand(commandSender, this, s, strings);
                    }

                    @Override
                    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                        return new PayCommand(plugin).onTabComplete(sender, this, alias, args);
                    }
                };
                commandMap.register(plugin.getPlugin().getName().toLowerCase(), payCommand);

                Command balanceCommand = new Command("balance") {
                    @Override
                    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
                        return new BalanceCommand(plugin).onCommand(commandSender, this, s, strings);
                    }

                    @Override
                    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                        return new BalanceCommand(plugin).onTabComplete(sender, this, alias, args);
                    }
                };
                commandMap.register(plugin.getPlugin().getName().toLowerCase(), balanceCommand);
            }

            commandMapField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to register dynamic commands", e);
        }
    }
}