package net.lax1dude.eaglercraft.v1_8.pvp;

import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfigStore;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElementConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;

public final class GuiScreenPvpHudEditor extends GuiScreen {

	private static final int PANEL_WIDTH = 138;
	private static final int PANEL_HEIGHT = 232;

	private final PvpClient pvpClient;
	private ScaledResolution resolution;
	private HudElement selectedElement;
	private GuiButton visibilityButton;
	private String selectionText = "";
	private String visibilityText = "";
	private String scaleText = "";
	private String instructionText = "";
	private int panelX;
	private int panelY;
	private int dragOffsetX;
	private int dragOffsetY;
	private int guideX = -1;
	private int guideY = -1;
	private boolean dragging;

	public GuiScreenPvpHudEditor(PvpClient pvpClient) {
		this.pvpClient = pvpClient;
	}

	@Override
	public void initGui() {
		resolution = new ScaledResolution(mc);
		panelX = Math.max(4, width - PANEL_WIDTH - 4);
		panelY = Math.max(4, Math.min(18, height - PANEL_HEIGHT - 4));
		instructionText = Keyboard.getKeyName(mc.gameSettings.keyBindPvpHudEditor.getKeyCode())
				+ " toggles editor; Esc closes";
		if(selectedElement == null) {
			selectedElement = pvpClient.getHudManager().getElement(0);
		}

		buttonList.add(visibilityButton = new GuiButton(1, panelX + 6, panelY + 40, PANEL_WIDTH - 12, 20, ""));
		buttonList.add(new GuiButton(2, panelX + 6, panelY + 64, 38, 20, "-"));
		buttonList.add(new GuiButton(3, panelX + PANEL_WIDTH - 44, panelY + 64, 38, 20, "+"));
		buttonList.add(new GuiButton(4, panelX + 6, panelY + 88, PANEL_WIDTH - 12, 20, "Reset Selected"));
		buttonList.add(new GuiButton(5, panelX + 6, panelY + 112, PANEL_WIDTH - 12, 20, "Reset All"));
		buttonList.add(new GuiButton(6, panelX + 6, panelY + 136, PANEL_WIDTH - 12, 20, "Done"));
		buttonList.add(new GuiButton(7, panelX + 6, panelY + 160, PANEL_WIDTH - 12, 20, ""));
		buttonList.add(new GuiButton(8, panelX + 6, panelY + 184, 38, 20, "BG"));
		buttonList.add(new GuiButton(9, panelX + PANEL_WIDTH - 44, panelY + 184, 38, 20, "Border"));
		buttonList.add(new GuiButton(10, panelX + 6, panelY + 208, PANEL_WIDTH - 12, 20, ""));
		updateControls();
		pvpClient.getHudManager().renderEditor(resolution, 0.0f, pvpClient.getRuntime());
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		HudManager hudManager = pvpClient.getHudManager();
		hudManager.renderEditor(resolution, partialTicks, pvpClient.getRuntime());
		drawSnapGuides();
		drawElementBounds(hudManager);

		drawRect(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xD0101010);
		fontRendererObj.drawStringWithShadow("HUD Editor", panelX + 7, panelY + 7, 0xFFFFFFFF);
		fontRendererObj.drawStringWithShadow(selectionText, panelX + 7, panelY + 19, 0xFFDDDDDD);
		fontRendererObj.drawStringWithShadow(visibilityText, panelX + 7, panelY + 30, 0xFFDDDDDD);
		fontRendererObj.drawStringWithShadow(scaleText, panelX + 48, panelY + 70, 0xFFFFFFFF);
		fontRendererObj.drawStringWithShadow(instructionText, 7, height - 14, 0xFFDDDDDD);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawElementBounds(HudManager hudManager) {
		for(int i = 0, l = hudManager.getElementCount(); i < l; ++i) {
			HudElement element = hudManager.getElement(i);
			int x = hudManager.getElementX(element, resolution);
			int y = hudManager.getElementY(element, resolution);
			int right = x + Math.max(1, hudManager.getElementWidth(element));
			int bottom = y + Math.max(1, hudManager.getElementHeight(element));
			HudElementConfig elementConfig = hudManager.getElementConfig(element);
			int color = element == selectedElement ? 0xFFFFD54A
					: elementConfig.enabled ? 0xAAFFFFFF : 0xAAFF6666;
			drawRect(x, y, right, y + 1, color);
			drawRect(x, bottom - 1, right, bottom, color);
			drawRect(x, y, x + 1, bottom, color);
			drawRect(right - 1, y, right, bottom, color);
		}
	}

	private void drawSnapGuides() {
		if(guideX >= 0) {
			drawRect(guideX, 0, guideX + 1, height, 0xAA00FFFF);
		}
		if(guideY >= 0) {
			drawRect(0, guideY, width, guideY + 1, 0xAA00FFFF);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		PvpConfigStore store = pvpClient.getConfigStore();
		PvpConfig config = store.getConfig();
		HudElementConfig elementConfig = pvpClient.getHudManager().getElementConfig(selectedElement);
		switch(button.id) {
		case 1:
			elementConfig.enabled = !elementConfig.enabled;
			store.markDirty();
			break;
		case 2:
			setScale(elementConfig.scale - 0.1f);
			store.markDirty();
			break;
		case 3:
			setScale(elementConfig.scale + 0.1f);
			store.markDirty();
			break;
		case 4:
			config.resetElement(selectedElement.getId());
			store.markDirty();
			break;
		case 5:
			config.resetElements();
			store.markDirty();
			break;
		case 6:
			closeEditor();
			return;
		case 7:
			elementConfig.appearanceOverride = !elementConfig.appearanceOverride;
			store.markDirty();
			break;
		case 8:
			HudAppearanceConfig style = elementConfig.appearanceOverride ? elementConfig.appearance : config.appearance;
			style.backgroundEnabled = !style.backgroundEnabled;
			store.markDirty();
			break;
		case 9:
			HudAppearanceConfig borderStyle = elementConfig.appearanceOverride ? elementConfig.appearance : config.appearance;
			borderStyle.borderEnabled = !borderStyle.borderEnabled;
			store.markDirty();
			break;
		case 10:
			HudAppearanceConfig presetStyle = elementConfig.appearanceOverride ? elementConfig.appearance : config.appearance;
			presetStyle.applyPreset(presetStyle.preset == HudAppearanceConfig.Preset.MODERN_GLASS
					? HudAppearanceConfig.Preset.MINIMAL_COMPETITIVE
					: HudAppearanceConfig.Preset.MODERN_GLASS);
			store.markDirty();
			break;
		default:
			return;
		}
		updateControls();
	}

	private void setScale(float scale) {
		HudElementConfig elementConfig = pvpClient.getHudManager().getElementConfig(selectedElement);
		elementConfig.scale = Math.max(0.5f, Math.min(3.0f, Math.round(scale * 10.0f) / 10.0f));
	}

	private void updateControls() {
		HudElementConfig elementConfig = pvpClient.getHudManager().getElementConfig(selectedElement);
		selectionText = "Selected: " + selectedElement.getId().toUpperCase();
		visibilityText = "Visible: " + (elementConfig.enabled ? "Yes" : "No");
		visibilityButton.displayString = elementConfig.enabled ? "Hide Element" : "Show Element";
		scaleText = "Scale: " + elementConfig.scale + "x";
		((GuiButton) buttonList.get(6)).displayString =
				elementConfig.appearanceOverride ? "Use Global Appearance" : "Override Appearance";
		HudAppearanceConfig style = elementConfig.appearanceOverride ? elementConfig.appearance : configAppearance();
		((GuiButton) buttonList.get(9)).displayString =
				"Preset: " + (style.preset == HudAppearanceConfig.Preset.MODERN_GLASS ? "Modern Glass" : "Minimal");
	}

	private HudAppearanceConfig configAppearance() {
		return pvpClient.getConfigStore().getConfig().appearance;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
		if((button != 0 && button != 12345) || mouseX >= panelX && mouseX < panelX + PANEL_WIDTH
				&& mouseY >= panelY && mouseY < panelY + PANEL_HEIGHT) {
			return;
		}
		HudManager hudManager = pvpClient.getHudManager();
		HudElement element = hudManager.getElementAt(mouseX, mouseY, resolution);
		if(element != null) {
			selectedElement = element;
			dragOffsetX = mouseX - hudManager.getElementX(element, resolution);
			dragOffsetY = mouseY - hudManager.getElementY(element, resolution);
			dragging = true;
			guideX = -1;
			guideY = -1;
			updateControls();
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long elapsed) {
		if(dragging && (button == 0 || button == 12345)) {
			HudManager hudManager = pvpClient.getHudManager();
			hudManager.moveElement(selectedElement, mouseX - dragOffsetX, mouseY - dragOffsetY, resolution);
			pvpClient.getConfigStore().markDirty();
			updateGuides(hudManager.getElementConfig(selectedElement));
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
		super.mouseReleased(mouseX, mouseY, button);
		if(button == 0 || button == 12345) {
			dragging = false;
			guideX = -1;
			guideY = -1;
		}
	}

	private void updateGuides(HudElementConfig elementConfig) {
		int column = anchorColumn(elementConfig.anchor);
		int row = anchorRow(elementConfig.anchor);
		int elementWidth = pvpClient.getHudManager().getElementWidth(selectedElement);
		int elementHeight = pvpClient.getHudManager().getElementHeight(selectedElement);
		if(elementConfig.offsetX == 0) {
			guideX = column == 1 ? width / 2 : column == 2 ? width - 1 : 0;
		}else {
			guideX = -1;
		}
		if(elementConfig.offsetY == 0) {
			guideY = row == 1 ? height / 2 : row == 2 ? height - 1 : 0;
		}else {
			guideY = -1;
		}
		if(elementWidth <= 0 || elementHeight <= 0) {
			guideX = -1;
			guideY = -1;
		}
	}

	private int anchorColumn(HudElementConfig.Anchor anchor) {
		switch(anchor) {
		case TOP_CENTER:
		case CENTER:
		case BOTTOM_CENTER:
			return 1;
		case TOP_RIGHT:
		case CENTER_RIGHT:
		case BOTTOM_RIGHT:
			return 2;
		default:
			return 0;
		}
	}

	private int anchorRow(HudElementConfig.Anchor anchor) {
		switch(anchor) {
		case CENTER_LEFT:
		case CENTER:
		case CENTER_RIGHT:
			return 1;
		case BOTTOM_LEFT:
		case BOTTOM_CENTER:
		case BOTTOM_RIGHT:
			return 2;
		default:
			return 0;
		}
	}

	@Override
	protected void keyTyped(char character, int keyCode) {
		if(keyCode == 1 || keyCode == mc.gameSettings.keyBindClose.getKeyCode()
				|| keyCode > 0 && keyCode == mc.gameSettings.keyBindPvpHudEditor.getKeyCode()) {
			closeEditor();
		}else {
			super.keyTyped(character, keyCode);
		}
	}

	private void closeEditor() {
		pvpClient.getConfigStore().saveNow();
		mc.displayGuiScreen(null);
		if(mc.currentScreen == null) {
			mc.setIngameFocus();
		}
	}

}
