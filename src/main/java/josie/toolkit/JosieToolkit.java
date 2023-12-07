// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2020 JosieToolkit contributors
// SPDX-FileCopyrightText: 2020 lordpipe
package josie.toolkit;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.plugin.java.JavaPlugin;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class JosieToolkit extends JavaPlugin {
    static {
        RocksDB.loadLibrary();
    }

    private final Path pluginFolder = Paths.get(getDataFolder().getAbsolutePath());

    public Options rocksdbOptions;
    public static RocksDB rocksdb;

    public static HikariDataSource ds;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        var translations = TranslationRegistry.create(Key.key("josie", "main"));
        translations.defaultLocale(new Locale("en"));
        translations.registerAll(
                new Locale("en"),
                ResourceBundle.getBundle("locale", new Locale("en"), UTF8ResourceBundleControl.get()),
                true);
        translations.registerAll(
                new Locale("da"),
                ResourceBundle.getBundle("locale", new Locale("da"), UTF8ResourceBundleControl.get()),
                true);
        GlobalTranslator.translator().addSource(translations);

        rocksdbOptions = new Options();

        rocksdbOptions.setCreateIfMissing(true);

        try {
            rocksdb = RocksDB.open(
                    rocksdbOptions, pluginFolder.resolve("database").toString());
        } catch (RocksDBException err) {
            throw new RuntimeException(err);
        }

        if (getConfig().getBoolean("database.enabled")) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(getConfig().getString("database.uri"));
            config.setUsername(getConfig().getString("database.username"));
            config.setPassword(getConfig().getString("database.password"));
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("characterEncoding", "utf8");
            config.addDataSourceProperty("useUnicode", "true");
            config.addDataSourceProperty("connectionInitSql", "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci");

            ds = new HikariDataSource(config);
        }

        try {
            var con = ds.getConnection();
            var result = con.prepareStatement("select * from co_block limit 1").executeQuery();
            result.next();
            System.out.println(result.getString("rowid"));
        } catch (SQLException err) {
            throw new RuntimeException(err);
        }
    }

    @Override
    public void onDisable() {
        rocksdb.close();
        rocksdbOptions.close();
    }
}
