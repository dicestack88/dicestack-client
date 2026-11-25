package net.lax1dude.eaglercraft.v1_8.pvp.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElementConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;

public final class PvpConfig {

	public static final int CONFIG_VERSION = 3;

	public boolean enabled = true;
	public boolean hudEnabled = true;
	public final Map<String, HudElementConfig> elements = new LinkedHashMap<>();
	private int loadedVersion;
	private boolean needsMigration;
	public final HudAppearanceConfig appearance = HudAppearanceConfig.defaults();
	public boolean automaticLayout = false;
	public int layoutSpacing = 4;
	public int layoutSafeMargin = 4;

	public PvpConfig() {
		elements.put("fps", new HudElementConfig(true, HudElementConfig.Anchor.TOP_LEFT, 5, 5, 1.0f));
		elements.put("coordinates", new HudElementConfig(true, HudElementConfig.Anchor.TOP_LEFT, 5, 20, 1.0f));
		elements.put("cps", new HudElementConfig(false, HudElementConfig.Anchor.TOP_LEFT, 5, 35, 1.0f));
		elements.put("keystrokes", new HudElementConfig(false, HudElementConfig.Anchor.TOP_LEFT, 5, 50, 1.0f));
		elements.put("compass", new HudElementConfig(false, HudElementConfig.Anchor.TOP_LEFT, 5, 98, 1.0f));
		elements.put("speed", new HudElementConfig(false, HudElementConfig.Anchor.TOP_LEFT, 5, 113, 1.0f));
		elements.put("biome", new HudElementConfig(false, HudElementConfig.Anchor.TOP_LEFT, 5, 128, 1.0f));
		elements.put("world_clock", new HudElementConfig(false, HudElementConfig.Anchor.TOP_LEFT, 5, 143, 1.0f));
		elements.put("session_time", new HudElementConfig(false, HudElementConfig.Anchor.TOP_LEFT, 5, 158, 1.0f));
		elements.put("health", new HudElementConfig(false, HudElementConfig.Anchor.TOP_RIGHT, 5, 5, 1.0f));
		elements.put("hunger", new HudElementConfig(false, HudElementConfig.Anchor.TOP_RIGHT, 5, 20, 1.0f));
		elements.put("armor", new HudElementConfig(false, HudElementConfig.Anchor.TOP_RIGHT, 5, 35, 1.0f));
		elements.put("armor_durability", new HudElementConfig(false, HudElementConfig.Anchor.TOP_RIGHT, 5, 50, 1.0f));
		elements.put("held_item", new HudElementConfig(false, HudElementConfig.Anchor.TOP_RIGHT, 5, 65, 1.0f));
		elements.put("held_durability", new HudElementConfig(false, HudElementConfig.Anchor.TOP_RIGHT, 5, 80, 1.0f));
		elements.put("server_address", new HudElementConfig(false, HudElementConfig.Anchor.BOTTOM_LEFT, 5, 5, 1.0f));
		elements.put("game_mode", new HudElementConfig(false, HudElementConfig.Anchor.BOTTOM_LEFT, 5, 20, 1.0f));
	}

	public HudElementConfig getElement(String id) {
		HudElementConfig config = elements.get(id);
		if(config == null) {
			config = new HudElementConfig();
			elements.put(id, config);
		}
		return config;
	}

	public void resetElement(String id) {
		PvpConfig defaults = new PvpConfig();
		copyElement(getElement(id), defaults.getElement(id));
	}

	public void resetElements() {
		PvpConfig defaults = new PvpConfig();
		for(Map.Entry<String, HudElementConfig> entry : elements.entrySet()) {
			copyElement(entry.getValue(), defaults.getElement(entry.getKey()));
		}
	}

	private void copyElement(HudElementConfig destination, HudElementConfig source) {
		destination.enabled = source.enabled;
		destination.anchor = source.anchor;
		destination.offsetX = source.offsetX;
		destination.offsetY = source.offsetY;
		destination.scale = source.scale;
		destination.appearanceOverride = source.appearanceOverride;
		copyAppearance(destination.appearance, source.appearance);
		destination.keystrokesWASD = source.keystrokesWASD;
		destination.keystrokesSpace = source.keystrokesSpace;
		destination.keystrokesShift = source.keystrokesShift;
		destination.keystrokesMouse = source.keystrokesMouse;
		destination.keystrokesLabels = source.keystrokesLabels;
		destination.keystrokesCPS = source.keystrokesCPS;
		destination.keystrokesKeySize = source.keystrokesKeySize;
		destination.keystrokesSpacing = source.keystrokesSpacing;
		destination.keystrokesPadding = source.keystrokesPadding;
		destination.keystrokesPressedColor = source.keystrokesPressedColor;
		destination.keystrokesUnpressedColor = source.keystrokesUnpressedColor;
		destination.cpsLeft = source.cpsLeft;
		destination.cpsRight = source.cpsRight;
		destination.cpsCombined = source.cpsCombined;
		destination.cpsLabels = source.cpsLabels;
		destination.cpsPeak = source.cpsPeak;
		destination.cpsDisplayDuration = source.cpsDisplayDuration;
	}

	public int getLoadedVersion() {
		return loadedVersion;
	}

	public boolean needsMigration() {
		return needsMigration;
	}

	public void markSaved() {
		loadedVersion = CONFIG_VERSION;
		needsMigration = false;
	}

	public static PvpConfig fromJson(String json) {
		PvpConfig config = new PvpConfig();
		JSONObject root = new JSONObject(json);
		config.loadedVersion = root.optInt("version", 0);
		config.needsMigration = config.loadedVersion != CONFIG_VERSION;
		if(config.loadedVersion <= 0) {
			config.migrateLegacyRoot(root);
			return config;
		}

		config.enabled = root.optBoolean("enabled", true);
		config.hudEnabled = root.optBoolean("hudEnabled", true);
		JSONObject elements = root.optJSONObject("elements");
		if(elements != null) {
			for(Map.Entry<String, HudElementConfig> entry : config.elements.entrySet()) {
				JSONObject element = elements.optJSONObject(entry.getKey());
				if(element != null) {
					entry.setValue(HudElementConfig.fromJson(element, entry.getValue()));
				}
			}
		}
		JSONObject appearance = root.optJSONObject("appearance");
		if(appearance != null) {
			HudAppearanceConfig defaults = HudAppearanceConfig.defaults();
			HudAppearanceConfig parsed = HudAppearanceConfig.fromJson(appearance, defaults);
			copyAppearance(config.appearance, parsed);
		}
		config.automaticLayout = root.optBoolean("automaticLayout", false);
		config.layoutSpacing = Math.max(0, Math.min(32, root.optInt("layoutSpacing", 4)));
		config.layoutSafeMargin = Math.max(0, Math.min(32, root.optInt("layoutSafeMargin", 4)));
		return config;
	}

	private void migrateLegacyRoot(JSONObject root) {
		enabled = root.optBoolean("enabled", true);
		hudEnabled = root.optBoolean("hudEnabled", true);
		JSONObject legacyElements = root.optJSONObject("elements");
		if(legacyElements != null) {
			for(Map.Entry<String, HudElementConfig> entry : elements.entrySet()) {
				JSONObject element = legacyElements.optJSONObject(entry.getKey());
				if(element != null) {
					entry.setValue(HudElementConfig.fromJson(element, entry.getValue()));
				}
			}
		}
	}

	public JSONObject toJson() {
		JSONObject root = new JSONObject();
		root.put("version", CONFIG_VERSION);
		root.put("enabled", enabled);
		root.put("hudEnabled", hudEnabled);
		root.put("appearance", appearance.toJson());
		root.put("automaticLayout", automaticLayout);
		root.put("layoutSpacing", layoutSpacing);
		root.put("layoutSafeMargin", layoutSafeMargin);
		JSONObject elementsObject = new JSONObject();
		for(Map.Entry<String, HudElementConfig> entry : elements.entrySet()) {
			elementsObject.put(entry.getKey(), entry.getValue().toJson());
		}
		root.put("elements", elementsObject);
		return root;
	}

	private static void copyAppearance(HudAppearanceConfig destination, HudAppearanceConfig source) {
		JSONObject json = source.toJson();
		HudAppearanceConfig copied = HudAppearanceConfig.fromJson(json, destination);
		destination.preset = copied.preset;
		destination.backgroundEnabled = copied.backgroundEnabled;
		destination.gradientEnabled = copied.gradientEnabled;
		destination.gradientDirection = copied.gradientDirection;
		destination.gradientStart = copied.gradientStart;
		destination.gradientEnd = copied.gradientEnd;
		destination.backgroundOpacity = copied.backgroundOpacity;
		destination.borderEnabled = copied.borderEnabled;
		destination.borderThickness = copied.borderThickness;
		destination.borderColor = copied.borderColor;
		destination.borderOpacity = copied.borderOpacity;
		destination.textColor = copied.textColor;
		destination.textOpacity = copied.textOpacity;
		destination.accentColor = copied.accentColor;
		destination.textShadow = copied.textShadow;
		destination.panelShadow = copied.panelShadow;
		destination.shadowIntensity = copied.shadowIntensity;
		destination.padding = copied.padding;
		destination.spacing = copied.spacing;
		destination.cornerRadius = copied.cornerRadius;
		destination.textScale = copied.textScale;
		destination.textAlignment = copied.textAlignment;
		destination.rainbowEnabled = copied.rainbowEnabled;
	}

}
