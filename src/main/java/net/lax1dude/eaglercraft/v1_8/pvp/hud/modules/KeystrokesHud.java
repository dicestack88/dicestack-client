package net.lax1dude.eaglercraft.v1_8.pvp.hud.modules;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElementConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.PvpContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public final class KeystrokesHud implements HudElement {

	private final Minecraft minecraft;
	private int activeKeys = -1;

	public KeystrokesHud(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public String getId() {
		return "keystrokes";
	}

	@Override
	public int getWidth(PvpContext context) {
		HudElementConfig config = context.config.getElement(getId());
		int size = config.keystrokesKeySize;
		int spacing = config.keystrokesSpacing;
		int width = config.keystrokesMouse ? size * 2 + spacing : size * 3 + spacing * 2;
		if(config.keystrokesWASD) {
			width = Math.max(width, size * 3 + spacing * 2);
		}
		return width + config.keystrokesPadding * 2;
	}

	@Override
	public int getHeight(PvpContext context) {
		HudElementConfig config = context.config.getElement(getId());
		int size = config.keystrokesKeySize;
		int spacing = config.keystrokesSpacing;
		int rows = 0;
		if(config.keystrokesWASD) rows += 3;
		if(config.keystrokesSpace) rows += 1;
		if(config.keystrokesShift) rows += 1;
		if(config.keystrokesMouse) rows += 1;
		if(rows == 0) rows = 1;
		return rows * size + Math.max(0, rows - 1) * spacing + config.keystrokesPadding * 2;
	}

	@Override
	public void render(PvpContext context, int x, int y) {
		refresh();
		HudElementConfig config = context.config.getElement(getId());
		int width = getWidth(context);
		int height = getHeight(context);
		HudRenderer.drawPanel(context, getId(), x, y, width, height);
		int size = config.keystrokesKeySize;
		int spacing = config.keystrokesSpacing;
		int pad = config.keystrokesPadding;
		int cursorY = y + pad;
		if(config.keystrokesWASD) {
			int gridX = x + pad + (width - pad * 2 - size * 3 - spacing * 2) / 2;
			drawKey("W", (activeKeys & 1) != 0, gridX + size + spacing, cursorY, size, size, config);
			drawKey("A", (activeKeys & 2) != 0, gridX, cursorY + size + spacing, size, size, config);
			drawKey("S", (activeKeys & 4) != 0, gridX + size + spacing, cursorY + size + spacing, size, size, config);
			drawKey("D", (activeKeys & 8) != 0, gridX + (size + spacing) * 2, cursorY + size + spacing, size, size, config);
			cursorY += (size + spacing) * 2;
		}
		if(config.keystrokesSpace) {
			drawKey("SPACE", (activeKeys & 16) != 0, x + pad, cursorY,
					size * 3 + spacing * 2, size, config);
			cursorY += size + spacing;
		}
		if(config.keystrokesShift) {
			drawKey("SHIFT", (activeKeys & 128) != 0, x + pad, cursorY,
					size * 3 + spacing * 2, size, config);
			cursorY += size + spacing;
		}
		if(config.keystrokesMouse) {
			String leftLabel = config.keystrokesCPS && context.runtime != null
					? "L " + context.runtime.getLeftCps() : "LMB";
			String rightLabel = config.keystrokesCPS && context.runtime != null
					? "R " + context.runtime.getRightCps() : "RMB";
			drawKey(leftLabel, (activeKeys & 32) != 0, x + pad, cursorY,
					(size * 3 + spacing * 2 - spacing) / 2, size, config);
			drawKey(rightLabel, (activeKeys & 64) != 0, x + pad + (size * 3 + spacing * 2 + spacing) / 2, cursorY,
					(size * 3 + spacing * 2 - spacing) / 2, size, config);
		}
	}

	private void refresh() {
		int keys = 0;
		if(minecraft.gameSettings.keyBindForward.isKeyDown()) keys |= 1;
		if(minecraft.gameSettings.keyBindLeft.isKeyDown()) keys |= 2;
		if(minecraft.gameSettings.keyBindBack.isKeyDown()) keys |= 4;
		if(minecraft.gameSettings.keyBindRight.isKeyDown()) keys |= 8;
		if(minecraft.gameSettings.keyBindJump.isKeyDown()) keys |= 16;
		if(minecraft.gameSettings.keyBindAttack.isKeyDown()) keys |= 32;
		if(minecraft.gameSettings.keyBindUseItem.isKeyDown()) keys |= 64;
		if(minecraft.gameSettings.keyBindSneak.isKeyDown()) keys |= 128;
		activeKeys = keys;
	}

	private void drawKey(String label, boolean active, int x, int y, int width, int height,
			HudElementConfig config) {
		int background = active ? config.keystrokesPressedColor : config.keystrokesUnpressedColor;
		Gui.drawRect(x, y, x + width, y + height, background);
		Gui.drawRect(x, y, x + width, y + 1,
				active ? config.keystrokesPressedColor : HudRenderer.withAlpha(config.appearance.accentColor, 0.45f));
		int textWidth = minecraft.fontRendererObj.getStringWidth(label);
		int textX = x + Math.max(1, (width - textWidth) / 2);
		int textY = y + Math.max(1, (height - 8) / 2);
		if(config.keystrokesLabels) {
			minecraft.fontRendererObj.drawStringWithShadow(label, textX, textY,
					active ? 0xFF102019 : 0xFFF1F7F5);
		}
	}
}
