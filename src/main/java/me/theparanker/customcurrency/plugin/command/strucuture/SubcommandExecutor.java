package me.theparanker.customcurrency.plugin.command.strucuture;

import me.theparanker.customcurrency.CustomCurrency;
import me.theparanker.customcurrency.core.domain.currency.Currency;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubcommandExecutor {

    boolean execute(CustomCurrency plugin, CommandSender sender, String[] args, Currency currency);

    List<String> tabComplete(CustomCurrency plugin, CommandSender sender, String[] args, Currency currency);
}
