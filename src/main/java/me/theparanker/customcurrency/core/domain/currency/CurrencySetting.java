package me.theparanker.customcurrency.core.domain.currency;

import me.theparanker.customcurrency.core.domain.config.ConfigFile;

public record CurrencySetting(
        double startingBalance,
        boolean canBeNegative,
        boolean playerCanPay,
        boolean playerCanSeesOtherBalances
) {

    public static CurrencySetting fromConfig(ConfigFile config) {
        double startingBalance = config.getDouble("starting-balance", 0.0);
        boolean canBeNegative = config.getBoolean("can-be-negative", false);
        boolean playerCanPay = config.getBoolean("player-can-pay-each-other", true);
        boolean playerCanSeesOtherBalances = config.getBoolean("player-can-check-others-balance", false);
        return new CurrencySetting(startingBalance, canBeNegative, playerCanPay, playerCanSeesOtherBalances);
    }

}
