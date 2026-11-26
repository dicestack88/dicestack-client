package net.lax1dude.eaglercraft.v1_8.pvp;

import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GuiScreenPvpQolSettings extends GuiScreen {

	private static final int[][] PAGES = {
			{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
			{ 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 58, 59, 65, 66, 67, 68, 69, 102, 104 },
			{ 30, 31, 32, 33, 34, 35, 38, 70, 71, 72, 73, 74, 75, 76, 93, 94, 95, 96, 100 },
			{ 40, 41, 42, 43, 44, 45, 46, 47 },
			{ 50, 51, 52, 53, 54, 55, 56, 57 },
			{ 60, 61, 62, 63, 64, 78, 101 },
			{ 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 97, 98, 99, 103 }
	};
	private static final int[][] CROSSHAIR_COLORS = {
			{ 255, 255, 255 }, { 83, 214, 163 }, { 255, 85, 85 }, { 255, 209, 102 }, { 112, 167, 255 }
	};

	private final PvpClient pvpClient;
	private final GuiScreen parent;
	private GuiTextField searchField;
	private String searchText = "";
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private int rowHeight;
	private int page;
	private int settingsPage;
	private int footerY;
	private String titleText;
	private String searchHint;
	private String pageLabel;
	private int searchFieldWidth;

	public GuiScreenPvpQolSettings(PvpClient pvpClient, GuiScreen parent) {
		this(pvpClient, parent, 0, "");
	}

	public GuiScreenPvpQolSettings(PvpClient pvpClient, GuiScreen parent, int initialPage, String initialSearch) {
		this.pvpClient = pvpClient;
		this.parent = parent;
		this.page = Math.max(0, Math.min(PAGES.length - 1, initialPage));
		this.searchText = initialSearch == null ? "" : initialSearch;
	}

	@Override
	public void initGui() {
		fontRendererObj = PvpUiFontRenderer.get(mc);
		panelWidth = Math.max(1, Math.min(560, width - 8));
		panelHeight = Math.max(1, Math.min(340, height - 8));
		panelX = (width - panelWidth) / 2;
		panelY = Math.max(0, (height - panelHeight) / 2);
		buttonList.clear();
		searchFieldWidth = Math.max(1, panelWidth - 16);
		searchField = new GuiTextField(100, fontRendererObj, panelX + 8, panelY + 31, searchFieldWidth, 18);
		searchField.setMaxStringLength(32);
		searchField.setText(searchText);
		settingsPage = Math.min(settingsPage, getSettingsPageCount() - 1);
		titleText = fontRendererObj.trimStringToWidth("DICESTACK  /  " + pageTitle(),
				Math.max(0, panelWidth - 16));
		searchHint = fontRendererObj.trimStringToWidth("Search all settings...",
				Math.max(0, searchFieldWidth - 8));
		List<Integer> displayedIds = getDisplayedButtonIds();
		int rows = (displayedIds.size() + 1) / 2;
		int footerHeight = Math.min(20, Math.max(1, panelHeight - 1));
		footerY = panelY + panelHeight - footerHeight - 4;
		int availableListHeight = Math.max(1, footerY - (panelY + 55) - 4);
		rowHeight = Math.max(1, Math.min(22,
				(availableListHeight - Math.max(0, rows - 1) * 2) / Math.max(1, rows)));
		int gap = 4;
		int buttonWidth = Math.max(1, (panelWidth - 24 - gap) / 2);
		int buttonX = panelX + 8;
		int startY = panelY + 55;
		for(int i = 0; i < displayedIds.size(); ++i) {
			int column = i % 2;
			int row = i / 2;
			int id = displayedIds.get(i);
			int x = buttonX + column * (buttonWidth + gap);
			int y = startY + row * (rowHeight + 2);
			PvpSliderButton slider = createSlider(id, x, y, buttonWidth, rowHeight);
			buttonList.add(slider == null ? new PvpGuiButton(id, x, y, buttonWidth, rowHeight, getLabel(id)) : slider);
		}
		int footerGap = Math.min(4, Math.max(0, (panelWidth - 3) / 6));
		int footerWidth = Math.max(1, (panelWidth - 16 - footerGap * 2) / 3);
		buttonList.add(new PvpGuiButton(12, panelX + 8, footerY, footerWidth, footerHeight, "<"));
		buttonList.add(new PvpGuiButton(13, panelX + 8 + footerWidth + footerGap, footerY, footerWidth,
				footerHeight, ">"));
		buttonList.add(new PvpGuiButton(11, panelX + 8 + (footerWidth + footerGap) * 2, footerY,
				footerWidth, footerHeight, "Done"));
		pageLabel = (settingsPage + 1) + " / " + getSettingsPageCount();
	}

	@Override
	public void updateScreen() {
		searchField.updateCursorCounter();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		HudAppearanceConfig appearance = pvpClient.getConfigStore().getConfig().appearance;
		HudRenderer.drawPanel(appearance, panelX, panelY, panelWidth, panelHeight);
		drawCenteredString(fontRendererObj, titleText, width / 2, panelY + 9, 0xFFF1F7F5);
		if(searchField.getText().isEmpty()) {
			fontRendererObj.drawStringWithShadow(searchHint, searchField.xPosition + 4,
					searchField.yPosition + 5, 0xFF758396);
		}
		searchField.drawTextBox();
		drawCenteredString(fontRendererObj, pageLabel, width / 2,
				footerY - 10, 0xFF9BA8B8);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button instanceof PvpSliderButton) {
			return;
		}
		PvpConfig config = pvpClient.getConfigStore().getConfig();
		switch(button.id) {
		case 11:
			mc.displayGuiScreen(parent);
			return;
		case 12:
			if(settingsPage > 0) {
				--settingsPage;
			}else if(searchText.isEmpty()) {
				page = (page + PAGES.length - 1) % PAGES.length;
				settingsPage = getSettingsPageCount() - 1;
			}else {
				settingsPage = getSettingsPageCount() - 1;
			}
			initGui();
			return;
		case 13:
			if(settingsPage + 1 < getSettingsPageCount()) {
				++settingsPage;
			}else if(searchText.isEmpty()) {
				page = (page + 1) % PAGES.length;
				settingsPage = 0;
			}else {
				settingsPage = 0;
			}
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
		case 29: config.customFovEnabled = !config.customFovEnabled; break;
		case 102: config.freelookEnabled = !config.freelookEnabled; break;
		case 103: config.vanillaFoodOverlayEnabled = !config.vanillaFoodOverlayEnabled; break;
		case 104: config.freelookToggleMode = !config.freelookToggleMode; break;
		case 58: config.customFov = Math.max(30.0f, config.customFov - 5.0f); break;
		case 59: config.customFov = Math.min(110.0f, config.customFov + 5.0f); break;
		case 60:
			config.chatTimestamps = !config.chatTimestamps;
			mc.ingameGUI.getChatGUI().refreshChat();
			break;
		case 61:
			config.chatTimestamp24Hour = !config.chatTimestamp24Hour;
			mc.ingameGUI.getChatGUI().refreshChat();
			break;
		case 62: config.chatHighlightMentions = !config.chatHighlightMentions; break;
		case 63: mc.gameSettings.chatOpacity = Math.max(0.0f, mc.gameSettings.chatOpacity - 0.1f); mc.gameSettings.saveOptions(); break;
		case 64: mc.gameSettings.chatOpacity = Math.min(1.0f, mc.gameSettings.chatOpacity + 0.1f); mc.gameSettings.saveOptions(); break;
		case 65: config.thirdPersonDistance = Math.max(2.0f, config.thirdPersonDistance - 0.5f); break;
		case 66: config.thirdPersonDistance = Math.min(12.0f, config.thirdPersonDistance + 0.5f); break;
		case 67: config.zoomSmoothness=Math.max(0,config.zoomSmoothness-10);break;
		case 68: config.zoomSmoothness=Math.min(100,config.zoomSmoothness+10);break;
		case 69: config.localTime=Math.max(0,config.localTime-1000);break;
		case 93: config.localTime=Math.min(24000,config.localTime+1000);break;
		case 70: config.clearWater = !config.clearWater; break;
		case 71: config.customFogColorEnabled = !config.customFogColorEnabled; break;
		case 72: config.localTimeEnabled = !config.localTimeEnabled; break;
		case 73: config.streamerMode = !config.streamerMode; break;
		case 74: config.hideUsername = !config.hideUsername; break;
		case 75: config.notificationsEnabled = !config.notificationsEnabled; break;
		case 76: config.screenshotMode = !config.screenshotMode; break;
		case 78: config.chatCompact = !config.chatCompact; break;
		case 101:
			config.chatBackgroundColor=config.chatBackgroundColor==0xFF000000?0xFF172638:
				config.chatBackgroundColor==0xFF172638?0xFF26384B:0xFF000000;
			break;
		case 79:
			int[] fpsLimits = {30,60,120,144,260};
			int index=0;
			for(int i=0;i<fpsLimits.length;++i) if(config.fpsLimit==fpsLimits[i]) {index=i;break;}
			config.fpsLimit=fpsLimits[(index+1)%fpsLimits.length];
			mc.gameSettings.limitFramerate=config.fpsLimit;
			mc.gameSettings.saveOptions();
			break;
		case 80:
			PvpConfig.PerformanceProfile profile=config.performanceProfile;
			int nextProfile=(profile.ordinal()+1)%PvpConfig.PerformanceProfile.values().length;
			config.applyPerformanceProfile(PvpConfig.PerformanceProfile.values()[nextProfile]);
			mc.gameSettings.limitFramerate=config.fpsLimit;
			mc.gameSettings.saveOptions();
			break;
		case 81: pvpClient.exportConfig(); return;
		case 82: pvpClient.importConfigFromClipboard(); break;
		case 83: config.resetVisualCategory(); break;
		case 84: config.resetPrivacyCategory(); break;
		case 85: config.resetChatCategory(); break;
		case 86: config.heldItemScale=Math.max(0.5f,config.heldItemScale-0.1f);break;
		case 87: config.heldItemScale=Math.min(2.0f,config.heldItemScale+0.1f);break;
		case 88: config.heldItemOffsetX=Math.max(-1.0f,config.heldItemOffsetX-0.05f);break;
		case 89: config.heldItemOffsetX=Math.min(1.0f,config.heldItemOffsetX+0.05f);break;
		case 90: config.heldItemOffsetY=Math.max(-1.0f,config.heldItemOffsetY-0.05f);break;
		case 91: config.heldItemOffsetY=Math.min(1.0f,config.heldItemOffsetY+0.05f);break;
		case 92: config.glintEnabled=!config.glintEnabled;break;
		case 94: config.blockOutlineEnabled=!config.blockOutlineEnabled;break;
		case 95: config.blockOutlineThickness=Math.max(1,config.blockOutlineThickness-1);break;
		case 96: config.blockOutlineThickness=Math.min(8,config.blockOutlineThickness+1);break;
		case 97: config.glintIntensity=Math.max(0.0f,config.glintIntensity-0.1f);break;
		case 98: config.glintIntensity=Math.min(2.0f,config.glintIntensity+0.1f);break;
		case 99:
			config.glintColor=config.glintColor==0xFF5030FF?0xFF55DDA0:
				config.glintColor==0xFF55DDA0?0xFFFF5555:0xFF5030FF;
			break;
		case 100:
			config.customFogColor=config.customFogColor==0xFFB6C8D8?0xFFB8A8D8:
				config.customFogColor==0xFFB8A8D8?0xFFD8B6A8:0xFFB6C8D8;
			break;
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
		if(!searchText.isEmpty()) {
			settingsPage = 0;
		}
		initGui();
	}

	@Override
	protected void keyTyped(char character, int keyCode) {
		if(keyCode == 1 || keyCode == mc.gameSettings.keyBindClose.getKeyCode()) {
			mc.displayGuiScreen(parent);
			return;
		}
		if(searchField.textboxKeyTyped(character, keyCode)) {
			String nextSearch = searchField.getText();
			if(!nextSearch.equals(searchText)) {
				searchText = nextSearch;
				settingsPage = 0;
				initGui();
			}
			return;
		}
		super.keyTyped(character, keyCode);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		searchField.mouseClicked(mouseX, mouseY, button);
		super.mouseClicked(mouseX, mouseY, button);
	}

	private List<Integer> getDisplayedButtonIds() {
		List<Integer> settings = new ArrayList<>();
		if(searchText.isEmpty()) {
			for(int id : PAGES[page]) {
				if(!isSliderCompanion(id)) {
					settings.add(id);
				}
			}
		}else {
			settings = getFilteredSettingIds();
		}
		int pageSize = getSettingsPageSize();
		int start = Math.min(settingsPage * pageSize, settings.size());
		return new ArrayList<>(settings.subList(start, Math.min(start + pageSize, settings.size())));
	}

	private int getSettingsPageSize() {
		int footerHeight = Math.min(20, Math.max(1, panelHeight - 1));
		int footerY = panelY + panelHeight - footerHeight - 4;
		int availableListHeight = Math.max(1, footerY - (panelY + 55) - 4);
		int rows = Math.max(1, (availableListHeight + 2) / 24);
		return rows * 2;
	}

	private int getSettingsPageCount() {
		int settingsCount = 0;
		if(searchText.isEmpty()) {
			for(int id : PAGES[page]) {
				if(!isSliderCompanion(id)) {
					++settingsCount;
				}
			}
		}else {
			settingsCount = getFilteredSettingIds().size();
		}
		return Math.max(1, (settingsCount + getSettingsPageSize() - 1) / getSettingsPageSize());
	}

	private List<Integer> getFilteredSettingIds() {
		List<Integer> matches = new ArrayList<>();
		String query = searchText.toLowerCase(Locale.ROOT);
		for(int[] ids : PAGES) {
			for(int id : ids) {
				if(!isSliderCompanion(id) && getSearchLabel(id).toLowerCase(Locale.ROOT).contains(query)) {
					matches.add(id);
				}
			}
		}
		return matches;
	}

	private boolean isSliderCompanion(int id) {
		switch(id) {
		case 6:
		case 43:
		case 45:
		case 47:
		case 51:
		case 55:
		case 59:
		case 64:
		case 66:
		case 68:
		case 87:
		case 89:
		case 91:
		case 93:
		case 96:
		case 98:
			return true;
		default:
			return false;
		}
	}

	private String getSearchLabel(int id) {
		String sliderLabel = getSliderLabel(id);
		if(sliderLabel == null) {
			return getLabel(id);
		}
		return sliderLabel + " " + getLabel(id) + " " + getLabel(getSliderCompanion(id));
	}

	private int getSliderCompanion(int id) {
		switch(id) {
		case 5: return 6;
		case 42: return 43;
		case 44: return 45;
		case 46: return 47;
		case 50: return 51;
		case 54: return 55;
		case 58: return 59;
		case 63: return 64;
		case 65: return 66;
		case 67: return 68;
		case 69: return 93;
		case 86: return 87;
		case 88: return 89;
		case 90: return 91;
		case 95: return 96;
		case 97: return 98;
		default: return -1;
		}
	}

	private String getSliderLabel(int id) {
		switch(id) {
		case 5: return "Zoom Strength";
		case 42: return "Crosshair Size";
		case 44: return "Crosshair Thickness";
		case 46: return "Crosshair Gap";
		case 50: return "Crosshair Opacity";
		case 54: return "Low Health Threshold";
		case 58: return "Custom FOV";
		case 63: return "Chat Opacity";
		case 65: return "Third-Person Distance";
		case 67: return "Zoom Smoothness";
		case 69: return "Local Time";
		case 86: return "Held Item Scale";
		case 88: return "Held Item X Offset";
		case 90: return "Held Item Y Offset";
		case 95: return "Block Outline Thickness";
		case 97: return "Glint Intensity";
		default: return null;
		}
	}

	private PvpSliderButton createSlider(int id, int x, int y, int width, int height) {
		String label = getSliderLabel(id);
		if(label == null) {
			return null;
		}
		float minimum;
		float maximum;
		float step;
		switch(id) {
		case 5: minimum = 4.0f; maximum = 32.0f; step = 0.5f; break;
		case 42: minimum = 1.0f; maximum = 16.0f; step = 1.0f; break;
		case 44: minimum = 1.0f; maximum = 6.0f; step = 1.0f; break;
		case 46: minimum = 0.0f; maximum = 12.0f; step = 1.0f; break;
		case 50: minimum = 0.1f; maximum = 1.0f; step = 0.01f; break;
		case 54: minimum = 1.0f; maximum = 20.0f; step = 0.5f; break;
		case 58: minimum = 30.0f; maximum = 110.0f; step = 1.0f; break;
		case 63: minimum = 0.0f; maximum = 1.0f; step = 0.01f; break;
		case 65: minimum = 2.0f; maximum = 12.0f; step = 0.1f; break;
		case 67: minimum = 0.0f; maximum = 100.0f; step = 1.0f; break;
		case 69: minimum = 0.0f; maximum = 24000.0f; step = 100.0f; break;
		case 86: minimum = 0.5f; maximum = 2.0f; step = 0.05f; break;
		case 88:
		case 90: minimum = -1.0f; maximum = 1.0f; step = 0.01f; break;
		case 95: minimum = 1.0f; maximum = 8.0f; step = 1.0f; break;
		case 97: minimum = 0.0f; maximum = 2.0f; step = 0.01f; break;
		default: return null;
		}
		return new PvpSliderButton(id, x, y, width, height, label, minimum, maximum, step,
				getSliderValue(id), value -> setSliderValue(id, value),
				value -> formatSliderValue(id, value));
	}

	private float getSliderValue(int id) {
		PvpConfig config = pvpClient.getConfigStore().getConfig();
		switch(id) {
		case 5: return config.zoomStrength;
		case 42: return config.crosshairSize;
		case 44: return config.crosshairThickness;
		case 46: return config.crosshairGap;
		case 50: return config.crosshairOpacity;
		case 54: return config.lowHealthThreshold;
		case 58: return config.customFov;
		case 63: return mc.gameSettings.chatOpacity;
		case 65: return config.thirdPersonDistance;
		case 67: return config.zoomSmoothness;
		case 69: return config.localTime;
		case 86: return config.heldItemScale;
		case 88: return config.heldItemOffsetX;
		case 90: return config.heldItemOffsetY;
		case 95: return config.blockOutlineThickness;
		case 97: return config.glintIntensity;
		default: return 0.0f;
		}
	}

	private void setSliderValue(int id, float value) {
		PvpConfig config = pvpClient.getConfigStore().getConfig();
		switch(id) {
		case 5: config.zoomStrength = value; break;
		case 42: config.crosshairSize = Math.round(value); break;
		case 44: config.crosshairThickness = Math.round(value); break;
		case 46: config.crosshairGap = Math.round(value); break;
		case 50: config.crosshairOpacity = value; break;
		case 54: config.lowHealthThreshold = value; break;
		case 58: config.customFov = value; break;
		case 63:
			mc.gameSettings.chatOpacity = value;
			mc.gameSettings.saveOptions();
			break;
		case 65: config.thirdPersonDistance = value; break;
		case 67: config.zoomSmoothness = Math.round(value); break;
		case 69: config.localTime = Math.round(value); break;
		case 86: config.heldItemScale = value; break;
		case 88: config.heldItemOffsetX = value; break;
		case 90: config.heldItemOffsetY = value; break;
		case 95: config.blockOutlineThickness = Math.round(value); break;
		case 97: config.glintIntensity = value; break;
		default: return;
		}
		pvpClient.getConfigStore().markDirty();
	}

	private String formatSliderValue(int id, float value) {
		switch(id) {
		case 5: return oneDecimal(value) + "x";
		case 42:
		case 44:
		case 46:
		case 95: return Integer.toString(Math.round(value)) + " px";
		case 50:
		case 63:
		case 67: return Math.round(value * (id == 67 ? 1.0f : 100.0f)) + "%";
		case 54: return oneDecimal(value) + " HP";
		case 58: return Math.round(value) + " deg";
		case 65: return oneDecimal(value) + " blocks";
		case 69: return formatWorldTime(Math.round(value));
		case 86:
		case 97: return twoDecimal(value) + "x";
		case 88:
		case 90: return twoDecimal(value);
		default: return Float.toString(value);
		}
	}

	private String oneDecimal(float value) {
		return Float.toString(Math.round(value * 10.0f) / 10.0f);
	}

	private String twoDecimal(float value) {
		int hundredths = Math.round(Math.abs(value) * 100.0f);
		return (value < 0.0f ? "-" : "") + hundredths / 100 + "."
				+ (hundredths % 100 < 10 ? "0" : "") + hundredths % 100;
	}

	private String formatWorldTime(int ticks) {
		int minuteOfDay = (ticks % 24000) * 1440 / 24000;
		int hours = minuteOfDay / 60;
		int minutes = minuteOfDay % 60;
		return (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes;
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
		case 29: return "Custom FOV: " + on(c.customFovEnabled);
		case 102: return "Third-Person Freelook: " + on(c.freelookEnabled);
		case 103: return "Vanilla Food Overlay: " + on(c.vanillaFoodOverlayEnabled);
		case 104: return "Freelook Mode: " + (c.freelookToggleMode ? "Toggle" : "Hold");
		case 58: return "Custom FOV " + Math.round(c.customFov) + " -";
		case 59: return "Custom FOV " + Math.round(c.customFov) + " +";
		case 60: return "Chat Timestamps: " + on(c.chatTimestamps);
		case 61: return "Timestamp Format: " + (c.chatTimestamp24Hour ? "24-hour" : "12-hour");
		case 62: return "Highlight Own Name: " + on(c.chatHighlightMentions);
		case 63: return "Chat Opacity - " + Math.round(mc.gameSettings.chatOpacity * 100.0f) + "%";
		case 64: return "Chat Opacity + " + Math.round(mc.gameSettings.chatOpacity * 100.0f) + "%";
		case 65: return "Third Person Distance -";
		case 66: return "Third Person Distance +";
		case 67: return "Zoom Smoothness -";
		case 68: return "Zoom Smoothness: " + c.zoomSmoothness + "%";
		case 69: return "Local Time -";
		case 93: return "Local Time " + c.localTime + " +";
		case 70: return "Clear Water: " + on(c.clearWater);
		case 71: return "Custom Fog Colour: " + on(c.customFogColorEnabled);
		case 72: return "Local Time Override: " + on(c.localTimeEnabled);
		case 73: return "Streamer Mode: " + on(c.streamerMode);
		case 74: return "Hide Username: " + on(c.hideUsername);
		case 75: return "Notifications: " + on(c.notificationsEnabled);
		case 76: return "Screenshot Mode: " + on(c.screenshotMode);
		case 78: return "Compact Chat: " + on(c.chatCompact);
		case 101: return "Chat Background";
		case 79: return "FPS Limit: " + c.fpsLimit;
		case 80: return "Performance: " + c.performanceProfile.name();
		case 81: return "Export Configuration";
		case 82: return "Import from Clipboard";
		case 83: return "Reset Visual Category";
		case 84: return "Reset Privacy Category";
		case 85: return "Reset Chat Category";
		case 86: return "Held Item Size -";
		case 87: return "Held Item Size +";
		case 88: return "Held Item X -";
		case 89: return "Held Item X +";
		case 90: return "Held Item Y -";
		case 91: return "Held Item Y +";
		case 92: return "Enchantment Glint: " + on(c.glintEnabled);
		case 94: return "Block Outline: " + on(c.blockOutlineEnabled);
		case 95: return "Outline Thickness -";
		case 96: return "Outline Thickness " + c.blockOutlineThickness + " +";
		case 97: return "Glint Intensity -";
		case 98: return "Glint Intensity " + Math.round(c.glintIntensity*100.0f)+"%";
		case 99: return "Glint Colour";
		case 100: return "Fog Colour";
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
		if(!searchText.isEmpty()) {
			return "SEARCH RESULTS";
		}
		switch(page) {
		case 1: return "CAMERA & WORLD";
		case 2: return "OVERLAYS & PRIVACY";
		case 3: return "CROSSHAIR";
		case 4: return "CROSSHAIR & WARNINGS";
		case 5: return "CHAT DISPLAY";
		case 6: return "PERFORMANCE & DATA";
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
