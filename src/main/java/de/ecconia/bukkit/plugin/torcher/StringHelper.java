package de.ecconia.bukkit.plugin.torcher;

public class StringHelper
{
	public static boolean partOf(String part, String... strings)
	{
		part = part.toLowerCase();
		for (String string : strings)
		{
			if (part.length() <= string.length())
			{
				string = string.toLowerCase();
				if (part.equals(string.substring(0, part.length()))) { return true; }
			}
		}
		return false;
	}
}
