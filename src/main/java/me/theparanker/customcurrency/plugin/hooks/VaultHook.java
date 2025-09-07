package me.theparanker.customcurrency.plugin.hooks;

import me.theparanker.customcurrency.CustomCurrency;
import me.theparanker.customcurrency.application.currency.CurrencyManager;
import me.theparanker.customcurrency.core.domain.currency.Currency;
import me.theparanker.utilslib.number.NumberUtil;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class VaultHook implements Economy {

    private final CustomCurrency plugin;
    private final Currency currency;
    private final CurrencyManager currencyManager;

    public VaultHook(CustomCurrency plugin) {
        this.plugin = plugin;
        this.currency = this.plugin.getVaultCurrency();
        this.currencyManager = this.plugin.getService().getManager(CurrencyManager.class);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return currency.id();
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double number) {
        return NumberUtil.format(number) + currency.symbol();
    }

    @Override
    public String currencyNamePlural() {
        return currency.displayName();
    }

    @Override
    public String currencyNameSingular() {
        return currency.displayName();
    }

    @Override
    public boolean hasAccount(String playerMame) {
        return true;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return hasAccount(player.getName());
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player.getName(), worldName);
    }

    @Override
    public double getBalance(String playerName) {
        return currencyManager.getBalance(currency, playerName) == null ? 0 : currencyManager.getBalance(currency, playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return getBalance(player.getName());
    }

    @Override
    public double getBalance(String playerName, String worldName) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String worldName) {
        return getBalance(player.getName());
    }

    @Override
    public boolean has(String playerName, double amount) {
        if (currency.setting().canBeNegative()) {
            return true;
        }
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return has(player.getName(), amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        if (currency.setting().canBeNegative()) {
            return true;
        }
        return getBalance(playerName, worldName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player.getName(), worldName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amounts");
        }

        if (amount == 0) {
            return new EconomyResponse(0, getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, null);
        }
        double currentBalance = getBalance(playerName);

        if (!currency.setting().canBeNegative() && !has(playerName, amount)) {
            return new EconomyResponse(0, currentBalance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }

        try {
            currencyManager.removeBalance(currency, playerName, amount);

            return new EconomyResponse(amount, currentBalance - amount, EconomyResponse.ResponseType.SUCCESS, null);

        } catch (Exception e) {
            return new EconomyResponse(0, currentBalance, EconomyResponse.ResponseType.FAILURE, "Error processing withdrawal: " + e.getMessage());
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return withdrawPlayer(player.getName(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player.getName(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amounts");
        }

        if (amount == 0) {
            return new EconomyResponse(0, getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, null);
        }
        double currentBalance = getBalance(playerName);

        try {
            currencyManager.addBalance(currency, playerName, amount);

            return new EconomyResponse(amount, currentBalance + amount, EconomyResponse.ResponseType.SUCCESS, null);

        } catch (Exception e) {
            return new EconomyResponse(0, currentBalance, EconomyResponse.ResponseType.FAILURE, "Error processing deposit: " + e.getMessage());
        }
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return depositPlayer(player.getName(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player.getName(), amount);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "This feature is not implemented");
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "This feature is not implemented");
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "This feature is not implemented");
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "This feature is not implemented");
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "This feature is not implemented");
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "This feature is not implemented");
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "This feature is not implemented");
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "This feature is not implemented");
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "This feature is not implemented");
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "This feature is not implemented");
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "This feature is not implemented");
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return true;
    }
}
