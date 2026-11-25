package net.lax1dude.eaglercraft.v1_8.pvp;

import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfigStore;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElementConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public final class GuiScreenPvpHudEditor extends GuiScreen {

	private static final int TOOLBAR_BUTTONS = 7;
	private static final int POPUP_BUTTONS = 15;
	private static final int[] TOOLBAR_IDS = { 1, 2, 20, 3, 4, 5, 6 };
	private static final String[] TOOLBAR_LABELS = { "Hide", "-", "", "+", "Reset", "All", "Done" };
	private static final int[] POPUP_IDS = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 24, 25 };
	private static final int[] COLORS = { 0xFFF1F7F5, 0xFF53D6A3, 0xFF55DDA0, 0xFFFFD166, 0xFF70A7FF,
			0xFFFF7777, 0xFFFFFFFF, 0xFF111820, 0xFF172638, 0xFF0F1722, 0xFF8BA0B5 };

	private final PvpClient pvpClient;
	private final int[] toolbarX = new int[TOOLBAR_BUTTONS];
	private final int[] toolbarY = new int[TOOLBAR_BUTTONS];
	private final int[] toolbarWidth = new int[TOOLBAR_BUTTONS];
	private final int[] toolbarHeight = new int[TOOLBAR_BUTTONS];
	private final int[] popupXByButton = new int[POPUP_BUTTONS];
	private final int[] popupYByButton = new int[POPUP_BUTTONS];
	private int[] popupWidthByButton = new int[POPUP_BUTTONS];
	private int[] popupHeightByButton = new int[POPUP_BUTTONS];
	private String[] popupLabels = new String[POPUP_BUTTONS];
	private ScaledResolution resolution;
	private HudElement selectedElement;
	private String instructionText = "";
	private int toolbarPanelX;
	private int toolbarPanelY;
	private int toolbarPanelWidth;
	private int toolbarPanelHeight;
	private int popupPanelX;
	private int popupPanelY;
	private int popupPanelWidth;
	private int popupPanelHeight;
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
		instructionText = Keyboard.getKeyName(mc.gameSettings.keyBindPvpHudEditor.getKeyCode())
				+ " toggles editor; Esc closes";
		if(selectedElement == null) {
			selectedElement = pvpClient.getHudManager().getElement(0);
		}
		updateControlLabels();
		layoutControls();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		HudManager hudManager = pvpClient.getHudManager();
		hudManager.renderEditor(resolution, partialTicks, pvpClient.getRuntime());
		drawSnapGuides();
		drawElementBounds(hudManager);
		layoutControls();
		drawToolbar();
		if(selectedElement != null) {
			drawAppearancePopup();
		}
		fontRendererObj.drawStringWithShadow(instructionText, 7, height - 14, 0xFFDDDDDD);
	}

	private void layoutControls() {
		int availableWidth = Math.max(40, width - 8);
		toolbarPanelWidth = Math.min(256, availableWidth);
		toolbarPanelHeight = 24;
		toolbarPanelX = (width - toolbarPanelWidth) / 2;
		toolbarPanelY = Math.max(3, Math.min(height - toolbarPanelHeight - 3, (height - toolbarPanelHeight) / 2));
		int toolbarInnerWidth = Math.max(1, toolbarPanelWidth - 10);
		int gap = toolbarInnerWidth >= TOOLBAR_BUTTONS * 25 ? 2 : 1;
		int buttonWidth = Math.max(1, (toolbarInnerWidth - gap * (TOOLBAR_BUTTONS - 1)) / TOOLBAR_BUTTONS);
		int buttonX = toolbarPanelX + 5;
		for(int i = 0; i < TOOLBAR_BUTTONS; ++i) {
			toolbarX[i] = buttonX;
			toolbarY[i] = toolbarPanelY + 4;
			toolbarWidth[i] = buttonWidth;
			toolbarHeight[i] = 16;
			buttonX += buttonWidth + gap;
		}
		layoutPopup();
	}

	private void layoutPopup() {
		if(selectedElement == null) {
			popupPanelWidth = 0;
			popupPanelHeight = 0;
			return;
		}
		HudManager manager = pvpClient.getHudManager();
		int elementX = manager.getElementX(selectedElement, resolution);
		int elementY = manager.getElementY(selectedElement, resolution);
		int elementWidth = manager.getElementWidth(selectedElement);
		int elementHeight = manager.getElementHeight(selectedElement);
		int safeWidth = Math.max(1, width - 8);
		int columns = safeWidth >= 220 ? 4 : safeWidth >= 165 ? 3 : 2;
		popupPanelWidth = Math.min(204, safeWidth);
		int rows = (POPUP_BUTTONS + columns - 1) / columns;
		int gap = 2;
		int innerWidth = Math.max(1, popupPanelWidth - 10);
		int cellWidth = Math.max(1, (innerWidth - gap * (columns - 1)) / columns);
		int rowHeight = Math.max(10, Math.min(16, (height - 20 - gap * (rows - 1)) / rows));
		popupPanelHeight = Math.min(height - 8, rows * rowHeight + (rows - 1) * gap + 10);
		int belowY = elementY + elementHeight + 3;
		int aboveY = elementY - popupPanelHeight - 3;
		boolean fitsBelow = belowY + popupPanelHeight <= height - 4;
		boolean fitsAbove = aboveY >= 4;
		popupPanelY = fitsBelow ? belowY
				: aboveY >= 4 ? aboveY : Math.max(4, Math.min(height - popupPanelHeight - 4, belowY));
		popupPanelX = elementX + elementWidth / 2 - popupPanelWidth / 2;
		popupPanelX = Math.max(4, Math.min(width - popupPanelWidth - 4, popupPanelX));
		if(overlaps(popupPanelX, popupPanelY, popupPanelWidth, popupPanelHeight,
				toolbarPanelX, toolbarPanelY, toolbarPanelWidth, toolbarPanelHeight)) {
			if(popupPanelY == belowY && fitsAbove) {
				popupPanelY = aboveY;
			}else if(popupPanelY == aboveY && fitsBelow) {
				popupPanelY = belowY;
			}
		}

		int effectiveRowHeight = Math.max(1,
				(popupPanelHeight - 10 - gap * (rows - 1)) / rows);
		for(int i = 0; i < POPUP_BUTTONS; ++i) {
			int column = i % columns;
			int row = i / columns;
			popupXByButton[i] = popupPanelX + 5 + column * (cellWidth + gap);
			popupYByButton[i] = popupPanelY + 5 + row * (effectiveRowHeight + gap);
			popupWidthByButton[i] = column == columns - 1
					? popupPanelX + popupPanelWidth - 5 - popupXByButton[i] : cellWidth;
			popupHeightByButton[i] = effectiveRowHeight;
		}
	}

	private void drawToolbar() {
		drawPanel(toolbarPanelX, toolbarPanelY, toolbarPanelWidth, toolbarPanelHeight);
		HudElementConfig elementConfig = selectedElement == null ? null
				: pvpClient.getHudManager().getElementConfig(selectedElement);
		for(int i = 0; i < TOOLBAR_BUTTONS; ++i) {
			String label = TOOLBAR_LABELS[i];
			if(TOOLBAR_IDS[i] == 20) {
				label = elementConfig == null ? "Scale" : Float.toString(elementConfig.scale) + "x";
			}else if(TOOLBAR_IDS[i] == 1 && elementConfig != null && !elementConfig.enabled) {
				label = "Show";
			}
			drawControl(toolbarX[i], toolbarY[i], toolbarWidth[i], toolbarHeight[i], label,
					TOOLBAR_IDS[i] == 1 && elementConfig != null && elementConfig.enabled);
		}
	}

	private void drawAppearancePopup() {
		drawPanel(popupPanelX, popupPanelY, popupPanelWidth, popupPanelHeight);
		for(int i = 0; i < POPUP_BUTTONS; ++i) {
			drawControl(popupXByButton[i], popupYByButton[i], popupWidthByButton[i], popupHeightByButton[i],
					popupLabels[i], isPopupControlActive(POPUP_IDS[i]));
		}
	}

	private void drawPanel(int x, int y, int panelWidth, int panelHeight) {
		Gui.drawRect(x, y, x + panelWidth, y + panelHeight, 0xE8121E2A);
		Gui.drawRect(x, y, x + panelWidth, y + 1, 0xFF53D6A3);
		Gui.drawRect(x, y + panelHeight - 1, x + panelWidth, y + panelHeight, 0xAA53D6A3);
		Gui.drawRect(x, y, x + 1, y + panelHeight, 0xAA53D6A3);
		Gui.drawRect(x + panelWidth - 1, y, x + panelWidth, y + panelHeight, 0xAA53D6A3);
	}

	private void drawControl(int x, int y, int controlWidth, int controlHeight, String label, boolean active) {
		int background = active ? 0xFF294538 : 0xCC202D3A;
		Gui.drawRect(x, y, x + controlWidth, y + controlHeight, background);
		Gui.drawRect(x, y, x + controlWidth, y + 1, active ? 0xFF53D6A3 : 0xFF526477);
		int textWidth = fontRendererObj.getStringWidth(label);
		int textX = x + Math.max(1, (controlWidth - textWidth) / 2);
		int textY = y + Math.max(0, (controlHeight - 8) / 2);
		fontRendererObj.drawString(label, textX, textY, 0xFFF1F7F5);
	}

	private void drawElementBounds(HudManager hudManager) {
		for(int i = 0, count = hudManager.getElementCount(); i < count; ++i) {
			HudElement element = hudManager.getElement(i);
			int x = hudManager.getElementX(element, resolution);
			int y = hudManager.getElementY(element, resolution);
			int right = x + Math.max(1, hudManager.getElementWidth(element));
			int bottom = y + Math.max(1, hudManager.getElementHeight(element));
			HudElementConfig elementConfig = hudManager.getElementConfig(element);
			int color = element == selectedElement ? 0xFF53D6A3
					: elementConfig.enabled ? 0xAAFFFFFF : 0xAAFF6666;
			Gui.drawRect(x, y, right, y + 1, color);
			Gui.drawRect(x, bottom - 1, right, bottom, color);
			Gui.drawRect(x, y, x + 1, bottom, color);
			Gui.drawRect(right - 1, y, right, bottom, color);
		}
	}

	private void drawSnapGuides() {
		if(guideX >= 0) {
			Gui.drawRect(guideX, 0, guideX + 1, height, 0xAA53D6A3);
		}
		if(guideY >= 0) {
			Gui.drawRect(0, guideY, width, guideY + 1, 0xAA53D6A3);
		}
	}

	private void updateControlLabels() {
		if(selectedElement == null) {
			return;
		}
		HudElementConfig element = pvpClient.getHudManager().getElementConfig(selectedElement);
		HudAppearanceConfig style = getAppearance(element);
		popupLabels[0] = element.appearanceOverride ? "LOCAL" : "GLOBAL";
		popupLabels[1] = style.preset == HudAppearanceConfig.Preset.MODERN_GLASS ? "GLASS" : "MINIMAL";
		popupLabels[2] = style.backgroundEnabled ? "BG ON" : "BG OFF";
		popupLabels[3] = "BG CLR";
		popupLabels[4] = style.gradientEnabled ? "GRAD ON" : "GRAD OFF";
		popupLabels[5] = "G2 CLR";
		popupLabels[6] = "TXT CLR";
		popupLabels[7] = "OP -";
		popupLabels[8] = "OP +";
		popupLabels[9] = style.borderEnabled ? "BORD ON" : "BORD OFF";
		popupLabels[10] = "B CLR";
		popupLabels[11] = "B -";
		popupLabels[12] = "B +";
		popupLabels[13] = style.gradientDirection == HudAppearanceConfig.GradientDirection.HORIZONTAL ? "GRAD H"
				: "GRAD V";
		popupLabels[14] = style.textAlignment == HudAppearanceConfig.TextAlignment.LEFT ? "ALIGN L"
				: style.textAlignment == HudAppearanceConfig.TextAlignment.CENTER ? "ALIGN C" : "ALIGN R";
	}

	private boolean overlaps(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
		return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
	}

	private boolean isPopupControlActive(int id) {
		HudElementConfig element = pvpClient.getHudManager().getElementConfig(selectedElement);
		HudAppearanceConfig style = getAppearance(element);
		switch(id) {
		case 10: return element.appearanceOverride;
		case 12: return style.backgroundEnabled;
		case 14: return style.gradientEnabled;
		case 18: return style.borderEnabled;
		default: return false;
		}
	}

	private HudAppearanceConfig getAppearance(HudElementConfig element) {
		PvpConfig config = pvpClient.getConfigStore().getConfig();
		return element.appearanceOverride ? element.appearance : config.appearance;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		if(button != 0 && button != 12345) {
			return;
		}
		layoutControls();
		int toolbarControl = findToolbarControl(mouseX, mouseY);
		if(toolbarControl >= 0) {
			handleToolbarAction(TOOLBAR_IDS[toolbarControl]);
			return;
		}
		int popupControl = selectedElement == null ? -1 : findPopupControl(mouseX, mouseY);
		if(popupControl >= 0) {
			handlePopupAction(POPUP_IDS[popupControl]);
			return;
		}
		HudManager hudManager = pvpClient.getHudManager();
		HudElement element = hudManager.getElementAt(mouseX, mouseY, resolution);
		guideX = -1;
		guideY = -1;
		if(element != null) {
			selectedElement = element;
			dragOffsetX = mouseX - hudManager.getElementX(element, resolution);
			dragOffsetY = mouseY - hudManager.getElementY(element, resolution);
			dragging = true;
			updateControlLabels();
		}else {
			selectedElement = null;
			dragging = false;
		}
	}

	private int findToolbarControl(int mouseX, int mouseY) {
		for(int i = 0; i < TOOLBAR_BUTTONS; ++i) {
			if(contains(toolbarX[i], toolbarY[i], toolbarWidth[i], toolbarHeight[i], mouseX, mouseY)) {
				return i;
			}
		}
		return -1;
	}

	private int findPopupControl(int mouseX, int mouseY) {
		for(int i = 0; i < POPUP_BUTTONS; ++i) {
			if(contains(popupXByButton[i], popupYByButton[i], popupWidthByButton[i], popupHeightByButton[i],
					mouseX, mouseY)) {
				return i;
			}
		}
		return -1;
	}

	private boolean contains(int x, int y, int controlWidth, int controlHeight, int mouseX, int mouseY) {
		return mouseX >= x && mouseX < x + controlWidth && mouseY >= y && mouseY < y + controlHeight;
	}

	private void handleToolbarAction(int id) {
		if(id == 6) {
			closeEditor();
			return;
		}
		if(selectedElement == null) {
			return;
		}
		HudElementConfig element = pvpClient.getHudManager().getElementConfig(selectedElement);
		switch(id) {
		case 1:
			element.enabled = !element.enabled;
			break;
		case 2:
			setScale(element.scale - 0.1f);
			break;
		case 3:
			setScale(element.scale + 0.1f);
			break;
		case 4:
			pvpClient.getConfigStore().getConfig().resetElement(selectedElement.getId());
			updateControlLabels();
			break;
		case 5:
			pvpClient.getConfigStore().getConfig().resetElements();
			updateControlLabels();
			break;
		}
		pvpClient.getConfigStore().markDirty();
	}

	private void handlePopupAction(int id) {
		HudElementConfig element = pvpClient.getHudManager().getElementConfig(selectedElement);
		HudAppearanceConfig style = getAppearance(element);
		switch(id) {
		case 10:
			if(!element.appearanceOverride) {
				element.appearance.copyFrom(pvpClient.getConfigStore().getConfig().appearance);
				element.appearanceOverride = true;
			}else {
				element.appearanceOverride = false;
			}
			break;
		case 11:
			style.applyPreset(style.preset == HudAppearanceConfig.Preset.MODERN_GLASS
					? HudAppearanceConfig.Preset.MINIMAL_COMPETITIVE : HudAppearanceConfig.Preset.MODERN_GLASS);
			break;
		case 12:
			style.backgroundEnabled = !style.backgroundEnabled;
			break;
		case 13:
			style.gradientStart = nextColor(style.gradientStart);
			break;
		case 14:
			style.gradientEnabled = !style.gradientEnabled;
			break;
		case 15:
			style.gradientEnd = nextColor(style.gradientEnd);
			break;
		case 16:
			style.textColor = nextColor(style.textColor);
			break;
		case 17:
			style.backgroundOpacity = Math.max(0.0f, style.backgroundOpacity - 0.1f);
			break;
		case 18:
			style.backgroundOpacity = Math.min(1.0f, style.backgroundOpacity + 0.1f);
			break;
		case 19:
			style.borderEnabled = !style.borderEnabled;
			break;
		case 21:
			style.borderColor = nextColor(style.borderColor);
			break;
		case 22:
			style.borderThickness = Math.max(0, style.borderThickness - 1);
			break;
		case 23:
			style.borderThickness = Math.min(4, style.borderThickness + 1);
			break;
		case 24:
			style.gradientDirection = style.gradientDirection == HudAppearanceConfig.GradientDirection.HORIZONTAL
					? HudAppearanceConfig.GradientDirection.VERTICAL
					: HudAppearanceConfig.GradientDirection.HORIZONTAL;
			break;
		case 25:
			style.textAlignment = style.textAlignment == HudAppearanceConfig.TextAlignment.LEFT
					? HudAppearanceConfig.TextAlignment.CENTER
					: style.textAlignment == HudAppearanceConfig.TextAlignment.CENTER
							? HudAppearanceConfig.TextAlignment.RIGHT
							: HudAppearanceConfig.TextAlignment.LEFT;
			break;
		}
		updateControlLabels();
		pvpClient.getConfigStore().markDirty();
	}

	private int nextColor(int color) {
		for(int i = 0; i < COLORS.length; ++i) {
			if(COLORS[i] == color) {
				return COLORS[(i + 1) % COLORS.length];
			}
		}
		return COLORS[0];
	}

	private void setScale(float scale) {
		HudElementConfig elementConfig = pvpClient.getHudManager().getElementConfig(selectedElement);
		elementConfig.scale = Math.max(0.5f, Math.min(3.0f, Math.round(scale * 10.0f) / 10.0f));
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long elapsed) {
		if(dragging && selectedElement != null && (button == 0 || button == 12345)) {
			HudManager hudManager = pvpClient.getHudManager();
			hudManager.moveElement(selectedElement, mouseX - dragOffsetX, mouseY - dragOffsetY, resolution);
			pvpClient.getConfigStore().markDirty();
			updateGuides(hudManager.getElementConfig(selectedElement));
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
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
		guideX = elementConfig.offsetX == 0 ? column == 1 ? width / 2 : column == 2 ? width - 1 : 0 : -1;
		guideY = elementConfig.offsetY == 0 ? row == 1 ? height / 2 : row == 2 ? height - 1 : 0 : -1;
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
