package com.kxysl1k.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ExampleMixin {

	@Inject(method = "run", at = @At("HEAD"))
	private void onRun(CallbackInfo info) {
		System.out.println("[SecureJoinClientMod] MinecraftClient is starting!");
	}
}
