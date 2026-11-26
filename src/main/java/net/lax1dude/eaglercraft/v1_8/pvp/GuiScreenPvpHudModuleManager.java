package net.lax1dude.eaglercraft.v1_8.pvp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElementConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudManager;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudModuleInfo;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.IDicestackBackgroundScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public final class GuiScreenPvpHudModuleManager extends GuiScreen implements IDicestackBackgroundScreen {

	private static final int PAGE_SIZE = 7;
	private static final int BUTTON_EDIT_LAYOUT = 1;
	private static final int BUTTON_DISABLE_ALL = 2;
	private static final int BUTTON_RESET_DEFAULTS = 3;
	private static final int BUTTON_DONE = 4;
	private static final int BUTTON_PREVIOUS = 5;
	private static final int BUTTON_NEXT = 6;
	private static final int BUTTON_SCALE_DOWN = 7;
	private static final int BUTTON_SCALE_UP = 8;
	private static final int BUTTON_CATEGORY_BASE = 20;
	private static final int BUTTON_APPEARANCE_OVERRIDE = 9;
	private static final int BUTTON_THEME_PRESET = 10;
	private static final int BUTTON_FAVORITE = 11;
	private static final int BUTTON_FAVORITES_ONLY = 12;
	private static final int BUTTON_PROFILES = 13;

	private final PvpClient pvpClient;
	private final GuiScreen parent;
	private final List<HudModuleInfo> filteredModules = new ArrayList<>();
	private final List<String> filteredModuleLabels = new ArrayList<>();
	private HudModuleInfo.Category category = HudModuleInfo.Category.CORE;
	private HudModuleInfo selectedModule;
	private GuiTextField searchField;
	private GuiButton previousButton;
	private GuiButton nextButton;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private int listX;
	private int listY;
	private int listWidth;
	private int visibleRows;
	private int page;
	private String previousSearch = "";
	private int searchFieldWidth;
	private String titleText;
	private String descriptionText;
	private String searchHint;
	private String pageLabel;
	private HudModuleInfo selectedNameSource;
	private int selectedNameWidth = -1;
	private String selectedModuleLabel = "";
	private boolean favoritesOnly;

	public GuiScreenPvpHudModuleManager(PvpClient pvpClient) {
		this(pvpClient, null);
	}

	public GuiScreenPvpHudModuleManager(PvpClient pvpClient, GuiScreen parent) {
		this(pvpClient, parent, "");
	}

	public GuiScreenPvpHudModuleManager(PvpClient pvpClient, GuiScreen parent, String initialSearch) {
		this.pvpClient = pvpClient;
		this.parent = parent;
		this.previousSearch = initialSearch == null ? "" : initialSearch;
	}

	@Override
	public void initGui() {
		fontRendererObj = PvpUiFontRenderer.get(mc);
		buttonList.clear();
		panelWidth = Math.max(1, Math.min(620, width - 12));
		panelHeight = Math.max(1, Math.min(430, height - 12));
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;
		listX = panelX + 12;
		listY = panelY + 100;
		listWidth = Math.max(1, (panelWidth - 36) / 2);
		visibleRows = Math.max(1, Math.min(PAGE_SIZE, (panelHeight - 166) / 28));

		searchFieldWidth = Math.max(1, listWidth - 80);
		searchField = new GuiTextField(100, fontRendererObj, listX, panelY + 69, searchFieldWidth, 18);
		searchField.setMaxStringLength(32);
		searchField.setText(previousSearch);
		int headerWidth = Math.max(0, panelWidth - 24);
		titleText = fontRendererObj.trimStringToWidth("DICESTACK  /  HUD MODULES", headerWidth);
		descriptionText = fontRendererObj.trimStringToWidth("Search modules and configure your active HUD",
				headerWidth);
		searchHint = fontRendererObj.trimStringToWidth("Search...", Math.max(0, searchFieldWidth - 8));
		int tabX = panelX + 12;
		int tabWidth = Math.max(1, (panelWidth - 24) / HudModuleInfo.Category.values().length);
		HudModuleInfo.Category[] categories = HudModuleInfo.Category.values();
		for(int i = 0; i < categories.length; ++i) {
			buttonList.add(new PvpGuiButton(BUTTON_CATEGORY_BASE + i, tabX + i * tabWidth, panelY + 37,
					Math.max(1, tabWidth - 3), 20,
					categories[i].getDisplayName()));
		}
		updateCategoryButtons();
		int favoriteWidth = Math.min(72, Math.max(1, listWidth));
		buttonList.add(new PvpGuiButton(BUTTON_FAVORITES_ONLY, listX + listWidth - favoriteWidth, panelY + 68,
				favoriteWidth, 20, favoritesOnly ? "Favourites" : "All"));

		int footerY = panelY + panelHeight - 30;
		int footerGap = 3;
		int footerWidth = Math.max(1, (panelWidth - 24 - footerGap * 4) / 5);
		int footerX = panelX + 12;
		int[] footerIds = { BUTTON_EDIT_LAYOUT, BUTTON_DISABLE_ALL, BUTTON_RESET_DEFAULTS, BUTTON_PROFILES, BUTTON_DONE };
		String[] footerLabels = { "Edit HUD Layout", "Disable All", "Reset Defaults", "Profiles", "Done" };
		for(int i = 0; i < footerIds.length; ++i) {
			buttonList.add(new PvpGuiButton(footerIds[i], footerX, footerY, footerWidth, 20, footerLabels[i]));
			footerX += footerWidth + footerGap;
		}
		int navigationWidth = Math.min(52, Math.max(1, listWidth / 3));
		int navigationY = footerY - 25;
		buttonList.add(previousButton = new PvpGuiButton(BUTTON_PREVIOUS, listX, navigationY, navigationWidth, 20, "<"));
		buttonList.add(nextButton = new PvpGuiButton(BUTTON_NEXT, listX + navigationWidth + 4, navigationY,
				navigationWidth, 20, ">"));
		int sideX = listX + listWidth + 12;
		int sideWidth = Math.max(1, panelX + panelWidth - 12 - sideX);
		int smallButtonWidth = Math.max(1, (sideWidth - 4) / 2);
		int availableControlHeight = Math.max(0, footerY - 4 - (listY + 42));
		int sideControlGap = Math.min(4, Math.max(0, (availableControlHeight - 48) / 2));
		int sideControlHeight = Math.min(16,
				Math.max(1, (availableControlHeight - sideControlGap * 2) / 3));
		if(sideControlHeight >= 8) {
			int sideControlY = footerY - 4 - sideControlHeight * 3 - sideControlGap * 2;
			buttonList.add(new PvpGuiButton(BUTTON_SCALE_DOWN, sideX, sideControlY, smallButtonWidth,
					sideControlHeight, "-"));
			buttonList.add(new PvpGuiButton(BUTTON_SCALE_UP, sideX + smallButtonWidth + 4, sideControlY,
					smallButtonWidth, sideControlHeight, "+"));
			int appearanceY = sideControlY + sideControlHeight + sideControlGap;
			buttonList.add(new PvpGuiButton(BUTTON_APPEARANCE_OVERRIDE, sideX, appearanceY, sideWidth,
					sideControlHeight, "Appearance"));
			int presetY = appearanceY + sideControlHeight + sideControlGap;
			buttonList.add(new PvpGuiButton(BUTTON_THEME_PRESET, sideX, presetY, smallButtonWidth,
					sideControlHeight, "Preset"));
			buttonList.add(new PvpGuiButton(BUTTON_FAVORITE, sideX + smallButtonWidth + 4, presetY,
					smallButtonWidth, sideControlHeight, "Favourite"));
		}
		refreshModules();
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
		fontRendererObj.drawStringWithShadow(titleText, panelX + 12, panelY + 12, 0xFFFFFFFF);
		fontRendererObj.drawStringWithShadow(descriptionText, panelX + 12, panelY + 25, 0xFF9BA8B8);

		for(int i = 0; i < filteredModules.size(); ++i) {
			HudModuleInfo module = filteredModules.get(i);
			if(i < page * visibleRows || i >= (page + 1) * visibleRows) {
				continue;
			}
			int rowY = listY + (i - page * visibleRows) * 28;
			boolean selected = module == selectedModule;
			HudElementConfig config = pvpClient.getHudManager().getElementConfig(module.element);
			HudRenderer.drawRoundedRect(listX, rowY, listWidth, 25, appearance.cornerRadius,
					selected ? 0xFF26384B : 0xFF1A2431);
			HudRenderer.drawRoundedBorder(listX, rowY, listWidth, 25, appearance.cornerRadius, 1,
					selected ? appearance.accentColor : 0xFF344252);
			fontRendererObj.drawStringWithShadow(filteredModuleLabels.get(i), listX + 9, rowY + 4, 0xFFE9EFF7);
			fontRendererObj.drawStringWithShadow(config.enabled ? "ON" : "OFF",
					listX + listWidth - 25, rowY + 4, config.enabled ? 0xFF55DD88 : 0xFF9BA8B8);
			if(config.favorite) {
				fontRendererObj.drawStringWithShadow("*", listX + listWidth - 38, rowY + 4, 0xFFFFD166);
			}
		}

		int sideX = listX + listWidth + 12;
		int sideWidth = Math.max(1, panelX + panelWidth - 12 - sideX);
		int footerY = panelY + panelHeight - 30;
		HudRenderer.drawRoundedRect(sideX - 4, listY, sideWidth + 4,
				Math.max(1, footerY - 4 - listY), appearance.cornerRadius, 0xFF1A2431);
		fontRendererObj.drawStringWithShadow(fontRendererObj.trimStringToWidth("MODULE SETTINGS", sideWidth), sideX,
				listY + 10, 0xFF31C48D);
		if(selectedModule == null) {
			fontRendererObj.drawStringWithShadow(fontRendererObj.trimStringToWidth("Select a module", sideWidth),
					sideX, listY + 32, 0xFFE9EFF7);
		}else {
			HudElementConfig config = pvpClient.getHudManager().getElementConfig(selectedModule.element);
			if(selectedNameSource != selectedModule || selectedNameWidth != sideWidth) {
				selectedNameSource = selectedModule;
				selectedNameWidth = sideWidth;
				selectedModuleLabel = fontRendererObj.trimStringToWidth(selectedModule.name, sideWidth);
			}
			fontRendererObj.drawStringWithShadow(selectedModuleLabel, sideX, listY + 32, 0xFFE9EFF7);
			if(panelHeight >= 330) {
				fontRendererObj.drawStringWithShadow("Category: "
						+ fontRendererObj.trimStringToWidth(selectedModule.category.getDisplayName(),
								Math.max(0, sideWidth - fontRendererObj.getStringWidth("Category: "))),
						sideX, listY + 48, 0xFF9BA8B8);
				fontRendererObj.drawStringWithShadow("State: " + (config.enabled ? "Enabled" : "Disabled"), sideX,
						listY + 64, config.enabled ? 0xFF55DD88 : 0xFF9BA8B8);
				fontRendererObj.drawStringWithShadow("Favourite: " + (config.favorite ? "Yes" : "No"), sideX,
						listY + 80, config.favorite ? 0xFFFFD166 : 0xFF9BA8B8);
				fontRendererObj.drawStringWithShadow("Scale: " + config.scale + "x", sideX, listY + 108, 0xFFE9EFF7);
				fontRendererObj.drawStringWithShadow("Position and snapping are", sideX, listY + 148, 0xFF9BA8B8);
				fontRendererObj.drawStringWithShadow("available in HUD Layout.", sideX, listY + 160, 0xFF9BA8B8);
			}
		}

		if(searchField.getText().isEmpty()) {
			fontRendererObj.drawStringWithShadow(searchHint, searchField.xPosition + 4, searchField.yPosition + 5,
					0xFF758396);
		}
		searchField.drawTextBox();
		int pageLabelY = Math.min(panelY + panelHeight - 30 - 25 - 12, listY + visibleRows * 28);
		drawCenteredString(fontRendererObj, pageLabel, listX + listWidth / 2, pageLabelY, 0xFF9BA8B8);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id >= BUTTON_CATEGORY_BASE && button.id < BUTTON_CATEGORY_BASE + HudModuleInfo.Category.values().length) {
			category = HudModuleInfo.Category.values()[button.id - BUTTON_CATEGORY_BASE];
			page = 0;
			selectedModule = null;
			updateCategoryButtons();
			refreshModules();
			return;
		}

		switch(button.id) {
		case BUTTON_EDIT_LAYOUT:
			pvpClient.openHudEditor(this);
			break;
		case BUTTON_DISABLE_ALL:
			for(int i = 0; i < pvpClient.getHudManager().getElementCount(); ++i) {
				pvpClient.getConfigStore().getConfig()
						.getElement(pvpClient.getHudManager().getElement(i).getId()).enabled = false;
			}
			pvpClient.getConfigStore().markDirty();
			break;
		case BUTTON_RESET_DEFAULTS:
			pvpClient.getConfigStore().getConfig().resetElements();
			pvpClient.getConfigStore().markDirty();
			break;
		case BUTTON_PREVIOUS:
			page = Math.max(0, page - 1);
			break;
		case BUTTON_NEXT:
			page = Math.min(Math.max(0, (filteredModules.size() - 1) / visibleRows), page + 1);
			break;
		case BUTTON_SCALE_DOWN:
			changeSelectedScale(-0.1f);
			break;
		case BUTTON_SCALE_UP:
			changeSelectedScale(0.1f);
			break;
		case BUTTON_DONE:
			closeManager();
			break;
		case BUTTON_PROFILES:
			mc.displayGuiScreen(new GuiScreenPvpHudProfiles(pvpClient, this));
			break;
		case BUTTON_FAVORITE:
			if(selectedModule != null) {
				HudElementConfig selected = pvpClient.getHudManager().getElementConfig(selectedModule.element);
				selected.favorite = !selected.favorite;
				pvpClient.getConfigStore().markDirty();
				if(favoritesOnly && !selected.favorite) {
					refreshModules();
				}
			}
			break;
		case BUTTON_APPEARANCE_OVERRIDE:
			if(selectedModule != null) {
				HudElementConfig selected = pvpClient.getHudManager().getElementConfig(selectedModule.element);
				selected.appearanceOverride = !selected.appearanceOverride;
				pvpClient.getConfigStore().markDirty();
			}
			break;
		case BUTTON_THEME_PRESET:
			if(selectedModule != null) {
				HudElementConfig selected = pvpClient.getHudManager().getElementConfig(selectedModule.element);
				HudAppearanceConfig style = selected.appearanceOverride
						? selected.appearance : pvpClient.getConfigStore().getConfig().appearance;
				style.applyPreset(style.preset == HudAppearanceConfig.Preset.MODERN_GLASS
						? HudAppearanceConfig.Preset.MINIMAL_COMPETITIVE
						: HudAppearanceConfig.Preset.MODERN_GLASS);
				pvpClient.getConfigStore().markDirty();
			}
			break;
		case BUTTON_FAVORITES_ONLY:
			favoritesOnly = !favoritesOnly;
			page = 0;
			refreshModules();
			break;
		}
		updatePagingButtons();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		if(button == 0 && mouseX >= listX && mouseX < listX + listWidth
				&& mouseY >= listY && mouseY < listY + visibleRows * 28) {
			int row = (mouseY - listY) / 28;
			int index = page * visibleRows + row;
			if((mouseY - listY) % 28 < 25 && index >= 0 && index < filteredModules.size()) {
				HudModuleInfo module = filteredModules.get(index);
				selectedModule = module;
				searchField.setFocused(false);
				updatePagingButtons();
				if(mouseX >= listX + listWidth - 50) {
					HudElementConfig config = pvpClient.getHudManager().getElementConfig(module.element);
					config.enabled = !config.enabled;
					pvpClient.getConfigStore().markDirty();
				}
				return;
			}
		}
		searchField.mouseClicked(mouseX, mouseY, button);
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void keyTyped(char character, int keyCode) {
		if(keyCode == 1 || keyCode == mc.gameSettings.keyBindClose.getKeyCode()) {
			closeManager();
			return;
		}
		if(searchField.textboxKeyTyped(character, keyCode)) {
			String search = searchField.getText();
			if(!search.equals(previousSearch)) {
				previousSearch = search;
				page = 0;
				refreshModules();
			}
			return;
		}
		super.keyTyped(character, keyCode);
	}

	private void changeSelectedScale(float amount) {
		if(selectedModule == null) {
			return;
		}
		HudElementConfig config = pvpClient.getHudManager().getElementConfig(selectedModule.element);
		config.scale = Math.max(0.5f, Math.min(3.0f, Math.round((config.scale + amount) * 10.0f) / 10.0f));
		pvpClient.getConfigStore().markDirty();
	}

	private void refreshModules() {
		filteredModules.clear();
		filteredModuleLabels.clear();
		String query = previousSearch.toLowerCase(Locale.ROOT);
		HudManager manager = pvpClient.getHudManager();
		for(int i = 0; i < manager.getElementCount(); ++i) {
			HudModuleInfo module = manager.getModuleInfo(i);
			boolean favorite = manager.getElementConfig(module.element).favorite;
			if((!query.isEmpty() || module.category == category) && module.name.toLowerCase(Locale.ROOT).contains(query)
					&& (!favoritesOnly || favorite)) {
				filteredModules.add(module);
				filteredModuleLabels.add(fontRendererObj.trimStringToWidth(module.name, Math.max(0, listWidth - 54)));
			}
		}
		if(selectedModule != null && !filteredModules.contains(selectedModule)) {
			selectedModule = null;
		}
		updatePagingButtons();
	}

	private void updatePagingButtons() {
		int pageCount = Math.max(1, (filteredModules.size() + visibleRows - 1) / visibleRows);
		if(page >= pageCount) {
			page = pageCount - 1;
		}
		if(previousButton != null) {
			previousButton.enabled = page > 0;
			nextButton.enabled = page + 1 < pageCount;
		}
		pageLabel = (filteredModules.isEmpty() ? "0" : Integer.toString(page + 1)) + " / " + pageCount;
		for(int i = 0; i < buttonList.size(); ++i) {
			GuiButton button = (GuiButton) buttonList.get(i);
			if(button.id == BUTTON_FAVORITES_ONLY) {
				button.displayString = favoritesOnly ? "Favourites" : "All";
			}else if(button.id == BUTTON_FAVORITE) {
				HudElementConfig config = selectedModule == null ? null
						: pvpClient.getHudManager().getElementConfig(selectedModule.element);
				button.displayString = config != null && config.favorite ? "Unfavourite" : "Favourite";
				button.enabled = selectedModule != null;
			}
		}
	}

	private void updateCategoryButtons() {
		for(int i = 0; i < HudModuleInfo.Category.values().length; ++i) {
			GuiButton button = (GuiButton) buttonList.get(i);
			HudModuleInfo.Category buttonCategory = HudModuleInfo.Category.values()[i];
			button.displayString = buttonCategory == category ? "> " + buttonCategory.getDisplayName()
					: buttonCategory.getDisplayName();
		}
	}

	private void closeManager() {
		pvpClient.getConfigStore().saveNow();
		mc.displayGuiScreen(parent);
		if(parent == null && mc.currentScreen == null) {
			mc.setIngameFocus();
		}
	}

}
