package de.ecconia.bukkit.plugin.torcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.selections.Selection;

import de.ecconia.bukkit.plugin.torcher.helpers.StringHelper;
import de.ecconia.bukkit.plugin.torcher.helpers.WorldEditHelper;

public class TorcherPlugin extends JavaPlugin implements Listener
{
	private HashMap<UUID, PlayerROM> roms;
	protected static final String prefix = ChatColor.WHITE + "[" + ChatColor.GOLD + "Torcher" + ChatColor.WHITE + "]" + ChatColor.GRAY + " ";
	
	@Override
	public void onEnable()
	{
		roms = new HashMap<UUID, PlayerROM>();
		saveDefaultConfig();
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable()
	{
		roms.clear();
		roms = null;
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onAllCommands(PlayerCommandPreprocessEvent event)
	{
		if(StringUtils.startsWithIgnoreCase(event.getMessage(), "/torcher "))
		{
			String commandContent = event.getMessage().substring("/torcher ".length()).trim();
			int firstSpace = commandContent.indexOf(' ');
			if(firstSpace >= 0)
			{
				String subcommand = commandContent.substring(0, firstSpace);
				if(StringHelper.partOf(subcommand, "binary") && event.getPlayer().hasPermission("torcher"))
				{
					event.setCancelled(true);
					
					String content = commandContent.substring(firstSpace+1).trim();
					Player player = event.getPlayer();
					if(content.indexOf(' ') >= 0)
					{
						player.sendMessage(prefix + "Your data is broken, check that it doesn't contain spaces.");
					}
					else
					{
						if (roms.containsKey(player.getUniqueId()))
						{
							roms.get(player.getUniqueId()).dataInput(player, content);
						}
						else
						{
							player.sendMessage(prefix + "You have to select a ROM with WorldEdit and use \"/torcher define\".");
						}
					}
				}
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage(prefix + "This command does not work from console.");
			return true;
		}
		
		//Interface
		Player player = (Player) sender;
		
		if (args.length == 0)
		{
			printSimpleHelp(player);
		}
		else
		{
			if (StringHelper.partOf(args[0], "?", "help"))
			{
				printHelp(player);
			}
			else if (StringHelper.partOf(args[0], "about"))
			{
				printAbout(player);
			}
			else if (StringHelper.partOf(args[0], "client"))
			{
				reloadConfig();
				String client = getConfig().getString("client");
				if(client == null)
				{
					player.sendMessage(prefix + "There is no link to tools provided.");
				}
				else
				{
					player.sendMessage(prefix + "Link to tools: " + client);
				}
			}
			else if (StringHelper.partOf(args[0], "define"))
			{
				Selection s = WorldEditHelper.getSelection(player, prefix);
				if (s == null) { return true; }
				PlayerROM rom = PlayerROM.create(player, s.getMinimumPoint(), s.getMaximumPoint());
				if (rom != null)
				{
					roms.put(player.getUniqueId(), rom);
					player.sendMessage(prefix + "ROM with " + rom.getAddresses() + " addresses of each " + rom.getBitwidth() + " bits defined. " + " Direction: " + rom.getDirectionString());
				}
			}
			else
			{
				if (roms.containsKey(player.getUniqueId()))
				{
					if (StringHelper.partOf(args[0], "reset"))
					{
						roms.get(player.getUniqueId()).resetCounter(player);
					}
					else
					{
						printSimpleHelp(player);
					}
				}
				else
				{
					player.sendMessage(prefix + "You have to select a ROM with WorldEdit and use \"/torcher define\".");
				}
			}
		}
		
		return true;
	}
	
	private static void printSimpleHelp(Player player)
	{
		sendFeedback(player, "Subcommands", "List",
			"help", "",
			"about", "",
			"client", "",
			"define [head direction]", "",
			"reset", "",
			"binary <data>", ""
		);
	}
	
	private static void printHelp(Player player)
	{
		sendFeedback(player, "Command", "Help",
			"about", "Information about how this plugin works.",
			"client", "Here you'll find tools that compress the data for you.",
			"define [head direction]", "Select a normal torch ROM with WorldEdit, look in the same direction as the torches and use this command.",
			"reset", "Resets the counter, if you want to write from address 0 again.",
			"binary <data>", "Send the compressed data using this command.",
			"Smaller Commands", "You can leave characters away in a command: \"/torcher define\" = \"/torcher d\"."
		);
	}
	
	private static void printAbout(Player player)
	{
		sendFeedback(player, "About", "Page",
			"ROM's", "ROM's are the base of all standard computers. You have to select the ROM you want to flash with binary data. The direction the torches are facing is the direction of the ROM. Torches have [ ]-o --this-> direction.",
			"Data", "To send as many bits as possible to the ROM each letter in the send command sends 15 Bits at once (not less). It's possible to send 89 letters with the command, that makes 1335 bits per command.",
			"Compression", "The order of the bits is defined and can't be changed. If you look in the torch direction the first bit is always left. The first addresses are in the top layer of your ROM. The first address is at the front (torchdirection).",
			"Author", "This Plugin was written by the player Ecconia, since he totally sucks at placing torches in ROM's. It should get the input by an automated typer though chat."
		);
	}
	
	private static void sendFeedback(Player player, String title1, String title2, String...subLines)
	{
		List<String> lines = new ArrayList<>();
		lines.add(ChatColor.GOLD + "Torcher " + ChatColor.WHITE + title1 + ChatColor.GRAY + title2 + ":");
		for(int i = 0; i < subLines.length / 2; i += 2)
		{
			String details = subLines[i+1];
			if(!details.isEmpty())
			{
				details = ChatColor.GRAY + " - " + details;
			}
			
			lines.add(ChatColor.WHITE + "-" + ChatColor.GOLD + subLines[i] + details);
		}
		
		player.sendMessage(lines.toArray(new String[lines.size()]));
	}
}
