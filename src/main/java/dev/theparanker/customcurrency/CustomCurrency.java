package dev.theparanker.customcurrency;


import dev.theparanker.customcurrency.application.commands.CommandsManager;
import dev.theparanker.customcurrency.application.currency.CurrencyManager;
import dev.theparanker.customcurrency.application.user.UserManager;
import dev.theparanker.customcurrency.core.domain.config.ConfigFile;
import dev.theparanker.customcurrency.infra.persistance.MySQLManager;
import dev.theparanker.customcurrency.plugin.hooks.PlaceholderAPIHook;
import dev.theparanker.customcurrency.plugin.listeners.DataListener;
import lombok.Getter;
import me.theparanker.managerservice.ManagerService;
import me.theparanker.managerservice.ManagerServiceProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

@Getter
public class CustomCurrency {

    private final JavaPlugin plugin;
    private final Logger logger;

    private ManagerService service;
    private ConfigFile configFile, storageFile;

    public CustomCurrency(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void init() {
        registerConfig();
        registerServices();
        registerListeners();
        registerHooks();
    }

    public void shutdown() {
        this.service.shutdown();
    }

    private void registerConfig() {
        this.configFile = new ConfigFile(this.plugin, "config");
        this.storageFile = new ConfigFile(this.plugin, "storage");
    }

    private void registerServices() {
        this.service = new ManagerService();
        this.service.init();
        new ManagerServiceProvider(this.service);

        this.service.addManager(new MySQLManager(this));
        this.service.addManager(new CurrencyManager(this));
        this.service.addManager(new UserManager(this));
        this.service.addManager(new CommandsManager(this));

        this.service.startAll();
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
    }

}
