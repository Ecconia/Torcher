package de.ecconia.bukkit.plugin.torcher;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PlayerROM
{
	// Location information
	private Location min;
	private Location max;
	private TorchDirection direction;
	
	//ROM Settings
	private ROMSettings romSettings;

	//Factory begin///////////////////////////////////////////////////////////////

	public static PlayerROM create(Player player, Location min, Location max)
	{
		TorchDirection direction = TorchDirection.create(player);
		if (direction == null) { return null;}

		//Check width: (must be odd)
		if (isOddWidth(min, max, direction))
		{
			player.sendMessage(Torcher.prefix + "The width of your ROM should be odd.");
			return null;
		}
		//Check length: (bigger than 1)
		if ((direction.isParaX() ? max.getBlockZ() - min.getBlockZ() : max.getBlockX() - min.getBlockX()) < 1)
		{
			player.sendMessage(Torcher.prefix + "The length of your ROM should be bigger than 1.");
			return null;
		}

		//Fix Top:
		if (isRedstoneOnTop(min, max, direction))
		{
			if (max.getBlockY() == min.getBlockY())
			{
				player.sendMessage(Torcher.prefix + "You should select a ROM, not random redstone.");
				return null;
			}
			max.setY(max.getBlockY() - 1);
			//player.sendMessage("Removed one Layer from the top.");
		}

		//Fix Length:
		if (isOddLength(min, max, direction))
		{
			switch (direction.getData()) {
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

		return new PlayerROM(min, max, direction);
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

	private PlayerROM(Location min, Location max, TorchDirection direction)
	{
		this.min = min;
		this.max = max;
		this.direction = direction;
	}
	
	
	// Command format: 
	// /torcher setrom ws:#number config:-w-l-h flip:true
	public boolean config(Player player, String[] para)
	{
		int wordSize = 0;
		int[] romOrder = null;
		boolean[] reverse = null;
		boolean flip = false;
		
		for(int i = 1; i < para.length; i++)
		{
			String str = para[i];
			
			if(str.startsWith("ws:"))
			{
				if(NumberUtils.isNumber(str.substring(str.indexOf(':') + 1)))
				{
					wordSize = Integer.parseInt(str.substring(str.indexOf(':') + 1));
				}
				else
				{
					player.sendMessage(Torcher.prefix + "Illegal non-numeric argument: " + str);
					return false;
				}
			}
			else if(str.startsWith("config:"))
			{
				romOrder = new int[3];
				reverse = new boolean[3];
				
				String s = str.substring(str.indexOf(':') + 1);
				
				int count = 0;
				
				for(int j = s.length() - 1; j >= 0; j--)
				{	
					if(count > 2)
					{
						player.sendMessage(Torcher.prefix + "Illegal argument format: " + str);
						return false;
					}
					
					boolean r = false;
					
					if(j != 0)
					{
						r = s.charAt(j - 1) == '-';
					}
					
					if(s.charAt(j) == 'w')
					{
						reverse[0] = r;
						romOrder[2 - count] = 0;
					}
					else if(s.charAt(j) == 'l')
					{
						reverse[1] = r;
						romOrder[2 - count] = 1;
					}
					else if(s.charAt(j) == 'h')
					{
						reverse[2] = r;
						romOrder[2 - count] = 2;
					}
					else
					{
						player.sendMessage(Torcher.prefix + "Illegal character in argument: " + str);
						return false;
					}
					
					if(r)
					{
						s = s.substring(0, s.length() - 2);
						j--;
					}
					else
					{
						s = s.substring(0, s.length() - 1);
					}
					
					count++;
				}
				
				if(romOrder[0] == romOrder[1] || romOrder[0] == romOrder[2] || romOrder[1] == romOrder[2])
				{
					player.sendMessage(Torcher.prefix + "Illegal argument format: " + str);
					return false;
				}
			}
			else if(str.startsWith("flip:"))
			{
				if(str.substring(str.indexOf(':') + 1).equals("true"))
				{
					flip = true;
				}
				else if(str.substring(str.indexOf(':') + 1).equals("false"))
				{
					flip = false;
				}
				else
				{
					player.sendMessage(Torcher.prefix + "Illegal argument: " + str);
					return false;
				}	
			}
			else
			{
				player.sendMessage(Torcher.prefix + "Illegal argument: " + str);
				return false;
			}
		}
		
		romSettings = ROMSettings.create(player, min, max, Math.abs(wordSize), direction, romOrder == null ? new int[]{2, 1, 0} : romOrder, 
				reverse == null ? new boolean[]{false, false, false} : reverse, flip);
		
		if(romSettings == null)
		{
			return false;
		}
		
		return true;
	}

	public void resetCounter(Player player)
	{
		romSettings.resetCounter(player);
	}

	public void dataInput(Player player, String para)
	{
		romSettings.dataInput(player, para);
	}
	
	public String getROMInfo()
	{
		return romSettings.toString();
	}
	
	public String getDirection()
	{
		return direction.getDirection();
	}
}
