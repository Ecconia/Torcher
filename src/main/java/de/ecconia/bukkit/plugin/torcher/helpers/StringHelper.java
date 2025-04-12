package de.ecconia.bukkit.plugin.torcher.helpers;

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
				if (part.equals(string.substring(0, part.length())))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean startsWithIgnoreCase(String text, String prefix)
	{
		// Could be a bit more efficient when performing toLowerCase() per letter. But for small prefix values this should not be too relevant.
		// In that case one could also limit the prefix to ASCII.
		return text.length() > prefix.length() && text.substring(0, prefix.length()).toLowerCase().equals(prefix);
	}
}
