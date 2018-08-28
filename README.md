# Torcher Plugin
A Bukkit plugin which eats binary code from chat and inserts it into a simple redstone torch ROM.

A Bukkit plugin which is compatible with 1.12.2.

Its required to use an additional program to compress binary into some unicode format which allows to send 3675 bits per chat line.

When looking at the ROM at the side which the torches are attatched to, the plugin will place the blocks from the top right corner filling up that row. It continues to fill the current layer and does the same for the next layers.
External bit flipper are required to change this. (To be changed...)