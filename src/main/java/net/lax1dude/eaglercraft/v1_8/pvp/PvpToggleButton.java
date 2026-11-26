package net.lax1dude.eaglercraft.v1_8.pvp;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public final class PvpToggleButton extends GuiButton {

	private boolean enabledState;

	public PvpToggleButton(int id, int x, int y, int width, int height, boolean enabled) {
		super(id, x, y, width, height, "");
		this.enabledState = enabled;
	}

	public void setEnabledState(boolean enabled) {
		this.enabledState = enabled;
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
		if(!visible) {
			return;
		}
		hovered = mouseX >= xPosition && mouseY >= yPosition
				&& mouseX < xPosition + width && mouseY < yPosition + height;
		HudAppearanceConfig appearance = null;
		PvpClient client = PvpClient.getInstance();
		if(client != null) {
			appearance = client.getConfigStore().getConfig().appearance;
		}
		int radius = appearance == null ? Math.min(height / 2, 9) : Math.min(height / 2,
				appearance.cornerRadius + 3);
		int track = enabledState ? 0xFF53D6A3 : 0xFF334150;
		HudRenderer.drawRoundedRect(xPosition, yPosition + 3, width, height - 6, radius, track);
		int knobX = enabledState ? xPosition + width - height + 3 : xPosition + 3;
		HudRenderer.drawRoundedRect(knobX, yPosition + 3, height - 6, height - 6,
				Math.max(1, height / 2 - 3),
				hovered ? 0xFFFFFFFF : 0xFFE4EDF4);
	}
}
