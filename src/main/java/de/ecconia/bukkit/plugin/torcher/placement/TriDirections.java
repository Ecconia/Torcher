package de.ecconia.bukkit.plugin.torcher.placement;

import org.bukkit.block.BlockFace;

public class TriDirections<T>
{
	private final Direction<T> first;
	private final Direction<T> second;
	private final Direction<T> third;
	
	private Direction<T> width;
	private Direction<T> depth;
	private Direction<T> height;
	
	private final Direction<T> xDir;
	private final Direction<T> yDir;
	private final Direction<T> zDir;
	
	public TriDirections(BlockFace direction, char f, char s, char t)
	{
		//Create objects for linking the same data.
		first = new Direction<>(f);
		second = new Direction<>(s);
		third = new Direction<>(t);
		
		//Sort each direction to its corresponding ROM (whd) axis.
		sortAxis(first);
		sortAxis(second);
		sortAxis(third);
		
		//Sort each direction to its corresponding xyz axis.
		yDir = height;
		switch (direction) {
			case NORTH, SOUTH -> {
				zDir = depth;
				xDir = width;
			}
			case EAST, WEST -> {
				xDir = depth;
				zDir = width;
			}
			default -> throw new InternalError("Received a BlockFace which should never happen to be here: " + direction);
		}
		
		//Determine, if an axis is incrementing or decrementing.
		height.setIncreasing(height.getChar() == 'u');
		switch (direction) {
			case WEST -> {
				depth.setIncreasing(depth.getChar() == 'b');
				width.setIncreasing(width.getChar() == 'l');
			}
			case EAST -> {
				depth.setIncreasing(depth.getChar() == 'f');
				width.setIncreasing(width.getChar() == 'r');
			}
			case NORTH -> {
				depth.setIncreasing(depth.getChar() == 'b');
				width.setIncreasing(width.getChar() == 'r');
			}
			case SOUTH -> {
				depth.setIncreasing(depth.getChar() == 'f');
				width.setIncreasing(width.getChar() == 'l');
			}
		}
	}
	
	private void sortAxis(Direction<T> direction)
	{
		switch (direction.getChar()) {
			case 'd', 'u' -> height = direction;
			case 'l', 'r' -> width = direction;
			case 'b', 'f' -> depth = direction;
		}
	}
	
	private static class Direction<T>
	{
		private final char character;
		
		private boolean increasing;
		private T object;
		
		public Direction(char character)
		{
			this.character = character;
		}
		
		public char getChar()
		{
			return character;
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
