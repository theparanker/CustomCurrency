package me.theparanker.customcurrency.core.domain.user;


import me.theparanker.customcurrency.application.user.UserManager;
import me.theparanker.customcurrency.core.domain.currency.Currency;
import lombok.With;

import java.util.Map;
import java.util.UUID;

@With
public record UserStructure(
        UUID uuid,
        String name,
        Map<Currency, Double> balances
) {
    public void setBalance(Currency currency, double amount, UserManager userManager) {
        balances.put(currency, amount);
        userManager.updateCache(this.withBalances(balances), false);
    }

    public Double getBalance(Currency currency) {
        return balances.getOrDefault(currency, 0.0);
    }
}
