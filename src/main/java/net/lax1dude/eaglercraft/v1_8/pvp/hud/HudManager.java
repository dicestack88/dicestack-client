package net.lax1dude.eaglercraft.v1_8.pvp.hud;

import java.util.ArrayList;
import java.util.List;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.pvp.PvpRuntime;
import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.modules.CoordinatesHud;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.modules.CpsHud;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.modules.FpsHud;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.modules.KeystrokesHud;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.modules.TextHud;
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
	private final List<HudModuleInfo> modules = new ArrayList<>();
	private final PvpContext context;

	public HudManager(PvpConfig config, Minecraft minecraft) {
		this.config = config;
		this.minecraft = minecraft;
		this.context = new PvpContext(minecraft, null, 0.0f, null);
		this.context.config = config;
		register(new FpsHud(minecraft), "FPS Counter", HudModuleInfo.Category.CORE);
		register(new CoordinatesHud(minecraft), "Coordinates", HudModuleInfo.Category.CORE);
		register(new CpsHud(minecraft), "CPS Counter", HudModuleInfo.Category.PVP);
		register(new KeystrokesHud(minecraft), "Keystrokes", HudModuleInfo.Category.CORE);
		register(new TextHud(minecraft, "compass", TextHud.Type.COMPASS), "Compass / Direction",
				HudModuleInfo.Category.CORE);
		register(new TextHud(minecraft, "speed", TextHud.Type.SPEED), "Speedometer", HudModuleInfo.Category.CORE);
		register(new TextHud(minecraft, "biome", TextHud.Type.BIOME), "Biome Display", HudModuleInfo.Category.WORLD);
		register(new TextHud(minecraft, "world_clock", TextHud.Type.WORLD_CLOCK), "In-game Clock",
				HudModuleInfo.Category.WORLD);
		register(new TextHud(minecraft, "session_time", TextHud.Type.SESSION_TIME), "Session Playtime",
				HudModuleInfo.Category.CORE);
		register(new TextHud(minecraft, "health", TextHud.Type.HEALTH), "Health Display",
				HudModuleInfo.Category.PLAYER);
		register(new TextHud(minecraft, "hunger", TextHud.Type.HUNGER), "Hunger Display",
				HudModuleInfo.Category.PLAYER);
		register(new TextHud(minecraft, "armor", TextHud.Type.ARMOR), "Armor Status",
				HudModuleInfo.Category.PLAYER);
		register(new TextHud(minecraft, "armor_durability", TextHud.Type.ARMOR_DURABILITY), "Armor Durability",
				HudModuleInfo.Category.PLAYER);
		register(new TextHud(minecraft, "held_item", TextHud.Type.HELD_ITEM), "Held Item Display",
				HudModuleInfo.Category.PLAYER);
		register(new TextHud(minecraft, "held_durability", TextHud.Type.HELD_DURABILITY), "Held Item Durability",
				HudModuleInfo.Category.PLAYER);
		register(new TextHud(minecraft, "server_address", TextHud.Type.SERVER_ADDRESS), "Server Address",
				HudModuleInfo.Category.WORLD);
		register(new TextHud(minecraft, "game_mode", TextHud.Type.GAME_MODE), "Current Game Mode",
				HudModuleInfo.Category.WORLD);
		register(new TextHud(minecraft, "potion_effects", TextHud.Type.POTION_EFFECTS), "Potion Effects",
				HudModuleInfo.Category.PLAYER);
		register(new TextHud(minecraft, "saturation", TextHud.Type.SATURATION), "Saturation",
				HudModuleInfo.Category.PLAYER);
		register(new TextHud(minecraft, "arrow_count", TextHud.Type.ARROW_COUNT), "Arrow Counter",
				HudModuleInfo.Category.PVP);
		register(new TextHud(minecraft, "sprint_status", TextHud.Type.SPRINT_STATUS), "Sprint Status",
				HudModuleInfo.Category.CORE);
		register(new TextHud(minecraft, "sneak_status", TextHud.Type.SNEAK_STATUS), "Sneak Status",
				HudModuleInfo.Category.CORE);
		register(new TextHud(minecraft, "target", TextHud.Type.TARGET), "Target HUD",
				HudModuleInfo.Category.PVP);
		register(new TextHud(minecraft, "block_info", TextHud.Type.BLOCK_INFO), "Block Information",
				HudModuleInfo.Category.WORLD);
		register(new TextHud(minecraft, "day_counter", TextHud.Type.DAY_COUNTER), "Day Counter",
				HudModuleInfo.Category.WORLD);
		register(new TextHud(minecraft, "light_level", TextHud.Type.LIGHT_LEVEL), "Light Level",
				HudModuleInfo.Category.WORLD);
		register(new TextHud(minecraft, "low_health", TextHud.Type.LOW_HEALTH), "Low Health Warning",
				HudModuleInfo.Category.PLAYER);
	}

	private void register(HudElement element, String name, HudModuleInfo.Category category) {
		modules.add(new HudModuleInfo(element, name, category));
	}

	public void render(ScaledResolution resolution, float partialTicks, PvpRuntime runtime) {
		context.update(minecraft, resolution, partialTicks, runtime);
		for(int i = 0, l = modules.size(); i < l; ++i) {
			HudElement element = modules.get(i).element;
			if(config.getElement(element.getId()).enabled) {
				renderElement(element, resolution);
			}
		}
	}

	public void renderEditor(ScaledResolution resolution, float partialTicks, PvpRuntime runtime) {
		context.update(minecraft, resolution, partialTicks, runtime);
		for(int i = 0, l = modules.size(); i < l; ++i) {
			renderElement(modules.get(i).element, resolution);
		}
	}

	public int getElementCount() {
		return modules.size();
	}

	public HudElement getElement(int index) {
		return modules.get(index).element;
	}

	public HudModuleInfo getModuleInfo(int index) {
		return modules.get(index);
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
		if(config.automaticLayout) {
			HudElementConfig.Anchor anchor = elementConfig.anchor;
			if(anchor == HudElementConfig.Anchor.TOP_LEFT
					|| anchor == HudElementConfig.Anchor.CENTER_LEFT
					|| anchor == HudElementConfig.Anchor.BOTTOM_LEFT) {
				return config.layoutSafeMargin;
			}
			if(anchor == HudElementConfig.Anchor.TOP_RIGHT
					|| anchor == HudElementConfig.Anchor.CENTER_RIGHT
					|| anchor == HudElementConfig.Anchor.BOTTOM_RIGHT) {
				return screenWidth - elementWidth - config.layoutSafeMargin;
			}
		}
		return clamp(resolveX(elementConfig, screenWidth, elementWidth), 0, screenWidth - elementWidth);
	}

	public int getElementY(HudElement element, ScaledResolution resolution) {
		HudElementConfig elementConfig = getElementConfig(element);
		int elementHeight = getElementHeight(element);
		int screenHeight = resolution.getScaledHeight();
		if(config.automaticLayout) {
			HudElementConfig.Anchor anchor = elementConfig.anchor;
			if(anchor == HudElementConfig.Anchor.TOP_LEFT || anchor == HudElementConfig.Anchor.TOP_RIGHT) {
				int y = config.layoutSafeMargin;
				for(int i = 0; i < modules.size(); ++i) {
					HudElement other = modules.get(i).element;
					HudElementConfig otherConfig = getElementConfig(other);
					if(other == element) break;
					if(otherConfig.enabled && (otherConfig.anchor == HudElementConfig.Anchor.TOP_LEFT
							&& anchor == HudElementConfig.Anchor.TOP_LEFT
							|| otherConfig.anchor == HudElementConfig.Anchor.TOP_RIGHT
							&& anchor == HudElementConfig.Anchor.TOP_RIGHT)) {
						y += getElementHeight(other) + config.layoutSpacing;
					}
				}
				return clamp(y, config.layoutSafeMargin, screenHeight - elementHeight);
			}
			if(anchor == HudElementConfig.Anchor.BOTTOM_LEFT || anchor == HudElementConfig.Anchor.BOTTOM_RIGHT) {
				int y = screenHeight - elementHeight - config.layoutSafeMargin;
				for(int i = modules.size() - 1; i >= 0; --i) {
					HudElement other = modules.get(i).element;
					HudElementConfig otherConfig = getElementConfig(other);
					if(other == element) break;
					if(otherConfig.enabled && (otherConfig.anchor == HudElementConfig.Anchor.BOTTOM_LEFT
							&& anchor == HudElementConfig.Anchor.BOTTOM_LEFT
							|| otherConfig.anchor == HudElementConfig.Anchor.BOTTOM_RIGHT
							&& anchor == HudElementConfig.Anchor.BOTTOM_RIGHT)) {
						y -= getElementHeight(other) + config.layoutSpacing;
					}
				}
				return clamp(y, 0, screenHeight - elementHeight - config.layoutSafeMargin);
			}
		}
		return clamp(resolveY(elementConfig, screenHeight, elementHeight), 0, screenHeight - elementHeight);
	}

	public HudElement getElementAt(int x, int y, ScaledResolution resolution) {
		for(int i = modules.size() - 1; i >= 0; --i) {
			HudElement element = modules.get(i).element;
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
