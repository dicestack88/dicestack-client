package net.lax1dude.eaglercraft.v1_8.pvp;

import net.lax1dude.eaglercraft.v1_8.minecraft.EaglerFontRenderer;
import net.lax1dude.eaglercraft.v1_8.opengl.EaglercraftGPU;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

public final class PvpUiFontRenderer {

	private static FontRenderer fontRenderer;

	private PvpUiFontRenderer() {
	}

	public static FontRenderer get(Minecraft minecraft) {
		if(fontRenderer == null) {
			try {
				String fontTexture = EaglercraftGPU.checkInstancingCapable()
						? "dicestack:textures/font/ui_sans.png"
						: "dicestack:textures/font/ui_sans_fallback.png";
				FontRenderer renderer = EaglerFontRenderer.createSupportedFontRenderer(minecraft.gameSettings,
						new ResourceLocation(fontTexture),
						minecraft.getTextureManager(), false);
				renderer.setUnicodeFlag(false);
				renderer.setBidiFlag(false);
				minecraft.registerResourceReloadListener(renderer);
				fontRenderer = renderer;
			}catch(Throwable ex) {
				fontRenderer = minecraft.fontRendererObj;
			}
		}
		return fontRenderer;
	}
}
