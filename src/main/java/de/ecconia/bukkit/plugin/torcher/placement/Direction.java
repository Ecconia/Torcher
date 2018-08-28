package de.ecconia.bukkit.plugin.torcher.placement;

import java.util.HashSet;
import java.util.Set;

public enum Direction
{
	UP		('h'),
	DOWN	('h'),
	LEFT	('w'),
	RIGHT	('w'),
	BACK	('d'),
	FORWARD	('d');
	
	private final char axis;
	
	private Direction(char axis)
	{
		this.axis = axis;
	}
	
	public char getAxis()
	{
		return axis;
	}
	
	/**
	 * @return true if an axis is given multiple times
	 */
	public static boolean checkForSameAxis(Direction...directions)
	{
		Set<Character> axis = new HashSet<>();
		
		for(Direction d : directions)
		{
			if(!axis.add(d.getAxis()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static Direction lookup(char dir)
	{
		switch(dir)
		{
		case 'u': return UP;
		case 'd': return DOWN;
		case 'l': return LEFT;
		case 'r': return RIGHT;
		case 'b': return BACK;
		case 'f': return FORWARD;
		}
		
		return null;
	}
}
