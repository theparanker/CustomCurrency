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

            getPlugin().getPlugin().getCommand("pay").setExecutor(new PayCommand(plugin));
            getPlugin().getPlugin().getCommand("balance").setExecutor(new BalanceCommand(plugin));

            commandMapField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to register dynamic commands", e);
        }
    }
}