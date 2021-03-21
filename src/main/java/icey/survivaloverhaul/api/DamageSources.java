package icey.survivaloverhaul.api;

import icey.survivaloverhaul.Main;
import net.minecraft.util.DamageSource;

public class DamageSources
{
	public static final DamageSource ELECTROCUTION = new DamageSource(Main.MOD_ID + ".electrocution").bypassArmor().bypassMagic();
	public static final DamageSource HYPOTHERMIA = new DamageSource(Main.MOD_ID + ".hypothermia").bypassArmor();
	public static final DamageSource HYPERTHERMIA = new DamageSource(Main.MOD_ID + ".hyperthermia").bypassArmor();
}
