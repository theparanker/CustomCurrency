package dev.theparanker.customcurrency.infra.persistance;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.theparanker.customcurrency.CustomCurrency;
import me.theparanker.managerservice.Manager;
import org.bukkit.configuration.ConfigurationSection;
import org.mariadb.jdbc.Configuration;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class MySQLManager extends Manager<CustomCurrency> {

    private HikariDataSource dataSource;
    private final String host, database, user, password;
    private final int port;

    public MySQLManager(CustomCurrency plugin) {
        super(plugin);
        ConfigurationSection mysqlSection = plugin.getStorageFile().getConfigurationSection("MySQL");
        ConfigurationSection authSection = mysqlSection.getConfigurationSection("Auth");
        this.host = mysqlSection.getString("host");
        this.port = mysqlSection.getInt("port");
        this.database = mysqlSection.getString("database");
        this.user = authSection.getString("user");
        this.password = authSection.getString("password");
    }

    @Override
    public CompletableFuture<Void> start() {
        var future = CompletableFuture.runAsync(() -> {
            HikariConfig config = new HikariConfig();
            config.setUsername(user);
            config.setPassword(password);
            try {
                config.setDataSource(new MariaDbDataSource(new Configuration.Builder()
                        .addHost(host, port)
                        .database(database)
                        .maxPoolSize(12)
                        .socketTimeout(3000)
                        .build()
                        .initialUrl()));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            this.dataSource = new HikariDataSource(config);
        });
        future.thenRun(this::testConnection).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Unable to establish a valid connection to the database:" + ex.getMessage(), ex);
            plugin.getPlugin().getPluginLoader().disablePlugin(plugin.getPlugin());
            return null;
        });
        return future;
    }

    private void testConnection() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isClosed() || !connection.isValid(2) || dataSource.isClosed() || !dataSource.isRunning()) {
                plugin.getLogger().severe("Unable to establish a valid connection to the database.");
                plugin.getPlugin().getPluginLoader().disablePlugin(plugin.getPlugin());
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Unable to establish a valid connection to the database.");
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                dataSource = null;
            }
            plugin.getPlugin().getPluginLoader().disablePlugin(plugin.getPlugin());
        }
    }

    @Override
    public CompletableFuture<Void> stop() {
        return CompletableFuture.runAsync(() -> {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        });
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Il DataSource MySQL non è disponibile o è stato chiuso");
        }
        return dataSource.getConnection();
    }

}