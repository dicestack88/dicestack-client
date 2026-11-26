package net.lax1dude.eaglercraft.v1_8.pvp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudManager;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.IDicestackBackgroundScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public final class GuiScreenPvpClientMenu extends GuiScreen implements IDicestackBackgroundScreen {

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
		final int[] settings;
		final HudElement hudElement;

		ModEntry(String name, String category, String description, int... settings) {
			this(name, category, description, settings, null);
		}

		ModEntry(String name, String category, String description, int[] settings, HudElement hudElement) {
			this.name = name;
			this.category = category;
			this.description = description;
			this.settings = settings;
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
		sidebarWidth = Math.min(Math.max(1, panelWidth - 36),
				panelWidth >= 620 ? 150 : Math.max(96, panelWidth / 4));
		contentX = panelX + sidebarWidth + 12;
		contentWidth = Math.max(1, panelX + panelWidth - 12 - contentX);

		searchFieldWidth = Math.max(1, contentWidth);
		searchField = new GuiTextField(100, fontRendererObj, contentX, panelY + 35,
				searchFieldWidth, 18);
		searchField.setMaxStringLength(48);
		searchField.setText(searchText);

		int sidebarY = panelY + 54;
		int sidebarGap = 3;
		int sidebarButtonHeight = Math.min(22, Math.max(8,
				(panelHeight - 66 - sidebarGap * (CATEGORIES.length - 1)) / CATEGORIES.length));
		for(int i = 0; i < CATEGORIES.length; ++i) {
			PvpGuiButton button = new PvpGuiButton(ID_CATEGORY_BASE + i,
					panelX + 10, sidebarY + i * (sidebarButtonHeight + sidebarGap),
					Math.max(1, sidebarWidth - 20), sidebarButtonHeight, CATEGORIES[i]);
			button.setSelected(CATEGORIES[i].equals(activeCategory));
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
			int buttonId = ID_MOD_BASE + allEntries.indexOf(entry);
			if(isNavigationEntry(entry)) {
				buttonList.add(new PvpGuiButton(buttonId, contentX + contentWidth - 58,
						rowY + 7, 50, 16, "Open"));
			}else {
				buttonList.add(new PvpToggleButton(buttonId,
						contentX + contentWidth - 58, rowY + 7, 50, 16, isEnabled(entry)));
			}
		}

		int footerY = panelY + panelHeight - 31;
		int footerButtonWidth = Math.max(1, Math.min(58, (contentWidth - 8) / 4));
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
		String actionHint = "Select a mod to view its settings";
		actionHint = fontRendererObj.trimStringToWidth(actionHint, Math.max(0, contentWidth - 8));
		fontRendererObj.drawStringWithShadow(actionHint, contentX + 2, listY - 9, 0xFF8797A8);

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
				if(isNavigationEntry(entry)) {
					openEntryConfig(entry);
					return;
				}
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
		allEntries.add(new ModEntry("Toggle Sprint", "QoL", "Automatically keep sprint active", 1));
		allEntries.add(new ModEntry("Toggle Sneak", "QoL", "Keep sneak toggled while walking", 2));
		allEntries.add(new ModEntry("Zoom", "Camera", "Configurable camera zoom", 3, 4, 5, 67));
		allEntries.add(new ModEntry("Freelook", "Camera", "Orbit while preserving movement", 102, 104, 65));
		allEntries.add(new ModEntry("Custom FOV", "Camera", "Independent field-of-view settings", 29, 58));
		allEntries.add(new ModEntry("Crosshair", "Crosshair", "Crosshair style, size and colour",
				35, 40, 41, 42, 44, 46, 50, 52, 53, 54, 56, 57));
		allEntries.add(new ModEntry("Chat", "QoL", "Timestamps, mentions and compact chat",
				60, 61, 62, 63, 78, 101));
		allEntries.add(new ModEntry("Streamer Mode", "Privacy", "Hide identifying information", 73));
		allEntries.add(new ModEntry("Privacy", "Privacy", "Hide chat, addresses and overlays",
				30, 31, 32, 33, 34, 74, 76));
		allEntries.add(new ModEntry("Fullbright", "Visuals", "Increase world visibility", 24));
		allEntries.add(new ModEntry("Fog and Weather", "Visuals", "Environment rendering controls",
				22, 23, 70, 71, 72, 69, 100));
		allEntries.add(new ModEntry("Block Outline", "Visuals", "Custom block outline appearance", 94, 95));
		allEntries.add(new ModEntry("Held Item", "Visuals", "Scale and offset held items",
				86, 88, 90, 92, 97, 99));
		allEntries.add(new ModEntry("Performance", "Performance", "FPS limit and performance profile", 79, 80));
		allEntries.add(new ModEntry("Notifications", "QoL", "Toggle Dicestack notifications", 75));
		allEntries.add(new ModEntry("Appearance", "Appearance", "Modern Glass and HUD appearance"));
		allEntries.add(new ModEntry("HUD Profiles", "Profiles", "Save and restore complete HUD layouts",
				new int[0]));

		HudManager manager = pvpClient.getHudManager();
		for(int i = 0; i < manager.getElementCount(); ++i) {
			HudElement element = manager.getElement(i);
			String name = pvpClient.getHudManager().getModuleInfo(i).name;
			allEntries.add(new ModEntry(name, "HUD", "Position and appearance controls",
					getHudSettings(element.getId()), element));
		}
	}

	private int[] getHudSettings(String elementId) {
		if("coordinates".equals(elementId)) return new int[] { 7 };
		if("world_clock".equals(elementId)) return new int[] { 8, 9 };
		if("food_info".equals(elementId)) return new int[] { 103 };
		return new int[0];
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
		return GuiScreenPvpModuleSettings.isModuleEnabled(pvpClient, entry.name, entry.hudElement);
	}

	private boolean isNavigationEntry(ModEntry entry) {
		return "Appearance".equals(entry.name) || "HUD Profiles".equals(entry.name)
				|| "Profiles".equals(entry.category);
	}

	private void setEnabled(ModEntry entry, boolean enabled) {
		GuiScreenPvpModuleSettings.setModuleEnabled(pvpClient, mc, entry.name, entry.hudElement, enabled);
	}

	private void openEntryConfig(ModEntry entry) {
		if("HUD Profiles".equals(entry.name) || "Profiles".equals(entry.category)) {
			mc.displayGuiScreen(new GuiScreenPvpHudProfiles(pvpClient, this));
			return;
		}
		if("Appearance".equals(entry.name)) {
			pvpClient.openHudEditor(this);
			return;
		}
		mc.displayGuiScreen(new GuiScreenPvpModuleSettings(pvpClient, this, entry.name,
				entry.description, entry.settings, entry.hudElement));
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
		if(button == 1 || button == 0) {
			int start = page * visibleRows;
			int end = Math.min(start + visibleRows, filteredEntries.size());
			for(int i = start; i < end; ++i) {
				ModEntry entry = filteredEntries.get(i);
				int rowY = listY + (i - start) * 34;
				if(mouseX >= contentX && mouseX < contentX + contentWidth
						&& mouseY >= rowY && mouseY < rowY + 30) {
					int toggleX = contentX + contentWidth - 58;
					if(button == 0 && mouseX >= toggleX && mouseY >= rowY + 7
							&& mouseY < rowY + 23) {
						super.mouseClicked(mouseX, mouseY, button);
						return;
					}
					statusText = "";
					openEntryConfig(entry);
					return;
				}
			}
			if(button == 1) {
				return;
			}
		}
		super.mouseClicked(mouseX, mouseY, button);
	}
}
