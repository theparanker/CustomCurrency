package dev.theparanker.customcurrency.plugin.hooks;

import dev.theparanker.customcurrency.CustomCurrency;
import dev.theparanker.customcurrency.application.currency.CurrencyManager;
import dev.theparanker.customcurrency.application.user.UserManager;
import dev.theparanker.customcurrency.core.domain.currency.Currency;
import dev.theparanker.customcurrency.core.domain.user.UserStructure;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.theparanker.utilslib.number.NumberUtil;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final CustomCurrency plugin;

    public PlaceholderAPIHook(CustomCurrency plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "customcurrency";
    }

    @Override
    public @NotNull String getAuthor() {
        return "_theparanker_";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) return "";

        UserStructure user = plugin.getService().getManager(UserManager.class)
                .getUser(offlinePlayer.getUniqueId());
        String[] args = params.split("_");

        Currency currency = plugin.getService().getManager(CurrencyManager.class).getCurrency(args[0]);
        if (currency == null) return "";

        switch (args[1]) {
            case "balance":
                return String.valueOf(user.getBalance(currency));
            case "balanceFormatted":
                return NumberUtil.format(user.getBalance(currency));
        }

        return "";
    }
}

