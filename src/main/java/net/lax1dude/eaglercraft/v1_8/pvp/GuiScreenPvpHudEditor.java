package net.lax1dude.eaglercraft.v1_8.pvp;

import net.lax1dude.eaglercraft.v1_8.Keyboard;
import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfigStore;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElementConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudManager;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public final class GuiScreenPvpHudEditor extends GuiScreen {

	private static final int TOOLBAR_BUTTONS = 7;
	private static final int POPUP_BUTTONS = 26;
	private static final int[] TOOLBAR_IDS = { 1, 2, 20, 3, 4, 5, 6 };
	private static final String[] TOOLBAR_LABELS = { "Hide", "", "", "", "Reset", "All", "Done" };
	private static final int[] POPUP_IDS = { 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 21, 22, 23, 24, 25,
			26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36 };
	private static final int[] COLORS = { 0xFFF1F7F5, 0xFF53D6A3, 0xFF55DDA0, 0xFFFFD166, 0xFF70A7FF,
			0xFFFF7777, 0xFFFFFFFF, 0xFF111820, 0xFF172638, 0xFF0F1722, 0xFF8BA0B5 };

	private final PvpClient pvpClient;
	private final GuiScreen parent;
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
	private boolean adjustingScale;
	private int activePopupSlider = -1;

	public GuiScreenPvpHudEditor(PvpClient pvpClient) {
		this(pvpClient, null);
	}

	public GuiScreenPvpHudEditor(PvpClient pvpClient, GuiScreen parent) {
		this.pvpClient = pvpClient;
		this.parent = parent;
	}

	@Override
	public void initGui() {
		fontRendererObj = PvpUiFontRenderer.get(mc);
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
		drawToolbar(mouseX, mouseY);
		if(selectedElement != null) {
			drawAppearancePopup(mouseX, mouseY);
			drawSelectedModuleLabel();
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

	private void drawToolbar(int mouseX, int mouseY) {
		drawPanel(toolbarPanelX, toolbarPanelY, toolbarPanelWidth, toolbarPanelHeight);
		HudElementConfig elementConfig = selectedElement == null ? null
				: pvpClient.getHudManager().getElementConfig(selectedElement);
		for(int i = 0; i < TOOLBAR_BUTTONS; ++i) {
			if(i == 1 || i == 3) {
				continue;
			}
			if(i == 2) {
				drawScaleSlider(elementConfig, mouseX, mouseY);
				continue;
			}
			String label = TOOLBAR_LABELS[i];
			if(TOOLBAR_IDS[i] == 1 && elementConfig != null && !elementConfig.enabled) {
				label = "Show";
			}
			drawControl(toolbarX[i], toolbarY[i], toolbarWidth[i], toolbarHeight[i], label,
					TOOLBAR_IDS[i] == 1 && elementConfig != null && elementConfig.enabled,
					contains(toolbarX[i], toolbarY[i], toolbarWidth[i], toolbarHeight[i], mouseX, mouseY));
		}
	}

	private void drawScaleSlider(HudElementConfig elementConfig, int mouseX, int mouseY) {
		int x = toolbarX[1];
		int y = toolbarY[1];
		int right = toolbarX[3] + toolbarWidth[3];
		int sliderWidth = right - x;
		boolean hovered = contains(x, y, sliderWidth, toolbarHeight[1], mouseX, mouseY);
		int radius = getAppearance(selectedElement == null ? null
				: pvpClient.getHudManager().getElementConfig(selectedElement)).cornerRadius;
		HudRenderer.drawRoundedRect(x, y, sliderWidth, toolbarHeight[1], radius,
				hovered ? 0xFF304257 : 0xE51E2B3A);
		HudRenderer.drawRoundedBorder(x, y, sliderWidth, toolbarHeight[1], radius, 1, 0xFF53D6A3);
		float scale = elementConfig == null ? 1.0f : elementConfig.scale;
		String label = "Scale  " + Math.round(scale * 100.0f) / 100.0f + "x";
		fontRendererObj.drawString(fontRendererObj.trimStringToWidth(label, Math.max(0, sliderWidth - 6)),
				x + 3, y + 2, 0xFFF1F7F5);
		int trackLeft = x + 4;
		int trackWidth = Math.max(1, sliderWidth - 8);
		int trackY = y + toolbarHeight[1] - 4;
		Gui.drawRect(trackLeft, trackY, trackLeft + trackWidth, trackY + 2, 0xFF0B1119);
		int filled = Math.round(trackWidth * (scale - 0.5f) / 2.5f);
		Gui.drawRect(trackLeft, trackY, trackLeft + Math.max(1, filled), trackY + 2, 0xFF53D6A3);
		Gui.drawRect(trackLeft + filled - 1, trackY - 2, trackLeft + filled + 2, trackY + 4, 0xFFFFFFFF);
	}

	private void drawAppearancePopup(int mouseX, int mouseY) {
		drawPanel(popupPanelX, popupPanelY, popupPanelWidth, popupPanelHeight);
		for(int i = 0; i < POPUP_BUTTONS; ++i) {
			int id = POPUP_IDS[i];
			if(id == 18 || id == 23 || id == 32 || id == 34 || id == 36) {
				continue;
			}
			if(id == 17 || id == 22 || id == 31 || id == 33 || id == 35) {
				int companion = id + 1;
				int companionIndex = popupIndex(companion);
				if(companionIndex >= 0) {
					int sliderX = popupXByButton[i];
					int sliderY = popupYByButton[i];
					int sliderWidth = popupXByButton[companionIndex] + popupWidthByButton[companionIndex] - sliderX;
					drawPopupSlider(id, sliderX, sliderY, sliderWidth,
							popupHeightByButton[i], mouseX, mouseY);
					continue;
				}
			}
			drawControl(popupXByButton[i], popupYByButton[i], popupWidthByButton[i], popupHeightByButton[i],
					popupLabels[i], isPopupControlActive(id),
					contains(popupXByButton[i], popupYByButton[i], popupWidthByButton[i],
							popupHeightByButton[i], mouseX, mouseY));
		}
	}

	private void drawPopupSlider(int id, int x, int y, int controlWidth, int controlHeight, int mouseX, int mouseY) {
		boolean hovered = contains(x, y, controlWidth, controlHeight, mouseX, mouseY);
		int radius = getAppearance(pvpClient.getHudManager().getElementConfig(selectedElement)).cornerRadius;
		HudRenderer.drawRoundedRect(x, y, controlWidth, controlHeight, radius,
				hovered ? 0xFF304257 : 0xCC202D3A);
		HudRenderer.drawRoundedBorder(x, y, controlWidth, controlHeight, radius, 1,
				hovered ? 0xFF53D6A3 : 0xFF526477);
		String label = fontRendererObj.trimStringToWidth(popupSliderLabel(id), Math.max(0, controlWidth - 8));
		fontRendererObj.drawString(label, x + 3, y + Math.max(0, (controlHeight - 8) / 2 - 2), 0xFFF1F7F5);
		int trackLeft = x + 4;
		int trackWidth = Math.max(1, controlWidth - 8);
		int trackY = y + controlHeight - 3;
		float minimum = popupSliderMinimum(id);
		float maximum = popupSliderMaximum(id);
		float value = popupSliderValue(id);
		Gui.drawRect(trackLeft, trackY, trackLeft + trackWidth, trackY + 2, 0xFF0B1119);
		int filled = Math.round(trackWidth * (value - minimum) / Math.max(1.0f, maximum - minimum));
		Gui.drawRect(trackLeft, trackY, trackLeft + Math.max(1, filled), trackY + 2, 0xFF53D6A3);
		Gui.drawRect(trackLeft + filled - 1, trackY - 2, trackLeft + filled + 2, trackY + 4, 0xFFFFFFFF);
	}

	private int popupIndex(int id) {
		for(int i = 0; i < POPUP_IDS.length; ++i) {
			if(POPUP_IDS[i] == id) {
				return i;
			}
		}
		return -1;
	}

	private String popupSliderLabel(int id) {
		switch(id) {
		case 17: return "BG OPACITY  " + Math.round(popupSliderValue(id) * 100.0f) + "%";
		case 22: return "BORDER WIDTH  " + Math.round(popupSliderValue(id));
		case 31: return "X OFFSET  " + pvpClient.getHudManager().getElementConfig(selectedElement).offsetX;
		case 33: return "Y OFFSET  " + pvpClient.getHudManager().getElementConfig(selectedElement).offsetY;
		case 35: return "CORNER RADIUS  " + Math.round(popupSliderValue(id));
		default: return "";
		}
	}

	private float popupSliderValue(int id) {
		HudElementConfig element = pvpClient.getHudManager().getElementConfig(selectedElement);
		HudAppearanceConfig style = getAppearance(element);
		switch(id) {
		case 17: return style.backgroundOpacity;
		case 22: return style.borderThickness;
		case 31: return element.offsetX;
		case 33: return element.offsetY;
		case 35: return style.cornerRadius;
		default: return 0.0f;
		}
	}

	private float popupSliderMinimum(int id) {
		return id == 17 || id == 22 || id == 35 ? 0.0f : id == 31 ? -width : -height;
	}

	private float popupSliderMaximum(int id) {
		return id == 17 ? 1.0f : id == 22 ? 4.0f : id == 35 ? 12.0f : id == 31 ? width : height;
	}

	private boolean startPopupSlider(int mouseX, int mouseY) {
		for(int id : new int[] { 17, 22, 31, 33, 35 }) {
			int index = popupIndex(id);
			int companionIndex = popupIndex(id + 1);
			if(index < 0 || companionIndex < 0) {
				continue;
			}
			int x = popupXByButton[index];
			int y = popupYByButton[index];
			int right = popupXByButton[companionIndex] + popupWidthByButton[companionIndex];
			int h = popupHeightByButton[index];
			if(contains(x, y, right - x, h, mouseX, mouseY)) {
				activePopupSlider = id;
				updatePopupSlider(mouseX);
				return true;
			}
		}
		return false;
	}

	private void updatePopupSlider(int mouseX) {
		int index = popupIndex(activePopupSlider);
		int companionIndex = popupIndex(activePopupSlider + 1);
		if(index < 0 || companionIndex < 0) {
			return;
		}
		int x = popupXByButton[index] + 4;
		int trackWidth = Math.max(1, popupXByButton[companionIndex] + popupWidthByButton[companionIndex]
				- popupXByButton[index] - 8);
		float fraction = Math.max(0.0f, Math.min(1.0f, (mouseX - x) / (float) trackWidth));
		float value = popupSliderMinimum(activePopupSlider)
				+ (popupSliderMaximum(activePopupSlider) - popupSliderMinimum(activePopupSlider)) * fraction;
		HudElementConfig element = pvpClient.getHudManager().getElementConfig(selectedElement);
		HudAppearanceConfig style = getAppearance(element);
		switch(activePopupSlider) {
		case 17: style.backgroundOpacity = value; break;
		case 22: style.borderThickness = Math.round(value); break;
		case 35: style.cornerRadius = Math.round(value); break;
		case 31: element.offsetX = Math.round(value); break;
		case 33: element.offsetY = Math.round(value); break;
		}
		pvpClient.getConfigStore().markDirty();
		updateControlLabels();
	}

	private void drawPanel(int x, int y, int panelWidth, int panelHeight) {
		HudAppearanceConfig style = selectedElement == null ? pvpClient.getConfigStore().getConfig().appearance
				: getAppearance(pvpClient.getHudManager().getElementConfig(selectedElement));
		HudRenderer.drawPanel(style, x, y, panelWidth, panelHeight);
	}

	private void drawControl(int x, int y, int controlWidth, int controlHeight, String label,
			boolean active, boolean hovered) {
		int background = active ? 0xFF294538 : hovered ? 0xE12B3B4B : 0xCC202D3A;
		HudAppearanceConfig style = selectedElement == null ? pvpClient.getConfigStore().getConfig().appearance
				: getAppearance(pvpClient.getHudManager().getElementConfig(selectedElement));
		int radius = style.cornerRadius;
		HudRenderer.drawRoundedRect(x, y, controlWidth, controlHeight, radius, background);
		HudRenderer.drawRoundedBorder(x, y, controlWidth, controlHeight, radius, 1,
				active || hovered ? 0xFF53D6A3 : 0xFF526477);
		int availableWidth = Math.max(0, controlWidth - 6);
		if(fontRendererObj.getStringWidth(label) > availableWidth) {
			String suffix = availableWidth >= fontRendererObj.getStringWidth("...") ? "..." : "";
			int textLimit = Math.max(0, availableWidth - fontRendererObj.getStringWidth(suffix));
			label = fontRendererObj.trimStringToWidth(label, textLimit) + suffix;
		}
		int textWidth = fontRendererObj.getStringWidth(label);
		int textX = x + Math.max(1, Math.min(controlWidth - textWidth - 1, (controlWidth - textWidth) / 2));
		int textY = y + Math.max(0, (controlHeight - 8) / 2);
		fontRendererObj.drawString(label, textX, textY, active || hovered ? 0xFFFFFFFF : 0xFFD8E1EA);
	}

	private void drawSelectedModuleLabel() {
		String name = selectedElement.getId();
		HudManager manager = pvpClient.getHudManager();
		for(int i = 0; i < manager.getElementCount(); ++i) {
			if(manager.getElement(i) == selectedElement) {
				name = manager.getModuleInfo(i).name;
				break;
			}
		}
		HudElementConfig config = manager.getElementConfig(selectedElement);
		String label = name + "  |  " + config.anchor.name().replace('_', ' ')
				+ "  |  drag to position";
		label = fontRendererObj.trimStringToWidth(label, Math.max(0, width - 8));
		fontRendererObj.drawStringWithShadow(label, Math.max(4, width / 2 - fontRendererObj.getStringWidth(label) / 2),
				Math.max(3, toolbarPanelY - 11), 0xFFF1F7F5);
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
		popupLabels[8] = "";
		popupLabels[9] = style.borderEnabled ? "BORD ON" : "BORD OFF";
		popupLabels[10] = "B CLR";
		popupLabels[11] = "BORDER";
		popupLabels[12] = "";
		popupLabels[13] = style.gradientDirection == HudAppearanceConfig.GradientDirection.HORIZONTAL ? "GRAD H"
				: "GRAD V";
		popupLabels[14] = style.textAlignment == HudAppearanceConfig.TextAlignment.LEFT ? "ALIGN L"
				: style.textAlignment == HudAppearanceConfig.TextAlignment.CENTER ? "ALIGN C" : "ALIGN R";
		popupLabels[15] = "FPS HI";
		popupLabels[16] = "FPS MID";
		popupLabels[17] = "FPS LOW";
		popupLabels[18] = style.animationsEnabled ? "ANIM ON" : "ANIM OFF";
		popupLabels[19] = "ANCHOR";
		popupLabels[20] = "X OFFSET";
		popupLabels[21] = "";
		popupLabels[22] = "Y OFFSET";
		popupLabels[23] = "";
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
		case 29: return style.animationsEnabled;
		default: return false;
		}
	}

	private HudAppearanceConfig getAppearance(HudElementConfig element) {
		PvpConfig config = pvpClient.getConfigStore().getConfig();
		return element == null || !element.appearanceOverride ? config.appearance : element.appearance;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		if(button != 0 && button != 12345) {
			return;
		}
		layoutControls();
		if(button == 0 && selectedElement != null && isScaleSlider(mouseX, mouseY)) {
			adjustingScale = true;
			updateScaleFromMouse(mouseX);
			return;
		}
		int toolbarControl = findToolbarControl(mouseX, mouseY);
		if(toolbarControl >= 0) {
			handleToolbarAction(TOOLBAR_IDS[toolbarControl]);
			return;
		}
		if(button == 0 && selectedElement != null && startPopupSlider(mouseX, mouseY)) {
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
			if(i == 1 || i == 3) {
				continue;
			}
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
		case 26:
			style.performanceGoodColor = nextColor(style.performanceGoodColor);
			break;
		case 27:
			style.performanceMediumColor = nextColor(style.performanceMediumColor);
			break;
		case 28:
			style.performanceLowColor = nextColor(style.performanceLowColor);
			break;
		case 29:
			style.animationsEnabled = !style.animationsEnabled;
			break;
		case 30:
			element.anchor = HudElementConfig.Anchor.values()[(element.anchor.ordinal() + 1)
					% HudElementConfig.Anchor.values().length];
			break;
		case 31:
			element.offsetX += anchorColumn(element.anchor) == 2 ? 2 : -2;
			break;
		case 32:
			element.offsetX += anchorColumn(element.anchor) == 2 ? -2 : 2;
			break;
		case 33:
			element.offsetY += anchorRow(element.anchor) == 2 ? 2 : -2;
			break;
		case 34:
			element.offsetY += anchorRow(element.anchor) == 2 ? -2 : 2;
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
		if(activePopupSlider >= 0 && (button == 0 || button == 12345)) {
			updatePopupSlider(mouseX);
			return;
		}
		if(adjustingScale && selectedElement != null && (button == 0 || button == 12345)) {
			updateScaleFromMouse(mouseX);
			return;
		}
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
			adjustingScale = false;
			activePopupSlider = -1;
			guideX = -1;
			guideY = -1;
		}
	}

	private boolean isScaleSlider(int mouseX, int mouseY) {
		int x = toolbarX[1];
		int y = toolbarY[1];
		int width = toolbarX[3] + toolbarWidth[3] - x;
		return contains(x, y, width, toolbarHeight[1], mouseX, mouseY);
	}

	private void updateScaleFromMouse(int mouseX) {
		int x = toolbarX[1] + 4;
		int width = Math.max(1, toolbarX[3] + toolbarWidth[3] - toolbarX[1] - 8);
		float fraction = Math.max(0.0f, Math.min(1.0f, (mouseX - x) / (float) width));
		float scale = 0.5f + fraction * 2.5f;
		HudElementConfig config = pvpClient.getHudManager().getElementConfig(selectedElement);
		config.scale = Math.max(0.5f, Math.min(3.0f, Math.round(scale * 100.0f) / 100.0f));
		pvpClient.getConfigStore().markDirty();
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
		mc.displayGuiScreen(parent);
		if(parent == null && mc.currentScreen == null) {
			mc.setIngameFocus();
		}
	}
}
