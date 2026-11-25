package net.lax1dude.eaglercraft.v1_8.pvp.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElementConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;

public final class PvpConfig {

	public static final int CONFIG_VERSION = 5;

	public enum CrosshairStyle {
		VANILLA,
		DOT,
		CIRCLE,
		CROSS,
		CIRCLE_DOT
	}

	public enum CoordinateFormat {
		LABELLED,
		COMMAS,
		SLASHES
	}

	public boolean enabled = true;
	public boolean hudEnabled = true;
	public final Map<String, HudElementConfig> elements = new LinkedHashMap<>();
	private int loadedVersion;
	private boolean needsMigration;
	public final HudAppearanceConfig appearance = HudAppearanceConfig.defaults();
	public boolean automaticLayout = false;
	public int layoutSpacing = 4;
	public int layoutSafeMargin = 4;
	public boolean toggleSprintEnabled = false;
	public boolean toggleSneakEnabled = false;
	public boolean zoomEnabled = true;
	public boolean zoomToggle = false;
	public float zoomStrength = 18.0f;
	public CoordinateFormat coordinateFormat = CoordinateFormat.LABELLED;
	public boolean clock24Hour = true;
	public boolean clockShowSeconds = false;
	public String lastServerName = "";
	public String lastServerAddress = "";
	public boolean noDynamicFov;
	public boolean noHurtTilt;
	public boolean noFog;
	public boolean hideWeather;
	public boolean hideVignette;
	public boolean hideFireOverlay;
	public boolean hidePumpkinOverlay;
	public boolean hidePortalOverlay;
	public boolean fullbright;
	public boolean disableEntityShadows;
	public boolean hideServerAddress;
	public boolean hideChat;
	public float lowHealthThreshold = 4.0f;
	public boolean crosshairEnabled;
	public CrosshairStyle crosshairStyle = CrosshairStyle.CROSS;
	public int crosshairSize = 5;
	public int crosshairThickness = 2;
	public int crosshairGap = 3;
	public int crosshairColor = 0xFFFFFFFF;
	public float crosshairOpacity = 1.0f;
	public boolean crosshairOutline = true;
	public boolean crosshairDynamicColor;
	public boolean crosshairHideInMenus = true;
	public boolean crosshairHideInThirdPerson = true;

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
		elements.put("potion_effects", new HudElementConfig(false, HudElementConfig.Anchor.TOP_RIGHT, 5, 95, 1.0f));
		elements.put("saturation", new HudElementConfig(false, HudElementConfig.Anchor.TOP_LEFT, 5, 173, 1.0f));
		elements.put("arrow_count", new HudElementConfig(false, HudElementConfig.Anchor.BOTTOM_RIGHT, 5, 5, 1.0f));
		elements.put("sprint_status", new HudElementConfig(false, HudElementConfig.Anchor.BOTTOM_LEFT, 5, 35, 1.0f));
		elements.put("sneak_status", new HudElementConfig(false, HudElementConfig.Anchor.BOTTOM_LEFT, 5, 50, 1.0f));
		elements.put("target", new HudElementConfig(false, HudElementConfig.Anchor.TOP_CENTER, 0, 5, 1.0f));
		elements.put("block_info", new HudElementConfig(false, HudElementConfig.Anchor.BOTTOM_CENTER, 0, 5, 1.0f));
		elements.put("day_counter", new HudElementConfig(false, HudElementConfig.Anchor.TOP_LEFT, 5, 188, 1.0f));
		elements.put("light_level", new HudElementConfig(false, HudElementConfig.Anchor.TOP_LEFT, 5, 203, 1.0f));
		elements.put("low_health", new HudElementConfig(false, HudElementConfig.Anchor.TOP_CENTER, 0, 20, 1.0f));
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
		config.toggleSprintEnabled = root.optBoolean("toggleSprintEnabled", false);
		config.toggleSneakEnabled = root.optBoolean("toggleSneakEnabled", false);
		config.zoomEnabled = root.optBoolean("zoomEnabled", true);
		config.zoomToggle = root.optBoolean("zoomToggle", false);
		config.zoomStrength = clampFloat(root.optDouble("zoomStrength", 18.0d), 4.0f, 32.0f, 18.0f);
		String coordinateFormat = root.optString("coordinateFormat", CoordinateFormat.LABELLED.name());
		try {
			config.coordinateFormat = CoordinateFormat.valueOf(coordinateFormat);
		}catch(RuntimeException ex) {
			config.coordinateFormat = CoordinateFormat.LABELLED;
		}
		config.clock24Hour = root.optBoolean("clock24Hour", true);
		config.clockShowSeconds = root.optBoolean("clockShowSeconds", false);
		config.lastServerName = root.optString("lastServerName", "");
		config.lastServerAddress = root.optString("lastServerAddress", "");
		config.noDynamicFov = root.optBoolean("noDynamicFov", false);
		config.noHurtTilt = root.optBoolean("noHurtTilt", false);
		config.noFog = root.optBoolean("noFog", false);
		config.hideWeather = root.optBoolean("hideWeather", false);
		config.hideVignette = root.optBoolean("hideVignette", false);
		config.hideFireOverlay = root.optBoolean("hideFireOverlay", false);
		config.hidePumpkinOverlay = root.optBoolean("hidePumpkinOverlay", false);
		config.hidePortalOverlay = root.optBoolean("hidePortalOverlay", false);
		config.fullbright = root.optBoolean("fullbright", false);
		config.disableEntityShadows = root.optBoolean("disableEntityShadows", false);
		config.hideServerAddress = root.optBoolean("hideServerAddress", false);
		config.hideChat = root.optBoolean("hideChat", false);
		config.lowHealthThreshold = clampFloat(root.optDouble("lowHealthThreshold", 4.0d), 1.0f, 20.0f, 4.0f);
		config.crosshairEnabled = root.optBoolean("crosshairEnabled", false);
		String crosshairStyle = root.optString("crosshairStyle", CrosshairStyle.CROSS.name());
		try {
			config.crosshairStyle = CrosshairStyle.valueOf(crosshairStyle);
		}catch(RuntimeException ex) {
			config.crosshairStyle = CrosshairStyle.CROSS;
		}
		config.crosshairSize = Math.max(1, Math.min(16, root.optInt("crosshairSize", 5)));
		config.crosshairThickness = Math.max(1, Math.min(6, root.optInt("crosshairThickness", 2)));
		config.crosshairGap = Math.max(0, Math.min(12, root.optInt("crosshairGap", 3)));
		config.crosshairColor = root.optInt("crosshairColor", 0xFFFFFFFF);
		config.crosshairOpacity = clampFloat(root.optDouble("crosshairOpacity", 1.0d), 0.1f, 1.0f, 1.0f);
		config.crosshairOutline = root.optBoolean("crosshairOutline", true);
		config.crosshairDynamicColor = root.optBoolean("crosshairDynamicColor", false);
		config.crosshairHideInMenus = root.optBoolean("crosshairHideInMenus", true);
		config.crosshairHideInThirdPerson = root.optBoolean("crosshairHideInThirdPerson", true);
		return config;
	}

	public static String formatCoordinates(CoordinateFormat format, int x, int y, int z) {
		if(format == CoordinateFormat.COMMAS) {
			return x + ", " + y + ", " + z;
		}else if(format == CoordinateFormat.SLASHES) {
			return x + " / " + y + " / " + z;
		}else {
			return "X: " + x + ", Y: " + y + ", Z: " + z;
		}
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
		root.put("toggleSprintEnabled", toggleSprintEnabled);
		root.put("toggleSneakEnabled", toggleSneakEnabled);
		root.put("zoomEnabled", zoomEnabled);
		root.put("zoomToggle", zoomToggle);
		root.put("zoomStrength", zoomStrength);
		root.put("coordinateFormat", coordinateFormat.name());
		root.put("clock24Hour", clock24Hour);
		root.put("clockShowSeconds", clockShowSeconds);
		root.put("lastServerName", lastServerName);
		root.put("lastServerAddress", lastServerAddress);
		root.put("noDynamicFov", noDynamicFov);
		root.put("noHurtTilt", noHurtTilt);
		root.put("noFog", noFog);
		root.put("hideWeather", hideWeather);
		root.put("hideVignette", hideVignette);
		root.put("hideFireOverlay", hideFireOverlay);
		root.put("hidePumpkinOverlay", hidePumpkinOverlay);
		root.put("hidePortalOverlay", hidePortalOverlay);
		root.put("fullbright", fullbright);
		root.put("disableEntityShadows", disableEntityShadows);
		root.put("hideServerAddress", hideServerAddress);
		root.put("hideChat", hideChat);
		root.put("lowHealthThreshold", lowHealthThreshold);
		root.put("crosshairEnabled", crosshairEnabled);
		root.put("crosshairStyle", crosshairStyle.name());
		root.put("crosshairSize", crosshairSize);
		root.put("crosshairThickness", crosshairThickness);
		root.put("crosshairGap", crosshairGap);
		root.put("crosshairColor", crosshairColor);
		root.put("crosshairOpacity", crosshairOpacity);
		root.put("crosshairOutline", crosshairOutline);
		root.put("crosshairDynamicColor", crosshairDynamicColor);
		root.put("crosshairHideInMenus", crosshairHideInMenus);
		root.put("crosshairHideInThirdPerson", crosshairHideInThirdPerson);
		JSONObject elementsObject = new JSONObject();
		for(Map.Entry<String, HudElementConfig> entry : elements.entrySet()) {
			elementsObject.put(entry.getKey(), entry.getValue().toJson());
		}
		root.put("elements", elementsObject);
		return root;
	}

	private static void copyAppearance(HudAppearanceConfig destination, HudAppearanceConfig source) {
		destination.copyFrom(source);
	}

	private static float clampFloat(double value, float min, float max, float fallback) {
		if(Double.isNaN(value) || Double.isInfinite(value)) {
			return fallback;
		}
		return Math.max(min, Math.min(max, (float) value));
	}

}
