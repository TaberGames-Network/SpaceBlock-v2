package me.fuzzybot.spaceblock.bungee;

/**
 * Created by tfrew on 10/18/14.
 */
public class SpaceBlockBungeePlayer {
    private String suffix;
    private String prefix;

    public SpaceBlockBungeePlayer() {
        this.suffix = "";
        this.prefix = "";
    }

    public String getSuffix() {
        return suffix;
    }

    public SpaceBlockBungeePlayer setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public SpaceBlockBungeePlayer setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

}
