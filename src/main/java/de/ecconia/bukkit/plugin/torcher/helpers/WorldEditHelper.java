package de.ecconia.bukkit.plugin.torcher.helpers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class WorldEditHelper
{
	public static boolean isWorldEditInstalled()
	{
		return getWE() != null;
	}

	public static boolean isWorldEditEnabled()
	{
		return Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit");
	}

	private static WorldEditPlugin getWE()
	{
		return (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
	}

	public static boolean hasSelection(Player player)
	{
		return getSelection(player) != null;
	}

	public static boolean isCorrectWorld(Player player)
	{
		return getWE().getSelection(player).getWorld().equals(player.getWorld());
	}

	public static boolean isCubicSelection(Player player)
	{
		return getSelection(player) instanceof CuboidSelection;
	}

	public static Selection getSelection(Player player)
	{
		return getWE().getSelection(player);
	}

	public static Selection getSelection(Player player, String prefix)
	{
		if (!isWorldEditEnabled())
		{
			player.sendMessage(prefix + "Could not access WorldEdit, please check if it's installed and enabled.");
			return null;
		}

		Selection sel = ((WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit")).getSelection(player);

		if (sel == null)
		{
			player.sendMessage(prefix + "Please make a WorldEdit selection before using this command.");
			return null;
		}
		if (!sel.getWorld().equals(player.getWorld()))
		{
			player.sendMessage(prefix + "You should be in the same world as your WorldEdit selection.");
			return null;
		}
		if (!(sel instanceof CuboidSelection))
		{
			player.sendMessage(prefix + "This command only accepts cuboid selection, use \"//sel ?\" if you don't know how to change your selection type.");
			return null;
		}
		return sel;
	}
}
