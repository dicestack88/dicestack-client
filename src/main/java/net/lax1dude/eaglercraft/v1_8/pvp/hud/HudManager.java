package net.lax1dude.eaglercraft.v1_8.pvp.hud;

import java.util.ArrayList;
import java.util.List;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.pvp.PvpRuntime;
import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.modules.CoordinatesHud;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.modules.CpsHud;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.modules.FpsHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public final class HudManager {

	private static final int SNAP_DISTANCE = 6;
	private static final HudElementConfig.Anchor[][] ANCHORS = {
			{ HudElementConfig.Anchor.TOP_LEFT, HudElementConfig.Anchor.TOP_CENTER,
					HudElementConfig.Anchor.TOP_RIGHT },
			{ HudElementConfig.Anchor.CENTER_LEFT, HudElementConfig.Anchor.CENTER,
					HudElementConfig.Anchor.CENTER_RIGHT },
			{ HudElementConfig.Anchor.BOTTOM_LEFT, HudElementConfig.Anchor.BOTTOM_CENTER,
					HudElementConfig.Anchor.BOTTOM_RIGHT } };

	private final PvpConfig config;
	private final Minecraft minecraft;
	private final List<HudElement> orderedElements = new ArrayList<>();
	private final PvpContext context;

	public HudManager(PvpConfig config, Minecraft minecraft) {
		this.config = config;
		this.minecraft = minecraft;
		this.context = new PvpContext(minecraft, null, 0.0f, null);
		register(new FpsHud(minecraft));
		register(new CoordinatesHud(minecraft));
		register(new CpsHud(minecraft));
	}

	private void register(HudElement element) {
		orderedElements.add(element);
	}

	public void render(ScaledResolution resolution, float partialTicks, PvpRuntime runtime) {
		context.update(minecraft, resolution, partialTicks, runtime);
		for(int i = 0, l = orderedElements.size(); i < l; ++i) {
			HudElement element = orderedElements.get(i);
			if(config.getElement(element.getId()).enabled) {
				renderElement(element, resolution);
			}
		}
	}

	public void renderEditor(ScaledResolution resolution, float partialTicks, PvpRuntime runtime) {
		context.update(minecraft, resolution, partialTicks, runtime);
		for(int i = 0, l = orderedElements.size(); i < l; ++i) {
			renderElement(orderedElements.get(i), resolution);
		}
	}

	public int getElementCount() {
		return orderedElements.size();
	}

	public HudElement getElement(int index) {
		return orderedElements.get(index);
	}

	public HudElementConfig getElementConfig(HudElement element) {
		return config.getElement(element.getId());
	}

	public int getElementWidth(HudElement element) {
		return scaleDimension(element.getWidth(context), getElementConfig(element).scale);
	}

	public int getElementHeight(HudElement element) {
		return scaleDimension(element.getHeight(context), getElementConfig(element).scale);
	}

	public int getElementX(HudElement element, ScaledResolution resolution) {
		HudElementConfig elementConfig = getElementConfig(element);
		int elementWidth = getElementWidth(element);
		int screenWidth = resolution.getScaledWidth();
		return clamp(resolveX(elementConfig, screenWidth, elementWidth), 0, screenWidth - elementWidth);
	}

	public int getElementY(HudElement element, ScaledResolution resolution) {
		HudElementConfig elementConfig = getElementConfig(element);
		int elementHeight = getElementHeight(element);
		int screenHeight = resolution.getScaledHeight();
		return clamp(resolveY(elementConfig, screenHeight, elementHeight), 0, screenHeight - elementHeight);
	}

	public HudElement getElementAt(int x, int y, ScaledResolution resolution) {
		for(int i = orderedElements.size() - 1; i >= 0; --i) {
			HudElement element = orderedElements.get(i);
			int elementX = getElementX(element, resolution);
			int elementY = getElementY(element, resolution);
			int elementWidth = getElementWidth(element);
			int elementHeight = getElementHeight(element);
			if(x >= elementX && y >= elementY && x < elementX + elementWidth && y < elementY + elementHeight) {
				return element;
			}
		}
		return null;
	}

	public void moveElement(HudElement element, int x, int y, ScaledResolution resolution) {
		int screenWidth = resolution.getScaledWidth();
		int screenHeight = resolution.getScaledHeight();
		int elementWidth = getElementWidth(element);
		int elementHeight = getElementHeight(element);
		x = clamp(x, 0, screenWidth - elementWidth);
		y = clamp(y, 0, screenHeight - elementHeight);

		int column = 0;
		int centerX = (screenWidth - elementWidth) / 2;
		int rightX = screenWidth - elementWidth;
		int snapX = x;
		int snapDistanceX = Math.abs(x);
		if(Math.abs(x - centerX) < snapDistanceX) {
			snapDistanceX = Math.abs(x - centerX);
			snapX = centerX;
			column = 1;
		}
		if(Math.abs(x - rightX) < snapDistanceX) {
			snapDistanceX = Math.abs(x - rightX);
			snapX = rightX;
			column = 2;
		}
		if(snapDistanceX <= SNAP_DISTANCE) {
			x = snapX;
		}else {
			column = 0;
		}

		int row = 0;
		int centerY = (screenHeight - elementHeight) / 2;
		int bottomY = screenHeight - elementHeight;
		int snapY = y;
		int snapDistanceY = Math.abs(y);
		if(Math.abs(y - centerY) < snapDistanceY) {
			snapDistanceY = Math.abs(y - centerY);
			snapY = centerY;
			row = 1;
		}
		if(Math.abs(y - bottomY) < snapDistanceY) {
			snapDistanceY = Math.abs(y - bottomY);
			snapY = bottomY;
			row = 2;
		}
		if(snapDistanceY <= SNAP_DISTANCE) {
			y = snapY;
		}else {
			row = 0;
		}

		HudElementConfig elementConfig = getElementConfig(element);
		elementConfig.anchor = ANCHORS[row][column];
		elementConfig.offsetX = column == 0 ? x
				: column == 1 ? x - centerX : screenWidth - elementWidth - x;
		elementConfig.offsetY = row == 0 ? y
				: row == 1 ? y - centerY : screenHeight - elementHeight - y;
	}

	private void renderElement(HudElement element, ScaledResolution resolution) {
		HudElementConfig elementConfig = getElementConfig(element);
		int x = getElementX(element, resolution);
		int y = getElementY(element, resolution);

		GlStateManager.pushMatrix();
		try {
			GlStateManager.translate((float) x, (float) y, 0.0f);
			GlStateManager.scale(elementConfig.scale, elementConfig.scale, 1.0f);
			element.render(context, 0, 0);
		}finally {
			GlStateManager.popMatrix();
		}
	}

	private int resolveX(HudElementConfig config, int screenWidth, int width) {
		switch(config.anchor) {
		case TOP_CENTER:
		case CENTER:
		case BOTTOM_CENTER:
			return (screenWidth - width) / 2 + config.offsetX;
		case TOP_RIGHT:
		case CENTER_RIGHT:
		case BOTTOM_RIGHT:
			return screenWidth - width - config.offsetX;
		default:
			return config.offsetX;
		}
	}

	private int resolveY(HudElementConfig config, int screenHeight, int height) {
		switch(config.anchor) {
		case CENTER_LEFT:
		case CENTER:
		case CENTER_RIGHT:
			return (screenHeight - height) / 2 + config.offsetY;
		case BOTTOM_LEFT:
		case BOTTOM_CENTER:
		case BOTTOM_RIGHT:
			return screenHeight - height - config.offsetY;
		default:
			return config.offsetY;
		}
	}

	private int scaleDimension(int dimension, float scale) {
		return (int) (dimension * scale + 0.5f);
	}

	private int clamp(int value, int min, int max) {
		return max < min ? min : Math.max(min, Math.min(value, max));
	}

}
