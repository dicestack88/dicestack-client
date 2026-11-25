package net.lax1dude.eaglercraft.v1_8.pvp.hud;

import org.json.JSONObject;

public final class HudAppearanceConfig {

	public enum Preset {
		MODERN_GLASS,
		MINIMAL_COMPETITIVE
	}

	public enum GradientDirection {
		HORIZONTAL,
		VERTICAL
	}

	public enum TextAlignment {
		LEFT,
		CENTER,
		RIGHT
	}

	public Preset preset = Preset.MODERN_GLASS;
	public boolean backgroundEnabled = true;
	public boolean gradientEnabled = true;
	public GradientDirection gradientDirection = GradientDirection.VERTICAL;
	public int gradientStart = 0xFF172638;
	public int gradientEnd = 0xFF0F1722;
	public float backgroundOpacity = 0.78f;
	public boolean borderEnabled = true;
	public int borderThickness = 1;
	public int borderColor = 0xFF53D6A3;
	public float borderOpacity = 0.72f;
	public int textColor = 0xFFF1F7F5;
	public float textOpacity = 1.0f;
	public int accentColor = 0xFF53D6A3;
	public boolean textShadow = true;
	public boolean panelShadow = false;
	public float shadowIntensity = 0.5f;
	public int padding = 4;
	public int spacing = 4;
	public int cornerRadius = 3;
	public float textScale = 1.0f;
	public TextAlignment textAlignment = TextAlignment.LEFT;
	public boolean rainbowEnabled = false;
	public boolean animationsEnabled = true;
	public int performanceGoodColor = 0xFF53D6A3;
	public int performanceMediumColor = 0xFFFFD166;
	public int performanceLowColor = 0xFFFF7777;

	public void applyPreset(Preset preset) {
		this.preset = preset;
		if(preset == Preset.MINIMAL_COMPETITIVE) {
			backgroundEnabled = true;
			gradientEnabled = false;
			backgroundOpacity = 0.42f;
			gradientStart = 0xFF10151C;
			gradientEnd = 0xFF10151C;
			borderEnabled = true;
			borderThickness = 1;
			borderColor = 0xFF8BA0B5;
			borderOpacity = 0.45f;
			textColor = 0xFFFFFFFF;
			textOpacity = 1.0f;
			accentColor = 0xFF55DDA0;
			textShadow = true;
			panelShadow = false;
			padding = 2;
			spacing = 2;
			cornerRadius = 0;
			animationsEnabled = false;
		}else {
			backgroundEnabled = true;
			gradientEnabled = true;
			backgroundOpacity = 0.78f;
			gradientDirection = GradientDirection.VERTICAL;
			gradientStart = 0xFF172638;
			gradientEnd = 0xFF0F1722;
			borderEnabled = true;
			borderThickness = 1;
			borderColor = 0xFF53D6A3;
			borderOpacity = 0.72f;
			textColor = 0xFFF1F7F5;
			textOpacity = 1.0f;
			accentColor = 0xFF53D6A3;
			textShadow = true;
			panelShadow = false;
			padding = 4;
			spacing = 4;
			cornerRadius = 3;
			animationsEnabled = true;
		}
	}

	public static HudAppearanceConfig defaults() {
		HudAppearanceConfig result = new HudAppearanceConfig();
		result.applyPreset(Preset.MODERN_GLASS);
		return result;
	}

	public static HudAppearanceConfig fromJson(JSONObject json, HudAppearanceConfig defaults) {
		HudAppearanceConfig result = copy(defaults);
		String presetName = json.optString("preset", defaults.preset.name());
		try {
			result.preset = Preset.valueOf(presetName);
		}catch(Throwable ex) {
			result.preset = defaults.preset;
		}
		result.backgroundEnabled = json.optBoolean("backgroundEnabled", defaults.backgroundEnabled);
		result.gradientEnabled = json.optBoolean("gradientEnabled", defaults.gradientEnabled);
		String direction = json.optString("gradientDirection", defaults.gradientDirection.name());
		try {
			result.gradientDirection = GradientDirection.valueOf(direction);
		}catch(Throwable ex) {
			result.gradientDirection = defaults.gradientDirection;
		}
		result.gradientStart = clampColor(json.optInt("gradientStart", defaults.gradientStart));
		result.gradientEnd = clampColor(json.optInt("gradientEnd", defaults.gradientEnd));
		result.backgroundOpacity = clamp(json.optDouble("backgroundOpacity", defaults.backgroundOpacity), 0.0f, 1.0f);
		result.borderEnabled = json.optBoolean("borderEnabled", defaults.borderEnabled);
		result.borderThickness = clampInt(json.optInt("borderThickness", defaults.borderThickness), 0, 4);
		result.borderColor = clampColor(json.optInt("borderColor", defaults.borderColor));
		result.borderOpacity = clamp(json.optDouble("borderOpacity", defaults.borderOpacity), 0.0f, 1.0f);
		result.textColor = clampColor(json.optInt("textColor", defaults.textColor));
		result.textOpacity = clamp(json.optDouble("textOpacity", defaults.textOpacity), 0.0f, 1.0f);
		result.accentColor = clampColor(json.optInt("accentColor", defaults.accentColor));
		result.textShadow = json.optBoolean("textShadow", defaults.textShadow);
		result.panelShadow = json.optBoolean("panelShadow", defaults.panelShadow);
		result.shadowIntensity = clamp(json.optDouble("shadowIntensity", defaults.shadowIntensity), 0.0f, 1.0f);
		result.padding = clampInt(json.optInt("padding", defaults.padding), 0, 16);
		result.spacing = clampInt(json.optInt("spacing", defaults.spacing), 0, 16);
		result.cornerRadius = clampInt(json.optInt("cornerRadius", defaults.cornerRadius), 0, 12);
		result.textScale = clamp(json.optDouble("textScale", defaults.textScale), 0.5f, 2.0f);
		String alignment = json.optString("textAlignment", defaults.textAlignment.name());
		try {
			result.textAlignment = TextAlignment.valueOf(alignment);
		}catch(Throwable ex) {
			result.textAlignment = defaults.textAlignment;
		}
		result.rainbowEnabled = json.optBoolean("rainbowEnabled", defaults.rainbowEnabled);
		result.animationsEnabled = json.optBoolean("animationsEnabled", defaults.animationsEnabled);
		result.performanceGoodColor = json.optInt("performanceGoodColor", defaults.performanceGoodColor);
		result.performanceMediumColor = json.optInt("performanceMediumColor", defaults.performanceMediumColor);
		result.performanceLowColor = json.optInt("performanceLowColor", defaults.performanceLowColor);
		return result;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("preset", preset.name());
		json.put("backgroundEnabled", backgroundEnabled);
		json.put("gradientEnabled", gradientEnabled);
		json.put("gradientDirection", gradientDirection.name());
		json.put("gradientStart", gradientStart);
		json.put("gradientEnd", gradientEnd);
		json.put("backgroundOpacity", backgroundOpacity);
		json.put("borderEnabled", borderEnabled);
		json.put("borderThickness", borderThickness);
		json.put("borderColor", borderColor);
		json.put("borderOpacity", borderOpacity);
		json.put("textColor", textColor);
		json.put("textOpacity", textOpacity);
		json.put("accentColor", accentColor);
		json.put("textShadow", textShadow);
		json.put("panelShadow", panelShadow);
		json.put("shadowIntensity", shadowIntensity);
		json.put("padding", padding);
		json.put("spacing", spacing);
		json.put("cornerRadius", cornerRadius);
		json.put("textScale", textScale);
		json.put("textAlignment", textAlignment.name());
		json.put("rainbowEnabled", rainbowEnabled);
		json.put("animationsEnabled", animationsEnabled);
		json.put("performanceGoodColor", performanceGoodColor);
		json.put("performanceMediumColor", performanceMediumColor);
		json.put("performanceLowColor", performanceLowColor);
		return json;
	}

	public static HudAppearanceConfig copy(HudAppearanceConfig source) {
		HudAppearanceConfig result = new HudAppearanceConfig();
		result.copyFrom(source);
		return result;
	}

	public void copyFrom(HudAppearanceConfig source) {
		if(source == null) {
			return;
		}
		preset = source.preset;
		backgroundEnabled = source.backgroundEnabled;
		gradientEnabled = source.gradientEnabled;
		gradientDirection = source.gradientDirection;
		gradientStart = source.gradientStart;
		gradientEnd = source.gradientEnd;
		backgroundOpacity = source.backgroundOpacity;
		borderEnabled = source.borderEnabled;
		borderThickness = source.borderThickness;
		borderColor = source.borderColor;
		borderOpacity = source.borderOpacity;
		textColor = source.textColor;
		textOpacity = source.textOpacity;
		accentColor = source.accentColor;
		textShadow = source.textShadow;
		panelShadow = source.panelShadow;
		shadowIntensity = source.shadowIntensity;
		padding = source.padding;
		spacing = source.spacing;
		cornerRadius = source.cornerRadius;
		textScale = source.textScale;
		textAlignment = source.textAlignment;
		rainbowEnabled = source.rainbowEnabled;
		animationsEnabled = source.animationsEnabled;
		performanceGoodColor = source.performanceGoodColor;
		performanceMediumColor = source.performanceMediumColor;
		performanceLowColor = source.performanceLowColor;
	}

	private static int clampColor(int color) {
		return color;
	}

	private static int clampInt(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static float clamp(double value, float min, float max) {
		float result = (float) value;
		if(Float.isNaN(result) || Float.isInfinite(result)) {
			return min;
		}
		return Math.max(min, Math.min(max, result));
	}
}
