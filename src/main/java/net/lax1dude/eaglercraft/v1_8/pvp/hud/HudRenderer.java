package net.lax1dude.eaglercraft.v1_8.pvp.hud;

import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.GL_QUADS;
import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.GL_SRC_ALPHA;
import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.GL_ONE_MINUS_SRC_ALPHA;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.opengl.WorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.gui.Gui;

public final class HudRenderer {

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
	}

	public static void drawText(PvpContext context, String elementId, Minecraft minecraft,
			String text, int x, int y, int width) {
		HudAppearanceConfig style = resolveAppearance(context, elementId);
		int textX = x;
		int textWidth = minecraft.fontRendererObj.getStringWidth(text);
		if(style.textAlignment == HudAppearanceConfig.TextAlignment.CENTER) {
			textX = x + (width - textWidth) / 2;
		}else if(style.textAlignment == HudAppearanceConfig.TextAlignment.RIGHT) {
			textX = x + width - textWidth;
		}
		int color = withAlpha(rainbowColor(style, text) & 0x00FFFFFF, style.textOpacity);
		if(style.textShadow) {
			minecraft.fontRendererObj.drawStringWithShadow(text, textX, y, color);
		}else {
			minecraft.fontRendererObj.drawString(text, textX, y, color);
		}
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
