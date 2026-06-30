package dev.latvian.mods.globalkeybinds.core.mixin;

import dev.latvian.mods.globalkeybinds.GlobalKeybinds;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {
	@Redirect(method = "processOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options$FieldAccess;process(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
	private String vl$processOptionsForge(Options.FieldAccess fieldAccess, String key, String fallback) {
		var override = key.startsWith("key_") ? GlobalKeybinds.get(key.substring(4)) : null;
		return override == null ? fallback : override;
	}

	@Inject(method = "save", at = @At("HEAD"))
	private void vl$save(CallbackInfo ci) {
		GlobalKeybinds.saveKeybinds((Options) (Object) this);
	}
}