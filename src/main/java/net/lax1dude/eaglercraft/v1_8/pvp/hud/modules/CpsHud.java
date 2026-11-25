package net.lax1dude.eaglercraft.v1_8.pvp.hud.modules;

import net.lax1dude.eaglercraft.v1_8.pvp.PvpRuntime;
import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.PvpContext;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElementConfig;
import net.minecraft.client.Minecraft;

public final class CpsHud implements HudElement {

	private final Minecraft minecraft;
	private int cachedLeftCps = -1;
	private int cachedRightCps = -1;
	private String cachedLeftValue = "0";
	private String cachedRightValue = "0";
	private String cachedCombinedValue = "0";
	private String cachedPeakValue = "0";
	private int cachedPeak = -1;
	private int peakLeft;
	private int peakRight;
	private long peakResetAt;

	public CpsHud(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public String getId() {
		return "cps";
	}

	@Override
	public int getWidth(PvpContext context) {
		refresh(context);
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		HudElementConfig config = context.config.getElement(getId());
		int enabled = getEnabledCellCount(config);
		return enabled * 38 + (config.cpsPeak ? 38 : 0) + style.padding * 2;
	}

	@Override
	public int getHeight(PvpContext context) {
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		return (context.config.getElement(getId()).cpsPeak ? 30 : 20) + style.padding * 2;
	}

	@Override
	public void render(PvpContext context, int x, int y) {
		refresh(context);
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		HudElementConfig config = context.config.getElement(getId());
		HudRenderer.drawPanel(context, getId(), x, y, getWidth(context), getHeight(context));
		int cell = 0;
		int cellX = x + style.padding + 2;
		if(config.cpsLeft) {
			drawCell(context, style, cellX + cell++ * 38, y + style.padding,
					config.cpsLabels ? "LEFT" : "L", cachedLeftValue, 0xFFFF7777);
		}
		if(config.cpsRight) {
			drawCell(context, style, cellX + cell++ * 38, y + style.padding,
					config.cpsLabels ? "RIGHT" : "R", cachedRightValue, 0xFF70A7FF);
		}
		if(config.cpsCombined) {
			drawCell(context, style, cellX + cell++ * 38, y + style.padding,
					config.cpsLabels ? "TOTAL" : "C", cachedCombinedValue, style.accentColor);
		}
		if(config.cpsPeak) {
			drawCell(context, style, cellX + cell * 38, y + style.padding,
					"PEAK", cachedPeakValue, 0xFFFFD166);
		}
		if(!config.cpsLeft && !config.cpsRight && !config.cpsCombined && !config.cpsPeak) {
			drawCell(context, style, cellX, y + style.padding, "CPS", cachedLeftValue, style.accentColor);
		}
	}

	private int getEnabledCellCount(HudElementConfig config) {
		int enabled = (config.cpsLeft ? 1 : 0) + (config.cpsRight ? 1 : 0) + (config.cpsCombined ? 1 : 0);
		return enabled == 0 ? 1 : enabled;
	}

	private void drawCell(PvpContext context, HudAppearanceConfig style, int x, int y,
			String label, String value, int color) {
		HudRenderer.drawStatusIndicator(x, y + 1, color, true);
		HudRenderer.drawLabel(context, getId(), minecraft, label, x + 8, y);
		HudRenderer.drawTextColor(context, getId(), minecraft, value, x + 8, y + 9, 28, color);
	}

	private void refresh(PvpContext context) {
		PvpRuntime runtime = context.runtime;
		int leftCps = runtime != null ? runtime.getLeftCps() : 0;
		int rightCps = runtime != null ? runtime.getRightCps() : 0;
		HudElementConfig config = context.config.getElement(getId());
		long now = EagRuntime.steadyTimeMillis();
		if(peakResetAt == 0L) {
			peakResetAt = now + config.cpsDisplayDuration;
		}else if(now >= peakResetAt) {
			peakLeft = 0;
			peakRight = 0;
			peakResetAt = now + config.cpsDisplayDuration;
		}
		if(leftCps != cachedLeftCps || rightCps != cachedRightCps) {
			cachedLeftCps = leftCps;
			cachedRightCps = rightCps;
			cachedLeftValue = Integer.toString(leftCps);
			cachedRightValue = Integer.toString(rightCps);
			cachedCombinedValue = Integer.toString(leftCps + rightCps);
		}
		if(leftCps > peakLeft) peakLeft = leftCps;
		if(rightCps > peakRight) peakRight = rightCps;
		int peak = Math.max(peakLeft, peakRight);
		if(peak != cachedPeak) {
			cachedPeak = peak;
			cachedPeakValue = Integer.toString(peak);
		}
	}

}
