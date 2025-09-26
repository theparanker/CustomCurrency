package me.theparanker.customcurrency;


import me.theparanker.customcurrency.application.commands.CommandsManager;
import me.theparanker.customcurrency.application.currency.CurrencyManager;
import me.theparanker.customcurrency.application.user.UserManager;
import me.theparanker.customcurrency.core.domain.config.ConfigFile;
import me.theparanker.customcurrency.core.domain.currency.Currency;
import me.theparanker.customcurrency.infra.persistance.MySQLManager;
import me.theparanker.customcurrency.plugin.hooks.PlaceholderAPIHook;
import me.theparanker.customcurrency.plugin.hooks.VaultHook;
import me.theparanker.customcurrency.plugin.listeners.DataListener;
import lombok.Getter;
import me.theparanker.managerservice.ManagerService;
import me.theparanker.managerservice.ManagerServiceProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.mclicense.library.MCLicense;

import java.util.List;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

@Getter
public class CustomCurrency {

    private final JavaPlugin plugin;
    private final Logger logger;

    private ManagerService service;
    private ConfigFile configFile, storageFile;

    private Currency vaultCurrency;
    private Currency defaultCurrency;

    private boolean serviceStarted;

    public CustomCurrency(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void init() {
        if (!MCLicense.validateKey(this.getPlugin(), "68bc5f9eb16cbcc298119969")) {
            logger.severe("Invalid license key!");
            Bukkit.getPluginManager().disablePlugin(this.getPlugin());
            return;
        }
        registerConfig();
        registerServices();
        registerListeners();
        registerHooks();
    }

    public void shutdown() {
        if(isServiceStarted()) {
            this.service.shutdown();
        }
    }

    private void registerConfig() {
        this.configFile = new ConfigFile(this.plugin, "config");
        this.storageFile = new ConfigFile(this.plugin, "storage");
    }

    private void registerServices() {
        this.service = new ManagerService();
        this.service.init();
        new ManagerServiceProvider(this.logger, this.service, false);

        this.service.addManager(new MySQLManager(this));
        this.service.addManager(new CurrencyManager(this));
        this.service.addManager(new UserManager(this));
        this.service.addManager(new CommandsManager(this));

        this.service.startAll();
        this.serviceStarted = true;

        this.vaultCurrency = this.service.getManager(CurrencyManager.class).getCurrency(configFile.getString("vault-currency"));
        this.defaultCurrency = this.service.getManager(CurrencyManager.class).getCurrency(configFile.getString("default-currency"));
    }

    private void registerListeners() {
        List.of(
                new DataListener(this)
        ).forEach(listener -> plugin.getServer().getPluginManager().registerEvents(listener, plugin));
    }

    private void registerHooks() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
        }
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            if (this.vaultCurrency == null) return;
            getServer().getServicesManager().register(
                    Economy.class,
                    new VaultHook(this),
                    plugin,
                    ServicePriority.Highest
            );
        }
    }

}
