package cc.taber.spaceblock.core.commands.general;

import cc.taber.spaceblock.core.commands.CoreCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHeal extends CoreCommand {

    private String colorize(String message){
        ChatColor.translateAlternateColorCodes('&', "§");
        return message;
    }

    public CommandHeal() {
        super("heal", "spaceblock.general.heal", false);
    }

    public void execute(CommandSender sender, String[] args) {
    Player player = (Player) sender;
        player.sendMessage(colorize("&6&lSpaceBlock&8&l» &7You have been healed."));
        player.setHealth(20);
    }
}
