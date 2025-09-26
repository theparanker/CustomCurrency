package me.theparanker.customcurrency.application.currency;

import me.theparanker.customcurrency.CustomCurrency;
import me.theparanker.customcurrency.application.user.UserManager;
import me.theparanker.customcurrency.core.domain.currency.Currency;
import me.theparanker.customcurrency.core.domain.user.UserStructure;
import lombok.Getter;
import me.theparanker.managerservice.Manager;
import me.theparanker.managerservice.ManagerAsync;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class CurrencyManager extends ManagerAsync<CustomCurrency> {

    @Getter
    private HashMap<String, Currency> currencies;

    public CurrencyManager(CustomCurrency plugin) {
        super(plugin);
        this.currencies = new HashMap<>();
    }

    @Override
    public CompletableFuture<Void> start() {
        CurrencyRegistryManager currencyRegistry = new CurrencyRegistryManager(plugin, this);
        return currencyRegistry.loadCurrencies()
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    throw new RuntimeException("Failed to load currencies", ex);
                });
    }

    @Override
    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            if (currencies != null) {
                currencies.clear();
            }
        });
    }

    public void setBalance(Currency currency, String name, double amount) {
        Player player = Bukkit.getPlayer(name);
        if (player != null) {
            UserStructure user = getManager(UserManager.class).getUser(player.getUniqueId());
            user.setBalance(currency, amount, getManager(UserManager.class));
        }
        getManager(UserManager.class).setBalanceOfflineByName(currency, name, amount);
    }

    public Double getBalance(Currency currency, String name) {
        Player player = Bukkit.getPlayer(name);
        if (player != null) {
            UserStructure user = getManager(UserManager.class).getUser(player.getUniqueId());
            return user.getBalance(currency);
        }else {
            return getManager(UserManager.class).getOfflineBalance(currency, name).join();
        }
    }

    public void addBalance(Currency currency, String name, double amount) {
        setBalance(currency, name, getBalance(currency, name) + amount);
    }

    public void removeBalance(Currency currency, String name, double amount) {
        addBalance(currency, name, -amount);
    }

    public Currency getCurrency(String id) {
        if (id == null || id.isBlank()) return null;
        return currencies.getOrDefault(id, null);
    }

    public CompletableFuture<Boolean> transfer(Currency currency, String fromName, String toName, double amount) {
        return CompletableFuture.supplyAsync(() -> getBalance(currency, fromName))
                .thenCompose(fromBalance -> {
                    return CompletableFuture.supplyAsync(() -> {
                        if (getBalance(currency, toName) == null) return false;
                        addBalance(currency, toName, amount);
                        removeBalance(currency, fromName, amount);
                        return true;
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return false;
                });
    }

}