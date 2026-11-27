package net.lax1dude.eaglercraft.v1_8.pvp;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

public final class PvpColorPicker {

	public interface ColorListener {
		void onColorChanged(int color);
	}

	private final GuiTextField hexField;
	private final GuiTextField redField;
	private final GuiTextField greenField;
	private final GuiTextField blueField;
	private final ColorListener listener;
	private int color;
	private int defaultColor;
	private float hue;
	private float saturation;
	private float value;
	private float alpha;
	private int x;
	private int y;
	private int width;
	private int height;
	private int dragChannel = -1;

	public PvpColorPicker(Minecraft minecraft, int defaultColor, int initialColor, ColorListener listener) {
		this.defaultColor = defaultColor;
		this.listener = listener;
		this.hexField = new GuiTextField(900, PvpUiFontRenderer.get(minecraft), 0, 0, 72, 16);
		this.hexField.setMaxStringLength(8);
		this.redField = new GuiTextField(901, PvpUiFontRenderer.get(minecraft), 0, 0, 28, 16);
		this.greenField = new GuiTextField(902, PvpUiFontRenderer.get(minecraft), 0, 0, 28, 16);
		this.blueField = new GuiTextField(903, PvpUiFontRenderer.get(minecraft), 0, 0, 28, 16);
		this.redField.setMaxStringLength(3);
		this.greenField.setMaxStringLength(3);
		this.blueField.setMaxStringLength(3);
		setColor(initialColor, false);
	}

	public void setBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = Math.max(100, width);
		this.height = Math.max(76, height);
		this.hexField.xPosition = x + 6;
		this.hexField.yPosition = y + height - 20;
		this.redField.xPosition = x + 6;
		this.redField.yPosition = y + height - 40;
		this.greenField.xPosition = x + 38;
		this.greenField.yPosition = y + height - 40;
		this.blueField.xPosition = x + 70;
		this.blueField.yPosition = y + height - 40;
	}

	public void setColor(int color, boolean notify) {
		this.color = color;
		float[] hsv = rgbToHsv(
				(color >> 16) & 255,
				(color >> 8) & 255,
				color & 255);
		hue = hsv[0];
		saturation = hsv[1];
		value = hsv[2];
		alpha = (color >>> 24) / 255.0F;
		if(alpha == 0.0F && (color & 0x00FFFFFF) != 0) {
			alpha = 1.0F;
		}
		hexField.setText(toHex(color));
		redField.setText(Integer.toString((color >> 16) & 255));
		greenField.setText(Integer.toString((color >> 8) & 255));
		blueField.setText(Integer.toString(color & 255));
		if(notify && listener != null) {
			listener.onColorChanged(color);
		}
	}

	public int getColor() {
		return color;
	}

	public void updateScreen() {
		hexField.updateCursorCounter();
		redField.updateCursorCounter();
		greenField.updateCursorCounter();
		blueField.updateCursorCounter();
	}

	public void draw(Minecraft minecraft, int mouseX, int mouseY) {
		HudRenderer.drawRoundedRect(x, y, width, height, 6, 0xF016202B);
		HudRenderer.drawRoundedBorder(x, y, width, height, 6, 1, 0x88566A7D);

		int previewSize = 18;
		HudRenderer.drawRoundedRect(x + width - previewSize - 6, y + 6,
				previewSize, previewSize, 4, color);

		drawSlider(x + 6, y + 6, width - previewSize - 18, 12, hue, 0, 1.0F,
				hueColor(hue));
		drawSlider(x + 6, y + 22, width - 12, 12, saturation, 0, 1.0F,
				hsvToRgb(hue, saturation, value));
		drawSlider(x + 6, y + 38, width - 12, 12, value, 0, 1.0F,
				hsvToRgb(hue, saturation, value));
		drawSlider(x + 6, y + 54, width - 12, 12, alpha, 0, 1.0F,
				((int) (alpha * 255.0F) << 24) | (color & 0x00FFFFFF));

		fontRendererLabel(minecraft, "HEX", x + 6, y + height - 34);
		hexField.drawTextBox();
		fontRendererLabel(minecraft, "R", redField.xPosition, redField.yPosition - 10);
		fontRendererLabel(minecraft, "G", greenField.xPosition, greenField.yPosition - 10);
		fontRendererLabel(minecraft, "B", blueField.xPosition, blueField.yPosition - 10);
		redField.drawTextBox();
		greenField.drawTextBox();
		blueField.drawTextBox();

		int resetX = x + width - 42;
		HudRenderer.drawRoundedRect(resetX, y + height - 20, 36, 16, 4, 0xCC26384B);
		minecraft.fontRendererObj.drawStringWithShadow("RESET",
				resetX + 4, y + height - 16, 0xFFE2EAF1);
	}

	private void fontRendererLabel(Minecraft minecraft, String label, int x, int y) {
		PvpUiFontRenderer.get(minecraft).drawStringWithShadow(label, x, y, 0xFF9BA8B8);
	}

	private void drawSlider(int x, int y, int width, int height, float value,
			float minimum, float maximum, int knobColor) {
		HudRenderer.drawRoundedRect(x, y, width, height, 4, 0xFF0B1119);
		float normalized = maximum == minimum ? 0.0F : (value - minimum) / (maximum - minimum);
		int knobX = x + Math.round(normalized * (width - 8)) + 4;
		HudRenderer.drawRoundedRect(knobX - 3, y + 2, 6, height - 4, 3, knobColor);
	}

	public boolean mouseClicked(Minecraft minecraft, int mouseX, int mouseY, int button) {
		hexField.mouseClicked(mouseX, mouseY, button);
		redField.mouseClicked(mouseX, mouseY, button);
		greenField.mouseClicked(mouseX, mouseY, button);
		blueField.mouseClicked(mouseX, mouseY, button);
		if(button != 0) {
			return false;
		}
		if(mouseX >= x + width - 42 && mouseX < x + width - 6
				&& mouseY >= y + height - 20 && mouseY < y + height - 4) {
			setColor(defaultColor, true);
			return true;
		}
		if(mouseX >= x + 6 && mouseX < x + width - 6) {
			if(mouseY >= y + 6 && mouseY < y + 18) {
				dragChannel = 0;
			}else if(mouseY >= y + 22 && mouseY < y + 34) {
				dragChannel = 1;
			}else if(mouseY >= y + 38 && mouseY < y + 50) {
				dragChannel = 2;
			}else if(mouseY >= y + 54 && mouseY < y + 66) {
				dragChannel = 3;
			}else {
				dragChannel = -1;
			}
			if(dragChannel >= 0) {
				updateFromMouse(mouseX);
				return true;
			}
		}
		return false;
	}

	public void mouseClickMove(int mouseX, int mouseY, int button) {
		if(dragChannel >= 0 && button == 0) {
			updateFromMouse(mouseX);
		}
	}

	public void mouseReleased(int button) {
		dragChannel = -1;
	}

	private void updateFromMouse(int mouseX) {
		float normalized = Math.max(0.0F, Math.min(1.0F,
				(mouseX - (x + 6)) / (float) Math.max(1, width - 12)));
		switch(dragChannel) {
		case 0: hue = normalized; break;
		case 1: saturation = normalized; break;
		case 2: value = normalized; break;
		case 3: alpha = normalized; break;
		}
		int rgb = hsvToRgb(hue, saturation, value);
		int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
		color = (a << 24) | (rgb & 0x00FFFFFF);
		hexField.setText(toHex(color));
		if(listener != null) {
			listener.onColorChanged(color);
		}
	}

	public boolean keyTyped(char character, int keyCode) {
		if(hexField.isFocused()) {
			if(!hexField.textboxKeyTyped(character, keyCode)) {
				return false;
			}
			String text = hexField.getText();
			if(text.startsWith("#")) {
				text = text.substring(1);
			}
			if(text.length() == 6 || text.length() == 8) {
				try {
					long parsed = Long.parseLong(text, 16);
					int next;
					if(text.length() == 6) {
						next = 0xFF000000 | (int) parsed;
					}else {
						next = (int) parsed;
					}
					setColor(next, true);
				}catch(NumberFormatException ignored) {
				}
			}
			return true;
		}
		boolean rgbChanged = false;
		if(redField.isFocused()) {
			rgbChanged = redField.textboxKeyTyped(character, keyCode);
		}else if(greenField.isFocused()) {
			rgbChanged = greenField.textboxKeyTyped(character, keyCode);
		}else if(blueField.isFocused()) {
			rgbChanged = blueField.textboxKeyTyped(character, keyCode);
		}
		if(rgbChanged) {
			updateRgbFromFields();
		}
		return rgbChanged;
	}

	private void updateRgbFromFields() {
		int red = parseChannel(redField.getText());
		int green = parseChannel(greenField.getText());
		int blue = parseChannel(blueField.getText());
		int rgb = (red << 16) | (green << 8) | blue;
		int next = ((int) (alpha * 255.0F) << 24) | rgb;
		setColor(next, true);
	}

	private int parseChannel(String text) {
		try {
			return Math.max(0, Math.min(255, Integer.parseInt(text)));
		}catch(NumberFormatException ignored) {
			return 0;
		}
	}

	private static String toHex(int color) {
		if((color >>> 24) == 255) {
			return String.format("%06X", color & 0xFFFFFF);
		}
		return String.format("%08X", color);
	}

	private static int hueColor(float hue) {
		float h = hue * 6.0F;
		float r = 0.0F;
		float g = 0.0F;
		float b = 0.0F;
		int sector = (int) h;
		float fraction = h - sector;
		switch(sector % 6) {
		case 0: r = 1.0F; g = fraction; b = 0.0F; break;
		case 1: r = 1.0F - fraction; g = 1.0F; b = 0.0F; break;
		case 2: r = 0.0F; g = 1.0F; b = fraction; break;
		case 3: r = 0.0F; g = 1.0F - fraction; b = 1.0F; break;
		case 4: r = fraction; g = 0.0F; b = 1.0F; break;
		default: r = 1.0F; g = 0.0F; b = 1.0F - fraction; break;
		}
		return 0xFF000000 | ((int) (r * 255.0F) << 16)
				| ((int) (g * 255.0F) << 8) | (int) (b * 255.0F);
	}

	private static int hsvToRgb(float hue, float saturation, float value) {
		float h = hue * 6.0F;
		int sector = (int) h;
		float fraction = h - sector;
		float p = value * (1.0F - saturation);
		float q = value * (1.0F - saturation * fraction);
		float t = value * (1.0F - saturation * (1.0F - fraction));
		float r;
		float g;
		float b;
		switch(sector % 6) {
		case 0: r = value; g = t; b = p; break;
		case 1: r = q; g = value; b = p; break;
		case 2: r = p; g = value; b = t; break;
		case 3: r = p; g = q; b = value; break;
		case 4: r = t; g = p; b = value; break;
		default: r = value; g = p; b = q; break;
		}
		return 0xFF000000 | ((int) (r * 255.0F) << 16)
				| ((int) (g * 255.0F) << 8) | (int) (b * 255.0F);
	}

	private static float[] rgbToHsv(int red, int green, int blue) {
		float r = red / 255.0F;
		float g = green / 255.0F;
		float b = blue / 255.0F;
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float delta = max - min;
		float hue;
		if(delta == 0.0F) {
			hue = 0.0F;
		}else if(max == r) {
			hue = ((g - b) / delta) / 6.0F;
			if(hue < 0.0F) {
				hue += 1.0F;
			}
		}else if(max == g) {
			hue = (((b - r) / delta) + 2.0F) / 6.0F;
		}else {
			hue = (((r - g) / delta) + 4.0F) / 6.0F;
		}
		float saturation = max == 0.0F ? 0.0F : delta / max;
		return new float[] { hue, saturation, max };
	}
}
