package dev.theparanker.customcurrency.plugin.listeners;

import dev.theparanker.customcurrency.CustomCurrency;
import dev.theparanker.customcurrency.application.user.UserManager;
import dev.theparanker.customcurrency.core.domain.user.UserStructure;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DataListener implements Listener {

    private final CustomCurrency plugin;
    private final UserManager userManager;

    public DataListener(CustomCurrency plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getService().getManager(UserManager.class);
    }

    @EventHandler
    public void onPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        userManager.loadUser(event.getUniqueId(), event.getName()).thenAccept(user -> {
            if (user == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cAn error occurred while loading your data. Please try again later.");
                return;
            }
            userManager.updateCache(user, false);
        }).exceptionally((e) -> {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cAn error occurred while loading your data. Please try again later.");
            return null;
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UserStructure user = userManager.getUser(event.getPlayer().getUniqueId());
        if (user != null) {
            userManager.updateCache(user, true);
            userManager.save(user).thenAccept(saved -> {
               if (!saved) {
                   plugin.getLogger().severe("Unable to save user " + user.name() + " (" + user.uuid() + ") to the database.");
               }
            });
        }
    }

}
