package de.ecconia.bukkit.plugin.torcher.placement;

import org.bukkit.block.BlockFace;

public class TriDirections<T>
{
	Direction<T> first;
	Direction<T> second;
	Direction<T> third;
	
	Direction<T> width;
	Direction<T> depth;
	Direction<T> hight;
	
	Direction<T> xDir;
	Direction<T> yDir;
	Direction<T> zDir;
	
	public TriDirections(BlockFace direction, char f, char s, char t)
	{
		//Create objects for linking the same data.
		first = new Direction<>(f);
		second = new Direction<>(s);
		third = new Direction<>(t);
		
		//Sort each direction to its corrosponding ROM (whd) axis.
		sortAxis(first);
		sortAxis(second);
		sortAxis(third);
		
		//Sort each direction to its corrosponding xyz axis.
		yDir = hight;
		switch (direction)
		{
		case NORTH:
		case SOUTH:
			zDir = depth;
			xDir = width;
			break;
		case EAST:
		case WEST:
			xDir = depth;
			zDir = width;
			break;
		default:
			throw new InternalError("Received a BlockFace which should never happen to be here: " + direction.toString());
		}
		
		//Determine, if an axis is incrementing or decrementing.
		hight.setIncreasing(hight.getChar() == 'u');
		if(direction == BlockFace.WEST)
		{
			depth.setIncreasing(depth.getChar() == 'b');
			width.setIncreasing(width.getChar() == 'l');
		}
		else if(direction == BlockFace.EAST)
		{
			depth.setIncreasing(depth.getChar() == 'f');
			width.setIncreasing(width.getChar() == 'r');
		}
		else if(direction == BlockFace.NORTH)
		{
			depth.setIncreasing(depth.getChar() == 'b');
			width.setIncreasing(width.getChar() == 'r');
		}
		else if(direction == BlockFace.SOUTH)
		{
			depth.setIncreasing(depth.getChar() == 'f');
			width.setIncreasing(width.getChar() == 'l');
		}
	}
	
	private void sortAxis(Direction<T> d)
	{
		switch(d.getChar())
		{
		case 'd':
		case 'u':
			hight = d;
			break;
		case 'l':
		case 'r':
			width = d;
			break;
		case 'b':
		case 'f':
			depth = d;
			break;
		}
	}
	
	private static class Direction<T>
	{
		private final char c;
		
		private boolean increasing;
		private T object;
		
		public Direction(char c)
		{
			this.c = c;
		}
		
		public char getChar()
		{
			return c;
		}
		
		public void setIncreasing(boolean increasing)
		{
			this.increasing = increasing;
		}
		
		public boolean isIncreasing()
		{
			return increasing;
		}
		
		public void setObject(T object)
		{
			this.object = object;
		}
		
		public T getObject()
		{
			return object;
		}
	}
	
	//# Getters and Setters used after setup. #################################
	
	public boolean isXIncreasing()
	{
		return xDir.isIncreasing();
	}
	
	public boolean isZIncreasing()
	{
		return zDir.isIncreasing();
	}
	
	public boolean isYIncreasing()
	{
		return yDir.isIncreasing();
	}
	
	public void setXObject(T o)
	{
		xDir.setObject(o);
	}
	
	public void setYObject(T o)
	{
		yDir.setObject(o);
	}
	
	public void setZObject(T o)
	{
		zDir.setObject(o);
	}

	public T getFirstObject()
	{
		return first.getObject();
	}
	
	public T getSecondObject()
	{
		return second.getObject();
	}
	
	public T getThirdObject()
	{
		return third.getObject();
	}
}
