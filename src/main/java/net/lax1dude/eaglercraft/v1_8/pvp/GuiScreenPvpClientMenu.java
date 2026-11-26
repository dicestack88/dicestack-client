package net.lax1dude.eaglercraft.v1_8.pvp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElementConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudManager;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public final class GuiScreenPvpClientMenu extends GuiScreen {

	private static final String[] CATEGORIES = {
			"All Mods", "HUD", "Visuals", "Camera", "QoL", "Crosshair",
			"Performance", "Privacy", "Appearance", "Profiles"
	};
	private static final int ID_CATEGORY_BASE = 100;
	private static final int ID_MOD_BASE = 200;
	private static final int ID_PREVIOUS = 300;
	private static final int ID_NEXT = 301;
	private static final int ID_PROFILES = 302;
	private static final int ID_DONE = 303;

	private static final class ModEntry {
		final String name;
		final String category;
		final String description;
		final String configSearch;
		final int configPage;
		final HudElement hudElement;

		ModEntry(String name, String category, String description, String configSearch, int configPage) {
			this(name, category, description, configSearch, configPage, null);
		}

		ModEntry(String name, String category, String description, String configSearch, int configPage,
				HudElement hudElement) {
			this.name = name;
			this.category = category;
			this.description = description;
			this.configSearch = configSearch;
			this.configPage = configPage;
			this.hudElement = hudElement;
		}
	}

	private final PvpClient pvpClient;
	private final GuiScreen parent;
	private final List<ModEntry> allEntries = new ArrayList<>();
	private final List<ModEntry> filteredEntries = new ArrayList<>();
	private GuiTextField searchField;
	private String searchText = "";
	private String activeCategory = "All Mods";
	private String statusText = "";
	private int page;
	private int visibleRows;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private int sidebarWidth;
	private int contentX;
	private int contentWidth;
	private int listY;
	private int searchFieldWidth;

	public GuiScreenPvpClientMenu(PvpClient pvpClient, GuiScreen parent) {
		this.pvpClient = pvpClient;
		this.parent = parent;
	}

	@Override
	public void initGui() {
		fontRendererObj = PvpUiFontRenderer.get(mc);
		buttonList.clear();
		panelWidth = Math.max(1, Math.min(760, width - 12));
		panelHeight = Math.max(1, Math.min(480, height - 12));
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;
		sidebarWidth = panelWidth >= 620 ? 150 : Math.max(96, panelWidth / 4);
		contentX = panelX + sidebarWidth + 12;
		contentWidth = Math.max(1, panelX + panelWidth - 12 - contentX);

		searchFieldWidth = Math.max(1, contentWidth);
		searchField = new GuiTextField(100, fontRendererObj, contentX, panelY + 35,
				searchFieldWidth, 18);
		searchField.setMaxStringLength(48);
		searchField.setText(searchText);

		int sidebarY = panelY + 54;
		int sidebarGap = 3;
		int sidebarButtonHeight = Math.min(22, Math.max(14,
				(panelHeight - 78 - sidebarGap * (CATEGORIES.length - 1)) / CATEGORIES.length));
		for(int i = 0; i < CATEGORIES.length; ++i) {
			PvpGuiButton button = new PvpGuiButton(ID_CATEGORY_BASE + i,
					panelX + 10, sidebarY + i * (sidebarButtonHeight + sidebarGap),
					sidebarWidth - 20, sidebarButtonHeight, CATEGORIES[i]);
			buttonList.add(button);
		}

		buildEntries();
		visibleRows = Math.max(1, Math.min(10, (panelHeight - 118) / 34));
		refreshFilteredEntries();
		page = Math.min(page, getPageCount() - 1);
		listY = panelY + 64;

		int start = page * visibleRows;
		int end = Math.min(start + visibleRows, filteredEntries.size());
		for(int i = start; i < end; ++i) {
			ModEntry entry = filteredEntries.get(i);
			int row = i - start;
			int rowY = listY + row * 34;
			PvpToggleButton toggle = new PvpToggleButton(ID_MOD_BASE + allEntries.indexOf(entry),
					contentX + contentWidth - 58, rowY + 7, 50, 16, isEnabled(entry));
			buttonList.add(toggle);
		}

		int footerY = panelY + panelHeight - 31;
		int footerButtonWidth = Math.max(36, Math.min(58, contentWidth / 5));
		buttonList.add(new PvpGuiButton(ID_PREVIOUS, contentX, footerY, footerButtonWidth, 20, "<"));
		buttonList.add(new PvpGuiButton(ID_NEXT, contentX + footerButtonWidth + 4, footerY,
				footerButtonWidth, 20, ">"));
		buttonList.add(new PvpGuiButton(ID_PROFILES, contentX + contentWidth - footerButtonWidth * 2 - 4,
				footerY, footerButtonWidth, 20, "Profiles"));
		buttonList.add(new PvpGuiButton(ID_DONE, contentX + contentWidth - footerButtonWidth,
				footerY, footerButtonWidth, 20, "Done"));
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

		String title = "DICESTACK  /  MOD MENU";
		title = fontRendererObj.trimStringToWidth(title, sidebarWidth - 20);
		fontRendererObj.drawStringWithShadow(title, panelX + 10, panelY + 11, 0xFFF1F7F5);

		HudRenderer.drawRoundedRect(panelX + 6, panelY + 38, sidebarWidth - 12,
				panelHeight - 44, appearance.cornerRadius, 0xCC121923);
		HudRenderer.drawRoundedRect(contentX - 6, panelY + 6, contentWidth + 12,
				panelHeight - 12, appearance.cornerRadius, 0xC91B2633);

		fontRendererObj.drawStringWithShadow(activeCategory,
				contentX, panelY + 12, 0xFFF1F7F5);
		if(searchField.getText().isEmpty()) {
			fontRendererObj.drawStringWithShadow("Search mods and settings...",
					searchField.xPosition + 4, searchField.yPosition + 5, 0xFF758396);
		}
		searchField.drawTextBox();

		int start = page * visibleRows;
		int end = Math.min(start + visibleRows, filteredEntries.size());
		for(int i = start; i < end; ++i) {
			ModEntry entry = filteredEntries.get(i);
			int row = i - start;
			int rowY = listY + row * 34;
			boolean hovered = mouseX >= contentX && mouseX < contentX + contentWidth
					&& mouseY >= rowY && mouseY < rowY + 30;
			int fill = hovered ? 0xF02A3A4C : 0xE31B2633;
			HudRenderer.drawRoundedRect(contentX, rowY, contentWidth, 30,
					appearance.cornerRadius, fill);
			HudRenderer.drawRoundedBorder(contentX, rowY, contentWidth, 30,
					appearance.cornerRadius, 1,
					isEnabled(entry) ? 0x8853D6A3 : 0x883A4A5C);

			String name = fontRendererObj.trimStringToWidth(entry.name,
					Math.max(1, contentWidth - 72));
			fontRendererObj.drawStringWithShadow(name, contentX + 8, rowY + 5, 0xFFF1F7F5);
			String description = fontRendererObj.trimStringToWidth(entry.description,
					Math.max(1, contentWidth - 72));
			fontRendererObj.drawStringWithShadow(description, contentX + 8, rowY + 16, 0xFF9BA8B8);
		}
		if(filteredEntries.isEmpty()) {
			String message = "No mods match this search.";
			message = fontRendererObj.trimStringToWidth(message, Math.max(1, contentWidth - 16));
			fontRendererObj.drawStringWithShadow(message, contentX + 8, listY + 12, 0xFFADB9C6);
		}

		int pageCount = getPageCount();
		String pageLabel = (filteredEntries.isEmpty() ? "0" : Integer.toString(page + 1))
				+ " / " + pageCount;
		fontRendererObj.drawStringWithShadow(pageLabel,
				contentX + contentWidth / 2 - 20,
				panelY + panelHeight - 25,
				0xFF9BA8B8);
		if(statusText.length() > 0) {
			statusText = fontRendererObj.trimStringToWidth(statusText,
					Math.max(1, contentWidth - 16));
			fontRendererObj.drawStringWithShadow(statusText, contentX + 8,
					panelY + panelHeight - 43, 0xFFFFD166);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id >= ID_CATEGORY_BASE && button.id < ID_CATEGORY_BASE + CATEGORIES.length) {
			activeCategory = CATEGORIES[button.id - ID_CATEGORY_BASE];
			page = 0;
			statusText = "";
			initGui();
			return;
		}
		if(button.id >= ID_MOD_BASE && button.id < ID_PREVIOUS) {
			int index = button.id - ID_MOD_BASE;
			if(index >= 0 && index < allEntries.size()) {
				ModEntry entry = allEntries.get(index);
				setEnabled(entry, !isEnabled(entry));
				pvpClient.getConfigStore().markDirty();
				statusText = entry.name + " " + (isEnabled(entry) ? "enabled" : "disabled");
				initGui();
			}
			return;
		}
		switch(button.id) {
		case ID_PREVIOUS:
			page = Math.max(0, page - 1);
			initGui();
			break;
		case ID_NEXT:
			page = Math.min(getPageCount() - 1, page + 1);
			initGui();
			break;
		case ID_PROFILES:
			mc.displayGuiScreen(new GuiScreenPvpHudProfiles(pvpClient, this));
			break;
		case ID_DONE:
			mc.displayGuiScreen(parent);
			if(parent == null && mc.theWorld != null && mc.thePlayer != null) {
				mc.setIngameFocus();
			}
			break;
		}
	}

	private void buildEntries() {
		allEntries.clear();
		allEntries.add(new ModEntry("Toggle Sprint", "QoL", "Automatically keep sprint active",
				"Toggle Sprint", 0));
		allEntries.add(new ModEntry("Toggle Sneak", "QoL", "Keep sneak toggled while walking",
				"Toggle Sneak", 0));
		allEntries.add(new ModEntry("Zoom", "Camera", "Configurable camera zoom",
				"Zoom", 0));
		allEntries.add(new ModEntry("Freelook", "Camera", "Orbit while preserving movement",
				"Freelook", 1));
		allEntries.add(new ModEntry("Custom FOV", "Camera", "Independent field-of-view settings",
				"FOV", 1));
		allEntries.add(new ModEntry("Crosshair", "Crosshair", "Crosshair style, size and colour",
				"Crosshair", 4));
		allEntries.add(new ModEntry("Chat", "QoL", "Timestamps, mentions and compact chat",
				"Chat", 5));
		allEntries.add(new ModEntry("Streamer Mode", "Privacy", "Hide identifying information",
				"Streamer", 2));
		allEntries.add(new ModEntry("Privacy", "Privacy", "Hide chat, addresses and overlays",
				"Privacy", 2));
		allEntries.add(new ModEntry("Fullbright", "Visuals", "Increase world visibility",
				"Fullbright", 1));
		allEntries.add(new ModEntry("Fog and Weather", "Visuals", "Environment rendering controls",
				"Fog", 1));
		allEntries.add(new ModEntry("Block Outline", "Visuals", "Custom block outline appearance",
				"Block Outline", 2));
		allEntries.add(new ModEntry("Held Item", "Visuals", "Scale and offset held items",
				"Held Item", 2));
		allEntries.add(new ModEntry("Performance", "Performance", "FPS limit and performance profile",
				"FPS", 6));
		allEntries.add(new ModEntry("Notifications", "QoL", "Toggle Dicestack notifications",
				"Notifications", 2));
		allEntries.add(new ModEntry("Appearance", "Appearance", "Modern Glass and HUD appearance",
				"Appearance", -1));
		allEntries.add(new ModEntry("HUD Profiles", "Profiles", "Save and restore complete HUD layouts",
				"Profiles", -1));

		HudManager manager = pvpClient.getHudManager();
		for(int i = 0; i < manager.getElementCount(); ++i) {
			HudElement element = manager.getElement(i);
			String name = pvpClient.getHudManager().getModuleInfo(i).name;
			allEntries.add(new ModEntry(name, "HUD", "Position and appearance controls",
					name, -1, element));
		}
	}

	private void refreshFilteredEntries() {
		filteredEntries.clear();
		String query = searchText.toLowerCase(Locale.ROOT);
		for(ModEntry entry : allEntries) {
			boolean categoryMatches = "All Mods".equals(activeCategory)
					|| entry.category.equals(activeCategory);
			boolean searchMatches = query.length() == 0
					|| entry.name.toLowerCase(Locale.ROOT).contains(query)
					|| entry.description.toLowerCase(Locale.ROOT).contains(query)
					|| entry.category.toLowerCase(Locale.ROOT).contains(query);
			if(categoryMatches && searchMatches) {
				filteredEntries.add(entry);
			}
		}
		page = Math.min(page, getPageCount() - 1);
	}

	private int getPageCount() {
		return Math.max(1, (filteredEntries.size() + visibleRows - 1) / visibleRows);
	}

	private boolean isEnabled(ModEntry entry) {
		net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig config =
				pvpClient.getConfigStore().getConfig();
		if(entry.hudElement != null) {
			return pvpClient.getHudManager().getElementConfig(entry.hudElement).enabled;
		}
		if("Toggle Sprint".equals(entry.name)) return config.toggleSprintEnabled;
		if("Toggle Sneak".equals(entry.name)) return config.toggleSneakEnabled;
		if("Zoom".equals(entry.name)) return config.zoomEnabled;
		if("Freelook".equals(entry.name)) return config.freelookEnabled;
		if("Custom FOV".equals(entry.name)) return config.customFovEnabled;
		if("Crosshair".equals(entry.name)) return config.crosshairEnabled;
		if("Chat".equals(entry.name)) return config.chatTimestamps || config.chatCompact
				|| config.chatHighlightMentions;
		if("Streamer Mode".equals(entry.name)) return config.streamerMode;
		if("Privacy".equals(entry.name)) return config.hideChat || config.hideServerAddress
				|| config.hideUsername;
		if("Fullbright".equals(entry.name)) return config.fullbright;
		if("Fog and Weather".equals(entry.name)) return config.noFog || config.hideWeather;
		if("Block Outline".equals(entry.name)) return config.blockOutlineEnabled;
		if("Held Item".equals(entry.name)) return config.heldItemScale != 1.0f
				|| config.glintEnabled;
		if("Performance".equals(entry.name)) return config.performanceProfile
				!= net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig.PerformanceProfile.DEFAULT;
		if("Notifications".equals(entry.name)) return config.notificationsEnabled;
		if("Appearance".equals(entry.name)) return true;
		if("HUD Profiles".equals(entry.name)) return true;
		return false;
	}

	private void setEnabled(ModEntry entry, boolean enabled) {
		net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig config =
				pvpClient.getConfigStore().getConfig();
		if(entry.hudElement != null) {
			HudElementConfig element = pvpClient.getHudManager().getElementConfig(entry.hudElement);
			element.enabled = enabled;
			return;
		}
		if("Toggle Sprint".equals(entry.name)) config.toggleSprintEnabled = enabled;
		else if("Toggle Sneak".equals(entry.name)) config.toggleSneakEnabled = enabled;
		else if("Zoom".equals(entry.name)) config.zoomEnabled = enabled;
		else if("Freelook".equals(entry.name)) config.freelookEnabled = enabled;
		else if("Custom FOV".equals(entry.name)) config.customFovEnabled = enabled;
		else if("Crosshair".equals(entry.name)) config.crosshairEnabled = enabled;
		else if("Chat".equals(entry.name)) config.chatTimestamps = enabled;
		else if("Streamer Mode".equals(entry.name)) config.streamerMode = enabled;
		else if("Privacy".equals(entry.name)) {
			config.hideChat = enabled;
			config.hideServerAddress = enabled;
		}else if("Fullbright".equals(entry.name)) config.fullbright = enabled;
		else if("Fog and Weather".equals(entry.name)) {
			config.noFog = enabled;
			config.hideWeather = enabled;
		}else if("Block Outline".equals(entry.name)) config.blockOutlineEnabled = enabled;
		else if("Held Item".equals(entry.name)) config.glintEnabled = enabled;
		else if("Performance".equals(entry.name)) {
			config.applyPerformanceProfile(enabled
					? net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig.PerformanceProfile.BALANCED
					: net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig.PerformanceProfile.DEFAULT);
		}else if("Notifications".equals(entry.name)) config.notificationsEnabled = enabled;
	}

	private void openEntryConfig(ModEntry entry) {
		if(entry.hudElement != null) {
			mc.displayGuiScreen(new GuiScreenPvpHudEditor(pvpClient, this, entry.hudElement));
			return;
		}
		if("HUD Profiles".equals(entry.name) || "Profiles".equals(entry.category)) {
			mc.displayGuiScreen(new GuiScreenPvpHudProfiles(pvpClient, this));
			return;
		}
		if("Appearance".equals(entry.name)) {
			pvpClient.openHudEditor(this);
			return;
		}
		pvpClient.openQolSettings(this, entry.configPage, entry.configSearch);
	}

	@Override
	protected void keyTyped(char character, int keyCode) {
		if(keyCode == 1 || keyCode == mc.gameSettings.keyBindClose.getKeyCode()) {
			mc.displayGuiScreen(parent);
			if(parent == null && mc.theWorld != null && mc.thePlayer != null) {
				mc.setIngameFocus();
			}
			return;
		}
		if(searchField.textboxKeyTyped(character, keyCode)) {
			String next = searchField.getText();
			if(!next.equals(searchText)) {
				searchText = next;
				page = 0;
				initGui();
			}
			return;
		}
		super.keyTyped(character, keyCode);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		searchField.mouseClicked(mouseX, mouseY, button);
		if(button == 1) {
			int start = page * visibleRows;
			int end = Math.min(start + visibleRows, filteredEntries.size());
			for(int i = start; i < end; ++i) {
				ModEntry entry = filteredEntries.get(i);
				int rowY = listY + (i - start) * 34;
				if(mouseX >= contentX && mouseX < contentX + contentWidth
						&& mouseY >= rowY && mouseY < rowY + 30) {
					statusText = "";
					openEntryConfig(entry);
					return;
				}
			}
			return;
		}
		super.mouseClicked(mouseX, mouseY, button);
	}
}
