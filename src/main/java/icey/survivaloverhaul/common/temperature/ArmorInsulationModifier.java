package icey.survivaloverhaul.common.temperature;

import java.util.List;

import icey.survivaloverhaul.api.config.json.temperature.JsonArmorIdentity;
import icey.survivaloverhaul.api.temperature.ModifierBase;
import icey.survivaloverhaul.api.temperature.TemperatureEnum;
import icey.survivaloverhaul.api.temperature.TemperatureUtil;
import icey.survivaloverhaul.config.json.JsonConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

public class ArmorInsulationModifier extends ModifierBase
{
	public ArmorInsulationModifier()
	{
		super();
	}
	
	@Override
	public float getPlayerInfluence(PlayerEntity player)
	{
		int worldTemperature = TemperatureUtil.getWorldTemperature(player.level, player.blockPosition());
		int diff = TemperatureEnum.NORMAL.getMiddle() - worldTemperature;
		
		diff *= checkArmorSlot(player.getItemBySlot(EquipmentSlotType.HEAD));
		diff *= checkArmorSlot(player.getItemBySlot(EquipmentSlotType.CHEST));
		diff *= checkArmorSlot(player.getItemBySlot(EquipmentSlotType.LEGS));
		diff *= checkArmorSlot(player.getItemBySlot(EquipmentSlotType.FEET));
		
		return -diff;
	}
	
	private float checkArmorSlot(ItemStack stack)
	{
		if (stack.isEmpty())
				return 1.0f;
		
		return (float) Math.sqrt(Math.sqrt(processStackJson(stack)));
	}
	
	private float processStackJson(ItemStack stack)
	{
		List<JsonArmorIdentity> identity = JsonConfig.armorTemperatures.get(stack.getItem().getRegistryName().toString());
		
		if (identity != null)
		{
			for (JsonArmorIdentity jtm : identity)
			{
				if (jtm == null)
						continue;
				
				if (jtm.matches(stack))
				{
					return jtm.insulation;
				}
			}
		}
		
		return 0.0f;
	}
}
