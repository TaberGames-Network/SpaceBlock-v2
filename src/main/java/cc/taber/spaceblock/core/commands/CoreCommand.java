package cc.taber.spaceblock.core.commands;

import cc.taber.spaceblock.CoreClass;
import cc.taber.spaceblock.core.commands.general.CommandHeal;
import cc.taber.spaceblock.core.commands.planet.CreatePlanet;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CoreCommand implements CommandExecutor {

    final String commandName;
    final String permission;
    final boolean canConsoleUse;

   public static CoreClass plugin;


    public CoreCommand(final String commandName, final String permission, final boolean canConsoleUse) {
        this.commandName = commandName;
        this.permission = permission;
        this.canConsoleUse = canConsoleUse;
        plugin.getCommand(commandName).setExecutor(plugin);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args) {
        String cmdPrefix = "spaceblock";
        if(!cmd.getLabel().equalsIgnoreCase(cmdPrefix + commandName))
            return true;
        if(!sender.hasPermission(permission)){
            sender.sendMessage("You don't have permission for this.");
            return true;
        }
        if(!canConsoleUse && !(sender instanceof Player)){
            sender.sendMessage("Only players may use this command sorry!");
            return true;
        }
        execute(sender, args);
        return true;

    }
    public abstract void execute(CommandSender sender, String[] args);

    public static void registerCommands(JavaPlugin pl) {
        pl = plugin;
        new CommandHeal();

    }

}