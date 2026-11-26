package net.lax1dude.eaglercraft.v1_8.pvp;

import net.minecraft.client.Minecraft;

public final class PvpSliderButton extends PvpGuiButton {

	public interface ValueListener {
		void onValueChanged(float value);
	}

	public interface ValueFormatter {
		String format(float value);
	}

	private final float minimum;
	private final float maximum;
	private final float step;
	private final ValueListener listener;
	private final ValueFormatter formatter;
	private float value;
	private boolean dragging;

	public PvpSliderButton(int id, int x, int y, int width, int height, String label,
			float minimum, float maximum, float step, float value,
			ValueListener listener, ValueFormatter formatter) {
		super(id, x, y, width, height, label);
		this.minimum = minimum;
		this.maximum = maximum;
		this.step = step;
		this.listener = listener;
		this.formatter = formatter;
		setValue(value, false);
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
		if(!visible) {
			return;
		}
		if(dragging) {
			setValueAt(mouseX);
		}
		displayString = super.displayString;
		super.displayString = displayString + ": " + formatter.format(value);
		super.drawButton(minecraft, mouseX, mouseY);
		super.displayString = displayString;
		int trackY = yPosition + height - 4;
		int trackLeft = xPosition + 5;
		int trackWidth = Math.max(1, width - 10);
		drawRect(trackLeft, trackY, trackLeft + trackWidth, trackY + 2, 0xFF0B1119);
		float normalized = maximum == minimum ? 0.0f : (value - minimum) / (maximum - minimum);
		int filled = Math.round(trackWidth * normalized);
		int accent = 0xFF53D6A3;
		PvpClient client = PvpClient.getInstance();
		if(client != null) {
			accent = client.getConfigStore().getConfig().appearance.accentColor;
		}
		if(filled > 0) {
			drawRect(trackLeft, trackY, trackLeft + filled, trackY + 2, accent);
		}
		drawRect(trackLeft + filled - 1, trackY - 2, trackLeft + filled + 2, trackY + 4, 0xFFFFFFFF);
	}

	@Override
	public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
		if(!super.mousePressed(minecraft, mouseX, mouseY)) {
			return false;
		}
		dragging = true;
		setValueAt(mouseX);
		return true;
	}

	@Override
	protected void mouseDragged(Minecraft minecraft, int mouseX, int mouseY) {
		if(dragging) {
			setValueAt(mouseX);
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		dragging = false;
	}

	@Override
	public boolean isSliderTouchEvents() {
		return true;
	}

	public float getValue() {
		return value;
	}

	private void setValueAt(int mouseX) {
		int usableWidth = Math.max(1, width - 10);
		float position = Math.max(0.0f, Math.min(1.0f, (mouseX - (xPosition + 5)) / (float) usableWidth));
		setValue(minimum + (maximum - minimum) * position, true);
	}

	private void setValue(float nextValue, boolean notify) {
		float clamped = Math.max(minimum, Math.min(maximum, nextValue));
		if(step > 0.0f) {
			clamped = minimum + Math.round((clamped - minimum) / step) * step;
			clamped = Math.max(minimum, Math.min(maximum, clamped));
		}
		if(clamped != value) {
			value = clamped;
			if(notify && listener != null) {
				listener.onValueChanged(value);
			}
		}else {
			value = clamped;
		}
	}
}
