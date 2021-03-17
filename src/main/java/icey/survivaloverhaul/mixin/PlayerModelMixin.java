package icey.survivaloverhaul.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin extends BipedModel<LivingEntity>
{
	public PlayerModelMixin(float modelSize)
	{
		super(modelSize);
	}
	
	@Inject(
			method = "setRotationAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
			at = {
					@At(shift = At.Shift.AFTER, value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/model/BipedModel;setRotationAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V")
			}
	)
	public void setRotationAngles(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYawn, float headPitch, CallbackInfo info)
	{
		
	}
}
