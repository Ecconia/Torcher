# Torcher Plugin
A Bukkit plugin which eats binary code from chat and inserts it into a simple redstone torch ROM.

Plugin is only compatible with Bukkit 1.13+.
Plugin requires WorldEdit (v7.0.0+)

Its required to use an additional program to compress binary into some unicode format which allows to send 3675 bits per chat line.

# How to use
Select a torch ROM with WorldEdit. Use "/torcher define" to declare that area as a ROM and to set the order in which bits will be inserted into it. Run "/tocher binary" to send compressed binary data into the ROM.

With "/torcher reset" you can reset the bit place position to the first position again.

Please read the instructions provided in the Torcher command, for more details.
