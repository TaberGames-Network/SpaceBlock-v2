package me.fuzzybot.spaceblock.bukkit;

import com.google.common.base.Joiner;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
//import me.fuzzybot.spaceblock.util.MyFileUtils;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

//import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpaceBlock extends JavaPlugin implements Listener {



    private Chat chat;

    public Chat getChat() {
        return chat;
    }

    private static final Joiner joiner = Joiner.on(" ");
    private static String split = "######";
    private static Plugin instance;
    private final String SPACEBLOCK_CHANNEL = "sb";
    private final String BUNGEE_CHANNEL = "sbb";
   // File mainDataDir = new File("/home/mc/spaceblock_mothership/all_worlds/spaceblock/playerdata");
    private JedisPool pool;
    private ConcurrentHashMap<UUID, SpaceBlockPlayer> players = new ConcurrentHashMap<UUID, SpaceBlockPlayer>();
    private ConcurrentHashMap<UUID, String> savePlayerTask = new ConcurrentHashMap<UUID, String>();

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
    }

    private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        String ip = getConfig().getString("ip");
        int port = getConfig().getInt("port");
        String password = getConfig().getString("password");
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        if (!setupChat()) {
            getLogger().info("Vault not found! Plugin is disabling!!");
            getServer().getPluginManager().disablePlugin(this);
        }
        pool = new JedisPool(new JedisPoolConfig(), ip, port, 0, password);
        new BukkitRunnable() {
            @Override
            public void run() {
                Jedis jedis = pool.getResource();
                try {
                    jedis.subscribe(new JedisPubSubHandler(), SPACEBLOCK_CHANNEL);
                } catch (Exception e) {
                    e.printStackTrace();
                    pool.returnBrokenResource(jedis);
                    getLogger().severe("Unable to connect to Redis server.");
                    return;
                }
                pool.returnResource(jedis);
            }
        }.runTaskAsynchronously(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, String> player : savePlayerTask.entrySet()) {
                  /*  File from = getPlayerDataFolder(player.getValue(), player.getKey());
                    File to = getPlayerDataFolder("spaceblock_mothership", player.getKey());
                    try {
                        MyFileUtils.copyFolder(from, to);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } */
                }
                savePlayerTask.clear();
            }
        }.runTaskTimerAsynchronously(this, 20, 80);
    }


    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        final Player player = event.getPlayer();
        final UUID playerUUID = player.getUniqueId();


        player.sendMessage("Teleporting in 4 seconds...");
        instance.getServer().getScheduler().runTaskLater(instance, new Runnable() {
            @Override
            public void run() {
                player.sendMessage("Teleporting in 3 seconds...");
            }
        }, 20L);
        instance.getServer().getScheduler().runTaskLater(instance, new Runnable() {
            @Override
            public void run() {
                player.sendMessage("Teleporting in 2 seconds...");
            }
        }, 40L);
        instance.getServer().getScheduler().runTaskLater(instance, new Runnable() {
            @Override
            public void run() {
                player.sendMessage("Teleporting in 1 seconds...");
            }
        }, 60L);


        if (!players.containsKey(playerUUID)) {
            players.put(playerUUID, new SpaceBlockPlayer());
        }

        // do file copies async
        instance.getServer().getScheduler().runTaskLaterAsynchronously(instance, new Runnable() {
            @Override
            public void run() {

                final SpaceBlockPlayer sbPlayer = players.get(playerUUID);

                if (sbPlayer.getNewServer().equals("spaceblock") && sbPlayer.getLastServer().equals("spaceblock_mothership")) {

                    sbPlayer.setNewServer(sbPlayer.getPlanet());
                    sbPlayer.setPlanet("spaceblock_planet_0");
                 /*   File playerData = getPlayerDataFolder("spaceblock_mothership", playerUUID);
                    File goal = getPlayerDataFolder(sbPlayer.getNewServer(), playerUUID);
                    try {
                        MyFileUtils.copyFolder(playerData, goal);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } */

                } else if (sbPlayer.getNewServer().equals("spaceblock") && sbPlayer.getLastServer().contains("spaceblock_planet_")) {
                    if (sbPlayer.getPlanet().contains("0")) {
                        sbPlayer.setNewServer("spaceblock_mothership");
                    } else {
                        sbPlayer.setNewServer(sbPlayer.getPlanet());
                        sbPlayer.setPlanet("spaceblock_planet_0");
                    }
                 /*   File playerData = getPlayerDataFolder(sbPlayer.getLastServer(), playerUUID);
                    File goal = getPlayerDataFolder("spaceblock_mothership", playerUUID);
                    try {
                        MyFileUtils.copyFolder(playerData, goal);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } */
                } else {
                    sbPlayer.setNewServer("spaceblock_mothership");
                }


                // teleport sync
                instance.getServer().getScheduler().runTaskLater(instance, new Runnable() {
                    @Override
                    public void run() {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(players.get(playerUUID).getNewServer());
                        player.sendPluginMessage(instance, "BungeeCord", out.toByteArray());

                        instance.getServer().getScheduler().runTaskLater(instance, new Runnable() {
                            @Override
                            public void run() {
                               if (getServer().getPlayer(playerUUID) != null) {
                                   player.sendMessage("Uh Oh! Something went wrong :(");
                                   player.sendMessage("Please log out and log back in!");
                               }
                            }
                        }, 40L);
                    }
                }, 20L);
            }
        }, 60l);
    }

    /*public File getPlayerDataFolder(String serverName, UUID playerID) {
        if (serverName.equals("spaceblock_mothership")) {
            return new File("/home/mc/spaceblock_mothership/all_worlds/spaceblock/playerdata/", playerID.toString() + ".dat");
        } else {
            return new File("/home/mc/" + serverName + "/spaceblock/playerdata/", playerID.toString() + ".dat");
        }
    }*/

    public void sendPlayerRank (final UUID id) {

        // Make sure we run on main thread for vault access

        new BukkitRunnable() {
            @Override
            public void run() {
                OfflinePlayer player = instance.getServer().getOfflinePlayer(id);
                final String playerid = id.toString();
                final String prefix = chat.getPlayerPrefix("spaceblock", player);
                final String suffix = chat.getPlayerSuffix("spaceblock", player);


                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Jedis jedis = pool.getResource();
                        try {

                            jedis.publish(BUNGEE_CHANNEL, "rank" + split + playerid + split + prefix + split + suffix);

                        } catch (Exception e) {
                            pool.returnBrokenResource(jedis);
                        }
                        pool.returnResource(jedis);

                    }
                }.runTaskAsynchronously(instance);

            }
        }.runTask(instance);
    }

    // NO FILE COPY OPERATIONS OR PLAYER TELEPORTATIONS DONE HERE!
    class JedisPubSubHandler extends JedisPubSub {

        @Override
        public void onMessage(final String s, final String s2) {
            if (s2.trim().length() == 0) return;
            System.out.println(s2);

            String message[] = s2.split(split);
            if (message[0].equals("server_change")) {
                String targetServer = message[3];
                String oldServer = message[2];
                UUID id = UUID.fromString(message[1]);
                if ((!targetServer.contains("spaceblock") && !oldServer.contains("spaceblock")) || targetServer.equalsIgnoreCase(oldServer))
                    return;
                if (!targetServer.contains("spaceblock") && oldServer.contains("spaceblock_planet_")) {
                    savePlayerTask.put(id, oldServer);
                    return;
                }

                if (players.containsKey(id)) {
                    players.get(id).setLastServer(oldServer).setNewServer(targetServer);
                } else {
                    players.put(id, new SpaceBlockPlayer().setLastServer(oldServer).setNewServer(targetServer));
                }
                sendPlayerRank(id);
            } else if (message[0].equals("proxy_disconnnect")) {
                UUID id = UUID.fromString(message[1]);
                String oldServer = message[2];
                if (oldServer.contains("spaceblock_planet_")) {
                    savePlayerTask.put(id, oldServer);
                }
            } else if (message[0].equals("planetid")) {
                UUID id = UUID.fromString(message[1]);
                if (players.containsKey(id)) {
                    players.get(id).setPlanet("spaceblock_planet_" + message[2]);
                } else {
                    players.put(id, new SpaceBlockPlayer().setPlanet("spaceblock_planet_" + message[2]));
                }
            }
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

}

