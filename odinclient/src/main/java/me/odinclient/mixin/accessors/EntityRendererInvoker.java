package me.odinclient.mixin.accessors;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererInvoker {
    @Invoker("setupCameraTransform")
    void invokeSetupCameraTransform(float partialTicks, int pass);
}