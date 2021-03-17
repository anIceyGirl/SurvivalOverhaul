package icey.survivaloverhaul.api.injury;

import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Generic condition that can be used to represent injuries and diseases. <br>
 * These are similar to potion effects; however, they are unable to be cured with milk,
 * and they exclusively effect players.
 * @author Icey
 */
public class PlayerCondition extends ForgeRegistryEntry<PlayerCondition>
{
	@Nullable
	private String name;
	
	private boolean isFatal;
	
	public PlayerCondition(boolean isFatal)
	{
		this.isFatal = isFatal;
	}
	
	/**
	 * @return Whether or not this condition is fatal to a player without treatment.
	 */
	public boolean isFatal()
	{
		return this.isFatal;
	}
	
	protected String getOrCreateTranslationKey()
	{
		if (this.name == null) 
			this.name = Util.makeTranslationKey("player_condition", this.getRegistryName());
		
		return this.name;
	}

	public String getName()
	{
		return this.getOrCreateTranslationKey();
	}
	
	public ITextComponent getDisplayName() 
	{
		return new TranslationTextComponent(this.getName());
	}
	
	public boolean isProgressable()
	{
		return this instanceof IProgressableCondition;
	}
	
	public void initApply(PlayerEntity player)
	{
		
	}
	
	public void applyEffect(PlayerEntity player)
	{
		
	}
	
	/**
	 * Called when a condition expires.
	 * @param player The player this condition was effecting.
	 */
	public void onExpire(PlayerEntity player)
	{
		
	}
	
	/**
	 * 
	 * @param compound The NBT compound to be read from.
	 */
	public void fromNBT(CompoundNBT compound) 
	{
		
	}
	
	/**
	 * 
	 * @return Returns a compound NBT tag that represents this condition.
	 */
	public CompoundNBT toNBT()
	{
		return new CompoundNBT();
	}
	
	@Override
	public String toString()
	{
		return this.getRegistryName().toString();
	}
}
