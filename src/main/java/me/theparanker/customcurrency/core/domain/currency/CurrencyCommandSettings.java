package me.theparanker.customcurrency.core.domain.currency;

import me.theparanker.customcurrency.plugin.command.impl.AdminSubcommand;
import me.theparanker.customcurrency.plugin.command.impl.BalanceSubcommand;
import me.theparanker.customcurrency.plugin.command.impl.PaySubcommand;
import me.theparanker.customcurrency.plugin.command.strucutre.SubcommandExecutor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public record CurrencyCommandSettings(
        String command,
        String permission,
        String usage,
        String description,
        Map<String, Boolean> settings,
        SubcommandExecutor executor
) {
    public static Map<String, CurrencyCommandSettings> fromConfig(ConfigurationSection section, String commandName) {
        Map<String, CurrencyCommandSettings> commands = new HashMap<>();

        section.getKeys(false).forEach(key -> {
            if (!section.getBoolean(key + ".enabled")) return;

            Map<String, Boolean> settings = new HashMap<>();
            ConfigurationSection settingsSection = section.getConfigurationSection(key + ".settings");

            if (settingsSection != null) {
                settingsSection.getKeys(false).forEach(settingKey -> {
                    settings.put(settingKey, settingsSection.getBoolean(settingKey));
                });
            }

            String usage = section.getString(key + ".usage");
            if (usage != null) {
                usage = usage.replace("{command}", commandName);
            } else {
                usage = "";
            }

            CurrencyCommandSettings commandSettings = new CurrencyCommandSettings(
                    key,
                    section.getString(key + ".permission"),
                    usage,
                    section.getString(key + ".description"),
                    settings,
                    getExecutor(key)
            );
            commands.put(key, commandSettings);
        });
        return commands;
    }

    private static SubcommandExecutor getExecutor(String command) {
        return switch (command) {
            case "pay" -> new PaySubcommand();
            case "balance" -> new BalanceSubcommand();
            case "admin" -> new AdminSubcommand();
            default -> null;
        };
    }
}