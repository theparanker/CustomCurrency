package dev.theparanker.customcurrency.plugin.command.strucutre;

import dev.theparanker.customcurrency.CustomCurrency;
import dev.theparanker.customcurrency.core.domain.currency.Currency;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubcommandExecutor {

    boolean execute(CustomCurrency plugin, CommandSender sender, String[] args, Currency currency);

    List<String> tabComplete(CustomCurrency plugin, CommandSender sender, String[] args, Currency currency);
}
