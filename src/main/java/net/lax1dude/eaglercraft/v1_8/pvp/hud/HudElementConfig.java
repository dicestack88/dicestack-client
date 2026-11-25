package net.lax1dude.eaglercraft.v1_8.pvp.hud;

import org.json.JSONObject;

public final class HudElementConfig {

	public enum Anchor {
		TOP_LEFT,
		TOP_CENTER,
		TOP_RIGHT,
		CENTER_LEFT,
		CENTER,
		CENTER_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_CENTER,
		BOTTOM_RIGHT
	}

	public boolean enabled;
	public Anchor anchor;
	public int offsetX;
	public int offsetY;
	public float scale;
	public boolean appearanceOverride;
	public final HudAppearanceConfig appearance = HudAppearanceConfig.defaults();
	public boolean keystrokesWASD = true;
	public boolean keystrokesSpace = true;
	public boolean keystrokesShift = true;
	public boolean keystrokesMouse = true;
	public boolean keystrokesLabels = true;
	public boolean keystrokesCPS = false;
	public int keystrokesKeySize = 18;
	public int keystrokesSpacing = 2;
	public int keystrokesPadding = 3;
	public int keystrokesPressedColor = 0xFF55DDA0;
	public int keystrokesUnpressedColor = 0xCC1A2633;
	public boolean cpsLeft = true;
	public boolean cpsRight = true;
	public boolean cpsCombined = true;
	public boolean cpsLabels = true;
	public boolean cpsPeak = false;
	public int cpsDisplayDuration = 1000;

	public HudElementConfig() {
		this(true, Anchor.TOP_LEFT, 0, 0, 1.0f);
	}

	public HudElementConfig(boolean enabled, Anchor anchor, int offsetX, int offsetY, float scale) {
		this.enabled = enabled;
		this.anchor = anchor;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.scale = scale;
	}

	public static HudElementConfig fromJson(JSONObject json, HudElementConfig defaults) {
		HudElementConfig result = new HudElementConfig(defaults.enabled, defaults.anchor, defaults.offsetX,
				defaults.offsetY, defaults.scale);
		result.enabled = json.optBoolean("enabled", defaults.enabled);
		String anchorName = json.optString("anchor", defaults.anchor.name());
		try {
			result.anchor = Anchor.valueOf(anchorName);
		}catch(Throwable ex) {
			result.anchor = defaults.anchor;
		}
		result.offsetX = json.optInt("offsetX", defaults.offsetX);
		result.offsetY = json.optInt("offsetY", defaults.offsetY);
		result.scale = (float) json.optDouble("scale", defaults.scale);
		result.appearanceOverride = json.optBoolean("appearanceOverride", defaults.appearanceOverride);
		JSONObject appearance = json.optJSONObject("appearance");
		if(appearance != null) {
			result.appearance.copyFrom(HudAppearanceConfig.fromJson(appearance, defaults.appearance));
		}
		result.keystrokesWASD = json.optBoolean("keystrokesWASD", defaults.keystrokesWASD);
		result.keystrokesSpace = json.optBoolean("keystrokesSpace", defaults.keystrokesSpace);
		result.keystrokesShift = json.optBoolean("keystrokesShift", defaults.keystrokesShift);
		result.keystrokesMouse = json.optBoolean("keystrokesMouse", defaults.keystrokesMouse);
		result.keystrokesLabels = json.optBoolean("keystrokesLabels", defaults.keystrokesLabels);
		result.keystrokesCPS = json.optBoolean("keystrokesCPS", defaults.keystrokesCPS);
		result.keystrokesKeySize = Math.max(12, Math.min(32, json.optInt("keystrokesKeySize", defaults.keystrokesKeySize)));
		result.keystrokesSpacing = Math.max(0, Math.min(10, json.optInt("keystrokesSpacing", defaults.keystrokesSpacing)));
		result.keystrokesPadding = Math.max(0, Math.min(12, json.optInt("keystrokesPadding", defaults.keystrokesPadding)));
		result.keystrokesPressedColor = json.optInt("keystrokesPressedColor", defaults.keystrokesPressedColor);
		result.keystrokesUnpressedColor = json.optInt("keystrokesUnpressedColor", defaults.keystrokesUnpressedColor);
		result.cpsLeft = json.optBoolean("cpsLeft", defaults.cpsLeft);
		result.cpsRight = json.optBoolean("cpsRight", defaults.cpsRight);
		result.cpsCombined = json.optBoolean("cpsCombined", defaults.cpsCombined);
		result.cpsLabels = json.optBoolean("cpsLabels", defaults.cpsLabels);
		result.cpsPeak = json.optBoolean("cpsPeak", defaults.cpsPeak);
		result.cpsDisplayDuration = Math.max(250, Math.min(5000, json.optInt("cpsDisplayDuration", defaults.cpsDisplayDuration)));
		if(result.scale < 0.5f) {
			result.scale = 0.5f;
		}else if(result.scale > 3.0f) {
			result.scale = 3.0f;
		}
		return result;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("enabled", enabled);
		json.put("anchor", anchor.name());
		json.put("offsetX", offsetX);
		json.put("offsetY", offsetY);
		json.put("scale", scale);
		json.put("appearanceOverride", appearanceOverride);
		json.put("appearance", appearance.toJson());
		json.put("keystrokesWASD", keystrokesWASD);
		json.put("keystrokesSpace", keystrokesSpace);
		json.put("keystrokesShift", keystrokesShift);
		json.put("keystrokesMouse", keystrokesMouse);
		json.put("keystrokesLabels", keystrokesLabels);
		json.put("keystrokesCPS", keystrokesCPS);
		json.put("keystrokesKeySize", keystrokesKeySize);
		json.put("keystrokesSpacing", keystrokesSpacing);
		json.put("keystrokesPadding", keystrokesPadding);
		json.put("keystrokesPressedColor", keystrokesPressedColor);
		json.put("keystrokesUnpressedColor", keystrokesUnpressedColor);
		json.put("cpsLeft", cpsLeft);
		json.put("cpsRight", cpsRight);
		json.put("cpsCombined", cpsCombined);
		json.put("cpsLabels", cpsLabels);
		json.put("cpsPeak", cpsPeak);
		json.put("cpsDisplayDuration", cpsDisplayDuration);
		return json;
	}

}
