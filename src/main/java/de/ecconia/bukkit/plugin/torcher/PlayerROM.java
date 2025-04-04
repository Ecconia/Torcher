package de.ecconia.bukkit.plugin.torcher;

import static de.ecconia.bukkit.plugin.torcher.TorcherPlugin.prefix;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.RedstoneWallTorch;
import org.bukkit.entity.Player;

import de.ecconia.bukkit.plugin.torcher.helpers.StringHelper;
import de.ecconia.bukkit.plugin.torcher.placement.Locator;

public class PlayerROM
{
	//Location info
	private final Location min;
	private final Location max;
	private final TorchDirection direction;
	
	//Position helper
	private final Locator locator;
	
	//Factory begin///////////////////////////////////////////////////////////////
	
	public static PlayerROM create(Player player, Location min, Location max, String[] extraArgs)
	{
		TorchDirection direction = TorchDirection.create(player);
		if(direction == null)
		{
			player.sendMessage(prefix + "Could not determine your player rotation.");
			return null;
		}
		
		//Check width: (must be odd)
		if(isOddWidth(min, max, direction))
		{
			player.sendMessage(prefix + "The width of your ROM should be odd.");
			return null;
		}
		
		//Check length: (bigger than 1)
		if((direction.isParaX() ? max.getBlockZ() - min.getBlockZ() : max.getBlockX() - min.getBlockX()) < 1)
		{
			player.sendMessage(prefix + "The length of your ROM should be bigger than 1.");
			return null;
		}
		
		//Fix Top:
		if(isRedstoneOnTop(min, max, direction))
		{
			if(max.getBlockY() == min.getBlockY())
			{
				player.sendMessage(prefix + "You should select a ROM, not random redstone.");
				return null;
			}
			
			max.setY(max.getBlockY() - 1);
			//player.sendMessage("Removed one Layer from the top.");
		}
		
		//Fix Length:
		if(isOddLength(min, max, direction))
		{
			switch(direction.getData())
			{
			case TorchDirection.North:
				min.setZ(min.getBlockZ() + 1);
				break;
			case TorchDirection.East:
				max.setX(max.getBlockX() - 1);
				break;
			case TorchDirection.South:
				max.setZ(max.getBlockZ() - 1);
				break;
			case TorchDirection.West:
				min.setX(min.getBlockX() + 1);
				break;
			}
			//player.sendMessage("Removed one Layer from the length.");
		}
		
		//Fix Bottom:
		{
			int diff = max.getBlockY() - min.getBlockY();
			int count = diff / 4;
			int blocks = count * 4;
			min.setY(max.getBlockY() - blocks);
		}
		
		//Subtract one from back of ROM, to have the location set on the torch:
		switch(direction.getData())
		{
		case TorchDirection.North:
			max.setZ(max.getBlockZ() - 1);
			break;
		case TorchDirection.East:
			min.setX(min.getBlockX() + 1);
			break;
		case TorchDirection.South:
			min.setZ(min.getBlockZ() + 1);
			break;
		case TorchDirection.West:
			max.setX(max.getBlockX() - 1);
			break;
		}
		
		//Invlaid arg count:
		if(extraArgs.length != 0 && extraArgs.length != 3)
		{
			player.sendMessage(prefix + "Usage: \"/torcher define [f s t]\"");
			//TODO: Help for the flipping options.
			return null;
		}
		
		char vectors[] = {'l', 'b', 'u'};
		
		//Extra options:
		//TODO: For now this is 3, later guessing should be implemented.
		if(extraArgs.length == 3)
		{
			boolean hadW = false;
			boolean hadH = false;
			boolean hadD = false;
			
			for(int i = 0; i < extraArgs.length; i++)
			{
				String argument = extraArgs[i];
				if(StringHelper.partOf(argument, "right", "left", "up", "down", "back", "forward"))
				{
					char directionChar = argument.charAt(0);
					if((int) directionChar < 97)
					{
						directionChar += 32;
					}
					
					switch(directionChar)
					{
					case 'r':
					case 'l':
						if(hadW)
						{
							player.sendMessage(prefix + "You supplied two directions for the width of the ROM (multiple left/right).");
							return null;
						}
						hadW = true;
						break;
					case 'f':
					case 'b':
						if(hadD)
						{
							player.sendMessage(prefix + "You supplied two directions for the depth of the ROM (multiple back/forward).");
							return null;
						}
						hadD = true;
						break;
					case 'u':
					case 'd':
						if(hadH)
						{
							player.sendMessage(prefix + "You supplied two directions for the hight of the ROM (multiple up/down).");
							return null;
						}
						hadH = true;
						break;
					}
					
					vectors[i] = directionChar;
				}
				else
				{
					player.sendMessage(prefix + "Cannot parse argument \"" + argument + "\" to: right, left, up, down, back, forward.");
					return null;
				}
			}
		}
		
		return new PlayerROM(min, max, direction, vectors[0], vectors[1], vectors[2]);
	}
	
	private static boolean isOddWidth(Location min, Location max, TorchDirection direction)
	{
		return (widthDiff(min, max, direction) & 1) == 1;
	}
	
	private static boolean isOddLength(Location min, Location max, TorchDirection direction)
	{
		return (lengthDiff(min, max, direction) & 1) == 0;
	}
	
	private static int widthDiff(Location min, Location max, TorchDirection direction)
	{
		return direction.isParaX() ? max.getBlockX() - min.getBlockX() : max.getBlockZ() - min.getBlockZ();
	}
	
	private static int lengthDiff(Location min, Location max, TorchDirection direction)
	{
		return direction.isParaX() ? max.getBlockZ() - min.getBlockZ() : max.getBlockX() - min.getBlockX();
	}
	
	private static boolean isRedstoneOnTop(Location min, Location max, TorchDirection direction)
	{
		return min.getWorld().getBlockAt(new Location(min.getWorld(), direction.isMaxX() ? max.getBlockX() : min.getBlockX(), max.getY(), direction.isMaxZ() ? max.getBlockZ() : min.getBlockZ())).getType().equals(Material.REDSTONE_WIRE);
	}
	
	//Factory end/////////////////////////////////////////////////////////////////
	
	private PlayerROM(Location min, Location max, TorchDirection direction, char first, char second, char third)
	{
		this.min = min;
		this.max = max;
		this.direction = direction;
		
		locator = new Locator(min, max, direction.getBlockFace(), first, second, third);
	}
	
	public void resetCounter()
	{
		locator.reset();
	}
	
	public void dataInput(Player player, String binaryInput)
	{
		int counter = 0;
		boolean bits[] = new boolean[binaryInput.length() * 15];
		
		for(int letter = 0; letter < binaryInput.length(); letter++)
		{
			int number = binaryInput.charAt(letter) - 256;
			
			for(int bit = 0; bit < 15; bit++)
			{
				bits[counter++] = ((number & (1 << bit)) > 0 ? true : false);
			}
		}
		
		placeTorches(player, bits);
	}
	
	private void placeTorches(Player player, boolean[] bits)
	{
		Location loc;
		int bitsWritten = 0;
		
		for(int i = 0; i < bits.length; i++)
		{
			loc = locator.getNextLocation();
			if(loc == null)
			{
				resetCounter();
				player.sendMessage(prefix + ChatColor.RED + "Aborted writing" + ChatColor.GRAY + ": ROM is full, cannot write more bits.");
				player.sendMessage(prefix + "Wrote " + bitsWritten + "/" + bits.length + ".");
				return;
			}
			
			BlockState state = loc.getBlock().getState();
			Material oldType = state.getType();
			
			if(oldType == Material.AIR || oldType == Material.REDSTONE_WALL_TORCH)
			{
				if(bits[i])
				{
					state.setType(Material.REDSTONE_WALL_TORCH);
					RedstoneWallTorch blockData = (RedstoneWallTorch) state.getBlockData();
					blockData.setFacing(direction.getBlockFace());
					state.setBlockData(blockData);
				}
				else
				{
					state.setType(Material.AIR);
				}
				state.update(true, true);
			}
			else
			{
				resetCounter();
				player.sendMessage(prefix + ChatColor.RED + "Aborted writing" + ChatColor.GRAY + ": Block at x:" + loc.getBlockX() + " y:" + loc.getBlockY() + " z:" + loc.getBlockZ() + " is not a redstone torch or air, replacing could damage something. Fix the ROM or correct the selection.");
				return;
			}
			
			//Increment counter, for each bit placed.
			bitsWritten++;
		}
		
		player.sendMessage(prefix + "Wrote " + bitsWritten + " bits to ROM.");
	}
	
	public void dumpData(Player player) {
		var stringBuilder = new StringBuilder();
		
		locator.reset(); //Start from the beginning.
		Location loc = locator.getNextLocation();
		while(loc != null)
		{
			var material = loc.getBlock().getState().getType();
			switch (material) {
				case AIR -> stringBuilder.append('0');
				case REDSTONE_WALL_TORCH -> stringBuilder.append('1');
				default -> {
					player.sendMessage(prefix + ChatColor.RED + "Aborted dumping" + ChatColor.GRAY + ": Block at x:" + loc.getBlockX() + " y:" + loc.getBlockY() + " z:" + loc.getBlockZ() + " is not a redstone torch or air, cannot gather bit value. Fix the ROM or correct the selection.");
					return;
				}
			}
			
			loc = locator.getNextLocation();
		}
		
		var clickableText = new TextComponent(stringBuilder.toString());
		clickableText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://" + stringBuilder)); //For the clicking to work, it must be a "valid enough" URL
		player.spigot().sendMessage(new TextComponent(prefix + "Data: "), clickableText);
	}
	
	//Output-Helper
	
	public int getAddresses()
	{
		return ((lengthDiff(min, max, direction) + 1) / 2 + 1) * ((max.getBlockY() - min.getBlockY()) / 4 + 1);
	}
	
	public int getBitwidth()
	{
		return widthDiff(min, max, direction) / 2 + 1;
	}
	
	public String getDirectionString()
	{
		return direction.getDirection();
	}
}
