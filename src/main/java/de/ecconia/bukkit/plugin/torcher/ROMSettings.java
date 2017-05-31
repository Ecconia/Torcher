package de.ecconia.bukkit.plugin.torcher;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Torch;

public class ROMSettings 
{
	// The size of each address in the ROM
	private int wordSize;
	
	// World
	private World world;
	
	// Torch direction
	private TorchDirection direction;
	
	// The size of the ROM in words
	// size[0] = wordsPerWidth
	// size[1] = wordsPerLength
	// size[2] = wordsPerHeight
	private int[] size;
	
	// The order in which the ROM addresses increase
	private int[] order;
	
	// Reverse the direction for width, length, and height
	private boolean[] reverse;
	
	// Reverse the direction of each word
	private boolean flip;
	
	// Points to the first bit that has not been written yet
	private int counter = 0;
	
	// The total number of bits in this ROM
	private int maxCounter;
	
	// Starting location
	private int xs;
	private int ys;
	private int zs;
	
	public static ROMSettings create(Player player, Location min, Location max, int wordSize, TorchDirection direction, int[] order, boolean[] reverse, boolean flip)
	{
		int bitsPerWidth = widthDiff(min, max, direction) / 2 + 1;
		int bitsPerLength = (lengthDiff(min, max, direction) + 1) / 2;
		int bitsPerHeight = (heightDiff(min, max) + 4) / 4;
		
		if(wordSize == 0)
		{
			wordSize = bitsPerWidth;
		}
		
		if(bitsPerWidth % wordSize != 0)
		{
			player.sendMessage(Torcher.prefix + "Your ROM width is not a multiple of the desired word size.");
			return null;
		}
		
		return new ROMSettings(wordSize, new int[]{bitsPerWidth / wordSize, bitsPerLength, bitsPerHeight}, order, reverse, flip, min, max, direction);
	}
	
	private static int widthDiff(Location min, Location max, TorchDirection direction)
	{
		return direction.isParaX() ? max.getBlockX() - min.getBlockX() : max.getBlockZ() - min.getBlockZ();
	}

	private static int lengthDiff(Location min, Location max, TorchDirection direction)
	{
		return direction.isParaX() ? max.getBlockZ() - min.getBlockZ() : max.getBlockX() - min.getBlockX();
	}
	
	private static int heightDiff(Location min, Location max)
	{
		return max.getBlockY() - min.getBlockY();
	}
	
	private ROMSettings(int wordSize, int[] size, int[] order, boolean[] reverse, boolean flip, Location min, Location max, TorchDirection direction)
	{
		this.wordSize = wordSize;
		this.size = size;
		this.order = order;
		this.reverse = reverse;
		this.flip = flip;
		this.world = min.getWorld();
		this.direction = direction;
		
		maxCounter = wordSize * size[0] * size[1] * size[2];
		
		xs = direction.isMaxX() ? max.getBlockX() : min.getBlockX();
		ys = max.getBlockY();
		zs = direction.isMaxZ() ? max.getBlockZ() : min.getBlockZ();
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
					((Torch) mat).setFacingDirection(direction.getBlockFace());
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
	
	private Location getTorchLocation(int pos)
	{
		int[] location = new int[3];
		int position = pos / wordSize;
		
		location[order[2]] = reverse[order[2]] ? size[order[2]] - position % size[order[2]] - 1: position % size[order[2]];
		position /= size[order[2]];
		
		location[order[1]] = reverse[order[1]] ? size[order[1]] - position % size[order[1]] - 1 : position % size[order[1]];
		position /= size[order[1]];
		
		location[order[0]] = reverse[order[0]] ? size[order[0]] - position % size[order[0]] - 1 : position % size[order[0]];
		
		int width = flip ? location[0] * wordSize + pos % wordSize : location[0] * wordSize + (wordSize - pos % wordSize - 1);
		int length = location[1];
		int height = location[2];
		
		switch(direction.getData())
		{
		case TorchDirection.North:
			return new Location(world, xs + width * 2, ys - height * 4, zs - length * 2 - 1);
		case TorchDirection.East:
			return new Location(world, xs + length * 2 + 1, ys - height * 4, zs + width * 2);
		case TorchDirection.South:
			return new Location(world, xs - width * 2, ys - height * 4, zs + length * 2 + 1);
		}
		return new Location(world, xs - length * 2 - 1, ys - height * 4, zs - width * 2);
	}
	
	public void resetCounter(Player player)
	{
		counter = 0;
		player.sendMessage(Torcher.prefix + "Last paste position has been reset.");
	}
	
	@Override
	public String toString()
	{
		int totalBits = wordSize * size[0] * size[1] * size[2];
		
		return "Word size: " + wordSize + " bits, Width: " + size[0] + " word(s), Length: " + size[1] + " word(s), Height: " + size[2] + " word(s), "
				+ "TOTAL SIZE: " + size[0] * size[1] * size[2] + " word(s) totaling " + totalBits / 8.0 + " bytes (" + totalBits + " bits)";
	}
}
