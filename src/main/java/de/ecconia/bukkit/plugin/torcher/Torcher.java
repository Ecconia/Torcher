package de.ecconia.bukkit.plugin.torcher;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.selections.Selection;

public class Torcher extends JavaPlugin
{
	private HashMap<UUID, PlayerROM> roms;
	protected static final String prefix = ChatColor.WHITE + "[" + ChatColor.GOLD + "Torcher" + ChatColor.WHITE + "]" + ChatColor.GRAY + " ";

	@Override
	public void onEnable()
	{
		roms = new HashMap<UUID, PlayerROM>();
		saveDefaultConfig();
	}
	
	@Override
	public void onDisable()
	{
		roms.clear();
		roms = null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			if (!((Player) sender).hasPermission("ecconia.torcher"))
			{
				sender.sendMessage(prefix + "You don't have permission to use this command.");
				return true;
			}
		}
		else
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
				if(args.length > 1 && args[1].equals("config"))
				{
					printConfigHelp(player);
				}
				else
				{
					printHelp(player);
				}
			}
			else if (StringHelper.partOf(args[0], "about", "info"))
			{
				printAbout(player);
			}
			else if (StringHelper.partOf(args[0], "client", "files", "tools"))
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
			else if (StringHelper.partOf(args[0], "rom", "setrom", "definerom"))
			{
				Selection s = WorldEditHelper.getSelection(player, prefix);
				if (s == null) { return true; }
				PlayerROM rom = PlayerROM.create(player, s.getMinimumPoint(), s.getMaximumPoint());
				if (rom != null && rom.config(player, args))
				{
					roms.put(player.getUniqueId(), rom);
					player.sendMessage(prefix + "Defined ROM with direction " + rom.getDirection());
					player.sendMessage(rom.getROMInfo());
				}
			}
			else if (StringHelper.partOf(args[0], "config"))
			{
				PlayerROM rom = roms.get(player.getUniqueId());
				
				if(rom == null)
				{
					player.sendMessage(prefix + "You must define a ROM before you can configure it.");
					return true;
				}
				
				if(rom.config(player, args))
				{
					player.sendMessage(Torcher.prefix + "ROM Information:");
					player.sendMessage(rom.getROMInfo());
				}
			}
			else
			{
				if (roms.containsKey(player.getUniqueId()))
				{
					if (StringHelper.partOf(args[0], "resetcounter", "newbinary"))
					{
						roms.get(player.getUniqueId()).resetCounter(player);
					}
					else if (StringHelper.partOf(args[0], "sendbinary", "senddata", "data", "binary"))
					{
						if (args.length == 1)
						{
							player.sendMessage(prefix + "Syntax: /torcher <sendbinary/senddata/data/binary> <data>");
							return true;
						}
						if (args.length > 2)
						{
							player.sendMessage(prefix + "Warning, Your data could be broken, check that it doesn't contain spaces.");
						}
						roms.get(player.getUniqueId()).dataInput(player, args[1]);
					}
					else
					{
						printSimpleHelp(player);
					}
				}
				else
				{
					player.sendMessage(prefix + "You have to select a ROM with WorldEdit and use \"/torcher rom\".");
				}
			}
		}
		return true;

	}
	
	private static void printSimpleHelp(Player player)
	{
		player.sendMessage(new String[] { 
				ChatColor.GOLD + "Torcher " + ChatColor.WHITE + "Command " + ChatColor.GRAY + "Overview:", 
				ChatColor.WHITE + "-" + ChatColor.GOLD + "help", 
				ChatColor.WHITE + "-" + ChatColor.GOLD + "about", 
				ChatColor.WHITE + "-" + ChatColor.GOLD + "client",
				ChatColor.WHITE + "-" + ChatColor.GOLD + "definerom",
				ChatColor.WHITE + "-" + ChatColor.GOLD + "config",
				ChatColor.WHITE + "-" + ChatColor.GOLD + "resetcounter", 
				ChatColor.WHITE + "-" + ChatColor.GOLD + "sendbinary <data>"});
	}

	private static void printHelp(Player player)
	{
		player.sendMessage(new String[] { 
				ChatColor.GOLD + "Torcher " + ChatColor.WHITE + "Command " + ChatColor.GRAY + "Help:", 
				ChatColor.WHITE + "-" + ChatColor.GOLD + "about/info" + ChatColor.GRAY + " - " + "Information about how this plugin works.", 
				ChatColor.WHITE + "-" + ChatColor.GOLD + "client/files/tools" + ChatColor.GRAY + " - " + "Here you'll find tools that compress the data for you.",
				ChatColor.WHITE + "-" + ChatColor.GOLD + "rom/setrom/definerom" + ChatColor.GRAY + " - " + "Select a standard ROM with WorldEdit, look in the same direction as the torches and use this command.",
				ChatColor.WHITE + "-" + ChatColor.GOLD + "config" + ChatColor.GRAY + " - " + "Sets the configuration of your currently selected ROM, for more info use the command \"/torcher help config\".",
				ChatColor.WHITE + "-" + ChatColor.GOLD + "resetcounter/newbinary " + ChatColor.GRAY + " - " + "Resets the counter, if you want to write from address 0 again.", 
				ChatColor.WHITE + "-" + ChatColor.GOLD + "sendbinary/senddata/data/binary <data>" + ChatColor.GRAY + " - " + "Send the compressed data using this command.",
				ChatColor.WHITE + "-" + ChatColor.GOLD + "Smaller Commands" + ChatColor.GRAY + " - " + "You can leave characters away in a command: \"/torcher rom\" = \"/torcher r\", but be careful with \"/torcher data\", \"/torcher d\" is \"/torcher definerom\"."});
	}
	
	private static void printAbout(Player player)
	{
		player.sendMessage(new String[] { 
				ChatColor.GOLD + "Torcher " + ChatColor.WHITE + "About " + ChatColor.GRAY + "Page:", 
				ChatColor.WHITE + "-" + ChatColor.GOLD + "ROM's" + ChatColor.GRAY + " - " + "ROM's are the base of all standard computers. You have to select the ROM you want to flash with binary data. The direction the torches are facing is the direction of the ROM. Torches have [ ]-o --this-> direction.", 
				ChatColor.WHITE + "-" + ChatColor.GOLD + "Data" + ChatColor.GRAY + " - " + "To send as many bits as possible to the ROM each letter in the send command sends 15 Bits at once (not less). It's possible to send over 200 letters with the command, that makes over 3000 bits per command.", 
				ChatColor.WHITE + "-" + ChatColor.GOLD + "Compression" + ChatColor.GRAY + " - " + "The order of the bits is defined and can be changed with the \"/torcher config\" command. For the default configuration, if you look in the torch direction the first bit is always left. The first addresses are in the top layer of your ROM. The first address is at the front (torchdirection). ", 
				ChatColor.WHITE + "-" + ChatColor.GOLD + "Author" + ChatColor.GRAY + " - " + "This Plugin was written by the player Ecconia, since he totally sucks at placing torches in ROM's. It should get the input by an automated typer though chat."
});
	}
	
	private static void printConfigHelp(Player player)
	{
		player.sendMessage(new String[] {
				ChatColor.GOLD + "Torcher " + ChatColor.WHITE + "Configuration " + ChatColor.GRAY + "Help:",
				ChatColor.GRAY + "The torcher configuration command allows you to create many different varieties of ROMs.  This command has three different arguments that can be passed to it.",
				ChatColor.WHITE + "-" + ChatColor.GOLD + "ws:" + ChatColor.GRAY + " - " + "The word size of your ROM is specified by using the command \"/torcher ws:<wordSize>\" where <wordSize> is a positive integer.",
				ChatColor.WHITE + "-" + ChatColor.GOLD + "c:" + ChatColor.GRAY + " - " +  "The order words are placed into the ROM is determined by this argument. For example, the command \"/torcher config c:-l-hw\" specifies that the ROM begins filling at the front of the length dimension (\"torcher config c:l-hw\" would cause the ROM to fill starting from the back)."
						+ "It also specifies that the ROM should begin filling from the bottom (\"/torcher config c:-lhw\" would cause the ROM to start filling from the top) and that the ROM should begin filling on the left (\"/torcher config c:-l-h-w\" would cause the ROM to start filling from the right.  The order of the letters in the command specify where each successive "
						+ "word is placed relative to the previous word. For example, \"/torcher config c:lhw would cause each additional word to be placed next the previous word and only when there is no longer room along the ROMs width would the ROM begin filling the row behind the previous word.  Once there is no longer room along the ROMs length the successive words are placed "
						+ "below the previous words. Hence the configuration \"c:hlw\" specifies that the width of the ROM should be filled, then the length, and finally the height.",
				ChatColor.WHITE + "-" + ChatColor.GOLD + "flip:" + ChatColor.GRAY + " - " + "If you want each word within your ROM to be flipped (11010 to 01011) you can set this argument equal to true. For example, \"torcher config flip:true\" flips each word while \"torcher config flip:false\" does not.",
				ChatColor.WHITE + "-" + ChatColor.GOLD + "Default Configuration" + ChatColor.GRAY + " - " + "The default configuration for each ROM is equivalent to running the command \"/torcher config ws:<romWidthInBits> c:hlw flip:false\"."
		});
	}
}
