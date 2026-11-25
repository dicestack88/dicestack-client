package net.lax1dude.eaglercraft.v1_8.pvp.hud.modules;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.PvpContext;
import net.minecraft.client.Minecraft;

public final class KeystrokesHud implements HudElement {

	private static final String[] LABELS = { "W", "A", "S", "D", "SPACE", "LMB", "RMB" };
	private final Minecraft minecraft;
	private int activeKeys = -1;
	private int cachedWidth;

	public KeystrokesHud(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public String getId() {
		return "keystrokes";
	}

	@Override
	public int getWidth(PvpContext context) {
		refresh();
		return cachedWidth;
	}

	@Override
	public int getHeight(PvpContext context) {
		return 41;
	}

	@Override
	public void render(PvpContext context, int x, int y) {
		refresh();
		drawKey(LABELS[0], (activeKeys & 1) != 0, x + 20, y);
		drawKey(LABELS[1], (activeKeys & 2) != 0, x, y + 10);
		drawKey(LABELS[2], (activeKeys & 4) != 0, x + 20, y + 10);
		drawKey(LABELS[3], (activeKeys & 8) != 0, x + 40, y + 10);
		drawKey(LABELS[4], (activeKeys & 16) != 0, x + 20, y + 20);
		drawKey(LABELS[5], (activeKeys & 32) != 0, x, y + 30);
		drawKey(LABELS[6], (activeKeys & 64) != 0, x + 38, y + 30);
	}

	private void refresh() {
		int keys = 0;
		if(minecraft.gameSettings.keyBindForward.isKeyDown()) {
			keys |= 1;
		}
		if(minecraft.gameSettings.keyBindLeft.isKeyDown()) {
			keys |= 2;
		}
		if(minecraft.gameSettings.keyBindBack.isKeyDown()) {
			keys |= 4;
		}
		if(minecraft.gameSettings.keyBindRight.isKeyDown()) {
			keys |= 8;
		}
		if(minecraft.gameSettings.keyBindJump.isKeyDown()) {
			keys |= 16;
		}
		if(minecraft.gameSettings.keyBindAttack.isKeyDown()) {
			keys |= 32;
		}
		if(minecraft.gameSettings.keyBindUseItem.isKeyDown()) {
			keys |= 64;
		}
		if(keys != activeKeys) {
			activeKeys = keys;
			cachedWidth = Math.max(minecraft.fontRendererObj.getStringWidth(LABELS[4]) + 4, 58);
		}
	}

	private void drawKey(String label, boolean active, int x, int y) {
		int width = minecraft.fontRendererObj.getStringWidth(label) + 4;
		int color = active ? 0xFF55DD88 : 0xFFAAAAAA;
		minecraft.fontRendererObj.drawStringWithShadow(label, x + (width - minecraft.fontRendererObj.getStringWidth(label)) / 2,
				y + 1, color);
	}

}
