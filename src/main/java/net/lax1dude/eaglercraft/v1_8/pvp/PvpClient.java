package net.lax1dude.eaglercraft.v1_8.pvp;

import java.nio.charset.StandardCharsets;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfigStore;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudManager;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;

public final class PvpClient {

	private static PvpClient instance;
	private static final int[] CIRCLE_X = { 0, 1, 2, 2, 2, 1, 0, -1, -2, -2, -2, -1 };
	private static final int[] CIRCLE_Y = { -2, -2, -1, 0, 1, 2, 2, 2, 1, 0, -1, -2 };

	private final Minecraft minecraft;
	private final PvpConfigStore configStore;
	private final PvpRuntime runtime;
	private final HudManager hudManager;
	private boolean sprintToggled;
	private boolean sneakToggled;
	private boolean zoomToggled;
	private boolean temporaryHudHidden;
	private boolean zoomActive;

	private PvpClient(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.configStore = new PvpConfigStore();
		this.runtime = new PvpRuntime();
		this.hudManager = new HudManager(configStore.getConfig(), minecraft);
	}

	public static PvpClient initialize(Minecraft minecraft) {
		if(instance == null) {
			instance = new PvpClient(minecraft);
		}
		return instance;
	}

	public static PvpClient getInstance() {
		return instance;
	}

	public void tick() {
		runtime.tick(minecraft);
		configStore.tick();
		PvpConfig config = configStore.getConfig();
		if(config.enabled && minecraft.gameSettings.limitFramerate != config.fpsLimit) {
			minecraft.gameSettings.limitFramerate = config.fpsLimit;
		}
		if(minecraft.theWorld == null || minecraft.thePlayer == null || minecraft.currentScreen != null
				|| !minecraft.inGameHasFocus || !config.enabled || !config.toggleSprintEnabled) {
			if(sprintToggled && minecraft.thePlayer != null) {
				minecraft.thePlayer.setSprinting(false);
			}
			sprintToggled = false;
		}
		if(minecraft.theWorld == null || minecraft.thePlayer == null || minecraft.currentScreen != null
				|| !minecraft.inGameHasFocus || !config.enabled || !config.toggleSneakEnabled) {
			sneakToggled = false;
		}
		if(minecraft.theWorld == null || minecraft.thePlayer == null || minecraft.currentScreen != null
				|| !minecraft.inGameHasFocus || !config.enabled || !config.zoomEnabled) {
			zoomToggled = false;
		}
		if(minecraft.theWorld == null || minecraft.thePlayer == null) {
			temporaryHudHidden = false;
		}
		if(config.enabled && zoomActive && minecraft.isZoomKey) {
			float target = config.zoomStrength;
			float factor = 0.05f + config.zoomSmoothness * 0.0045f;
			minecraft.adjustedZoomValue += (target - minecraft.adjustedZoomValue) * factor;
		}
		zoomActive = minecraft.isZoomKey;
	}

	public void handleKeyPresses() {
		boolean toggleSprintPressed = minecraft.gameSettings.keyBindPvpToggleSprint.isPressed();
		boolean toggleSneakPressed = minecraft.gameSettings.keyBindPvpToggleSneak.isPressed();
		boolean reconnectPressed = minecraft.gameSettings.keyBindPvpQuickReconnect.isPressed();
		boolean copyCoordinatesPressed = minecraft.gameSettings.keyBindPvpCopyCoordinates.isPressed();
		boolean hideHudPressed = minecraft.gameSettings.keyBindPvpHideHud.isPressed();
		boolean screenshotPressed = minecraft.gameSettings.keyBindScreenshot.isPressed();
		PvpConfig config = configStore.getConfig();
		if(!config.enabled || minecraft.currentScreen != null || minecraft.theWorld == null
				|| minecraft.thePlayer == null || !minecraft.inGameHasFocus) {
			return;
		}
		if(toggleSprintPressed && config.toggleSprintEnabled) {
			sprintToggled = !sprintToggled;
			if(!sprintToggled) {
				minecraft.thePlayer.setSprinting(false);
			}
		}
		if(toggleSneakPressed && config.toggleSneakEnabled) {
			sneakToggled = !sneakToggled;
		}
		if(reconnectPressed) {
			reconnect(minecraft.currentScreen);
		}
		if(copyCoordinatesPressed) {
			copyCoordinates();
		}
		if(hideHudPressed) {
			temporaryHudHidden = !temporaryHudHidden;
			notifyHud(temporaryHudHidden ? "Dicestack HUD hidden" : "Dicestack HUD shown");
		}
		if(screenshotPressed && config.screenshotMode) {
			temporaryHudHidden = true;
		}
	}

	public boolean updateZoomKey(boolean held) {
		PvpConfig config = configStore.getConfig();
		minecraft.startZoomValue = config.zoomStrength;
		boolean pressed = minecraft.gameSettings.keyBindZoomCamera.isPressed();
		if(!config.enabled || !config.zoomEnabled || minecraft.theWorld == null || minecraft.thePlayer == null
				|| minecraft.currentScreen != null || !minecraft.inGameHasFocus) {
			zoomToggled = false;
			zoomActive = false;
			return false;
		}
		if(config.zoomToggle) {
			if(pressed) {
				zoomToggled = !zoomToggled;
			}
			zoomActive = zoomToggled;
			return zoomToggled;
		}
		zoomToggled = false;
		zoomActive = held;
		return held;
	}

	public static boolean isSneakToggled() {
		PvpClient client = instance;
		return client != null && client.configStore.getConfig().enabled
				&& client.configStore.getConfig().toggleSneakEnabled && client.sneakToggled;
	}

	public static boolean isSprintToggled() {
		PvpClient client = instance;
		return client != null && client.configStore.getConfig().enabled
				&& client.configStore.getConfig().toggleSprintEnabled && client.sprintToggled;
	}

	public static boolean isNoDynamicFovEnabled() {
		return instance != null && instance.configStore.getConfig().enabled
				&& instance.configStore.getConfig().noDynamicFov;
	}

	public static boolean isNoHurtTiltEnabled() {
		return instance != null && instance.configStore.getConfig().enabled
				&& instance.configStore.getConfig().noHurtTilt;
	}

	public static boolean isNoFogEnabled() {
		return instance != null && instance.configStore.getConfig().enabled
				&& instance.configStore.getConfig().noFog;
	}

	public static boolean isWeatherHidden() {
		return instance != null && instance.configStore.getConfig().enabled
				&& instance.configStore.getConfig().hideWeather;
	}

	public static boolean isVignetteHidden() {
		return instance != null && instance.configStore.getConfig().enabled
				&& instance.configStore.getConfig().hideVignette;
	}

	public static boolean isOverlayHidden(boolean configured) {
		return instance != null && instance.configStore.getConfig().enabled && configured;
	}

	public static boolean isEntityShadowsDisabled() {
		return instance != null && instance.configStore.getConfig().enabled
				&& instance.configStore.getConfig().disableEntityShadows;
	}

	public static float getEffectiveGamma(float configuredGamma) {
		return instance != null && instance.configStore.getConfig().enabled
				&& instance.configStore.getConfig().fullbright ? 16.0f : configuredGamma;
	}

	public static float getConfiguredFov(float vanillaFov) {
		PvpClient client = instance;
		PvpConfig config = client == null ? null : client.configStore.getConfig();
		return config != null && config.enabled && config.customFovEnabled && !client.minecraft.isZoomKey
				? config.customFov : vanillaFov;
	}

	public static float getThirdPersonDistance(float vanillaDistance) {
		PvpConfig config = instance == null ? null : instance.configStore.getConfig();
		return config != null && config.enabled ? config.thirdPersonDistance : vanillaDistance;
	}

	public static boolean isFreelookEnabled() {
		PvpConfig config = instance == null ? null : instance.configStore.getConfig();
		return config != null && config.enabled && config.freelookEnabled;
	}

	public static boolean isFreelookToggleMode() {
		PvpConfig config = instance == null ? null : instance.configStore.getConfig();
		return config != null && config.enabled && config.freelookToggleMode;
	}

	public static boolean isFreelookActive() {
		return instance != null && instance.minecraft.entityRenderer != null
				&& instance.minecraft.entityRenderer.isFreelookActive();
	}

	public static void renderVanillaFoodOverlay(EntityPlayer player, int foodLevel, int right, int foodY) {
		PvpClient client = instance;
		if(client == null || player == null || client.minecraft.thePlayer != player
				|| !client.configStore.getConfig().enabled
				|| !client.configStore.getConfig().vanillaFoodOverlayEnabled) {
			return;
		}

		float saturation = Math.max(0.0f, player.getFoodStats().getSaturationLevel());
		int saturationHalfPoints = Math.min(foodLevel, MathHelper.ceiling_float_int(saturation));
		for(int point = 0; point < saturationHalfPoints; ++point) {
			int icon = point / 2;
			int x = right - icon * 8 - 9;
			int left = point % 2 == 0 ? x : x + 5;
			float amount = Math.min(1.0f, saturation - point);
			int fillWidth = Math.max(1, Math.min(4, MathHelper.ceiling_float_int(amount * 4.0f)));
			Gui.drawRect(left, foodY + 6, left + fillWidth, foodY + 8, 0xDDEFCB62);
		}

		ItemStack held = player.getHeldItem();
		if(held != null && held.getItem() instanceof ItemFood && foodLevel < 20) {
			ItemFood food = (ItemFood) held.getItem();
			int restored = Math.min(20 - foodLevel, food.getHealAmount(held));
			for(int point = 0; point < restored; ++point) {
				int hungerPoint = foodLevel + point;
				int icon = hungerPoint / 2;
				int x = right - icon * 8 - 9;
				int left = hungerPoint % 2 == 0 ? x : x + 5;
				Gui.drawRect(left, foodY + 2, left + 4, foodY + 4, 0xB853D6A3);
			}
		}

		float exhaustion = player.getFoodStats().getFoodExhaustionLevel();
		int barLeft = right - 81;
		int barY = foodY + 10;
		Gui.drawRect(barLeft, barY, right, barY + 2, 0x990A1018);
		int exhaustionWidth = Math.max(0, Math.min(81, (int) (exhaustion * 20.25f)));
		if(exhaustionWidth > 0) {
			Gui.drawRect(barLeft, barY, barLeft + exhaustionWidth, barY + 1, 0xFF53D6A3);
		}
	}

	public static boolean isClearWaterEnabled() {
		return instance != null && instance.configStore.getConfig().enabled
				&& instance.configStore.getConfig().clearWater;
	}

	public static boolean isCustomFogColorEnabled() {
		return instance != null && instance.configStore.getConfig().enabled
				&& instance.configStore.getConfig().customFogColorEnabled;
	}

	public static int getCustomFogColor() {
		return instance == null ? 0xFFB6C8D8 : instance.configStore.getConfig().customFogColor;
	}

	public static boolean isLocalTimeEnabled() {
		return instance != null && instance.configStore.getConfig().enabled
				&& instance.configStore.getConfig().localTimeEnabled;
	}

	public static float getLocalTimeCelestialAngle(float vanillaAngle) {
		PvpConfig config = instance == null ? null : instance.configStore.getConfig();
		return config != null && config.enabled && config.localTimeEnabled
				? (config.localTime % 24000) / 24000.0f : vanillaAngle;
	}

	public static float getLocalTimeCelestialAngleRadians(float vanillaAngleRadians) {
		PvpConfig config = instance == null ? null : instance.configStore.getConfig();
		if(config != null && config.enabled && config.localTimeEnabled) {
			return (config.localTime % 24000) / 24000.0f * ((float) Math.PI * 2.0f);
		}
		return vanillaAngleRadians;
	}

	public static boolean isBlockOutlineEnabled() {
		return instance == null || instance.configStore.getConfig().enabled
				&& instance.configStore.getConfig().blockOutlineEnabled;
	}

	public static int getBlockOutlineColor() {
		return instance == null ? 0xFF000000 : instance.configStore.getConfig().blockOutlineColor;
	}

	public static float getBlockOutlineThickness() {
		return instance == null ? 2.0f : instance.configStore.getConfig().blockOutlineThickness;
	}

	public static float getHeldItemScale() {
		return instance == null ? 1.0f : instance.configStore.getConfig().enabled
				? instance.configStore.getConfig().heldItemScale : 1.0f;
	}

	public static float getHeldItemOffsetX() {
		return instance == null ? 0.0f : instance.configStore.getConfig().enabled
				? instance.configStore.getConfig().heldItemOffsetX : 0.0f;
	}

	public static float getHeldItemOffsetY() {
		return instance == null ? 0.0f : instance.configStore.getConfig().enabled
				? instance.configStore.getConfig().heldItemOffsetY : 0.0f;
	}

	public static boolean isGlintEnabled() {
		return instance == null || instance.configStore.getConfig().enabled
				&& instance.configStore.getConfig().glintEnabled;
	}

	public static float getGlintIntensity() {
		return instance == null ? 1.0f : instance.configStore.getConfig().enabled
				? instance.configStore.getConfig().glintIntensity : 1.0f;
	}

	public static int getGlintColor() {
		return instance == null ? 0xFF5030FF : instance.configStore.getConfig().glintColor;
	}

	public static boolean isStreamerModeEnabled() {
		return instance != null && instance.configStore.getConfig().enabled
				&& instance.configStore.getConfig().streamerMode;
	}

	public static boolean isUsernameHidden() {
		PvpConfig config = instance == null ? null : instance.configStore.getConfig();
		return config != null && config.enabled && (config.streamerMode || config.hideUsername);
	}

	public static String getPrivateName(String name) {
		return isUsernameHidden() ? "Player" : name;
	}

	public boolean exportConfig() {
		try {
			byte[] data = configStore.getConfig().toJson().toString().getBytes(StandardCharsets.UTF_8);
			EagRuntime.downloadFileWithName("dicestack-pvp-config-v"
					+ PvpConfig.CONFIG_VERSION + ".json", data);
			notifyHud("Configuration exported");
			return true;
		}catch(Throwable ex) {
			notifyHud("Could not export configuration");
			return false;
		}
	}

	public boolean importConfigFromClipboard() {
		try {
			String json = EagRuntime.getClipboard();
			if(json == null || json.trim().isEmpty()) {
				notifyHud("Clipboard is empty");
				return false;
			}
			PvpConfig parsed = PvpConfig.fromJson(json);
			if(parsed.getLoadedVersion() > PvpConfig.CONFIG_VERSION) {
				notifyHud("Configuration is from a newer client");
				return false;
			}
			configStore.installImportedConfig(parsed);
			hudManager.reloadConfig(parsed);
			notifyHud("Configuration imported");
			return true;
		}catch(Throwable ex) {
			notifyHud("Could not import configuration");
			return false;
		}
	}

	public boolean renderCustomCrosshair(int screenWidth, int screenHeight) {
		PvpConfig config = configStore.getConfig();
		if(!config.enabled || !config.crosshairEnabled || config.crosshairStyle == PvpConfig.CrosshairStyle.VANILLA) {
			return false;
		}
		if(config.crosshairHideInMenus && minecraft.currentScreen != null
				|| config.crosshairHideInThirdPerson && minecraft.gameSettings.thirdPersonView != 0) {
			return true;
		}
		int color = config.crosshairColor;
		if(config.crosshairDynamicColor && minecraft.objectMouseOver != null
				&& minecraft.objectMouseOver.entityHit != null) {
			color = 0xFFFF5555;
		}
		color = (Math.max(0, Math.min(255, (int) (config.crosshairOpacity * 255.0f))) << 24) | (color & 0x00FFFFFF);
		int centerX = screenWidth / 2;
		int centerY = screenHeight / 2;
		int size = config.crosshairSize;
		int thickness = config.crosshairThickness;
		int gap = config.crosshairGap;
		if(config.crosshairOutline) {
			int outlineThickness = thickness + 2;
			drawCrosshairShape(config.crosshairStyle, centerX, centerY, size + 1, outlineThickness,
					gap, 0xCC000000);
		}
		drawCrosshairShape(config.crosshairStyle, centerX, centerY, size, thickness, gap, color);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		return true;
	}

	private void drawCrosshairShape(PvpConfig.CrosshairStyle style, int centerX, int centerY, int size,
			int thickness, int gap, int color) {
		if(style == PvpConfig.CrosshairStyle.DOT || style == PvpConfig.CrosshairStyle.CIRCLE_DOT) {
			Gui.drawRect(centerX - thickness / 2, centerY - thickness / 2,
					centerX - thickness / 2 + thickness, centerY - thickness / 2 + thickness, color);
		}
		if(style == PvpConfig.CrosshairStyle.CIRCLE || style == PvpConfig.CrosshairStyle.CIRCLE_DOT) {
			int radius = Math.max(2, size);
			for(int i = 0; i < CIRCLE_X.length; ++i) {
				int px = centerX + CIRCLE_X[i] * radius / 2;
				int py = centerY + CIRCLE_Y[i] * radius / 2;
				Gui.drawRect(px - thickness / 2, py - thickness / 2,
						px - thickness / 2 + thickness, py - thickness / 2 + thickness, color);
			}
		}else if(style == PvpConfig.CrosshairStyle.CROSS) {
			Gui.drawRect(centerX - thickness / 2, centerY - gap - size,
					centerX - thickness / 2 + thickness, centerY - gap, color);
			Gui.drawRect(centerX - thickness / 2, centerY + gap + 1,
					centerX - thickness / 2 + thickness, centerY + gap + size + 1, color);
			Gui.drawRect(centerX - gap - size, centerY - thickness / 2,
					centerX - gap, centerY - thickness / 2 + thickness, color);
			Gui.drawRect(centerX + gap + 1, centerY - thickness / 2,
					centerX + gap + size + 1, centerY - thickness / 2 + thickness, color);
		}
	}

	public void recordMouseClick(int button) {
		runtime.recordMouseClick(button);
	}

	public void renderHud(ScaledResolution resolution, float partialTicks) {
		if(configStore.getConfig().enabled && configStore.getConfig().hudEnabled && !temporaryHudHidden
				&& minecraft.theWorld != null && minecraft.thePlayer != null
				&& minecraft.currentScreen == null) {
			hudManager.render(resolution, partialTicks, runtime);
		}
	}

	public void openHudEditor() {
		minecraft.displayGuiScreen(new GuiScreenPvpHudEditor(this, null));
	}

	public void openHudEditor(GuiScreen parent) {
		minecraft.displayGuiScreen(new GuiScreenPvpHudEditor(this, parent));
	}

	public void openHudModuleManager() {
		minecraft.displayGuiScreen(new GuiScreenPvpHudModuleManager(this, null));
	}

	public void openHudModuleManager(GuiScreen parent) {
		minecraft.displayGuiScreen(new GuiScreenPvpHudModuleManager(this, parent));
	}

	public void openHudModuleManager(GuiScreen parent, String searchText) {
		minecraft.displayGuiScreen(new GuiScreenPvpHudModuleManager(this, parent, searchText));
	}

	public void openClientMenu() {
		minecraft.displayGuiScreen(new GuiScreenPvpClientMenu(this, null));
	}

	public void openClientMenu(GuiScreen parent) {
		minecraft.displayGuiScreen(new GuiScreenPvpClientMenu(this, parent));
	}

	public void openQolSettings(GuiScreen parent) {
		minecraft.displayGuiScreen(new GuiScreenPvpQolSettings(this, parent));
	}

	public void openQolSettings(GuiScreen parent, int page, String searchText) {
		minecraft.displayGuiScreen(new GuiScreenPvpQolSettings(this, parent, page, searchText));
	}

	public void recordServerData(ServerData serverData) {
		if(serverData == null || serverData.serverIP == null || serverData.serverIP.trim().isEmpty()) {
			return;
		}
		recordServerAddress(serverData.serverName, serverData.serverIP);
	}

	public void recordServerAddress(String name, String address) {
		if(address == null || address.trim().isEmpty()) {
			return;
		}
		PvpConfig config = configStore.getConfig();
		name = name == null ? "" : name;
		if(!address.equals(config.lastServerAddress) || !name.equals(config.lastServerName)) {
			config.lastServerAddress = address;
			config.lastServerName = name;
			configStore.markDirty();
		}
	}

	public boolean reconnect(GuiScreen previousScreen) {
		PvpConfig config = configStore.getConfig();
		String address = config.lastServerAddress == null ? "" : config.lastServerAddress.trim();
		if(address.isEmpty()) {
			notifyHud("No previous server to reconnect to");
			return false;
		}
		if(minecraft.currentScreen instanceof GuiConnecting) {
			return false;
		}
		String name = config.lastServerName == null || config.lastServerName.isEmpty()
				? address : config.lastServerName;
		try {
			ServerData server = new ServerData(name, address, false);
			server.enableCookies = false;
			GuiScreen returnScreen = previousScreen == null ? new GuiMainMenu() : previousScreen;
			minecraft.displayGuiScreen(new GuiConnecting(returnScreen, minecraft, server));
			return true;
		}catch(RuntimeException ex) {
			notifyHud("Could not reconnect to the previous server");
			return false;
		}
	}

	private void copyCoordinates() {
		if(minecraft.thePlayer == null || minecraft.theWorld == null) {
			notifyHud("Coordinates are unavailable");
			return;
		}
		int x = MathHelper.floor_double(minecraft.thePlayer.posX);
		int y = MathHelper.floor_double(minecraft.thePlayer.posY);
		int z = MathHelper.floor_double(minecraft.thePlayer.posZ);
		String coordinates = PvpConfig.formatCoordinates(configStore.getConfig().coordinateFormat, x, y, z);
		int dimension = minecraft.thePlayer.dimension;
		String dimensionName = dimension == 0 ? "Overworld" : dimension == -1 ? "Nether"
				: dimension == 1 ? "The End" : "Dimension " + dimension;
		try {
			EagRuntime.setClipboard(coordinates + " (" + dimensionName + ")");
			notifyHud("Coordinates copied");
		}catch(RuntimeException ex) {
			notifyHud("Could not copy coordinates");
		}
	}

	private void notifyHud(String message) {
		PvpConfig config = configStore == null ? null : configStore.getConfig();
		if(config != null && !config.notificationsEnabled) {
			return;
		}
		if(minecraft.ingameGUI != null) {
			minecraft.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("\u00a7a[Dicestack] \u00a7f" + message));
		}
	}

	public void shutdown() {
		configStore.saveNow();
	}

	public PvpConfigStore getConfigStore() {
		return configStore;
	}

	public HudManager getHudManager() {
		return hudManager;
	}

	public PvpRuntime getRuntime() {
		return runtime;
	}

}
