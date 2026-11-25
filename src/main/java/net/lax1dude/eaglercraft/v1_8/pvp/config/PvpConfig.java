package net.lax1dude.eaglercraft.v1_8.pvp.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElementConfig;

public final class PvpConfig {

	public static final int CONFIG_VERSION = 2;

	public boolean enabled = true;
	public boolean hudEnabled = true;
	public final Map<String, HudElementConfig> elements = new LinkedHashMap<>();
	private int loadedVersion;
	private boolean needsMigration;

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
		JSONObject elementsObject = new JSONObject();
		for(Map.Entry<String, HudElementConfig> entry : elements.entrySet()) {
			elementsObject.put(entry.getKey(), entry.getValue().toJson());
		}
		root.put("elements", elementsObject);
		return root;
	}

}
