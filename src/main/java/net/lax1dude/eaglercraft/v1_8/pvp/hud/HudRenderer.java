package net.lax1dude.eaglercraft.v1_8.pvp.hud;

import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.GL_QUADS;
import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.GL_TRIANGLE_FAN;
import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.GL_TRIANGLE_STRIP;
import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.GL_SRC_ALPHA;
import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.GL_ONE_MINUS_SRC_ALPHA;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.opengl.WorldRenderer;
import net.lax1dude.eaglercraft.v1_8.pvp.PvpUiFontRenderer;
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
	private static final int CORNER_SEGMENTS = 4;
	private static final int CORNER_POINTS = (CORNER_SEGMENTS + 1) * 4;
	private static final float[] CORNER_COS = new float[CORNER_POINTS];
	private static final float[] CORNER_SIN = new float[CORNER_POINTS];

	static {
		int index = 0;
		for(int corner = 0; corner < 4; ++corner) {
			float start = 180.0f + corner * 90.0f;
			for(int segment = 0; segment <= CORNER_SEGMENTS; ++segment) {
				double radians = Math.toRadians(start + segment * 90.0f / CORNER_SEGMENTS);
				CORNER_COS[index] = (float) Math.cos(radians);
				CORNER_SIN[index] = (float) Math.sin(radians);
				++index;
			}
		}
	}

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
		drawPanel(resolveAppearance(context, elementId), x, y, width, height);
	}

	public static void drawPanel(HudAppearanceConfig style, int x, int y, int width, int height) {
		if(style == null || width <= 0 || height <= 0) {
			return;
		}
		if(!style.backgroundEnabled && !style.borderEnabled) {
			return;
		}
		if(style.panelShadow) {
			int shadowAlpha = (int) (90.0f * style.shadowIntensity);
			drawRoundedRect(x + 1, y + 2, width, height, style.cornerRadius,
					((shadowAlpha & 255) << 24));
		}
		if(style.backgroundEnabled) {
			int start = withAlpha(style.gradientStart, style.backgroundOpacity);
			int end = style.gradientEnabled
					? withAlpha(style.gradientEnd, style.backgroundOpacity)
					: start;
			drawRoundedGradientRect(x, y, width, height, style.cornerRadius, start, end,
					style.gradientEnabled
							&& style.gradientDirection == HudAppearanceConfig.GradientDirection.HORIZONTAL);
		}
		if(style.borderEnabled && style.borderThickness > 0) {
			int border = withAlpha(style.borderColor, style.borderOpacity);
			drawRoundedBorder(x, y, width, height, style.cornerRadius, style.borderThickness, border);
		}
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	public static void drawRoundedRect(int x, int y, int width, int height, int radius, int color) {
		drawRoundedGradientRect(x, y, width, height, radius, color, color, false);
	}

	public static void drawRoundedGradientRect(int x, int y, int width, int height, int radius,
			int startColor, int endColor, boolean horizontal) {
		if(width <= 0 || height <= 0) {
			return;
		}
		int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
		if(r == 0) {
			if(startColor == endColor) {
				Gui.drawRect(x, y, x + width, y + height, startColor);
			}else {
				drawGradient(x, y, x + width, y + height, startColor, endColor, horizontal);
			}
			return;
		}
		beginShape();
		try {
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer renderer = tessellator.getWorldRenderer();
			renderer.begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
			float centerX = x + width * 0.5f;
			float centerY = y + height * 0.5f;
			int centerColor = gradientColor(startColor, endColor, 0.5f);
			putVertex(renderer, centerX, centerY, centerColor);
			for(int i = 0; i <= CORNER_POINTS; ++i) {
				int point = i % CORNER_POINTS;
				int corner = point / (CORNER_SEGMENTS + 1);
				float cornerX = corner == 0 || corner == 3 ? x + r : x + width - r;
				float cornerY = corner < 2 ? y + r : y + height - r;
				float px = cornerX + CORNER_COS[point] * r;
				float py = cornerY + CORNER_SIN[point] * r;
				float fraction = horizontal ? (px - x) / width : (py - y) / height;
				putVertex(renderer, px, py, gradientColor(startColor, endColor, fraction));
			}
			tessellator.draw();
		}finally {
			endShape();
		}
	}

	public static void drawRoundedBorder(int x, int y, int width, int height, int radius,
			int thickness, int color) {
		if(width <= 0 || height <= 0 || thickness <= 0) {
			return;
		}
		int outerRadius = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
		int innerWidth = width - thickness * 2;
		int innerHeight = height - thickness * 2;
		if(innerWidth <= 0 || innerHeight <= 0) {
			drawRoundedRect(x, y, width, height, outerRadius, color);
			return;
		}
		int innerRadius = Math.max(0, outerRadius - thickness);
		beginShape();
		try {
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer renderer = tessellator.getWorldRenderer();
			renderer.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
			for(int i = 0; i <= CORNER_POINTS; ++i) {
				int point = i % CORNER_POINTS;
				int corner = point / (CORNER_SEGMENTS + 1);
				float outerX = corner == 0 || corner == 3 ? x + outerRadius : x + width - outerRadius;
				float outerY = corner < 2 ? y + outerRadius : y + height - outerRadius;
				float innerX = corner == 0 || corner == 3
						? x + thickness + innerRadius : x + width - thickness - innerRadius;
				float innerY = corner < 2
						? y + thickness + innerRadius : y + height - thickness - innerRadius;
				putVertex(renderer, outerX + CORNER_COS[point] * outerRadius,
						outerY + CORNER_SIN[point] * outerRadius, color);
				putVertex(renderer, innerX + CORNER_COS[point] * innerRadius,
						innerY + CORNER_SIN[point] * innerRadius, color);
			}
			tessellator.draw();
		}finally {
			endShape();
		}
	}

	private static void beginShape() {
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	private static void endShape() {
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	private static void putVertex(WorldRenderer renderer, float x, float y, int color) {
		renderer.pos(x, y, 0.0d)
				.color((color >> 16 & 255) / 255.0f, (color >> 8 & 255) / 255.0f,
						(color & 255) / 255.0f, (color >>> 24) / 255.0f)
				.endVertex();
	}

	private static int gradientColor(int start, int end, float fraction) {
		float t = Math.max(0.0f, Math.min(1.0f, fraction));
		int a = Math.round((start >>> 24) * (1.0f - t) + (end >>> 24) * t);
		int r = Math.round((start >> 16 & 255) * (1.0f - t) + (end >> 16 & 255) * t);
		int g = Math.round((start >> 8 & 255) * (1.0f - t) + (end >> 8 & 255) * t);
		int b = Math.round((start & 255) * (1.0f - t) + (end & 255) * t);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public static void drawText(PvpContext context, String elementId, Minecraft minecraft,
			String text, int x, int y, int width) {
		HudAppearanceConfig style = resolveAppearance(context, elementId);
		drawTextColor(context, elementId, minecraft, text, x, y, width, style.textColor);
	}

	public static void drawTextColor(PvpContext context, String elementId, Minecraft minecraft,
			String text, int x, int y, int width, int color) {
		if(text == null || text.isEmpty() || width <= 0) {
			return;
		}
		HudAppearanceConfig style = resolveAppearance(context, elementId);
		net.minecraft.client.gui.FontRenderer font = PvpUiFontRenderer.get(minecraft);
		int availableWidth = Math.max(0, width);
		int measuredWidth = font.getStringWidth(text);
		if(measuredWidth > availableWidth) {
			String suffix = availableWidth >= font.getStringWidth("...") ? "..." : "";
			int textLimit = Math.max(0, availableWidth - font.getStringWidth(suffix));
			text = font.trimStringToWidth(text, textLimit) + suffix;
		}
		int textX = x;
		int textWidth = font.getStringWidth(text);
		if(style.textAlignment == HudAppearanceConfig.TextAlignment.CENTER) {
			textX = x + (width - textWidth) / 2;
		}else if(style.textAlignment == HudAppearanceConfig.TextAlignment.RIGHT) {
			textX = x + width - textWidth;
		}
		color = withAlpha(style.rainbowEnabled ? rainbowColor(style, text) : color, style.textOpacity);
		if(style.textShadow) {
			font.drawStringWithShadow(text, textX, y, color);
		}else {
			font.drawString(text, textX, y, color);
		}
	}

	public static void drawLabel(PvpContext context, String elementId, Minecraft minecraft,
			String text, int x, int y) {
		HudAppearanceConfig style = resolveAppearance(context, elementId);
		PvpUiFontRenderer.get(minecraft).drawString(text, x, y,
				withAlpha(style.accentColor, Math.min(1.0f, style.textOpacity)));
	}

	public static void drawAccent(int x, int y, int height, HudAppearanceConfig style) {
		if(height > 0) {
			Gui.drawRect(x, y, x + 2, y + height, withAlpha(style.accentColor, style.textOpacity));
		}
	}

	public static void drawStatusIndicator(int x, int y, int color, boolean active) {
		int alpha = active ? 255 : 105;
		drawRoundedRect(x, y, 6, 6, 3, withAlpha(color, alpha / 255.0f));
		if(active) {
			drawRoundedRect(x + 1, y + 1, 4, 4, 2, withAlpha(color, 0.4f));
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
		drawRoundedRect(x, y, width, height, Math.min(height / 2, 2), 0xA60A1018);
		int filled = Math.round(width * progress);
		if(filled > 0) {
			drawRoundedRect(x, y, filled, height, Math.min(height / 2, 2), color);
			drawRoundedRect(x, y, filled, 1, 0, 0x66FFFFFF);
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
		try {
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
		}finally {
			GlStateManager.shadeModel(7424);
			GlStateManager.enableAlpha();
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}
}
