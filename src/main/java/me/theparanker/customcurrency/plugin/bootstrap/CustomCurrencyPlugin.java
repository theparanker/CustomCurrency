package me.theparanker.customcurrency.plugin.bootstrap;

import me.theparanker.customcurrency.CustomCurrency;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomCurrencyPlugin extends JavaPlugin {

    private CustomCurrency service;

    @Override
    public void onEnable() {
        this.service = new CustomCurrency(this);
        this.service.init();
    }

    @Override
    public void onDisable() {
        this.service.shutdown();
    }
}
