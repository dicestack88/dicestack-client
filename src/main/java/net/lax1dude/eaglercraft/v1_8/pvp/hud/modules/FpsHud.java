package net.lax1dude.eaglercraft.v1_8.pvp.hud.modules;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.PvpContext;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.minecraft.client.Minecraft;

public final class FpsHud implements HudElement {

	private final Minecraft minecraft;
	private int cachedFps = -1;
	private String cachedText = "";
	private int cachedWidth;

	public FpsHud(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public String getId() {
		return "fps";
	}

	@Override
	public int getWidth(PvpContext context) {
		refresh(context);
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		return cachedWidth + style.padding * 2 - 4;
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
		HudRenderer.drawPanel(context, getId(), x, y, getWidth(context), getHeight(context));
		HudRenderer.drawText(context, getId(), minecraft, cachedText, x + style.padding, y + style.padding,
				Math.max(1, getWidth(context) - style.padding * 2));
	}

	private void refresh(PvpContext context) {
		int fps = Minecraft.getDebugFPS();
		if(fps != cachedFps) {
			cachedFps = fps;
			cachedText = "FPS: " + fps;
			cachedWidth = minecraft.fontRendererObj.getStringWidth(cachedText) + 4;
		}
	}

}
