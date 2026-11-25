package net.lax1dude.eaglercraft.v1_8.pvp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElementConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudManager;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudModuleInfo;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;

public final class GuiScreenPvpHudModuleManager extends GuiScreen {

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

	private final PvpClient pvpClient;
	private final List<HudModuleInfo> filteredModules = new ArrayList<>();
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

	public GuiScreenPvpHudModuleManager(PvpClient pvpClient) {
		this.pvpClient = pvpClient;
	}

	@Override
	public void initGui() {
		panelWidth = Math.min(620, width - 12);
		panelHeight = Math.min(430, height - 12);
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;
		listX = panelX + 12;
		listY = panelY + 100;
		listWidth = panelWidth - 250;
		visibleRows = Math.max(1, Math.min(PAGE_SIZE, (panelHeight - 188) / 28));

		searchField = new GuiTextField(100, fontRendererObj, panelX + 12, panelY + 69, listWidth, 18);
		searchField.setMaxStringLength(32);
		searchField.setText(previousSearch);

		int tabX = panelX + 12;
		int tabWidth = (panelWidth - 24) / HudModuleInfo.Category.values().length;
		HudModuleInfo.Category[] categories = HudModuleInfo.Category.values();
		for(int i = 0; i < categories.length; ++i) {
			buttonList.add(new GuiButton(BUTTON_CATEGORY_BASE + i, tabX + i * tabWidth, panelY + 37, tabWidth - 3, 20,
					categories[i].getDisplayName()));
		}
		updateCategoryButtons();

		int sideX = panelX + listWidth + 26;
		buttonList.add(new GuiButton(BUTTON_SCALE_DOWN, sideX, listY + 116, 40, 20, "-"));
		buttonList.add(new GuiButton(BUTTON_SCALE_UP, sideX + 46, listY + 116, 40, 20, "+"));
		buttonList.add(new GuiButton(BUTTON_APPEARANCE_OVERRIDE, sideX, listY + 176, 86, 20, "Appearance"));
		buttonList.add(new GuiButton(BUTTON_THEME_PRESET, sideX, listY + 200, 86, 20, "Preset"));
		buttonList.add(previousButton = new GuiButton(BUTTON_PREVIOUS, listX, panelY + panelHeight - 88, 52, 20, "<"));
		buttonList.add(nextButton = new GuiButton(BUTTON_NEXT, listX + 58, panelY + panelHeight - 88, 52, 20, ">"));
		int footerY = panelY + panelHeight - 30;
		int footerWidth = (panelWidth - 36) / 4;
		buttonList.add(new GuiButton(BUTTON_EDIT_LAYOUT, panelX + 12, footerY, footerWidth, 20, "Edit HUD Layout"));
		buttonList.add(new GuiButton(BUTTON_DISABLE_ALL, panelX + 16 + footerWidth, footerY, footerWidth, 20,
				"Disable All"));
		buttonList.add(new GuiButton(BUTTON_RESET_DEFAULTS, panelX + 20 + footerWidth * 2, footerY, footerWidth, 20,
				"Reset Defaults"));
		buttonList.add(new GuiButton(BUTTON_DONE, panelX + 24 + footerWidth * 3, footerY, footerWidth, 20, "Done"));
		refreshModules();
	}

	@Override
	public void updateScreen() {
		searchField.updateCursorCounter();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF0121724);
		drawRect(panelX, panelY, panelX + panelWidth, panelY + 3, 0xFF31C48D);
		fontRendererObj.drawStringWithShadow("DICESTACK  /  HUD MODULES", panelX + 12, panelY + 12, 0xFFFFFFFF);
		fontRendererObj.drawStringWithShadow("Search modules and configure your active HUD", panelX + 12, panelY + 25,
				0xFF9BA8B8);

		for(int i = 0; i < filteredModules.size(); ++i) {
			HudModuleInfo module = filteredModules.get(i);
			if(i < page * visibleRows || i >= (page + 1) * visibleRows) {
				continue;
			}
			int rowY = listY + (i - page * visibleRows) * 28;
			boolean selected = module == selectedModule;
			HudElementConfig config = pvpClient.getHudManager().getElementConfig(module.element);
			drawRect(listX, rowY, listX + listWidth, rowY + 25, selected ? 0xFF26384B : 0xFF1A2431);
			drawRect(listX, rowY, listX + 2, rowY + 25, selected ? 0xFF31C48D : 0xFF344252);
			fontRendererObj.drawStringWithShadow(module.name, listX + 9, rowY + 4, 0xFFE9EFF7);
			fontRendererObj.drawStringWithShadow(config.enabled ? "ON" : "OFF",
					listX + listWidth - 42, rowY + 4, config.enabled ? 0xFF55DD88 : 0xFF9BA8B8);
		}

		int sideX = panelX + listWidth + 26;
		drawRect(sideX - 10, listY, panelX + panelWidth - 12, panelY + panelHeight - 68, 0xFF1A2431);
		fontRendererObj.drawStringWithShadow("MODULE SETTINGS", sideX, listY + 10, 0xFF31C48D);
		if(selectedModule == null) {
			fontRendererObj.drawStringWithShadow("Select a module", sideX, listY + 32, 0xFFE9EFF7);
			fontRendererObj.drawStringWithShadow("to view its settings.", sideX, listY + 44, 0xFF9BA8B8);
		}else {
			HudElementConfig config = pvpClient.getHudManager().getElementConfig(selectedModule.element);
			fontRendererObj.drawStringWithShadow(selectedModule.name, sideX, listY + 32, 0xFFE9EFF7);
			fontRendererObj.drawStringWithShadow("Category: " + selectedModule.category.getDisplayName(), sideX,
					listY + 48, 0xFF9BA8B8);
			fontRendererObj.drawStringWithShadow("State: " + (config.enabled ? "Enabled" : "Disabled"), sideX,
					listY + 64, config.enabled ? 0xFF55DD88 : 0xFF9BA8B8);
			fontRendererObj.drawStringWithShadow("Scale: " + config.scale + "x", sideX, listY + 96, 0xFFE9EFF7);
			fontRendererObj.drawStringWithShadow("Position and snapping are", sideX, listY + 148, 0xFF9BA8B8);
			fontRendererObj.drawStringWithShadow("available in HUD Layout.", sideX, listY + 160, 0xFF9BA8B8);
		}

		if(searchField.getText().isEmpty()) {
			fontRendererObj.drawStringWithShadow("Search...", searchField.xPosition + 4, searchField.yPosition + 5,
					0xFF758396);
		}
		searchField.drawTextBox();
		int pageCount = Math.max(1, (filteredModules.size() + visibleRows - 1) / visibleRows);
		fontRendererObj.drawStringWithShadow((filteredModules.isEmpty() ? "0" : Integer.toString(page + 1))
				+ " / " + pageCount, listX + 120, panelY + panelHeight - 82, 0xFF9BA8B8);
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
			pvpClient.openHudEditor();
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
		}
		updatePagingButtons();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		if(button == 0 && mouseX >= listX && mouseX < listX + listWidth && mouseY >= listY) {
			int row = (mouseY - listY) / 28;
			int index = page * visibleRows + row;
			if((mouseY - listY) % 28 < 25 && index >= 0 && index < filteredModules.size()) {
				HudModuleInfo module = filteredModules.get(index);
				selectedModule = module;
				searchField.setFocused(false);
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
		String query = previousSearch.toLowerCase(Locale.ROOT);
		HudManager manager = pvpClient.getHudManager();
		for(int i = 0; i < manager.getElementCount(); ++i) {
			HudModuleInfo module = manager.getModuleInfo(i);
			if(module.category == category && module.name.toLowerCase(Locale.ROOT).contains(query)) {
				filteredModules.add(module);
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
		mc.displayGuiScreen(null);
		if(mc.currentScreen == null) {
			mc.setIngameFocus();
		}
	}

}
