# Global settings - MOLTO conservativo
-dontwarn **
-ignorewarnings
-dontpreverify
-dontoptimize

# Keep your main plugin class COMPLETAMENTE
-keep public class bootstrap.plugin.me.theparanker.customcurrency.CustomCurrencyPlugin {
    *;
}

# Keep ALL JavaPlugin extensions
-keep class * extends org.bukkit.plugin.java.JavaPlugin {
    *;
}

# Keep ALL listeners
-keep class * implements org.bukkit.event.Listener {
    *;
}

# Keep ALL command executors
-keep class * implements org.bukkit.command.CommandExecutor {
    *;
}
-keep class * implements org.bukkit.command.TabCompleter {
    *;
}

# Keep ALL external dependencies
-keep class org.json.** { *; }
-keep class org.mariadb.jdbc.** { *; }
-keep class com.zaxxer.hikari.** { *; }
-keep class net.kyori.** { *; }
-keep class com.github.theparanker.** { *; }
-keep class org.bukkit.** { *; }
-keep class net.md_5.bungee.** { *; }
-keep class com.mojang.** { *; }
-keep class net.minecraft.** { *; }
-keep class org.spigotmc.** { *; }
-keep class io.papermc.** { *; }
-keep class me.clip.placeholderapi.** { *; }
-keep class net.milkbowl.vault.** { *; }

# Keep ALL attributes
-keepattributes *

# Ma permetti di rinominare le classi (non i membri)
-keepclassmembernames class dev.theparanker.customcurrency.** {
    *;
}

# Offuscamento MOLTO conservativo
-optimizationpasses 0
-dontobfuscate

# SOLO rinomina le tue classi, non i membri
-keep,allowobfuscation class dev.theparanker.customcurrency.** {
    *;
}