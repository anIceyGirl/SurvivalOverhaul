package icey.survivaloverhaul.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.MainMenuScreen;

@Mixin(MainMenuScreen.class)
public class TestMixin
{
	@Inject(at = @At("HEAD"), method = "init()V")
	private void init(CallbackInfo info)
	{
		// System.out.println("damn this is a nice mixin.");
	}
}
