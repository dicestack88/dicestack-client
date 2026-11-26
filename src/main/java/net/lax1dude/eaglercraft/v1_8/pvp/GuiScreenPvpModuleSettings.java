package net.lax1dude.eaglercraft.v1_8.pvp;

import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public final class GuiScreenPvpModuleSettings extends GuiScreenPvpQolSettings {

	public GuiScreenPvpModuleSettings(PvpClient pvpClient, GuiScreen parent, String moduleName,
			String moduleDescription, int[] moduleSettingIds) {
		super(pvpClient, parent, moduleName, moduleDescription, moduleSettingIds);
	}

	public GuiScreenPvpModuleSettings(PvpClient pvpClient, GuiScreen parent, String moduleName,
			String moduleDescription, int[] moduleSettingIds, HudElement moduleHudElement) {
		super(pvpClient, parent, moduleName, moduleDescription, moduleSettingIds, moduleHudElement);
	}

	static boolean isModuleEnabled(PvpClient client, String moduleName, HudElement hudElement) {
		if(hudElement != null) {
			return client.getHudManager().getElementConfig(hudElement).enabled;
		}
		PvpConfig config = client.getConfigStore().getConfig();
		if("Toggle Sprint".equals(moduleName)) return config.toggleSprintEnabled;
		if("Toggle Sneak".equals(moduleName)) return config.toggleSneakEnabled;
		if("Zoom".equals(moduleName)) return config.zoomEnabled;
		if("Freelook".equals(moduleName)) return config.freelookEnabled;
		if("Custom FOV".equals(moduleName)) return config.customFovEnabled;
		if("Crosshair".equals(moduleName)) return config.crosshairEnabled;
		if("Chat".equals(moduleName)) return config.chatTimestamps || config.chatCompact
				|| config.chatHighlightMentions;
		if("Streamer Mode".equals(moduleName)) return config.streamerMode;
		if("Privacy".equals(moduleName)) return config.hideChat || config.hideServerAddress
				|| config.hideUsername || config.hideFireOverlay || config.hidePumpkinOverlay
				|| config.hidePortalOverlay || config.screenshotMode;
		if("Fullbright".equals(moduleName)) return config.fullbright;
		if("Fog and Weather".equals(moduleName)) return config.noFog || config.hideWeather
				|| config.clearWater || config.customFogColorEnabled || config.localTimeEnabled;
		if("Block Outline".equals(moduleName)) return config.blockOutlineEnabled;
		if("Held Item".equals(moduleName)) return config.heldItemScale != 1.0f
				|| config.heldItemOffsetX != 0.0f || config.heldItemOffsetY != 0.0f || config.glintEnabled;
		if("Performance".equals(moduleName)) return config.performanceProfile != PvpConfig.PerformanceProfile.DEFAULT
				|| config.fpsLimit != 260;
		if("Notifications".equals(moduleName)) return config.notificationsEnabled;
		return false;
	}

	static void setModuleEnabled(PvpClient client, Minecraft minecraft, String moduleName,
			HudElement hudElement, boolean enabled) {
		if(hudElement != null) {
			client.getHudManager().getElementConfig(hudElement).enabled = enabled;
			return;
		}
		PvpConfig config = client.getConfigStore().getConfig();
		if("Toggle Sprint".equals(moduleName)) config.toggleSprintEnabled = enabled;
		else if("Toggle Sneak".equals(moduleName)) config.toggleSneakEnabled = enabled;
		else if("Zoom".equals(moduleName)) config.zoomEnabled = enabled;
		else if("Freelook".equals(moduleName)) config.freelookEnabled = enabled;
		else if("Custom FOV".equals(moduleName)) config.customFovEnabled = enabled;
		else if("Crosshair".equals(moduleName)) config.crosshairEnabled = enabled;
		else if("Chat".equals(moduleName)) {
			config.chatTimestamps = enabled;
			if(!enabled) {
				config.chatCompact = false;
				config.chatHighlightMentions = false;
			}
			minecraft.ingameGUI.getChatGUI().refreshChat();
		}else if("Streamer Mode".equals(moduleName)) config.streamerMode = enabled;
		else if("Privacy".equals(moduleName)) {
			config.hideChat = enabled;
			config.hideServerAddress = enabled;
			config.hideUsername = enabled;
			config.hideFireOverlay = enabled;
			config.hidePumpkinOverlay = enabled;
			config.hidePortalOverlay = enabled;
			config.screenshotMode = enabled;
		}else if("Fullbright".equals(moduleName)) config.fullbright = enabled;
		else if("Fog and Weather".equals(moduleName)) {
			config.noFog = enabled;
			config.hideWeather = enabled;
			config.clearWater = enabled;
			config.customFogColorEnabled = enabled;
			config.localTimeEnabled = enabled;
		}else if("Block Outline".equals(moduleName)) config.blockOutlineEnabled = enabled;
		else if("Held Item".equals(moduleName)) config.glintEnabled = enabled;
		else if("Performance".equals(moduleName)) {
			config.applyPerformanceProfile(enabled ? PvpConfig.PerformanceProfile.BALANCED
					: PvpConfig.PerformanceProfile.DEFAULT);
			minecraft.gameSettings.limitFramerate = config.fpsLimit;
			minecraft.gameSettings.saveOptions();
		}else if("Notifications".equals(moduleName)) config.notificationsEnabled = enabled;
	}
}
