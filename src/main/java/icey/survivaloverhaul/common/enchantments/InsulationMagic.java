package icey.survivaloverhaul.common.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

public class InsulationMagic extends GenericMagic
{	
	private final static EquipmentSlotType[] slots = new EquipmentSlotType[] {EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};
	private MagicType magicType;
	/**
	 * @param name of enchant
	 */
	public InsulationMagic(MagicType magicType, Rarity rarity, EnchantOptions Options) 
	{
		super(rarity, EnchantmentType.ARMOR, slots, Options);
		this.magicType = magicType;
	}
	
	public MagicType getMagicType()
	{
		return this.magicType;
	}
	
	@Override
	public boolean isTreasureOnly()
	{
		if (this.magicType == MagicType.Both)
			return true;
		else
			return false;
	}
	
	public enum MagicType
	{
		Both(1),
		Heat(2),
		Cool(3);
		private int Type;
		
		public int getType() {
			return Type;
		}

		MagicType(int i) 
		{
			Type = i;
		}
	}
	
	@Override
	protected boolean checkCompatibility(Enchantment ench) 
	{
		if (ench instanceof InsulationMagic)
		{
			InsulationMagic magic = (InsulationMagic) ench;
			
			if (this.getMagicType() != magic.getMagicType())
			{
				return false;
			}
		}
		
		return true;
	}
}
