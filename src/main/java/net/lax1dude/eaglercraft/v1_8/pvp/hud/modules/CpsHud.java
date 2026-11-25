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
	private String cachedText = "";
	private int cachedWidth;
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
		int enabled = (config.cpsLeft ? 1 : 0) + (config.cpsRight ? 1 : 0) + (config.cpsCombined ? 1 : 0);
		if(enabled == 0) {
			enabled = 1;
		}
		return Math.max(cachedWidth, enabled * 42) + style.padding * 2;
	}

	@Override
	public int getHeight(PvpContext context) {
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		return 9 + style.padding * 2;
	}

	@Override
	public void render(PvpContext context, int x, int y) {
		refresh(context);
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		HudElementConfig config = context.config.getElement(getId());
		HudRenderer.drawPanel(context, getId(), x, y, getWidth(context), getHeight(context));
		StringBuilder text = new StringBuilder();
		boolean first = true;
		if(config.cpsLeft) {
			appendCpsPart(text, first, config.cpsLabels ? "L " : "", cachedLeftCps);
			first = false;
		}
		if(config.cpsRight) {
			appendCpsPart(text, first, config.cpsLabels ? "R " : "", cachedRightCps);
			first = false;
		}
		if(config.cpsCombined) {
			appendCpsPart(text, first, config.cpsLabels ? "C " : "", cachedLeftCps + cachedRightCps);
		}
		if(config.cpsPeak) {
			text.append(config.cpsLabels ? "  Peak " : "  ").append(Math.max(peakLeft, peakRight));
		}
		if(text.length() == 0) {
			text.append("CPS ").append(cachedLeftCps);
		}
		HudRenderer.drawText(context, getId(), minecraft, text.toString(), x + style.padding, y + style.padding,
				Math.max(1, getWidth(context) - style.padding * 2));
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
			cachedText = "CPS: L " + leftCps + " R " + rightCps;
			cachedWidth = minecraft.fontRendererObj.getStringWidth(cachedText) + 4;
		}
		if(leftCps > peakLeft) peakLeft = leftCps;
		if(rightCps > peakRight) peakRight = rightCps;
	}

	private void appendCpsPart(StringBuilder text, boolean first, String label, int value) {
		if(!first) {
			text.append("  ");
		}
		text.append(label).append(value);
	}

}
