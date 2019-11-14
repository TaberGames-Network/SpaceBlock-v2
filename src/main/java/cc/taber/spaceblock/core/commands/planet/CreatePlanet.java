package cc.taber.spaceblock.core.commands.planet;

import cc.taber.spaceblock.core.commands.CoreCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreatePlanet extends CoreCommand {

    public CreatePlanet() {
        super("create", "spaceblock.planet.create", false);
    }


    public void execute(CommandSender sender, String[] args) {
    Player player = (Player) sender;

    }
}
