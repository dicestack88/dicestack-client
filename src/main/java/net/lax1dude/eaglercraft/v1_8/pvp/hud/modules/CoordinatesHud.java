package net.lax1dude.eaglercraft.v1_8.pvp.hud.modules;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.PvpContext;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig;
import net.minecraft.util.MathHelper;
import net.minecraft.client.Minecraft;

public final class CoordinatesHud implements HudElement {

	private final Minecraft minecraft;
	private int cachedX = Integer.MIN_VALUE;
	private int cachedY = Integer.MIN_VALUE;
	private int cachedZ = Integer.MIN_VALUE;
	private PvpConfig.CoordinateFormat cachedFormat;
	private String cachedText = "";
	private int cachedWidth;

	public CoordinatesHud(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public String getId() {
		return "coordinates";
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
		return 20 + style.padding * 2;
	}

	@Override
	public void render(PvpContext context, int x, int y) {
		if(minecraft.thePlayer != null) {
			refresh(context);
			HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
			HudRenderer.drawPanel(context, getId(), x, y, getWidth(context), getHeight(context));
			HudRenderer.drawAccent(x, y + style.padding, getHeight(context) - style.padding * 2, style);
			HudRenderer.drawLabel(context, getId(), minecraft, "COORDINATES", x + style.padding + 4,
					y + style.padding);
			HudRenderer.drawText(context, getId(), minecraft, cachedText, x + style.padding + 4,
					y + style.padding + 9, Math.max(1, getWidth(context) - style.padding * 2 - 4));
		}
	}

	private void refresh(PvpContext context) {
		if(minecraft.thePlayer == null) {
			cachedWidth = 0;
			cachedText = "";
			cachedX = Integer.MIN_VALUE;
			cachedY = Integer.MIN_VALUE;
			cachedZ = Integer.MIN_VALUE;
			cachedFormat = null;
			return;
		}
		int x = MathHelper.floor_double(minecraft.thePlayer.posX);
		int y = MathHelper.floor_double(minecraft.thePlayer.posY);
		int z = MathHelper.floor_double(minecraft.thePlayer.posZ);
		if(x != cachedX || y != cachedY || z != cachedZ || context.config.coordinateFormat != cachedFormat) {
			cachedX = x;
			cachedY = y;
			cachedZ = z;
			cachedFormat = context.config.coordinateFormat;
			cachedText = PvpConfig.formatCoordinates(cachedFormat, x, y, z);
			cachedWidth = minecraft.fontRendererObj.getStringWidth(cachedText) + 4;
		}
	}

}
