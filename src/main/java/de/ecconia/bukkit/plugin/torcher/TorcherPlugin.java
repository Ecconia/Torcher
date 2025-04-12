package de.ecconia.bukkit.plugin.torcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import de.ecconia.bukkit.plugin.torcher.helpers.StringHelper;
import de.ecconia.bukkit.plugin.torcher.helpers.WorldEditHelper;
import de.ecconia.bukkit.plugin.torcher.helpers.WorldEditHelper.Selection;

public class TorcherPlugin extends JavaPlugin implements Listener
{
	private HashMap<UUID, PlayerROM> roms;
	protected static final String prefix = ChatColor.WHITE + "[" + ChatColor.GOLD + "Torcher" + ChatColor.WHITE + "]" + ChatColor.GRAY + " ";
	
	@Override
	public void onEnable()
	{
		roms = new HashMap<>();
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
		var message = event.getMessage();
		var prefix = "/torcher ";
		if(!StringHelper.startsWithIgnoreCase(message, prefix))
		{
			return;
		}
		String commandContent = message.substring(prefix.length()).trim();
		
		int firstSpace = commandContent.indexOf(' ');
		if(firstSpace < 0)
		{
			return;
		}
		
		String subcommand = commandContent.substring(0, firstSpace);
		if(!(StringHelper.partOf(subcommand, "binary") && event.getPlayer().hasPermission("torcher")))
		{
			return;
		}
		
		event.setCancelled(true);
		
		String content = commandContent.substring(firstSpace + 1).trim();
		Player player = event.getPlayer();
		
		if(content.indexOf(' ') >= 0)
		{
			player.sendMessage(prefix + "Your data is broken, check that it doesn't contain spaces.");
			return;
		}
		
		var rom = roms.get(player.getUniqueId());
		if (rom == null)
		{
			player.sendMessage(prefix + "You have to select a ROM with WorldEdit and use \"/torcher define\".");
			return;
		}
		
		rom.dataInput(player, content);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(!(sender instanceof Player player))
		{
			sender.sendMessage(prefix + "This command does not work from console.");
			return true;
		}
		
		if(args.length == 0)
		{
			printSimpleHelp(player);
			return true;
		}
		
		if(StringHelper.partOf(args[0], "?", "help"))
		{
			printHelp(player);
		}
		else if(StringHelper.partOf(args[0], "about"))
		{
			printAbout(player);
		}
		else if(StringHelper.partOf(args[0], "tools"))
		{
			reloadConfig();
			String client = getConfig().getString("tools");
			
			if(client == null)
			{
				player.sendMessage(prefix + "There is no link to tools provided.");
				return true;
			}
			
			player.sendMessage(prefix + "Link to tools: " + client);
		}
		else if(StringHelper.partOf(args[0], "define"))
		{
			Selection s = WorldEditHelper.getSelection(player, prefix);
			if(s == null)
			{
				return true;
			}
			
			String[] extraArgs = new String[args.length-1];
			if(extraArgs.length > 0)
			{
				System.arraycopy(args, 1, extraArgs, 0, extraArgs.length);
			}
			
			PlayerROM rom = PlayerROM.create(player, s.getMin(), s.getMax(), extraArgs);
			if(rom != null)
			{
				roms.put(player.getUniqueId(), rom);
				player.sendMessage(prefix + "ROM with " + rom.getAddresses() + " addresses of each " + rom.getBitwidth() + " bits defined. " + " Direction: " + rom.getDirectionString());
			}
		}
		else if(StringHelper.partOf(args[0], "dump")) //Must be checked after 'define' to allow 'define' be triggered by '/torcher d'.
		{
			var rom = roms.get(player.getUniqueId());
			if(rom == null)
			{
				player.sendMessage(prefix + "You have to select a ROM with WorldEdit and use \"/torcher define\".");
				return true;
			}
			
			rom.dumpData(player);
		}
		else
		{
			if(!roms.containsKey(player.getUniqueId()))
			{
				player.sendMessage(prefix + "You have to select a ROM with WorldEdit and use \"/torcher define\".");
				return true;
			}
			
			if(!StringHelper.partOf(args[0], "reset"))
			{
				printSimpleHelp(player);
				return true;
			}
			
			roms.get(player.getUniqueId()).resetCounter();
			player.sendMessage(TorcherPlugin.prefix + "Last paste position has been reset.");
		}
		
		return true;
	}
	
	private static void printSimpleHelp(Player player)
	{
		sendFeedback(player, "Subcommands", "List",
			"help", "",
			"about", "",
			"tools", "",
			"define [head direction]", "",
			"reset", "",
			"binary <data>", "",
			"dump", ""
		);
	}
	
	private static void printHelp(Player player)
	{
		sendFeedback(player, "Command", "Help",
			"about", "Information about how this plugin works.",
			"tools", "Here you will find programs which compress the binary data for you.",
			"define [head direction]", "Select a normal torch ROM with WorldEdit, look in the same direction as the torches and use this command.",
			"reset", "Resets the last bit paste position, to start from address 0 again.",
			"binary <data>", "Send compressed <data>.",
			"dump", "Reads the binary content of a defined ROM and dumps it into chat as a binary string (which you can copy as URL).",
			"Smaller Commands", "You can shorten subcommands: \"/torcher define\" = \"/torcher d\"."
		);
	}
	
	private static void printAbout(Player player)
	{
		sendFeedback(player, "About", "Page",
			"ROMs", "Most simple Minecraft ROMs for CPU's use a simple torch ROM (cell/bit = 2*4*2). Selection such a ROM with WorldEdit and look into the direction the torches are facing ( [ ]-o --ThisDirection--> ) while defining it. Now you are able to flash binary data into it.",
			"Data/Compression", "To send as many bits as possible to the ROM each letter in the send command sends 15 Bits at once (not less). It's possible to send 245 letters with the command, that makes 3675 bits per command.",
			"Placement", "The order of bits cannot be changed, but the way they are inserted into the ROM. You can define which axis should be filled first, second and last. Its also possible in which direction each axis should be filled. To define this add (right, left; up, down; back, forward) to the define command.",
			"Author", "This Plugin was written by the player Ecconia, since he totally sucks at placing torches in ROMs. Now there is a faster way to do this."
		);
	}
	
	private static void sendFeedback(Player player, String title1, String title2, String...subLines)
	{
		List<String> lines = new ArrayList<>();
		lines.add(ChatColor.GOLD + "Torcher " + ChatColor.WHITE + title1 + " " + ChatColor.GRAY + title2 + ":");
		
		for(int i = 0; i < subLines.length; i += 2)
		{
			String details = subLines[i+1];
			if(!details.isEmpty())
			{
				details = ChatColor.GRAY + ": " + details;
			}
			
			lines.add(ChatColor.WHITE + "- " + ChatColor.GOLD + subLines[i] + details);
		}
		
		player.sendMessage(lines.toArray(new String[0]));
	}
}
