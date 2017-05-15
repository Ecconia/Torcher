package de.ecconia.bukkit.plugin.torcher;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Torch;

public class PlayerROM
{
	//Location info
	private final Location min;
	private final Location max;
	private final TorchDirection direction;

	//BitPosition
	private final int maxCounter;
	private int counter;

	//Start point
	private final int xs;
	private final int ys;
	private final int zs;

	//bitsInformation
	private final int bitsPerLayer;
	private final int bitsPerLine;

	//Factory begin///////////////////////////////////////////////////////////////

	public static PlayerROM create(Player player, Location min, Location max)
	{
		TorchDirection direction = TorchDirection.create(player);
		if (direction == null) { return null; }

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

		bitsPerLine = widthDiff(min, max, direction) / 2 + 1;
		bitsPerLayer = bitsPerLine * (lengthDiff(min, max, direction) + 1) / 2;

		//calculate max
		counter = 0;
		maxCounter = getAddresses() * bitsPerLine;

		xs = direction.isMaxX() ? max.getBlockX() : min.getBlockX();
		ys = max.getBlockY();
		zs = direction.isMaxZ() ? max.getBlockZ() : min.getBlockZ();

	}

	public void resetCounter(Player player)
	{
		counter = 0;
		player.sendMessage(Torcher.prefix + "Last paste position has been resetted.");
	}

	public void dataInput(Player player, String para)
	{
		final int bitMax = 15;
		int counter = 0;
		boolean bits[] = new boolean[para.length() * 15];

		doublebreak:
		{
			for (int letter = 0; letter < para.length(); letter++)
			{
				int number = para.charAt(letter) - 256;
				//	String filler = "";
				//	for (int i = 0; i < (bitMax - Integer.toBinaryString(number).length()); i++)
				//	{
				//		filler += '0';
				//	}
				//	player.sendMessage("Processing: " + filler + Integer.toBinaryString(number) + " " + number);
				for (int bit = 0; bit < bitMax; bit++)
				{
					if (this.counter + counter < maxCounter)
					{
						bits[counter++] = ((number & (1 << bit)) > 0 ? true : false);
					}
					else
					{
						break doublebreak;
					}
				}
			}
		}
		int bitsLeft = (para.length() * 15) - counter;
		player.sendMessage(Torcher.prefix + "Read " + counter + " bits. " + (bitsLeft > 14 ? "You send more data then the ROM can hold. " + bitsLeft + " bits are left." : ""));
		//	String code = "Code: ";
		//	or (int offset = 0; offset < counter; offset += 4)
		//	{
		//		try
		//		{
		//			code += (int) (((bits[offset] ? 1 : 0) << 0) | ((bits[offset + 1] ? 1 : 0) << 1) | ((bits[offset + 2] ? 1 : 0) << 2) | ((bits[offset + 3] ? 1 : 0) << 3));
		//			code += " ";
		//		}
		//		catch (Exception e)
		//		{
		//			player.sendMessage(e.getClass().getSimpleName());
		//		}
		//	}
		//	player.sendMessage(code);
		placeTorches(player, counter, bits);
	}

	private void placeTorches(Player player, int amount, boolean[] data)
	{
		Location loc = null;
		boolean broke = false;
		for (int i = 0; i < amount; i++)
		{
			loc = getTorchLocation(i + counter);
			BlockState state = loc.getBlock().getState();
			Material oldType = state.getType();
			if (oldType.equals(Material.AIR) || oldType.equals(Material.REDSTONE_TORCH_OFF) || oldType.equals(Material.REDSTONE_TORCH_ON))
			{
				if (data[i])
				{
					state.setType(Material.REDSTONE_TORCH_OFF);
					MaterialData mat = state.getData();
					if (mat instanceof Torch)
					{
						((Torch) mat).setFacingDirection(direction.getBlockFace());
					}
				}
				else
				{
					state.setType(Material.AIR);
				}
				state.update(true, true);
			}
			else
			{
				broke = true;
				break;
			}
		}
		if (broke)
		{
			this.counter = 0;
			player.sendMessage(Torcher.prefix + ChatColor.RED + "Aborted writing" + ChatColor.GRAY + ": Block at x:" + loc.getBlockX() + " y:" + loc.getBlockY() + " z:" + loc.getBlockZ() + " is not a redstone torch or air, replacing could damage something. Fix the ROM or correct the selection.");
			return;
		}
		counter += amount;
		player.sendMessage(Torcher.prefix + "Finished writing bits to ROM.");
	}

	private Location getTorchLocation(int position)
	{
		int layer = position % bitsPerLayer;

		int width = layer % bitsPerLine;
		int length = layer / bitsPerLine;
		int heigth = position / bitsPerLayer;

		switch (direction.getData()) {
		case TorchDirection.North:
			return new Location(min.getWorld(), xs + width * 2, ys - heigth * 4, zs - length * 2 - 1);
		case TorchDirection.East:
			return new Location(min.getWorld(), xs + length * 2 + 1, ys - heigth * 4, zs + width * 2);
		case TorchDirection.South:
			return new Location(min.getWorld(), xs - width * 2, ys - heigth * 4, zs + length * 2 + 1);
		}
		return new Location(min.getWorld(), xs - length * 2 - 1, ys - heigth * 4, zs - width * 2);
	}

	//Output-Helper

	public int getAddresses()
	{
		return (lengthDiff(min, max, direction) + 1) / 2 * ((max.getBlockY() - min.getBlockY()) / 4 + 1);
	}

	public int getBitwidth()
	{
		return bitsPerLine;
	}

	public String getDirectionString()
	{
		return direction.getDirection();
	}
}
