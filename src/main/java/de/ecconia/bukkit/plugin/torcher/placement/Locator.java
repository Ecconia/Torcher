package de.ecconia.bukkit.plugin.torcher.placement;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

public class Locator
{
	private final World world;
	
	private BiLoopTwo xBi;
	private BiLoopTwo yBi;
	private BiLoopTwo zBi;
	
	private BiLoopTwo iBi;
	private BiLoopTwo mBi;
	private BiLoopTwo oBi;
	
	public Locator(Location min, Location max, BlockFace torchDir, char first, char second, char third)
	{
		//TODO: Investigate if locations are exactly on the torches.
		world = min.getWorld();
		TriDirections<BiLoopTwo> vectors = new TriDirections<>(torchDir, first, second, third);
		
		//Create the loops for each axis
		xBi = new BiLoopTwo(min.getBlockX(), max.getBlockX(), vectors.isXIncreasing(), 2);
		yBi = new BiLoopTwo(min.getBlockY(), max.getBlockY(), vectors.isYIncreasing(), 4);
		zBi = new BiLoopTwo(min.getBlockZ(), max.getBlockZ(), vectors.isZIncreasing(), 2);
		
		//Throw them to the magic helper
		vectors.setXObject(xBi);
		vectors.setYObject(yBi);
		vectors.setZObject(zBi);
		
		//Get the correct loop from the magic helper
		iBi = vectors.getFirstObject();
		mBi = vectors.getSecondObject();
		oBi = vectors.getThirdObject();
	}
	
	public Location getNextLocation()
	{
		//Check if ROM is full.
		if(oBi.wasLastOverflow())
		{
			return null;
		}
		
		//Get current Location:
		Location loc = new Location(world, xBi.getValue(), yBi.getValue(), zBi.getValue());
		
		//Generate next Location:
		iBi.next();
		if(iBi.wasLastOverflow())
		{
			mBi.next();
			if(mBi.wasLastOverflow())
			{
				oBi.next();
			}
		}
		
		return loc;
	}
	
	public void reset()
	{
		iBi.reset();
		mBi.reset();
		oBi.reset();
	}
	
	private static class BiLoopTwo
	{
		private final int min;
		private final int max;
		private final boolean inc;
		
		private final int amount;
		private final int counterReset;
		
		private int counter;
		private boolean wasoverflow;
		
		public BiLoopTwo(int min, int max, boolean inc, int amount)
		{
			this.min = min;
			this.max = max;
			this.inc = inc;
			
			this.amount = inc ? amount : -amount;
			
			counter = inc ? min : max;
			counterReset = inc ? min : max;
		}
		
		public void next()
		{
			counter += amount;
			
			if((inc && counter > max) || (!inc && counter < min)) 
			{
				counter = counterReset;
				wasoverflow = true;
			}
		}
		
		public int getValue()
		{
			return counter;
		}
		
		public boolean wasLastOverflow()
		{
			boolean wasoverlow = this.wasoverflow;
			
			this.wasoverflow = false;
			
			return wasoverlow;
		}
		
		public void reset()
		{
			counter = counterReset;
			wasoverflow = false;
		}
	}
}
