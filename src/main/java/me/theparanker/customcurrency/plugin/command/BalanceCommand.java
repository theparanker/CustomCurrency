package me.theparanker.customcurrency.plugin.command;

import me.theparanker.customcurrency.CustomCurrency;
import me.theparanker.customcurrency.plugin.command.impl.BalanceSubcommand;
import me.theparanker.customcurrency.plugin.command.impl.PaySubcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BalanceCommand implements CommandExecutor, TabCompleter {


    private final CustomCurrency plugin;
    private BalanceSubcommand balancecommand;

    public BalanceCommand(CustomCurrency plugin) {
        this.plugin = plugin;
        this.balancecommand = new BalanceSubcommand();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        this.balancecommand.execute(plugin, sender, args, plugin.getDefaultCurrency());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return this.balancecommand.tabComplete(plugin, commandSender, strings, plugin.getDefaultCurrency());
    }
}
