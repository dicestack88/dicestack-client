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
	private final int[] categoryButtonX = new int[CATEGORIES.length];
	private final int[] categoryButtonY = new int[CATEGORIES.length];
	private final int[] categoryButtonHeight = new int[CATEGORIES.length];

	public GuiScreenPvpClientMenu(PvpClient pvpClient, GuiScreen parent) {
		this.pvpClient = pvpClient;
		this.parent = parent;
	}

	@Override
	public void initGui() {
		buttonList.clear();
		panelWidth = Math.min(520, Math.max(220, width - 12));
		panelHeight = Math.min(330, Math.max(180, height - 12));
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;
		int innerWidth = panelWidth - 24;
		searchField = new GuiTextField(100, fontRendererObj, panelX + 12, panelY + 36, innerWidth - 142, 18);
		searchField.setMaxStringLength(48);
		searchField.setText(searchText);
		int columns = innerWidth >= 330 ? 3 : 2;
		int gap = 6;
		int cellWidth = (innerWidth - gap * (columns - 1)) / columns;
		int rows = (CATEGORIES.length + columns - 1) / columns;
		int rowPitch = Math.max(14, Math.min(42, (panelHeight - 102) / rows));
		int categoryHeight = Math.max(12, Math.min(34, rowPitch - 8));
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
			buttonList.add(new PvpGuiButton(i + 1, x, y, cellWidth, categoryHeight, CATEGORIES[i]));
			++visibleCount;
		}
		int footerY = panelY + panelHeight - 28;
		int footerWidth = (innerWidth - 18) / 4;
		buttonList.add(new PvpGuiButton(20, panelX + 12, footerY, footerWidth, 20, "Modules"));
		buttonList.add(new PvpGuiButton(21, panelX + 18 + footerWidth, footerY, footerWidth, 20, "Edit"));
		buttonList.add(new PvpGuiButton(22, panelX + 24 + footerWidth * 2, footerY, footerWidth, 20, "Profiles"));
		buttonList.add(new PvpGuiButton(23, panelX + 30 + footerWidth * 3, footerY, footerWidth, 20, "Done"));
		buttonList.add(new PvpGuiButton(24, panelX + panelWidth - 142, panelY + 35, 64, 20, "Settings"));
		buttonList.add(new PvpGuiButton(25, panelX + panelWidth - 74, panelY + 35, 62, 20, "HUD"));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		HudAppearanceConfig appearance = pvpClient.getConfigStore().getConfig().appearance;
		int start = HudRenderer.withAlpha(appearance.gradientStart,
				Math.max(0.88f, appearance.backgroundOpacity));
		int end = HudRenderer.withAlpha(appearance.gradientEnd,
				Math.max(0.88f, appearance.backgroundOpacity));
		if(appearance.preset == HudAppearanceConfig.Preset.MODERN_GLASS && appearance.gradientEnabled) {
			drawGradientRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, start, end);
		}else {
			drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, start);
		}
		int border = HudRenderer.withAlpha(appearance.accentColor, Math.max(0.72f, appearance.borderOpacity));
		drawRect(panelX, panelY, panelX + panelWidth, panelY + Math.max(2, appearance.borderThickness), border);
		drawRect(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, border);
		drawRect(panelX, panelY, panelX + 1, panelY + panelHeight, border);
		drawRect(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, border);
		drawString(fontRendererObj, "DICESTACK  /  CLIENT CONTROL CENTER", panelX + 12, panelY + 12, 0xFFF1F7F5);
		if(searchField.getText().isEmpty()) {
			fontRendererObj.drawStringWithShadow("Search categories or every client setting...",
					searchField.xPosition + 4, searchField.yPosition + 5, 0xFF758396);
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
				fontRendererObj.drawStringWithShadow(DESCRIPTIONS[i], categoryButtonX[i] + 4,
						y + categoryButtonHeight[i] - 10, 0xFFADB9C6);
			}
			++visible;
		}
		if(visible == 0) {
			drawCenteredString(fontRendererObj, "No matching categories. Use Search for settings.",
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
