package net.lax1dude.eaglercraft.v1_8.pvp;

import java.util.Locale;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public final class GuiScreenPvpClientMenu extends GuiScreen {

	private static final String[] CATEGORIES = {
			"Combat", "Movement", "Visuals", "HUD", "World", "Player", "Chat", "Privacy", "Performance", "QoL",
			"Settings"
	};
	private static final String[] DESCRIPTIONS = {
			"Crosshair and combat indicators", "Camera, sprint and movement tools", "Rendering and appearance options",
			"Modules, layout, themes and profiles", "World, environment and overlays", "Player status and held item",
			"Chat appearance and timestamps", "Streamer and information controls", "Frame rate and performance",
			"Everyday quality-of-life features", "Import, export and configuration"
	};
	private static final int[] SETTINGS_PAGES = { 3, 0, 1, -1, 2, 6, 5, 2, 6, 0, 6 };

	private final PvpClient pvpClient;
	private final GuiScreen parent;
	private GuiTextField searchField;
	private String searchText = "";
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private String titleText;
	private String searchHint;
	private String noMatchesText;
	private final int[] categoryButtonX = new int[CATEGORIES.length];
	private final int[] categoryButtonY = new int[CATEGORIES.length];
	private final int[] categoryButtonHeight = new int[CATEGORIES.length];
	private final String[] visibleDescriptions = new String[CATEGORIES.length];

	public GuiScreenPvpClientMenu(PvpClient pvpClient, GuiScreen parent) {
		this.pvpClient = pvpClient;
		this.parent = parent;
	}

	@Override
	public void initGui() {
		fontRendererObj = PvpUiFontRenderer.get(mc);
		buttonList.clear();
		panelWidth = Math.max(1, Math.min(520, width - 12));
		panelHeight = Math.max(1, Math.min(330, height - 12));
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;
		int innerWidth = Math.max(1, panelWidth - 24);
		int headerControlsWidth = Math.min(126, innerWidth);
		int headerGap = Math.min(4, Math.max(0, headerControlsWidth / 5));
		int headerButtonWidth = Math.max(1, (headerControlsWidth - headerGap) / 2);
		int headerControlsX = panelX + panelWidth - 12 - headerControlsWidth;
		int searchWidth = Math.max(1, innerWidth - headerControlsWidth - 6);
		searchField = new GuiTextField(100, fontRendererObj, panelX + 12, panelY + 36, searchWidth, 18);
		searchField.setMaxStringLength(48);
		searchField.setText(searchText);
		titleText = fontRendererObj.trimStringToWidth("DICESTACK  /  CLIENT CONTROL CENTER",
				Math.max(0, panelWidth - 24));
		searchHint = fontRendererObj.trimStringToWidth("Search categories or every client setting...",
				Math.max(0, searchField.width - 8));
		noMatchesText = fontRendererObj.trimStringToWidth("No matching categories. Use Search for settings.",
				Math.max(0, panelWidth - 24));
		int columns = innerWidth >= 330 ? 3 : 2;
		int gap = 6;
		int cellWidth = (innerWidth - gap * (columns - 1)) / columns;
		int rows = (CATEGORIES.length + columns - 1) / columns;
		int rowPitch = Math.max(1, Math.min(42, (panelHeight - 106) / rows));
		int categoryHeight = Math.max(1, Math.min(34, rowPitch - 2));
		int visibleCount = 0;
		String query = searchText.toLowerCase(Locale.ROOT);
		for(int i = 0; i < CATEGORIES.length; ++i) {
			if(!categoryMatches(i, query)) {
				continue;
			}
			int column = visibleCount % columns;
			int row = visibleCount / columns;
			int x = panelX + 12 + column * (cellWidth + gap);
			int y = panelY + 64 + row * rowPitch;
			categoryButtonX[i] = x;
			categoryButtonY[i] = y;
			categoryButtonHeight[i] = categoryHeight;
			visibleDescriptions[i] = fontRendererObj.trimStringToWidth(DESCRIPTIONS[i],
					Math.max(0, cellWidth - 8));
			buttonList.add(new PvpGuiButton(i + 1, x, y, cellWidth, categoryHeight, CATEGORIES[i]));
			++visibleCount;
		}
		int footerY = panelY + panelHeight - 28;
		int footerGap = Math.min(6, Math.max(0, (innerWidth - 4) / 3));
		int footerWidth = Math.max(1, (innerWidth - footerGap * 3) / 4);
		int footerX = panelX + 12;
		int[] footerIds = { 20, 21, 22, 23 };
		String[] footerLabels = { "Modules", "Edit", "Profiles", "Done" };
		for(int i = 0; i < footerIds.length; ++i) {
			buttonList.add(new PvpGuiButton(footerIds[i], footerX, footerY, footerWidth, 20, footerLabels[i]));
			footerX += footerWidth + footerGap;
		}
		buttonList.add(new PvpGuiButton(24, headerControlsX, panelY + 35, headerButtonWidth, 20, "Settings"));
		buttonList.add(new PvpGuiButton(25, headerControlsX + headerButtonWidth + headerGap, panelY + 35,
				headerButtonWidth, 20, "HUD"));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		HudAppearanceConfig appearance = pvpClient.getConfigStore().getConfig().appearance;
		HudRenderer.drawPanel(appearance, panelX, panelY, panelWidth, panelHeight);
		drawString(fontRendererObj, titleText, panelX + 12, panelY + 12, 0xFFF1F7F5);
		if(searchField.getText().isEmpty()) {
			fontRendererObj.drawStringWithShadow(searchHint, searchField.xPosition + 4, searchField.yPosition + 5,
					0xFF758396);
		}
		searchField.drawTextBox();
		String query = searchText.toLowerCase(Locale.ROOT);
		int visible = 0;
		for(int i = 0; i < CATEGORIES.length; ++i) {
			if(!categoryMatches(i, query)) {
				continue;
			}
			int y = categoryButtonY[i];
			if(y == 0) {
				continue;
			}
			if(categoryButtonHeight[i] >= 28) {
				fontRendererObj.drawStringWithShadow(visibleDescriptions[i], categoryButtonX[i] + 4,
						y + categoryButtonHeight[i] - 10, 0xFFADB9C6);
			}
			++visible;
		}
		if(visible == 0) {
			drawCenteredString(fontRendererObj, noMatchesText,
					width / 2, panelY + 90, 0xFFADB9C6);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(net.minecraft.client.gui.GuiButton button) {
		if(button.id >= 1 && button.id <= CATEGORIES.length) {
			int index = button.id - 1;
			int page = SETTINGS_PAGES[index];
			if(page < 0) {
				pvpClient.openHudModuleManager(this, searchText);
			}else {
				pvpClient.openQolSettings(this, page, "");
			}
			return;
		}
		switch(button.id) {
		case 20:
			pvpClient.openHudModuleManager(this);
			break;
		case 21:
			pvpClient.openHudEditor(this);
			break;
		case 22:
			mc.displayGuiScreen(new GuiScreenPvpHudProfiles(pvpClient, this));
			break;
		case 23:
			mc.displayGuiScreen(parent);
			if(parent == null && mc.theWorld != null && mc.thePlayer != null) {
				mc.setIngameFocus();
			}
			break;
		case 24:
			pvpClient.openQolSettings(this, 0, searchText);
			break;
		case 25:
			pvpClient.openHudModuleManager(this, searchText);
			break;
		}
	}

	private boolean categoryMatches(int index, String query) {
		if(query.isEmpty() || CATEGORIES[index].toLowerCase(Locale.ROOT).contains(query)
				|| DESCRIPTIONS[index].toLowerCase(Locale.ROOT).contains(query)) {
			return true;
		}
		if(index == 3) {
			for(int i = 0; i < pvpClient.getHudManager().getElementCount(); ++i) {
				if(pvpClient.getHudManager().getModuleInfo(i).name.toLowerCase(Locale.ROOT).contains(query)) {
					return true;
				}
			}
		}
		return false;
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
			searchText = searchField.getText();
			initGui();
			return;
		}
		super.keyTyped(character, keyCode);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		searchField.mouseClicked(mouseX, mouseY, button);
		super.mouseClicked(mouseX, mouseY, button);
	}
}
