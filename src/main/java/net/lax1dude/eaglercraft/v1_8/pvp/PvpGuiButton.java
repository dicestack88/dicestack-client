package net.lax1dude.eaglercraft.v1_8.pvp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class PvpGuiButton extends GuiButton {

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
		PvpClient client = PvpClient.getInstance();
		if(client != null) {
			accent = client.getConfigStore().getConfig().appearance.accentColor;
		}
		int fill = !enabled ? 0xAA151D28 : hovered ? 0xFF304257 : 0xE51E2B3A;
		drawRect(xPosition, yPosition, xPosition + width, yPosition + height, fill);
		drawRect(xPosition, yPosition, xPosition + width, yPosition + 1,
				enabled ? accent : 0xFF566575);
		drawRect(xPosition, yPosition + height - 1, xPosition + width, yPosition + height,
				enabled && hovered ? accent : 0x883A4A5C);
		drawRect(xPosition, yPosition, xPosition + 1, yPosition + height, 0xAA8191A2);
		drawRect(xPosition + width - 1, yPosition, xPosition + width, yPosition + height, 0xAA8191A2);
		int textColor = !enabled ? 0xFF84909D : hovered ? 0xFFFFFFFF : 0xFFE0E8EF;
		drawCenteredString(minecraft.fontRendererObj, displayString,
				xPosition + width / 2, yPosition + (height - 8) / 2, textColor);
	}
}
