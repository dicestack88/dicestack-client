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
	private int cachedLeftCps = Integer.MIN_VALUE;
	private int cachedRightCps = Integer.MIN_VALUE;
	private String leftMouseLabel = "LMB";
	private String rightMouseLabel = "RMB";

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
		int width = config.keystrokesMouse ? size * 2 + spacing : 0;
		if(config.keystrokesWASD || config.keystrokesSpace || config.keystrokesShift) {
			width = Math.max(width, size * 3 + spacing * 2);
		}
		width = Math.max(width, size);
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
		refresh(context);
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
			int mouseRowWidth = size * 2 + spacing;
			int mouseX = x + pad + (width - pad * 2 - mouseRowWidth) / 2;
			drawKey(leftMouseLabel, (activeKeys & 32) != 0, mouseX, cursorY, size, size, config);
			drawKey(rightMouseLabel, (activeKeys & 64) != 0,
					mouseX + size + spacing, cursorY, size, size, config);
		}
	}

	private void refresh(PvpContext context) {
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
		HudElementConfig config = context.config.getElement(getId());
		if(config.keystrokesCPS && context.runtime != null) {
			int leftCps = context.runtime.getLeftCps();
			int rightCps = context.runtime.getRightCps();
			if(leftCps != cachedLeftCps) {
				cachedLeftCps = leftCps;
				leftMouseLabel = "L " + leftCps;
			}
			if(rightCps != cachedRightCps) {
				cachedRightCps = rightCps;
				rightMouseLabel = "R " + rightCps;
			}
		}else {
			cachedLeftCps = Integer.MIN_VALUE;
			cachedRightCps = Integer.MIN_VALUE;
			leftMouseLabel = "LMB";
			rightMouseLabel = "RMB";
		}
	}

	private void drawKey(String label, boolean active, int x, int y, int width, int height,
			HudElementConfig config) {
		int background = active ? config.keystrokesPressedColor : config.keystrokesUnpressedColor;
		Gui.drawRect(x, y + 2, x + width, y + height + 2, 0x88000000);
		Gui.drawRect(x, y, x + width, y + height,
				active ? HudRenderer.withAlpha(background, 1.0f) : background);
		Gui.drawRect(x, y, x + width, y + 1,
				active ? 0xFFFFFFFF : HudRenderer.withAlpha(config.appearance.accentColor, 0.72f));
		if(active) {
			Gui.drawRect(x, y + height - 2, x + width, y + height,
					HudRenderer.withAlpha(config.keystrokesPressedColor, 0.65f));
		}else {
			Gui.drawRect(x, y + height - 1, x + width, y + height, 0x55000000);
		}
		int textWidth = minecraft.fontRendererObj.getStringWidth(label);
		int textX = x + Math.max(1, (width - textWidth) / 2);
		int textY = y + Math.max(1, (height - 8) / 2) - (active ? 1 : 0);
		if(config.keystrokesLabels) {
			minecraft.fontRendererObj.drawStringWithShadow(label, textX, textY,
					active ? 0xFF102019 : 0xFFF1F7F5);
		}
	}
}
