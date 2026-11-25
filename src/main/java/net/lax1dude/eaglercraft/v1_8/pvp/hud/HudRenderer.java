package net.lax1dude.eaglercraft.v1_8.pvp.hud;

import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.GL_QUADS;
import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.GL_SRC_ALPHA;
import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.GL_ONE_MINUS_SRC_ALPHA;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.opengl.WorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public final class HudRenderer {

	private static final ResourceLocation POTION_ICONS = new ResourceLocation("textures/gui/container/inventory.png");

	private HudRenderer() {
	}

	public static HudAppearanceConfig resolveAppearance(PvpContext context, String elementId) {
		if(context == null || context.config == null) {
			return HudAppearanceConfig.defaults();
		}
		HudElementConfig element = context.config.getElement(elementId);
		return element.appearanceOverride ? element.appearance : context.config.appearance;
	}

	public static void drawPanel(PvpContext context, String elementId, int x, int y, int width, int height) {
		HudAppearanceConfig style = resolveAppearance(context, elementId);
		if(!style.backgroundEnabled && !style.borderEnabled) {
			return;
		}
		if(style.panelShadow) {
			int shadowAlpha = (int) (90.0f * style.shadowIntensity);
			Gui.drawRect(x + 1, y + 2, x + width + 1, y + height + 2,
					((shadowAlpha & 255) << 24));
		}
		if(style.backgroundEnabled) {
			int start = withAlpha(style.gradientStart, style.backgroundOpacity);
			int end = style.gradientEnabled
					? withAlpha(style.gradientEnd, style.backgroundOpacity)
					: start;
			if(style.gradientEnabled) {
				drawGradient(x, y, x + width, y + height, start, end,
						style.gradientDirection == HudAppearanceConfig.GradientDirection.HORIZONTAL);
			}else {
				Gui.drawRect(x, y, x + width, y + height, start);
			}
		}
		if(style.borderEnabled && style.borderThickness > 0) {
			int border = withAlpha(style.borderColor, style.borderOpacity);
			int thickness = style.borderThickness;
			Gui.drawRect(x, y, x + width, y + thickness, border);
			Gui.drawRect(x, y + height - thickness, x + width, y + height, border);
			Gui.drawRect(x, y, x + thickness, y + height, border);
			Gui.drawRect(x + width - thickness, y, x + width, y + height, border);
		}
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	public static void drawText(PvpContext context, String elementId, Minecraft minecraft,
			String text, int x, int y, int width) {
		HudAppearanceConfig style = resolveAppearance(context, elementId);
		drawTextColor(context, elementId, minecraft, text, x, y, width, style.textColor);
	}

	public static void drawTextColor(PvpContext context, String elementId, Minecraft minecraft,
			String text, int x, int y, int width, int color) {
		HudAppearanceConfig style = resolveAppearance(context, elementId);
		int textX = x;
		int textWidth = minecraft.fontRendererObj.getStringWidth(text);
		if(style.textAlignment == HudAppearanceConfig.TextAlignment.CENTER) {
			textX = x + (width - textWidth) / 2;
		}else if(style.textAlignment == HudAppearanceConfig.TextAlignment.RIGHT) {
			textX = x + width - textWidth;
		}
		color = withAlpha(style.rainbowEnabled ? rainbowColor(style, text) : color, style.textOpacity);
		if(style.textShadow) {
			minecraft.fontRendererObj.drawStringWithShadow(text, textX, y, color);
		}else {
			minecraft.fontRendererObj.drawString(text, textX, y, color);
		}
	}

	public static void drawLabel(PvpContext context, String elementId, Minecraft minecraft,
			String text, int x, int y) {
		HudAppearanceConfig style = resolveAppearance(context, elementId);
		minecraft.fontRendererObj.drawString(text, x, y,
				withAlpha(style.accentColor, Math.min(1.0f, style.textOpacity)));
	}

	public static void drawAccent(int x, int y, int height, HudAppearanceConfig style) {
		if(height > 0) {
			Gui.drawRect(x, y, x + 2, y + height, withAlpha(style.accentColor, style.textOpacity));
		}
	}

	public static void drawStatusIndicator(int x, int y, int color, boolean active) {
		int alpha = active ? 255 : 105;
		Gui.drawRect(x, y, x + 6, y + 6, withAlpha(color, alpha / 255.0f));
		if(active) {
			Gui.drawRect(x + 1, y + 1, x + 5, y + 5, withAlpha(color, 0.4f));
		}
	}

	public static void drawProgressBar(int x, int y, int width, int height, float progress, int color) {
		if(width <= 0 || height <= 0) {
			return;
		}
		if(Float.isNaN(progress) || Float.isInfinite(progress)) {
			progress = 0.0f;
		}
		progress = Math.max(0.0f, Math.min(1.0f, progress));
		Gui.drawRect(x, y, x + width, y + height, 0xA60A1018);
		int filled = Math.round(width * progress);
		if(filled > 0) {
			Gui.drawRect(x, y, x + filled, y + height, color);
			Gui.drawRect(x, y, x + filled, y + 1, 0x66FFFFFF);
		}
	}

	public static void drawItem(Minecraft minecraft, ItemStack stack, int x, int y) {
		if(stack == null || minecraft == null) {
			return;
		}
		RenderItem renderer = minecraft.getRenderItem();
		float oldZ = renderer.zLevel;
		GlStateManager.pushMatrix();
		try {
			GlStateManager.enableDepth();
			RenderHelper.enableGUIStandardItemLighting();
			renderer.zLevel = 150.0f;
			renderer.renderItemAndEffectIntoGUI(stack, x, y);
		}finally {
			renderer.zLevel = oldZ;
			RenderHelper.disableStandardItemLighting();
			GlStateManager.enableAlpha();
			GlStateManager.enableTexture2D();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.popMatrix();
		}
	}

	public static void drawPotionIcon(Minecraft minecraft, Potion potion, int x, int y) {
		if(minecraft == null || potion == null || !potion.hasStatusIcon()) {
			return;
		}
		int icon = potion.getStatusIconIndex();
		if(icon < 0) {
			return;
		}
		int iconX = icon % 8 * 18;
		int iconY = 198 + icon / 8 * 18;
		float u0 = iconX / 256.0f;
		float v0 = iconY / 256.0f;
		float u1 = (iconX + 18) / 256.0f;
		float v1 = (iconY + 18) / 256.0f;
		minecraft.getTextureManager().bindTexture(POTION_ICONS);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer renderer = tessellator.getWorldRenderer();
		renderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		renderer.pos(x, y + 18, 0.0d).tex(u0, v1).endVertex();
		renderer.pos(x + 18, y + 18, 0.0d).tex(u1, v1).endVertex();
		renderer.pos(x + 18, y, 0.0d).tex(u1, v0).endVertex();
		renderer.pos(x, y, 0.0d).tex(u0, v0).endVertex();
		tessellator.draw();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	public static int getPerformanceColor(HudAppearanceConfig style, int fps) {
		return fps >= 60 ? style.performanceGoodColor : fps >= 30
				? style.performanceMediumColor : style.performanceLowColor;
	}

	public static int withAlpha(int color, float opacity) {
		int alpha = Math.max(0, Math.min(255, (int) (opacity * 255.0f + 0.5f)));
		return (alpha << 24) | (color & 0x00FFFFFF);
	}

	private static int rainbowColor(HudAppearanceConfig style, String text) {
		if(!style.rainbowEnabled) {
			return style.textColor;
		}
		long time = System.currentTimeMillis();
		float hue = ((time / 30L) % 360L) / 360.0f;
		return hsvToRgb(hue, 0.72f, 1.0f);
	}

	private static int hsvToRgb(float hue, float saturation, float value) {
		float h = (hue - (float) Math.floor(hue)) * 6.0f;
		int sector = (int) h;
		float fraction = h - sector;
		float p = value * (1.0f - saturation);
		float q = value * (1.0f - saturation * fraction);
		float t = value * (1.0f - saturation * (1.0f - fraction));
		float r;
		float g;
		float b;
		switch(sector) {
		case 0: r = value; g = t; b = p; break;
		case 1: r = q; g = value; b = p; break;
		case 2: r = p; g = value; b = t; break;
		case 3: r = p; g = q; b = value; break;
		case 4: r = t; g = p; b = value; break;
		default: r = value; g = p; b = q; break;
		}
		int red = (int) (r * 255.0f + 0.5f);
		int green = (int) (g * 255.0f + 0.5f);
		int blue = (int) (b * 255.0f + 0.5f);
		return (red << 16) | (green << 8) | blue;
	}

	private static void drawGradient(int left, int top, int right, int bottom,
			int startColor, int endColor, boolean horizontal) {
		float startA = (startColor >> 24 & 255) / 255.0f;
		float startR = (startColor >> 16 & 255) / 255.0f;
		float startG = (startColor >> 8 & 255) / 255.0f;
		float startB = (startColor & 255) / 255.0f;
		float endA = (endColor >> 24 & 255) / 255.0f;
		float endR = (endColor >> 16 & 255) / 255.0f;
		float endG = (endColor >> 8 & 255) / 255.0f;
		float endB = (endColor & 255) / 255.0f;

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.shadeModel(7425);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer renderer = tessellator.getWorldRenderer();
		renderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		if(horizontal) {
			renderer.pos(right, top, 0.0d).color(endR, endG, endB, endA).endVertex();
			renderer.pos(left, top, 0.0d).color(startR, startG, startB, startA).endVertex();
			renderer.pos(left, bottom, 0.0d).color(startR, startG, startB, startA).endVertex();
			renderer.pos(right, bottom, 0.0d).color(endR, endG, endB, endA).endVertex();
		}else {
			renderer.pos(right, top, 0.0d).color(startR, startG, startB, startA).endVertex();
			renderer.pos(left, top, 0.0d).color(startR, startG, startB, startA).endVertex();
			renderer.pos(left, bottom, 0.0d).color(endR, endG, endB, endA).endVertex();
			renderer.pos(right, bottom, 0.0d).color(endR, endG, endB, endA).endVertex();
		}
		tessellator.draw();
		GlStateManager.shadeModel(7424);
		GlStateManager.enableAlpha();
	}
}
