package icey.survivaloverhaul.config.json;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import icey.survivaloverhaul.Main;
import icey.survivaloverhaul.api.config.json.JsonItemIdentity;
import icey.survivaloverhaul.api.config.json.JsonPropertyTemperature;
import icey.survivaloverhaul.api.config.json.JsonPropertyValue;
import icey.survivaloverhaul.api.config.json.JsonTemperature;
import icey.survivaloverhaul.api.config.json.JsonTemperatureIdentity;
import icey.survivaloverhaul.config.JsonFileName;
import icey.survivaloverhaul.config.JsonTypeToken;
import net.minecraft.util.ResourceLocation;

public class TemperatureConfig
{
	public static final JsonItemIdentity DEFAULT_IDENTITY = new JsonItemIdentity(null);
	
	public static void init(File configDir)
	{
		JsonConfig.registerBlockTemperature("minecraft:campfire", 7.5f, new JsonPropertyValue("lit", "true"));
		JsonConfig.registerBlockTemperature("minecraft:soul_campfire", 5.0f, new JsonPropertyValue("lit", "true"));
		JsonConfig.registerBlockTemperature("minecraft:campfire", 0.0f, new JsonPropertyValue("lit", "false"));
		JsonConfig.registerBlockTemperature("minecraft:soul_campfire", 0.0f, new JsonPropertyValue("lit", "false"));
		
		JsonConfig.registerBlockTemperature("minecraft:torch", 1.5f);
		JsonConfig.registerBlockTemperature("minecraft:soul_torch", 0.75f);
		
		JsonConfig.registerBlockTemperature("minecraft:fire", 5.0f);
		JsonConfig.registerBlockTemperature("minecraft:soul_fire", 2.5f);
		
		JsonConfig.registerBlockTemperature("minecraft:furnace", 5.0f, new JsonPropertyValue("lit", "true"));
		JsonConfig.registerBlockTemperature("minecraft:blast_furnace", 5.0f, new JsonPropertyValue("lit", "true"));
		JsonConfig.registerBlockTemperature("minecraft:smoker", 5.0f, new JsonPropertyValue("lit", "true"));
		
		JsonConfig.registerBlockTemperature("minecraft:furnace", 0.0f, new JsonPropertyValue("lit", "false"));
		JsonConfig.registerBlockTemperature("minecraft:blast_furnace", 0.0f, new JsonPropertyValue("lit", "false"));
		JsonConfig.registerBlockTemperature("minecraft:smoker", 0.0f, new JsonPropertyValue("false", "true"));
		
		JsonConfig.registerBlockTemperature("minecraft:magma_block", 7.5f);
		
		JsonConfig.registerBlockTemperature("minecraft:jack_o_lantern", 3.0f);
		
		JsonConfig.registerFluidTemperature("minecraft:lava", 10.0f);
		JsonConfig.registerFluidTemperature("minecraft:flowing_lava", 10.0f);
		
		JsonConfig.registerArmorTemperature("survivaloverhaul:snow_feet", 0.5f);
		JsonConfig.registerArmorTemperature("survivaloverhaul:snow_legs", 2.5f);
		JsonConfig.registerArmorTemperature("survivaloverhaul:snow_chest", 3.0f);
		JsonConfig.registerArmorTemperature("survivaloverhaul:snow_head", 1.5f);
		
		JsonConfig.registerArmorTemperature("minecraft:leather_boots", 0.25f);
		JsonConfig.registerArmorTemperature("minecraft:leather_leggings", 0.75f);
		JsonConfig.registerArmorTemperature("minecraft:leather_chestplate", 1.0f);
		JsonConfig.registerArmorTemperature("minecraft:leather_helmet", 0.5f);
		
		processAllJson(configDir);
	}
	
	public static void clearContainers()
	{
		JsonConfig.armorTemperatures.clear();
		JsonConfig.blockTemperatures.clear();
		JsonConfig.fluidTemperatures.clear();
	}
	
	public static void processAllJson(File jsonDir)
	{
		Map<String, List<JsonTemperatureIdentity>> jsonArmorTemperatures = processJson(JsonFileName.ARMOR, JsonConfig.armorTemperatures, jsonDir, true);
		
		if (jsonArmorTemperatures != null)
		{
			for (Map.Entry<String, List<JsonTemperatureIdentity>> entry : jsonArmorTemperatures.entrySet())
			{
				for (JsonTemperatureIdentity jtm : entry.getValue())
				{
					if (jtm.identity != null)
							jtm.identity.tryPopulateCompound();
					
					JsonConfig.registerArmorTemperature(entry.getKey(), jtm.temperature, jtm.identity == null ? DEFAULT_IDENTITY : jtm.identity);
				}
			}
		}
		
		Map<String, List<JsonPropertyTemperature>> jsonBlockTemperatures = processJson(JsonFileName.BLOCK, JsonConfig.blockTemperatures, jsonDir, true);
		
		if (jsonBlockTemperatures != null)
		{
			for (Map.Entry<String, List<JsonPropertyTemperature>> entry : jsonBlockTemperatures.entrySet())
			{
				for (JsonPropertyTemperature propTemp : entry.getValue())
				{
					JsonConfig.registerBlockTemperature(entry.getKey(), propTemp.temperature, propTemp.getAsPropertyArray());
				}
			}
			
			try
			{
				manuallyWriteToJson(JsonFileName.BLOCK, JsonConfig.blockTemperatures, jsonDir);
			}
			catch (Exception e)
			{
				Main.LOGGER.error("Error writing merged JSON file", e);
			}
		}
		
		Map<String, JsonTemperature> jsonFluidTemperatures = processJson(JsonFileName.LIQUID, JsonConfig.fluidTemperatures, jsonDir, true);
		
		if (jsonFluidTemperatures != null)
		{
			for (Map.Entry<String, JsonTemperature> entry : jsonFluidTemperatures.entrySet())
			{
				JsonConfig.registerFluidTemperature(entry.getKey(), entry.getValue().temperature);
			}
			
			try
			{
				manuallyWriteToJson(JsonFileName.LIQUID, JsonConfig.fluidTemperatures, jsonDir);
			}
			catch (Exception e)
			{
				Main.LOGGER.error("Error writing merged JSON file", e);
			}
		}
	}
	
	@Nullable
	public static <T> T processJson(JsonFileName jfn, final T container, File jsonDir, boolean forMerging)
	{
		try
		{
			return processUncaughtJson(jfn, container, jsonDir, forMerging);
		}
		catch (Exception e)
		{
			Main.LOGGER.error("Error managing JSON file: " + jfn.get(), e);
			
			if (forMerging)
			{
				return null;
			}
			else
			{
				return container;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> T processUncaughtJson(JsonFileName jfn, final T container, File jsonDir, boolean forMerging) throws Exception
	{
		String jsonFileName = jfn.get();
		Type type = JsonTypeToken.get(jfn);
		
		File jsonFile = new File(jsonDir, jsonFileName);
		
		if (jsonFile.exists())
		{
			Gson gson = buildNewGson();
			
			return (T) gson.fromJson(new FileReader(jsonFile), type);
		}
		else
		{
			Gson gson = buildNewGson();
			
			FileUtils.write(jsonFile, gson.toJson(container, type), (String) null);
			
			if (forMerging)
			{
				return null;
			}
			else
			{
				return container;
			}
		}
	}
	
	private static <T> void manuallyWriteToJson(JsonFileName jfn, final T container, File jsonDir) throws Exception
	{
		String jsonFileName = jfn.get();
		Type type = JsonTypeToken.get(jfn);
		
		Gson gson = buildNewGson();
		File jsonFile = new File(jsonDir, jsonFileName);
		FileUtils.write(jsonFile, gson.toJson(container, type), (String) null);
	}
	
	private static Gson buildNewGson()
	{
		return new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.PRIVATE).create();
	}
}