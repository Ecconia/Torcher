package de.ecconia.bukkit.plugin.torcher;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class TorchDirection
{
	public static final byte East = 1;
	public static final byte West = 2;
	public static final byte South = 3;
	public static final byte North = 4;

	/**
	 * 1 - East
	 * 2 - West
	 * 3 - South
	 * 4 - North
	 */
	private final byte data;

	public static TorchDirection create(Player player)
	{
		float direction = (player.getLocation().getYaw() % 360) < 0 ? player.getLocation().getYaw() + 360 : player.getLocation().getYaw();

		if (315 <= direction || direction < 45)
		{
			return new TorchDirection(South);
		}
		else if (45 <= direction && direction < 135)
		{
			return new TorchDirection(West);
		}
		else if (135 <= direction && direction < 225)
		{
			return new TorchDirection(North);
		}
		else if (225 <= direction && direction < 315)
		{
			return new TorchDirection(East);
		}
		else
		{
			player.sendMessage(TorcherPlugin.prefix + ChatColor.RED + "Something went wrong, no proper angle: " + ChatColor.WHITE + player.getLocation().getYaw());
			return null;
		}
	}

	private TorchDirection(byte data)
	{
		this.data = data;
	}

	public byte getData()
	{
		return data;
	}

	public String getDirection()
	{
		switch (data) {
		case North:
			return "North";
		case East:
			return "East";
		case South:
			return "South";
		case West:
			return "West";
		}
		return "";
	}
	
	public BlockFace getBlockFace()
	{
		switch (data) {
		case North:
			return BlockFace.NORTH;
		case East:
			return BlockFace.EAST;
		case South:
			return BlockFace.SOUTH;
		}
		return BlockFace.WEST;
	}

	public boolean isMaxX()
	{
		if (data == East || data == North) { return false; }
		return true;
	}

	public boolean isMaxZ()
	{
		if (data == East || data == South) { return false; }
		return true;
	}

	public boolean isParaX()
	{
		if (data == East || data == West) { return false; }
		return true;
	}
}
