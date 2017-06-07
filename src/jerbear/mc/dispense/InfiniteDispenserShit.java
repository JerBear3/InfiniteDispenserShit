package jerbear.mc.dispense;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class InfiniteDispenserShit extends JavaPlugin implements Listener
{
	private Logger log;
	private File save = new File("plugins/InfiniteDispenserShit/dispensers.txt");;
	private ArrayList<Location> dispensers = new ArrayList<Location>();
	
	@Override
	public void onEnable()
	{
		log = getLogger();
		getServer().getPluginManager().registerEvents(this, this);
	
		if(save.exists())
		{
			try
			{
				Scanner scanner = new Scanner(save);
				while(scanner.hasNextLine())
				{
					World world = getServer().getWorld(UUID.fromString(scanner.next()));
					Location loc = new Location(world, scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat());
					Block block = loc.getBlock();
					
					if(block.getState() instanceof Dispenser)
						dispensers.add(loc);
					else
						log.warning("Discarded non-dispenser");
				}
				
				scanner.close();
			}
			catch(Exception oops)
			{
				log.log(Level.WARNING, "Couldn't load dispensers", oops);
			}
			
			log.info("Successfully loaded " + dispensers.size() + " dispensers");
			save();
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("dispenseshit"))
		{
			if(!(sender instanceof Player))
				return true;
			
			Player player = (Player) sender;
			Set<Material> ignore = null;
			Block block = player.getTargetBlock(ignore, 5);
			Location loc = block.getLocation();
			
			if(block.getState() instanceof Dispenser)
			{
				if(dispensers.contains(loc))
				{
					dispensers.remove(loc);
					sender.sendMessage("Infinite dispenser shit toggled OFF");
				}
				else
				{
					dispensers.add(loc);
					sender.sendMessage("Infinite dispenser shit toggled ON");
				}
				
				save();
			}
			else
			{
				sender.sendMessage("Not a dispenser");
			}
			
			return true;
		}
		
		return false;
	}
	
	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event)
	{
		if(event.getBlock().getState() instanceof Dispenser)
		{
			Dispenser dispenser = (Dispenser) event.getBlock().getState();
			if(dispensers.contains(dispenser.getLocation()))
			{
				getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
				{
					@Override
					public void run()
					{
						dispenser.getInventory().addItem(event.getItem());
					}
				});
			}
		}
		else
		{
			dispensers.remove(event.getBlock().getLocation());
			log.warning("Discarded non-dispenser");
			save();
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		dispensers.remove(event.getBlock().getLocation());
		save();
	}
	
	@Override
	public void onDisable()
	{
		save();
		dispensers.clear();
	}
	
	private void save()
	{
		try
		{
			new File(save.getParent()).mkdirs();
			save.createNewFile();
			
			PrintStream print = new PrintStream(save);
			for(int i = 0; i < dispensers.size(); i++)
			{
				Location loc2 = dispensers.get(i);
				String output = loc2.getWorld().getUID().toString() + " " + loc2.getBlockX() + " " + loc2.getBlockY() + " " +loc2.getBlockZ();
				
				if(i == dispensers.size() - 1)
					print.print(output);
				else
					print.println(output);
			}
			
			print.close();
		}
		catch(IOException oops)
		{
			log.log(Level.WARNING, "Couldn't save dispensers", oops);
		}
	}
}