package me.fuzzybot.spaceblock.bungee;

import com.google.common.io.ByteStreams;
import me.fuzzybot.spaceblock.util.StringUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpaceBlock extends Plugin implements Listener {

    private JedisPool pool;
    private static String split = "######";
    private final String SPACEBLOCK_CHANNEL = "sb";
    private final String BUNGEE_CHANNEL = "sbb";
    private static Plugin instance;
    private ConcurrentHashMap<UUID, SpaceBlockBungeePlayer> players = new ConcurrentHashMap<UUID, SpaceBlockBungeePlayer>();

    Configuration config;

    @Override
    public void onEnable() {
        instance = this;
            try {
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(loadResource(this, "config.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            final String ip = config.getString("ip");
            final int port = config.getInt("port");
            final String password = config.getString("password");

            this.getProxy().getPluginManager().registerListener(this, this);

            getProxy().getScheduler().runAsync(this, new Runnable() {
                @Override
                public void run() {
                    pool = new JedisPool(new JedisPoolConfig(), ip, port, 0, password);
                    Jedis jedis = pool.getResource();
                    try {
                        jedis.subscribe(new JedisPubSubHandler(), BUNGEE_CHANNEL);
                        System.out.println("SpaceBlock Connected to Redis server.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        pool.returnBrokenResource(jedis);
                        getLogger().severe("SpaceBlock unable to connect to Redis server.");
                        return;
                    }
                    pool.returnResource(jedis);
                }
            });
    }


    @EventHandler(priority = Byte.MAX_VALUE)
    public void onChatEvent(ChatEvent event) {
        if (event.isCancelled()) return;

        if (event.getSender() instanceof ProxiedPlayer) {

            ProxiedPlayer player = (ProxiedPlayer) event.getSender();

            if (player.getServer().getInfo().getName().startsWith("spaceblock")) {

                if (player.getServer().getInfo().getName().startsWith("spaceblock_planet_")) {
                    if (event.getMessage().equalsIgnoreCase("/mothership") || event.getMessage().equalsIgnoreCase("/ms")) {
                        event.setCancelled(true);
                        player.connect(ProxyServer.getInstance().getServerInfo("spaceblock"));
                        return;
                    }
                }

                if (event.getMessage().startsWith("/")) return;

                String message = event.getMessage();
                event.setCancelled(true);

                SpaceBlockBungeePlayer bplayer = getBPlayer(player.getUniqueId());

                String prefix = bplayer.getPrefix();
                if (prefix.length() > 0) {
                    prefix = prefix + " ";
                }
                String suffix = bplayer.getSuffix();
                if (suffix.length() > 0) {
                    suffix = " " + suffix;
                }

                String server = "Portal";

                if (player.getServer().getInfo().getName().equalsIgnoreCase("spaceblock_mothership")) {
                    server = "Ship";
                } else if (player.getServer().getInfo().getName().startsWith("spaceblock_planet_")) {
                    server = "Planet " + player.getServer().getInfo().getName().substring(18);
                }

                message = ChatColor.BLUE + "[" + ChatColor.GOLD + server + ChatColor.BLUE + "] " + ChatColor.RESET + prefix + ChatColor.RESET + player.getDisplayName() + ChatColor.RESET + suffix + ChatColor.RESET + ChatColor.GRAY + " » " + ChatColor.RESET + message;

                sendBungeeMessage("chat" + split + message);

            }

        }
    }

    public SpaceBlockBungeePlayer getBPlayer(UUID uuid) {
        if (!players.containsKey(uuid)) {
            System.out.println("creating new player object for sb");
            players.put(uuid, new SpaceBlockBungeePlayer());
        }
        return players.get(uuid);
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onPlayerDisconnectEvent(PlayerDisconnectEvent event) {

        // This only fires if a player quits the proxy. not during a server change.

        String serverName = event.getPlayer().getServer().getInfo().getName();
        UUID playerUUID = event.getPlayer().getUniqueId();

        if (serverName.startsWith("spaceblock_")) {
            sendSpaceBlockMessage("proxy_disconnect" + split + playerUUID.toString() + split + event.getPlayer().getServer().getInfo().getName());
        }

        players.remove(playerUUID);
    }


    public void sendSpaceBlockMessage(final String message) {

        getProxy().getScheduler().runAsync(this, new Runnable() {
            @Override
            public void run() {

                Jedis jedis = pool.getResource();
                try {

                    jedis.publish(SPACEBLOCK_CHANNEL, message);

                } catch (Exception e) {
                    pool.returnBrokenResource(jedis);
                }
                pool.returnResource(jedis);
            }
        });
    }

    public void sendBungeeMessage(final String message) {

        getProxy().getScheduler().runAsync(this, new Runnable() {
            @Override
            public void run() {

                Jedis jedis = pool.getResource();
                try {

                    jedis.publish(BUNGEE_CHANNEL, message);

                } catch (Exception e) {
                    pool.returnBrokenResource(jedis);
                }
                pool.returnResource(jedis);
            }
        });
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void onServerConnectedEvent(ServerConnectEvent event) {
        //   System.out.println("scon");

        // This only fires during a server change, not if a player quits.

        String serverName = event.getPlayer().getServer().getInfo().getName();
        String targetServerName = event.getTarget().getName();
        UUID playerUUID = event.getPlayer().getUniqueId();

        sendSpaceBlockMessage("server_change" + split + playerUUID.toString() + split + serverName + split + targetServerName);
    }

    class JedisPubSubHandler extends JedisPubSub {

        @Override
        public void onMessage(final String s, final String s2) {
            if (s2.trim().length() == 0) return;
            getProxy().getScheduler().runAsync(SpaceBlock.this, new Runnable() {
                @Override
                public void run() {

                    //  System.out.println("got pubsub: " + s2);
                    String message[] = s2.split(split);
                    if (message[0].equals("chat")) {
                        // System.out.println("got chat: " + message[1]);
                        for (Map.Entry<String, ServerInfo> entry : ProxyServer.getInstance().getServers().entrySet()) {
                            if (entry.getKey().startsWith("spaceblock")) {
                                for (ProxiedPlayer player : entry.getValue().getPlayers()) {
                                    player.sendMessage(StringUtils.stringToComp(message[1]));
                                }
                            }
                        }

                    } else if (message[0].equals("rank")) {
                        System.out.println("got rank message");
                        if (getProxy().getPlayer(UUID.fromString(message[1])) != null) {
                            System.out.println("got rank online player: " + message[1] + " " + message[2] + " " + message[3]);
                            SpaceBlockBungeePlayer bPlayer = getBPlayer(UUID.fromString(message[1]));
                            bPlayer.setPrefix(message[2]).setSuffix(message[3]);
                            players.put(UUID.fromString(message[1]), bPlayer);
                            System.out.println("test fetch " + players.get(UUID.fromString(message[1])).getPrefix() + " " + players.get(UUID.fromString(message[1])).getSuffix());
                        }
                    }
                }
            });
        }

        @Override
        public void onPMessage(String s, String s2, String s3) {
        }

        @Override
        public void onSubscribe(String s, int i) {
        }

        @Override
        public void onUnsubscribe(String s, int i) {
        }

        @Override
        public void onPUnsubscribe(String s, int i) {
        }

        @Override
        public void onPSubscribe(String s, int i) {
        }
    }


    public static File loadResource(Plugin plugin, String resource) {
        File folder = plugin.getDataFolder();
        if (!folder.exists())
            folder.mkdir();
        File resourceFile = new File(folder, resource);
        try {
            if (!resourceFile.exists()) {
                resourceFile.createNewFile();
                InputStream in = plugin.getResourceAsStream(resource);
                OutputStream out = new FileOutputStream(resourceFile);
                ByteStreams.copy(in, out);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resourceFile;
    }
}

