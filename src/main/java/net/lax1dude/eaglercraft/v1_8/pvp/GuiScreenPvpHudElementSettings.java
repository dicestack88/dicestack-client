package net.lax1dude.eaglercraft.v1_8.pvp;

import java.util.Locale;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElementConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudManager;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IDicestackBackgroundScreen;

public final class GuiScreenPvpHudElementSettings extends GuiScreen implements IDicestackBackgroundScreen {

	private static final int ID_BACK = 1;
	private static final int ID_RESET = 2;
	private static final int ID_ENABLED = 3;
	private static final int ID_LOCAL = 10;
	private static final int ID_PRESET = 11;
	private static final int ID_BACKGROUND = 12;
	private static final int ID_BACKGROUND_COLOR = 13;
	private static final int ID_GRADIENT = 14;
	private static final int ID_GRADIENT_COLOR = 15;
	private static final int ID_TEXT_COLOR = 16;
	private static final int ID_TEXT_SHADOW = 17;
	private static final int ID_BORDER = 18;
	private static final int ID_BORDER_COLOR = 19;
	private static final int ID_GRADIENT_DIRECTION = 20;
	private static final int ID_TEXT_ALIGN = 21;
	private static final int ID_ACCENT_COLOR = 22;

	private final PvpClient pvpClient;
	private final GuiScreen parent;
	private final HudElement element;
	private PvpColorPicker colorPicker;
	private int colorProperty = -1;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;

	public GuiScreenPvpHudElementSettings(PvpClient pvpClient, GuiScreen parent, HudElement element) {
		this.pvpClient = pvpClient;
		this.parent = parent;
		this.element = element;
	}

	@Override
	public void initGui() {
		fontRendererObj = PvpUiFontRenderer.get(mc);
		buttonList.clear();
		panelWidth = Math.max(1, Math.min(620, width - 12));
		panelHeight = Math.max(1, Math.min(420, height - 12));
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;

		HudElementConfig config = pvpClient.getHudManager().getElementConfig(element);
		HudAppearanceConfig style = getStyle(config);
		int footerY = panelY + panelHeight - 28;
		int footerGap = 6;
		int footerWidth = Math.max(1, (panelWidth - 20 - footerGap * 2) / 3);
		buttonList.add(new PvpGuiButton(ID_BACK, panelX + 10, footerY, footerWidth, 20, "Back"));
		buttonList.add(new PvpGuiButton(ID_RESET, panelX + 10 + footerWidth + footerGap,
				footerY, footerWidth, 20, "Reset Appearance"));
		buttonList.add(new PvpToggleButton(ID_ENABLED, panelX + panelWidth - footerWidth - 10,
				footerY, footerWidth, 20, config.enabled));

		int contentTop = panelY + 96;
		int leftX = panelX + 10;
		int rightX = panelX + panelWidth / 2 + 4;
		int columnWidth = Math.max(1, panelWidth / 2 - 14);
		int rowHeight = 22;
		buttonList.add(new PvpGuiButton(ID_LOCAL, leftX, contentTop, columnWidth, rowHeight,
				config.appearanceOverride ? "Override: ON" : "Override: OFF"));
		buttonList.add(new PvpGuiButton(ID_PRESET, leftX, contentTop + 26, columnWidth, rowHeight,
				style.preset == HudAppearanceConfig.Preset.MODERN_GLASS ? "Preset: Glass" : "Preset: Minimal"));
		buttonList.add(new PvpGuiButton(ID_BACKGROUND, leftX, contentTop + 52, columnWidth, rowHeight,
				style.backgroundEnabled ? "Background: ON" : "Background: OFF"));
		buttonList.add(new PvpGuiButton(ID_GRADIENT, leftX, contentTop + 78, columnWidth, rowHeight,
				style.gradientEnabled ? "Gradient: ON" : "Gradient: OFF"));
		buttonList.add(new PvpGuiButton(ID_TEXT_SHADOW, leftX, contentTop + 104, columnWidth, rowHeight,
				style.textShadow ? "Text shadow: ON" : "Text shadow: OFF"));
		buttonList.add(new PvpGuiButton(ID_BORDER, leftX, contentTop + 130, columnWidth, rowHeight,
				style.borderEnabled ? "Border: ON" : "Border: OFF"));
		buttonList.add(new PvpGuiButton(ID_GRADIENT_DIRECTION, leftX, contentTop + 156, columnWidth, rowHeight,
				"Gradient: " + style.gradientDirection.name()));
		buttonList.add(new PvpGuiButton(ID_TEXT_ALIGN, leftX, contentTop + 182, columnWidth, rowHeight,
				"Text: " + style.textAlignment.name()));

		buttonList.add(new PvpGuiButton(ID_BACKGROUND_COLOR, rightX, contentTop,
				columnWidth, rowHeight, "Background Colour"));
		buttonList.add(new PvpGuiButton(ID_GRADIENT_COLOR, rightX, contentTop + 26,
				columnWidth, rowHeight, "Gradient Colour"));
		buttonList.add(new PvpGuiButton(ID_TEXT_COLOR, rightX, contentTop + 52,
				columnWidth, rowHeight, "Text Colour"));
		buttonList.add(new PvpGuiButton(ID_BORDER_COLOR, rightX, contentTop + 78,
				columnWidth, rowHeight, "Border Colour"));
		buttonList.add(new PvpGuiButton(ID_ACCENT_COLOR, rightX, contentTop + 104,
				columnWidth, rowHeight, "Accent Colour"));
		buttonList.add(new PvpSliderButton(30, rightX, contentTop + 130, columnWidth, rowHeight,
				"Scale", 0.5f, 3.0f, 0.05f, config.scale,
				value -> {
					pvpClient.getHudManager().getElementConfig(element).scale = value;
					pvpClient.getConfigStore().markDirty();
				},
				value -> String.format(Locale.ROOT, "%.2fx", value)));
		buttonList.add(new PvpSliderButton(31, rightX, contentTop + 156, columnWidth, rowHeight,
				"Opacity", 0.0f, 1.0f, 0.01f, style.backgroundOpacity,
				value -> {
					HudElementConfig target = pvpClient.getHudManager().getElementConfig(element);
					if(!target.appearanceOverride) {
						target.appearance.copyFrom(pvpClient.getConfigStore().getConfig().appearance);
						target.appearanceOverride = true;
					}
					target.appearance.backgroundOpacity = value;
					pvpClient.getConfigStore().markDirty();
				},
				value -> Math.round(value * 100.0f) + "%"));
		buttonList.add(new PvpSliderButton(32, rightX, contentTop + 182, columnWidth, rowHeight,
				"Padding", 0.0f, 16.0f, 1.0f, style.padding,
				value -> {
					HudElementConfig target = pvpClient.getHudManager().getElementConfig(element);
					if(!target.appearanceOverride) {
						target.appearance.copyFrom(pvpClient.getConfigStore().getConfig().appearance);
						target.appearanceOverride = true;
					}
					target.appearance.padding = Math.round(value);
					pvpClient.getConfigStore().markDirty();
				},
				value -> Integer.toString(Math.round(value))));
	}

	@Override
	public void updateScreen() {
		if(colorPicker != null) {
			colorPicker.updateScreen();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		HudAppearanceConfig appearance = pvpClient.getConfigStore().getConfig().appearance;
		HudRenderer.drawPanel(appearance, panelX, panelY, panelWidth, panelHeight);
		String name = getElementName();
		fontRendererObj.drawStringWithShadow(name, panelX + 10, panelY + 10, 0xFFF1F7F5);
		fontRendererObj.drawStringWithShadow("HUD element appearance and visibility",
				panelX + 10, panelY + 25, 0xFF9BA8B8);

		HudElementConfig config = pvpClient.getHudManager().getElementConfig(element);
		HudAppearanceConfig style = getStyle(config);
		int previewX = panelX + 10;
		int previewY = panelY + 42;
		int previewWidth = panelWidth - 20;
		int previewHeight = 46;
		HudRenderer.drawPanel(style, previewX, previewY, previewWidth, previewHeight);
		String previewText = name.toUpperCase(Locale.ROOT) + "  /  LIVE PREVIEW";
		previewText = fontRendererObj.trimStringToWidth(previewText,
				Math.max(1, previewWidth - style.padding * 2));
		fontRendererObj.drawStringWithShadow(previewText,
				previewX + style.padding,
				previewY + (previewHeight - 8) / 2,
				HudRenderer.withAlpha(style.textColor, style.textOpacity));
		super.drawScreen(mouseX, mouseY, partialTicks);

		if(colorPicker != null) {
			int pickerWidth = Math.min(240, panelWidth - 20);
			int pickerHeight = 112;
			int pickerX = panelX + panelWidth - pickerWidth - 10;
			int pickerY = panelY + panelHeight - pickerHeight - 34;
			colorPicker.setBounds(pickerX, pickerY, pickerWidth, pickerHeight);
			colorPicker.draw(mc, mouseX, mouseY);
		}
	}

	private String getElementName() {
		HudManager manager = pvpClient.getHudManager();
		for(int i = 0; i < manager.getElementCount(); ++i) {
			if(manager.getElement(i) == element) {
				return manager.getModuleInfo(i).name;
			}
		}
		return "HUD Element";
	}

	private HudAppearanceConfig getStyle(HudElementConfig elementConfig) {
		return elementConfig.appearanceOverride
				? elementConfig.appearance
				: pvpClient.getConfigStore().getConfig().appearance;
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		HudElementConfig elementConfig = pvpClient.getHudManager().getElementConfig(element);
		HudAppearanceConfig style = getStyle(elementConfig);
		if(button.id == ID_PRESET || button.id == ID_BACKGROUND || button.id == ID_GRADIENT
				|| button.id == ID_TEXT_SHADOW || button.id == ID_BORDER
				|| button.id == ID_GRADIENT_DIRECTION || button.id == ID_TEXT_ALIGN
				|| button.id == ID_BACKGROUND_COLOR || button.id == ID_GRADIENT_COLOR
				|| button.id == ID_TEXT_COLOR || button.id == ID_BORDER_COLOR
				|| button.id == ID_ACCENT_COLOR) {
			if(!elementConfig.appearanceOverride) {
				elementConfig.appearance.copyFrom(pvpClient.getConfigStore().getConfig().appearance);
				elementConfig.appearanceOverride = true;
				style = elementConfig.appearance;
			}
		}
		switch(button.id) {
		case ID_BACK:
			mc.displayGuiScreen(parent);
			return;
		case ID_RESET:
			elementConfig.appearanceOverride = true;
			elementConfig.appearance.copyFrom(HudAppearanceConfig.defaults());
			pvpClient.getConfigStore().markDirty();
			initGui();
			return;
		case ID_ENABLED:
			elementConfig.enabled = !elementConfig.enabled;
			pvpClient.getConfigStore().markDirty();
			initGui();
			return;
		case ID_LOCAL:
			if(!elementConfig.appearanceOverride) {
				elementConfig.appearance.copyFrom(pvpClient.getConfigStore().getConfig().appearance);
				elementConfig.appearanceOverride = true;
			}else {
				elementConfig.appearanceOverride = false;
			}
			break;
		case ID_PRESET:
			style.applyPreset(style.preset == HudAppearanceConfig.Preset.MODERN_GLASS
					? HudAppearanceConfig.Preset.MINIMAL_COMPETITIVE
					: HudAppearanceConfig.Preset.MODERN_GLASS);
			break;
		case ID_BACKGROUND:
			style.backgroundEnabled = !style.backgroundEnabled;
			break;
		case ID_GRADIENT:
			style.gradientEnabled = !style.gradientEnabled;
			break;
		case ID_TEXT_SHADOW:
			style.textShadow = !style.textShadow;
			break;
		case ID_BORDER:
			style.borderEnabled = !style.borderEnabled;
			break;
		case ID_GRADIENT_DIRECTION:
			style.gradientDirection = style.gradientDirection == HudAppearanceConfig.GradientDirection.HORIZONTAL
					? HudAppearanceConfig.GradientDirection.VERTICAL
					: HudAppearanceConfig.GradientDirection.HORIZONTAL;
			break;
		case ID_TEXT_ALIGN:
			style.textAlignment = style.textAlignment == HudAppearanceConfig.TextAlignment.LEFT
					? HudAppearanceConfig.TextAlignment.CENTER
					: style.textAlignment == HudAppearanceConfig.TextAlignment.CENTER
							? HudAppearanceConfig.TextAlignment.RIGHT
							: HudAppearanceConfig.TextAlignment.LEFT;
			break;
		case ID_BACKGROUND_COLOR:
			openColorPicker(ID_BACKGROUND_COLOR, style.gradientStart);
			return;
		case ID_GRADIENT_COLOR:
			openColorPicker(ID_GRADIENT_COLOR, style.gradientEnd);
			return;
		case ID_TEXT_COLOR:
			openColorPicker(ID_TEXT_COLOR, style.textColor);
			return;
		case ID_BORDER_COLOR:
			openColorPicker(ID_BORDER_COLOR, style.borderColor);
			return;
		case ID_ACCENT_COLOR:
			openColorPicker(ID_ACCENT_COLOR, style.accentColor);
			return;
		}
		pvpClient.getConfigStore().markDirty();
		initGui();
	}

	private void openColorPicker(int property, int initialColor) {
		colorProperty = property;
		colorPicker = new PvpColorPicker(mc, initialColor, initialColor, color -> {
			HudElementConfig elementConfig = pvpClient.getHudManager().getElementConfig(element);
			HudAppearanceConfig style = getStyle(elementConfig);
			if(colorProperty == ID_BACKGROUND_COLOR) {
				style.gradientStart = color;
			}else if(colorProperty == ID_GRADIENT_COLOR) {
				style.gradientEnd = color;
			}else if(colorProperty == ID_TEXT_COLOR) {
				style.textColor = color;
			}else if(colorProperty == ID_BORDER_COLOR) {
				style.borderColor = color;
			}else if(colorProperty == ID_ACCENT_COLOR) {
				style.accentColor = color;
			}
			pvpClient.getConfigStore().markDirty();
		});
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		if(colorPicker != null && colorPicker.mouseClicked(mc, mouseX, mouseY, button)) {
			pvpClient.getConfigStore().markDirty();
			return;
		}
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int button, long elapsed) {
		if(colorPicker != null) {
			colorPicker.mouseClickMove(mouseX, mouseY, button);
		}
		super.mouseClickMove(mouseX, mouseY, button, elapsed);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
		if(colorPicker != null) {
			colorPicker.mouseReleased(button);
		}
		super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	protected void keyTyped(char character, int keyCode) {
		if(keyCode == 1) {
			mc.displayGuiScreen(parent);
			return;
		}
		if(colorPicker != null && colorPicker.keyTyped(character, keyCode)) {
			pvpClient.getConfigStore().markDirty();
			return;
		}
		super.keyTyped(character, keyCode);
	}
}
