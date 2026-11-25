package net.lax1dude.eaglercraft.v1_8.pvp;

import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public final class GuiScreenPvpQolSettings extends GuiScreen {

	private static final int[][] PAGES = {
			{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
			{ 20, 21, 22, 23, 24, 25, 26, 27, 28 },
			{ 30, 31, 32, 33, 34, 35, 38 },
			{ 40, 41, 42, 43, 44, 45, 46, 47 },
			{ 50, 51, 52, 53, 54, 55, 56, 57 }
	};
	private static final int[][] CROSSHAIR_COLORS = {
			{ 255, 255, 255 }, { 83, 214, 163 }, { 255, 85, 85 }, { 255, 209, 102 }, { 112, 167, 255 }
	};

	private final PvpClient pvpClient;
	private final GuiScreen parent;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private int rowHeight;
	private int page;

	public GuiScreenPvpQolSettings(PvpClient pvpClient, GuiScreen parent) {
		this.pvpClient = pvpClient;
		this.parent = parent;
	}

	@Override
	public void initGui() {
		panelWidth = Math.min(390, Math.max(1, width - 8));
		panelHeight = Math.min(226, Math.max(112, height - 8));
		panelX = (width - panelWidth) / 2;
		panelY = Math.max(2, (height - panelHeight) / 2);
		int rows = (PAGES[page].length + 1) / 2;
		rowHeight = Math.max(10, Math.min(22,
				(panelHeight - 77 - Math.max(0, rows - 1) * 3) / Math.max(1, rows)));
		buttonList.clear();
		int gap = 4;
		int buttonWidth = Math.max(1, (panelWidth - 24 - gap) / 2);
		int buttonX = panelX + 8;
		int startY = panelY + 36;
		int[] pageIds = PAGES[page];
		for(int i = 0; i < pageIds.length; ++i) {
			int column = i % 2;
			int row = i / 2;
			buttonList.add(new GuiButton(pageIds[i], buttonX + column * (buttonWidth + gap),
					startY + row * (rowHeight + 3), buttonWidth, rowHeight, getLabel(pageIds[i])));
		}
		int footerY = panelY + panelHeight - 25;
		buttonList.add(new GuiButton(12, panelX + 8, footerY, 42, 20, "<"));
		buttonList.add(new GuiButton(13, panelX + 54, footerY, 42, 20, ">"));
		buttonList.add(new GuiButton(11, panelX + panelWidth - 82, footerY, 74, 20, "Done"));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE8121E2A);
		drawRect(panelX, panelY, panelX + panelWidth, panelY + 2, 0xFF53D6A3);
		drawRect(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, 0xAA53D6A3);
		drawCenteredString(fontRendererObj, "DICESTACK  /  " + pageTitle(), width / 2, panelY + 10, 0xFFF1F7F5);
		drawCenteredString(fontRendererObj, (page + 1) + " / " + PAGES.length, width / 2, panelY + 22, 0xFF9BA8B8);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		PvpConfig config = pvpClient.getConfigStore().getConfig();
		switch(button.id) {
		case 11:
			mc.displayGuiScreen(parent);
			return;
		case 12:
			page = (page + PAGES.length - 1) % PAGES.length;
			initGui();
			return;
		case 13:
			page = (page + 1) % PAGES.length;
			initGui();
			return;
		case 1: config.toggleSprintEnabled = !config.toggleSprintEnabled; break;
		case 2: config.toggleSneakEnabled = !config.toggleSneakEnabled; break;
		case 3: config.zoomEnabled = !config.zoomEnabled; break;
		case 4: config.zoomToggle = !config.zoomToggle; break;
		case 5: config.zoomStrength = Math.max(4.0f, config.zoomStrength - 2.0f); break;
		case 6: config.zoomStrength = Math.min(32.0f, config.zoomStrength + 2.0f); break;
		case 7: config.coordinateFormat = next(config.coordinateFormat); break;
		case 8: config.clock24Hour = !config.clock24Hour; break;
		case 9: config.clockShowSeconds = !config.clockShowSeconds; break;
		case 10: pvpClient.reconnect(this); return;
		case 20: config.noDynamicFov = !config.noDynamicFov; break;
		case 21: config.noHurtTilt = !config.noHurtTilt; break;
		case 22: config.noFog = !config.noFog; break;
		case 23: config.hideWeather = !config.hideWeather; break;
		case 24: config.fullbright = !config.fullbright; break;
		case 25: config.hideVignette = !config.hideVignette; break;
		case 26: config.disableEntityShadows = !config.disableEntityShadows; break;
		case 27: mc.gameSettings.viewBobbing = !mc.gameSettings.viewBobbing; mc.gameSettings.saveOptions(); break;
		case 28: mc.gameSettings.smoothCamera = !mc.gameSettings.smoothCamera; mc.gameSettings.saveOptions(); break;
		case 30: config.hideFireOverlay = !config.hideFireOverlay; break;
		case 31: config.hidePumpkinOverlay = !config.hidePumpkinOverlay; break;
		case 32: config.hidePortalOverlay = !config.hidePortalOverlay; break;
		case 33: config.hideChat = !config.hideChat; break;
		case 34: config.hideServerAddress = !config.hideServerAddress; break;
		case 38: mc.gameSettings.particleSetting = (mc.gameSettings.particleSetting + 1) % 3; mc.gameSettings.saveOptions(); break;
		case 35: config.crosshairEnabled = !config.crosshairEnabled; break;
		case 36: config.crosshairHideInMenus = !config.crosshairHideInMenus; break;
		case 37: config.crosshairHideInThirdPerson = !config.crosshairHideInThirdPerson; break;
		case 40: config.crosshairStyle = next(config.crosshairStyle); break;
		case 41: config.crosshairColor = nextColor(config.crosshairColor); break;
		case 42: config.crosshairSize = Math.max(1, config.crosshairSize - 1); break;
		case 43: config.crosshairSize = Math.min(16, config.crosshairSize + 1); break;
		case 44: config.crosshairThickness = Math.max(1, config.crosshairThickness - 1); break;
		case 45: config.crosshairThickness = Math.min(6, config.crosshairThickness + 1); break;
		case 46: config.crosshairGap = Math.max(0, config.crosshairGap - 1); break;
		case 47: config.crosshairGap = Math.min(12, config.crosshairGap + 1); break;
		case 50: config.crosshairOpacity = Math.max(0.1f, config.crosshairOpacity - 0.1f); break;
		case 51: config.crosshairOpacity = Math.min(1.0f, config.crosshairOpacity + 0.1f); break;
		case 52: config.crosshairOutline = !config.crosshairOutline; break;
		case 53: config.crosshairDynamicColor = !config.crosshairDynamicColor; break;
		case 54: config.lowHealthThreshold = Math.max(1.0f, config.lowHealthThreshold - 1.0f); break;
		case 55: config.lowHealthThreshold = Math.min(20.0f, config.lowHealthThreshold + 1.0f); break;
		case 56: config.crosshairHideInMenus = !config.crosshairHideInMenus; break;
		case 57: config.crosshairHideInThirdPerson = !config.crosshairHideInThirdPerson; break;
		default: return;
		}
		pvpClient.getConfigStore().markDirty();
		initGui();
	}

	private String getLabel(int id) {
		PvpConfig c = pvpClient.getConfigStore().getConfig();
		switch(id) {
		case 1: return "Toggle Sprint: " + on(c.toggleSprintEnabled);
		case 2: return "Toggle Sneak: " + on(c.toggleSneakEnabled);
		case 3: return "Zoom: " + on(c.zoomEnabled);
		case 4: return c.zoomToggle ? "Zoom Mode: Toggle" : "Zoom Mode: Hold";
		case 5: return "Zoom Strength -";
		case 6: return "Zoom Strength +";
		case 7: return "Coordinate Format";
		case 8: return "Clock: " + (c.clock24Hour ? "24-hour" : "12-hour");
		case 9: return "Clock Seconds: " + on(c.clockShowSeconds);
		case 10: return c.lastServerAddress == null || c.lastServerAddress.isEmpty()
				? "No Previous Server" : "Quick Reconnect";
		case 20: return "No Dynamic FOV: " + on(c.noDynamicFov);
		case 21: return "No Hurt Tilt: " + on(c.noHurtTilt);
		case 22: return "No Fog: " + on(c.noFog);
		case 23: return "Hide Rain/Snow: " + on(c.hideWeather);
		case 24: return "Fullbright: " + on(c.fullbright);
		case 25: return "Hide Vignette: " + on(c.hideVignette);
		case 26: return "Entity Shadows: " + (c.disableEntityShadows ? "Off" : "On");
		case 27: return "View Bobbing: " + on(mc.gameSettings.viewBobbing);
		case 28: return "Smooth Camera: " + on(mc.gameSettings.smoothCamera);
		case 30: return "Hide Fire Overlay: " + on(c.hideFireOverlay);
		case 31: return "Hide Pumpkin Overlay: " + on(c.hidePumpkinOverlay);
		case 32: return "Hide Portal Overlay: " + on(c.hidePortalOverlay);
		case 33: return "Hide Chat: " + on(c.hideChat);
		case 34: return "Hide Server Address: " + on(c.hideServerAddress);
		case 35: return "Custom Crosshair: " + on(c.crosshairEnabled);
		case 38: return "Particles: " + (mc.gameSettings.particleSetting == 0 ? "All"
				: mc.gameSettings.particleSetting == 1 ? "Reduced" : "Minimal");
		case 40: return "Style: " + c.crosshairStyle.name().replace('_', ' ');
		case 41: return "Crosshair Colour";
		case 42: return "Size -";
		case 43: return "Size +";
		case 44: return "Thickness -";
		case 45: return "Thickness +";
		case 46: return "Gap -";
		case 47: return "Gap +";
		case 50: return "Opacity - " + Math.round(c.crosshairOpacity * 100.0f) + "%";
		case 51: return "Opacity + " + Math.round(c.crosshairOpacity * 100.0f) + "%";
		case 52: return "Outline: " + on(c.crosshairOutline);
		case 53: return "Target Colour: " + on(c.crosshairDynamicColor);
		case 54: return "Low Health Threshold -";
		case 55: return "Low Health Threshold +";
		case 56: return "Hide Crosshair in Menus: " + on(c.crosshairHideInMenus);
		case 57: return "Hide Crosshair in 3rd Person: " + on(c.crosshairHideInThirdPerson);
		default: return "";
		}
	}

	private String pageTitle() {
		switch(page) {
		case 1: return "CAMERA & WORLD";
		case 2: return "OVERLAYS & PRIVACY";
		case 3: return "CROSSHAIR";
		case 4: return "CROSSHAIR & WARNINGS";
		default: return "QUALITY OF LIFE";
		}
	}

	private PvpConfig.CoordinateFormat next(PvpConfig.CoordinateFormat current) {
		switch(current) {
		case LABELLED: return PvpConfig.CoordinateFormat.COMMAS;
		case COMMAS: return PvpConfig.CoordinateFormat.SLASHES;
		default: return PvpConfig.CoordinateFormat.LABELLED;
		}
	}

	private PvpConfig.CrosshairStyle next(PvpConfig.CrosshairStyle current) {
		int next = (current.ordinal() + 1) % PvpConfig.CrosshairStyle.values().length;
		return PvpConfig.CrosshairStyle.values()[next];
	}

	private int nextColor(int current) {
		for(int i = 0; i < CROSSHAIR_COLORS.length; ++i) {
			int candidate = 0xFF000000 | (CROSSHAIR_COLORS[i][0] << 16)
					| (CROSSHAIR_COLORS[i][1] << 8) | CROSSHAIR_COLORS[i][2];
			if(candidate == current) {
				int[] next = CROSSHAIR_COLORS[(i + 1) % CROSSHAIR_COLORS.length];
				return 0xFF000000 | (next[0] << 16) | (next[1] << 8) | next[2];
			}
		}
		int[] first = CROSSHAIR_COLORS[0];
		return 0xFF000000 | (first[0] << 16) | (first[1] << 8) | first[2];
	}

	private String on(boolean value) {
		return value ? "On" : "Off";
	}
}
