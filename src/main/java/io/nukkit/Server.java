package io.nukkit;

import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.SQLitePlatform;
import com.avaje.ebeaninternal.server.lib.sql.TransactionIsolation;
import io.nukkit.command.CommandSender;
import io.nukkit.command.PluginCommand;
import io.nukkit.configuration.file.YamlConfiguration;
import io.nukkit.entity.Player;
import io.nukkit.item.ItemFactory;
import io.nukkit.plugin.PluginManager;
import io.nukkit.plugin.ServiceManager;
import io.nukkit.plugin.messaging.Messenger;
import io.nukkit.scheduler.Scheduler;
import io.nukkit.util.ServerShutdownThread;
import io.nukkit.util.Versioning;
import io.nukkit.util.Warning;
import io.nukkit.util.logging.ConsoleHandler;
import io.nukkit.util.logging.ConsoleWriter;
import io.nukkit.util.logging.LoggerOutputStream;
import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import joptsimple.OptionSet;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * author: MagicDroidX
 * Nukkit Project
 */
public class Server implements Runnable {
    /**
     * Used for all administrative messages, such as an operator using a
     * command.
     * <p>
     * For use in {@link #broadcast(java.lang.String, java.lang.String)}.
     */
    public static final String BROADCAST_CHANNEL_ADMINISTRATIVE = "nukkit.broadcast.admin";

    /**
     * Used for all announcement messages, such as informing users that a
     * player has joined.
     * <p>
     * For use in {@link #broadcast(java.lang.String, java.lang.String)}.
     */
    public static final String BROADCAST_CHANNEL_USERS = "nukkit.broadcast.user";

    private final String nukkitVersion = Versioning.getNukkitVersion();
    private final Logger logger = LogManager.getLogger("Nukkit");
    private YamlConfiguration configuration;

    public OptionSet options;

    public ConsoleReader reader;

    private boolean isRunning = true;
    private boolean isStopped;

    public int port = -1;

    List<String> commandQueue = new ArrayList<>();

    private Thread serverThread;

    public Server(OptionSet options) {
        Nukkit.setServer(this);

        this.options = options;

        if (System.console() == null && System.getProperty(jline.TerminalFactory.JLINE_TERMINAL) == null) {
            System.setProperty(jline.TerminalFactory.JLINE_TERMINAL, UnsupportedTerminal.class.getName());

            Nukkit.useJline = false;
        }

        if (options.has("port")) {
            int port = (Integer) options.valueOf("port");
            if (port > 0) {
                this.setPort(port);
            }
        }

        try {
            this.reader = new ConsoleReader(System.in, System.out);
            this.reader.setExpandEvents(false);
        } catch (Throwable e) {
            try {
                System.setProperty(jline.TerminalFactory.JLINE_TERMINAL, UnsupportedTerminal.class.getName());
                System.setProperty("user.language", "en");
                Nukkit.useJline = false;

                this.reader = new ConsoleReader(System.in, System.out);
                this.reader.setExpandEvents(false);
            } catch (IOException ex) {
                this.getLogger().log(Level.WARN, (String) null, ex);
            }
        }

        Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(this));

        (new ConsoleHandler(this)).start();

        (new ConsoleWriter(System.out, this.reader)).start();

        Logger rootLogger = LogManager.getRootLogger();
        System.setOut(new PrintStream(new LoggerOutputStream(rootLogger, Level.INFO), true));
        System.setErr(new PrintStream(new LoggerOutputStream(rootLogger, Level.WARN), true));

        this.serverThread = new Thread(this, "Server Thread");
        this.serverThread.start();
    }

    @Override
    public void run() {
        while (this.isRunning) {
            while (!commandQueue.isEmpty()) {
                //TODO: REAL HANDLING
                String command = commandQueue.get(0);
                commandQueue.remove(0);
                dispatchCommand(null, command);
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {

    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public boolean isStopped() {
        return this.isStopped;
    }

    public String getName() {
        return "Nukkit";
    }

    public String getVersion() {
        return Nukkit.MINECRAFT_VERSION;
    }

    public String getNukkitVersion() {
        return nukkitVersion;
    }

    public List<Player> getOnlinePlayers() {
        return new ArrayList<>();
        //TODO
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUpdateFolder() {
        return "update";
        //TODO
    }

    public PluginManager getPluginManager() {
        return null;
        //TODO
    }

    public Scheduler getScheduler() {
        return null;
        //TODO
    }

    public ServiceManager getServiceManager() {
        return null;
        //TODO
    }

    public Messenger getMessenger() {
        return null;
        //TODO
    }

    public boolean isServerThread() {
        return Thread.currentThread().equals(this.serverThread);
    }

    public Map<String, String[]> getCommandAliases() {
        return new HashMap<>();
        //TODO
    }

    /**
     * Gets the player with the given UUID.
     *
     * @param id UUID of the player to retrieve
     * @return a player object if one was found, null otherwise
     */
    public Player getPlayer(UUID id) {
        return null;
        //todo
    }

    /**
     * Gets a player object by the given username.
     * <p>
     * This method may not return objects for offline players.
     *
     * @param name the name to look up
     * @return a player if one was found, null otherwise
     * @deprecated Use {@link #getPlayer(UUID)} as player names are no longer
     * guaranteed to be unique
     */
    @Deprecated
    public Player getPlayer(String name) {
        //TODO
        return null;
    }

    /**
     * Gets the player with the exact given name, case insensitive.
     *
     * @param name Exact name of the player to retrieve
     * @return a player object if one was found, null otherwise
     * @deprecated Use {@link #getPlayer(UUID)} as player names are no longer
     * guaranteed to be unique
     */
    @Deprecated
    public Player getPlayerExact(String name) {
        return null;
    }

    public Warning.WarningState getWarningState() {
        return Warning.WarningState.DEFAULT;
        //TODO
    }

    public ItemFactory getItemFactory() {
        return null;
        // TODO
    }

    public int broadcast(String message, String permission) {
        return 0;
        //TODO
    }

    public PluginCommand getPluginCommand(String name) {
        return null;
        //TODO
    }

    public void configureDbConfig(ServerConfig config) {
        Validate.notNull(config, "Config cannot be null");
        DataSourceConfig ds = new DataSourceConfig();
        ds.setDriver(this.configuration.getString("database.driver"));
        ds.setUrl(this.configuration.getString("database.url"));
        ds.setUsername(this.configuration.getString("database.username"));
        ds.setPassword(this.configuration.getString("database.password"));
        ds.setIsolationLevel(TransactionIsolation.getLevel(this.configuration.getString("database.isolation")));
        if (ds.getDriver().contains("sqlite")) {
            config.setDatabasePlatform(new SQLitePlatform());
            config.getDatabasePlatform().getDbDdlSyntax().setIdentity("");
        }

        config.setDataSourceConfig(ds);
    }


    public Logger getLogger() {
        return this.logger;
    }

    public void issueCommand(String command) {
        this.commandQueue.add(command);
    }

    public boolean dispatchCommand(CommandSender sender, String commandLine) {
        this.getLogger().info(commandLine);
        return true;
        //TODO
    }
}
