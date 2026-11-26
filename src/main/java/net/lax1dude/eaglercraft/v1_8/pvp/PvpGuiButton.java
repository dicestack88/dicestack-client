package net.lax1dude.eaglercraft.v1_8.pvp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.FontRenderer;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;

public class PvpGuiButton extends GuiButton {

	private String cachedLabelSource;
	private String cachedVisibleLabel = "";
	private int cachedLabelLimit = -1;

	public PvpGuiButton(int buttonId, int x, int y, int width, int height, String text) {
		super(buttonId, x, y, width, height, text);
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
		if(!visible) {
			return;
		}
		hovered = mouseX >= xPosition && mouseY >= yPosition
				&& mouseX < xPosition + width && mouseY < yPosition + height;
		int accent = 0xFF53D6A3;
		int radius = 3;
		int borderColor = 0x883A4A5C;
		PvpClient client = PvpClient.getInstance();
		if(client != null) {
			HudAppearanceConfig appearance = client.getConfigStore().getConfig().appearance;
			accent = appearance.accentColor;
			radius = appearance.cornerRadius;
			borderColor = HudRenderer.withAlpha(appearance.borderColor, appearance.borderOpacity);
		}
		int fill = !enabled ? 0xAA151D28 : hovered ? 0xFF304257 : 0xE51E2B3A;
		HudRenderer.drawRoundedRect(xPosition, yPosition, width, height, radius, fill);
		HudRenderer.drawRoundedBorder(xPosition, yPosition, width, height, radius, 1,
				!enabled ? 0x66556677 : enabled && hovered ? accent : borderColor);
		drawRect(xPosition + Math.min(radius, width / 2), yPosition,
				xPosition + width - Math.min(radius, width / 2), yPosition + 1,
				enabled ? accent : 0xFF566575);
		int textColor = !enabled ? 0xFF84909D : hovered ? 0xFFFFFFFF : 0xFFE0E8EF;
		drawButtonLabel(PvpUiFontRenderer.get(minecraft), getButtonLabel(), textColor);
	}

	protected String getButtonLabel() {
		return displayString;
	}

	protected void drawButtonLabel(FontRenderer font, String label, int color) {
		int availableWidth = Math.max(0, width - 10);
		if(label == null) {
			label = "";
		}
		if(!label.equals(cachedLabelSource) || cachedLabelLimit != availableWidth) {
			cachedLabelSource = label;
			cachedLabelLimit = availableWidth;
			if(font.getStringWidth(label) <= availableWidth) {
				cachedVisibleLabel = label;
			}else {
				String suffix = availableWidth >= font.getStringWidth("...") ? "..." : "";
				int textLimit = Math.max(0, availableWidth - font.getStringWidth(suffix));
				cachedVisibleLabel = font.trimStringToWidth(label, textLimit) + suffix;
			}
		}
		drawCenteredString(font, cachedVisibleLabel,
				xPosition + width / 2, yPosition + (height - 8) / 2, color);
	}
}
