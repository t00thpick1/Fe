package org.melonbrew.fe.command.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.melonbrew.fe.Fe;
import org.melonbrew.fe.Phrase;
import org.melonbrew.fe.command.CommandType;
import org.melonbrew.fe.command.SubCommand;
import org.melonbrew.fe.database.converter.Converter;
import org.melonbrew.fe.database.converter.Response;
import org.melonbrew.fe.database.converter.ResponseType;
import org.melonbrew.fe.database.converter.converters.Converter_iConomy;
import org.melonbrew.fe.database.databases.MySQLDB;

public class ConvertCommand extends SubCommand {
	private final Fe plugin;
	
	private final List<Converter> converters;
	
	public ConvertCommand(Fe plugin){
		super("convert", "fe.convert", "convert (plugin) (database)", Phrase.COMMAND_CONVERT, CommandType.CONSOLE);
		
		this.plugin = plugin;
		
		converters = new ArrayList<Converter>();
		
		converters.add(new Converter_iConomy());
	}
	
	private void sendConversionList(CommandSender sender){
		String message = plugin.getEqualMessage(Phrase.CONVERSION.parse(), 7);
		
		sender.sendMessage(message);
		
		for (Converter converter : converters){
			message = ChatColor.GOLD + converter.getName();
			
			message += " " + ChatColor.DARK_GRAY + "(" + ChatColor.YELLOW;
			
			if (converter.isFlatFile()){
				message += Phrase.FLAT_FILE.parse();
			}
			
			if (converter.isMySQL()){
				if (converter.isFlatFile()){
					message += ", ";
				}
				
				message += Phrase.MYSQL.parse();
			}
			
			message += ChatColor.DARK_GRAY + ")";
			
			sender.sendMessage(message);
		}
		
		sender.sendMessage(plugin.getEndEqualMessage(message.length()));
	}
	
	private Converter getConverter(String name){
		for (Converter converter : converters){
			if (converter.getName().equalsIgnoreCase(name)){
				return converter;
			}
		}
		
		return null;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (args.length < 1){
			sendConversionList(sender);
			
			return true;
		}
		
		if (args.length < 2){
			return false;
		}
		
		String type = args[1];
		
		if (!type.equalsIgnoreCase(Phrase.FLAT_FILE.parse().replace(" ", "")) || !type.equalsIgnoreCase(Phrase.MYSQL.parse().replace(" ", ""))){
			return false;
		}
		
		Converter converter = getConverter(args[0]);
		
		if (converter == null){
			sender.sendMessage(plugin.getMessagePrefix() + Phrase.CONVERTER_DOES_NOT_EXIST.parse());
			
			return true;
		}
		
		String supported = null;
		
		if (type.equalsIgnoreCase(Phrase.FLAT_FILE.parse()) && !converter.isFlatFile()){
			supported = Phrase.FLAT_FILE.parse();
		}else if (type.equalsIgnoreCase(Phrase.MYSQL.parse())){
			if (!converter.mySQLtoFlatFile() && !(plugin.getFeDatabase() instanceof MySQLDB)){
				sender.sendMessage(Phrase.CONVERTER_DOES_NOT_SUPPORT.parse(Phrase.MYSQL_TO_FLAT_FILE.parse()));
				
				return true;
			}
			
			if (!converter.isMySQL()){
				supported = Phrase.MYSQL.parse();
			}
		}
		
		if (supported != null){
			sender.sendMessage(plugin.getMessagePrefix() + Phrase.CONVERTER_DOES_NOT_SUPPORT.parse(supported));
			
			return true;
		}
		
		Response response;
		
		if (type.equalsIgnoreCase(Phrase.FLAT_FILE.parse())){
			response = converter.convertFlatFile(plugin);
		}else {
			response = converter.convertMySQL(plugin);
		}
		
		if (response.getType() == ResponseType.FAILED){
			sender.sendMessage(plugin.getMessagePrefix() + Phrase.CONVERSION_FAILED.parse());
		}
		
		return true;
	}
}