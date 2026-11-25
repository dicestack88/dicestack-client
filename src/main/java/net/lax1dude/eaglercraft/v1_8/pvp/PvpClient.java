package net.lax1dude.eaglercraft.v1_8.pvp;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfigStore;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;

public final class PvpClient {

	private static PvpClient instance;

	private final Minecraft minecraft;
	private final PvpConfigStore configStore;
	private final PvpRuntime runtime;
	private final HudManager hudManager;
	private boolean sprintToggled;
	private boolean sneakToggled;
	private boolean zoomToggled;
	private boolean temporaryHudHidden;

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
	}

	public void handleKeyPresses() {
		boolean toggleSprintPressed = minecraft.gameSettings.keyBindPvpToggleSprint.isPressed();
		boolean toggleSneakPressed = minecraft.gameSettings.keyBindPvpToggleSneak.isPressed();
		boolean reconnectPressed = minecraft.gameSettings.keyBindPvpQuickReconnect.isPressed();
		boolean copyCoordinatesPressed = minecraft.gameSettings.keyBindPvpCopyCoordinates.isPressed();
		boolean hideHudPressed = minecraft.gameSettings.keyBindPvpHideHud.isPressed();
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
	}

	public boolean updateZoomKey(boolean held) {
		PvpConfig config = configStore.getConfig();
		minecraft.startZoomValue = config.zoomStrength;
		boolean pressed = minecraft.gameSettings.keyBindZoomCamera.isPressed();
		if(!config.enabled || !config.zoomEnabled || minecraft.theWorld == null || minecraft.thePlayer == null
				|| minecraft.currentScreen != null || !minecraft.inGameHasFocus) {
			zoomToggled = false;
			return false;
		}
		if(config.zoomToggle) {
			if(pressed) {
				zoomToggled = !zoomToggled;
			}
			return zoomToggled;
		}
		zoomToggled = false;
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
		minecraft.displayGuiScreen(new GuiScreenPvpHudEditor(this));
	}

	public void openHudModuleManager() {
		minecraft.displayGuiScreen(new GuiScreenPvpHudModuleManager(this));
	}

	public void openQolSettings(GuiScreen parent) {
		minecraft.displayGuiScreen(new GuiScreenPvpQolSettings(this, parent));
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
