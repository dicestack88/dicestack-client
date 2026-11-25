package net.lax1dude.eaglercraft.v1_8.pvp.hud.modules;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.PvpContext;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.minecraft.client.Minecraft;

public final class FpsHud implements HudElement {

	private final Minecraft minecraft;
	private int cachedFps = -1;
	private String cachedValue = "";
	private float animatedProgress;
	private boolean progressInitialized;

	public FpsHud(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public String getId() {
		return "fps";
	}

	@Override
	public int getWidth(PvpContext context) {
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		return 56 + style.padding * 2;
	}

	@Override
	public int getHeight(PvpContext context) {
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		return 23 + style.padding * 2;
	}

	@Override
	public void render(PvpContext context, int x, int y) {
		refresh(context);
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		HudRenderer.drawPanel(context, getId(), x, y, getWidth(context), getHeight(context));
		int left = x + style.padding + 4;
		HudRenderer.drawAccent(x, y + style.padding, getHeight(context) - style.padding * 2, style);
		HudRenderer.drawLabel(context, getId(), minecraft, "FPS", left, y + style.padding);
		HudRenderer.drawTextColor(context, getId(), minecraft, cachedValue, left, y + style.padding + 8,
				getWidth(context) - style.padding * 2 - 4, HudRenderer.getPerformanceColor(style, cachedFps));
		float target = Math.min(1.0f, cachedFps / 120.0f);
		if(!progressInitialized || !style.animationsEnabled) {
			animatedProgress = target;
			progressInitialized = true;
		}else {
			animatedProgress += (target - animatedProgress) * 0.22f;
		}
		HudRenderer.drawProgressBar(left, y + getHeight(context) - style.padding - 4,
				getWidth(context) - style.padding * 2 - 6, 2, animatedProgress,
				HudRenderer.getPerformanceColor(style, cachedFps));
	}

	private void refresh(PvpContext context) {
		int fps = Minecraft.getDebugFPS();
		if(fps != cachedFps) {
			cachedFps = fps;
			cachedValue = Integer.toString(fps);
		}
	}

}
