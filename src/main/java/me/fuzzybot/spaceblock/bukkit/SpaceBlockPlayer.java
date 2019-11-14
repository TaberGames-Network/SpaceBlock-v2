package me.fuzzybot.spaceblock.bukkit;

/**
 * Created by tfrew on 10/18/14.
 */
public class SpaceBlockPlayer {
    private int updateLock;
    private String lastServer;
    private String newServer;
    private String planet;

    public SpaceBlockPlayer() {
        this.updateLock = 0;
        this.lastServer = "lobby";
        this.newServer = "spaceblock_mothership";
        this.planet = "spaceblock_planet_1";
    }

    public int getUpdateLock() {
        return updateLock;
    }

    public void setUpdateLock(int updateLock1) {
        updateLock = updateLock1;
    }

    public String getPlanet() {
        return planet;
    }

    public SpaceBlockPlayer setPlanet(String planet) {
        this.planet = planet;
        return this;
    }

    public String getLastServer() {
        return lastServer;
    }

    public SpaceBlockPlayer setLastServer(String lastServer1) {
        lastServer = lastServer1;
        return this;
    }

    public String getNewServer() {
        return newServer;
    }

    public SpaceBlockPlayer setNewServer(String newServer1) {
        newServer = newServer1;
        return this;
    }

}
