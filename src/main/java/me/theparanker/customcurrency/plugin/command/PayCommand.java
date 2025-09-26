package me.theparanker.customcurrency.plugin.command;

import me.theparanker.customcurrency.CustomCurrency;
import me.theparanker.customcurrency.plugin.command.impl.PaySubcommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PayCommand implements CommandExecutor, TabCompleter {


    private final CustomCurrency plugin;
    private PaySubcommand paycommand;

    public PayCommand(CustomCurrency plugin) {
        this.plugin = plugin;
        this.paycommand = new PaySubcommand();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        this.paycommand.execute(plugin, sender, args, plugin.getDefaultCurrency());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return this.paycommand.tabComplete(plugin, commandSender, strings, plugin.getDefaultCurrency());
    }
}
