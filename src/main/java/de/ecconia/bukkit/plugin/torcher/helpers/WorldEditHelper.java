package de.ecconia.bukkit.plugin.torcher.helpers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.world.World;

public class WorldEditHelper
{
	public static Selection getSelection(Player player, String prefix)
	{
		WorldEditPlugin plugin = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		
		if(plugin == null)
		{
			player.sendMessage(prefix + "WorldEdit is not installed. It is required to make a selection.");
			return null;
		}
		
		if (!plugin.isEnabled())
		{
			player.sendMessage(prefix + "WorldEdit is not enabled. It is required to make a selection.");
			return null;
		}
		
		LocalSession session = plugin.getSession(player);
		World weWorld = session.getSelectionWorld();
		
		//No selection world, no selection
		if(weWorld == null)
		{
			player.sendMessage(prefix + "Please make a WorldEdit selection before using this command.");
			return null;
		}
		
		if(!(weWorld instanceof BukkitWorld weBWorld))
		{
			throw new InternalError("Expected getSelectionWorld() in WE API to return a BukkitWorld, but it was " + weWorld.getClass().getSimpleName());
		}
		
		if(weBWorld.getWorld() != player.getWorld())
		{
			player.sendMessage(prefix + "You should be in the same world as your WorldEdit selection.");
			return null;
		}
		
		RegionSelector selector = session.getRegionSelector(weWorld);
		
		if(!(selector instanceof CuboidRegionSelector cuboidRegionSelector))
		{
			player.sendMessage(prefix + "This command only accepts cuboid selection, use \"//sel ?\" if you don't know how to change your selection type.");
			return null;
		}
		
		try
		{
			return new Selection(cuboidRegionSelector.getRegion(), player.getWorld());
		}
		catch (IncompleteRegionException e)
		{
			player.sendMessage(prefix + "Please make a WorldEdit selection before using this command.");
			return null;
		}
	}
	
	public static class Selection
	{
		private final Location min;
		private final Location max;
		
		public Selection(CuboidRegion region, org.bukkit.World world)
		{
			min = new Location(world, region.getMinimumPoint().getX(), region.getMinimumPoint().getY(), region.getMinimumPoint().getZ());
			max = new Location(world, region.getMaximumPoint().getX(), region.getMaximumPoint().getY(), region.getMaximumPoint().getZ());
		}
		
		public Location getMin()
		{
			return min;
		}
		
		public Location getMax()
		{
			return max;
		}
	}
}
