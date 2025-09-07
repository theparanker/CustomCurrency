package me.theparanker.customcurrency.core.domain.currency;

import me.theparanker.customcurrency.core.domain.config.ConfigFile;
import me.theparanker.utilslib.string.CC;

import java.util.HashMap;
import java.util.Map;

public record Currency(
        String id,
        String displayName,
        String symbol,
        String commandName,
        CurrencySetting setting,
        Map<String, CurrencyCommandSettings> subCommands,
        Map<String, String> messages
) {
    public static Currency fromConfig(ConfigFile config) {
        Map<String, String> messages = new HashMap<>();
        var messagesSection = config.getConfigurationSection("messages");

        for (String key : messagesSection.getKeys(false)) {
            messages.put(key,
                    CC.translate(messagesSection.getString(key)
                            .replace("{currency}", config.getString("displayName"))
                            .replace("{symbol}", config.getString("symbol"))
                            .replace("{prefix}", messagesSection.getString("prefix"))
                    )
            );
        }
        return new Currency(
                config.getString("name"),
                config.getString("displayName"),
                config.getString("symbol"),
                config.getString("commands.command"),
                CurrencySetting.fromConfig(config),
                CurrencyCommandSettings.fromConfig(config.getConfigurationSection("commands"), config.getString("commands.command")),
                messages
        );
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "Message not found");
    }
}
