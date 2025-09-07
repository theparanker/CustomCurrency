package me.theparanker.customcurrency.application.currency;

import me.theparanker.customcurrency.CustomCurrency;
import me.theparanker.customcurrency.core.domain.config.ConfigFile;
import me.theparanker.customcurrency.core.domain.currency.Currency;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class CurrencyRegistryManager {

    private final CustomCurrency plugin;
    private final CurrencyManager currencyManager;

    public CurrencyRegistryManager(CustomCurrency plugin, CurrencyManager currencyManager) {
        this.plugin = plugin;
        this.currencyManager = currencyManager;
    }

    public CompletableFuture<Void> loadCurrencies() {
        return CompletableFuture.runAsync(() -> {
            File currenciesFolder = new File(plugin.getPlugin().getDataFolder(), "currencies");

            if (!currenciesFolder.exists()) {
                currenciesFolder.mkdirs();
            }

            File[] ymlFiles = currenciesFolder.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".yml"));

            if (ymlFiles == null || ymlFiles.length == 0) {
                new ConfigFile(plugin.getPlugin(), currenciesFolder, "currencies", "money", true);

                ymlFiles = currenciesFolder.listFiles((dir, name) ->
                        name.toLowerCase().endsWith(".yml"));
            }

            if (ymlFiles != null) {
                for (File file : ymlFiles) {
                    try {
                        String fileName = file.getName().replace(".yml", "");
                        ConfigFile currencyFile = new ConfigFile(
                                plugin.getPlugin(),
                                currenciesFolder,
                                "currencies",
                                fileName,
                                true
                        );

                        if (currencyFile.exists() && !currencyFile.getKeys(false).isEmpty()) {
                            Currency currency = Currency.fromConfig(currencyFile);
                            if (currency != null) {
                                this.currencyManager.getCurrencies().put(currency.id(), currency);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}