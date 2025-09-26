package me.theparanker.customcurrency.application.user;

import me.theparanker.customcurrency.CustomCurrency;
import me.theparanker.customcurrency.application.currency.CurrencyManager;
import me.theparanker.customcurrency.core.domain.currency.Currency;
import me.theparanker.customcurrency.core.domain.user.UserStructure;
import me.theparanker.customcurrency.infra.persistance.MySQLManager;
import lombok.Getter;
import me.theparanker.managerservice.Manager;
import me.theparanker.managerservice.ManagerAsync;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

@Getter
public class UserManager extends ManagerAsync<CustomCurrency> {

    private final Map<UUID, UserStructure> users = new ConcurrentHashMap<>();
    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();

    public UserManager(CustomCurrency plugin) {
        super(plugin);
    }

    @Override
    public CompletableFuture<Void> start() {
        return createTable()
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    throw new RuntimeException("Failed to create tables", ex);
                });
    }

    @Override
    public CompletableFuture<Void> stop() {
        List<CompletableFuture<Boolean>> saveFutures = new ArrayList<>();

        for (UserStructure user : users.values()) {
            saveFutures.add(save(user));
        }

        return CompletableFuture.allOf(saveFutures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    users.clear();
                });
    }

    private CompletableFuture<Void> createTable() {
        return CompletableFuture.runAsync(() -> {
            getManager(CurrencyManager.class).getCurrencies().keySet().forEach(tableName -> {
                String sql = String.format("""
                    CREATE TABLE IF NOT EXISTS `%s` (
                        uuid VARCHAR(36) PRIMARY KEY,
                        name VARCHAR(32),
                        value DOUBLE DEFAULT 0.0
                    );
                """, tableName);

                try (Connection conn = getManager(MySQLManager.class).getConnection();
                     Statement st = conn.createStatement()) {
                    st.executeUpdate(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public CompletableFuture<UserStructure> loadUser(UUID uuid, String name) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Currency, Double> balances = new HashMap<>();
            getManager(CurrencyManager.class).getCurrencies().values().forEach(currency -> {
                String sql = String.format("SELECT * FROM `%s` WHERE uuid = ?", currency.id());
                try (Connection conn = getManager(MySQLManager.class).getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setString(1, uuid.toString());
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        balances.put(currency, rs.getDouble("value"));
                    } else {
                        balances.put(currency, currency.setting().startingBalance());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            return new UserStructure(uuid, name, balances);
        }, executor);
    }

    public CompletableFuture<Boolean> save(UserStructure user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                user.balances().forEach((currency, balance) -> {
                    String updateSql = String.format("UPDATE `%s` SET name = ?, value = ? WHERE uuid = ?", currency.id());

                    try (Connection conn = getManager(MySQLManager.class).getConnection();
                         PreparedStatement ps = conn.prepareStatement(updateSql)) {

                        ps.setString(1, user.name());
                        ps.setDouble(2, balance);
                        ps.setString(3, user.uuid().toString());

                        int updatedRows = ps.executeUpdate();

                        if (updatedRows == 0) {
                            String insertSql = String.format("INSERT INTO `%s` (uuid, name, value) VALUES (?, ?, ?)", currency.id());
                            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                                insertPs.setString(1, user.uuid().toString());
                                insertPs.setString(2, user.name());
                                insertPs.setDouble(3, balance);
                                insertPs.executeUpdate();
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> setBalanceOfflineByName(Currency currency, String name, Double amount) {
        return CompletableFuture.supplyAsync(() -> {
            String updateSql = String.format("UPDATE `%s` SET value = ? WHERE name = ?", currency.id());
            try (Connection conn = getManager(MySQLManager.class).getConnection();
                 PreparedStatement ps = conn.prepareStatement(updateSql)) {

                ps.setDouble(1, amount);
                ps.setString(2, name);

                int rowsUpdated = ps.executeUpdate();
                return rowsUpdated > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Double> getOfflineBalance(Currency currency, String name) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = String.format("SELECT * FROM `%s` WHERE name = ?", currency.id());
            try (Connection conn = getManager(MySQLManager.class).getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return rs.getDouble("value");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
    }

    public void updateCache(UserStructure user, boolean remove) {
        if (remove) users.remove(user.uuid());
        else users.put(user.uuid(), user);
    }

    public UserStructure getUser(UUID uuid) {
        return users.get(uuid);
    }

}