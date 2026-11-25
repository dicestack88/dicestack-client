package net.lax1dude.eaglercraft.v1_8.pvp;

import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public final class GuiScreenPvpQolSettings extends GuiScreen {

	private static final int ROW_GAP = 2;

	private final PvpClient pvpClient;
	private final GuiScreen parent;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private int buttonWidth;
	private int rowHeight;

	public GuiScreenPvpQolSettings(PvpClient pvpClient, GuiScreen parent) {
		this.pvpClient = pvpClient;
		this.parent = parent;
	}

	@Override
	public void initGui() {
		panelWidth = Math.min(236, Math.max(1, width - 4));
		panelHeight = Math.min(258, Math.max(100, height - 4));
		buttonWidth = Math.max(1, panelWidth - 30);
		rowHeight = Math.max(12, Math.min(18, (panelHeight - 53 - ROW_GAP * 5) / 6));
		panelX = (width - panelWidth) / 2;
		panelY = Math.max(2, (height - panelHeight) / 2);
		int x = panelX + 8;
		int pairGap = 4;
		int pairWidth = Math.max(1, (panelWidth - 20 - pairGap) / 2);
		addPairButton(1, x, 0, pairWidth, "Sprint");
		addPairButton(2, x + pairWidth + pairGap, 0, pairWidth, "Sneak");
		addPairButton(3, x, 1, pairWidth, "Zoom");
		addPairButton(4, x + pairWidth + pairGap, 1, pairWidth, "Zoom Mode");
		addPairButton(5, x, 2, pairWidth, "Zoom -");
		addPairButton(6, x + pairWidth + pairGap, 2, pairWidth, "Zoom +");
		addFullRowButton(7, x, 3, "Coordinate Format");
		addPairButton(8, x, 4, pairWidth, "Clock Format");
		addPairButton(9, x + pairWidth + pairGap, 4, pairWidth, "Clock Seconds");
		addFullRowButton(10, x, 5, "Quick Reconnect");
		buttonList.add(new GuiButton(11, x, panelY + panelHeight - 23, panelWidth - 16, rowHeight, "Done"));
		updateLabels();
	}

	private void addPairButton(int id, int x, int row, int width, String label) {
		buttonList.add(new GuiButton(id, x, panelY + 26 + row * (rowHeight + ROW_GAP), width, rowHeight, label));
	}

	private void addFullRowButton(int id, int x, int row, String label) {
		buttonList.add(new GuiButton(id, x, panelY + 26 + row * (rowHeight + ROW_GAP),
				panelWidth - 16, rowHeight, label));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE8121E2A);
		drawRect(panelX, panelY, panelX + panelWidth, panelY + 2, 0xFF53D6A3);
		drawRect(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, 0xAA53D6A3);
		drawCenteredString(fontRendererObj, "DICESTACK  /  QUALITY OF LIFE  /  ZOOM FOV "
				+ Float.toString(pvpClient.getConfigStore().getConfig().zoomStrength),
				width / 2, panelY + 9, 0xFFF1F7F5);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		PvpConfig config = pvpClient.getConfigStore().getConfig();
		switch(button.id) {
		case 1:
			config.toggleSprintEnabled = !config.toggleSprintEnabled;
			break;
		case 2:
			config.toggleSneakEnabled = !config.toggleSneakEnabled;
			break;
		case 3:
			config.zoomEnabled = !config.zoomEnabled;
			break;
		case 4:
			config.zoomToggle = !config.zoomToggle;
			break;
		case 5:
			config.zoomStrength = Math.max(4.0f, config.zoomStrength - 2.0f);
			break;
		case 6:
			config.zoomStrength = Math.min(32.0f, config.zoomStrength + 2.0f);
			break;
		case 7:
			config.coordinateFormat = next(config.coordinateFormat);
			break;
		case 8:
			config.clock24Hour = !config.clock24Hour;
			break;
		case 9:
			config.clockShowSeconds = !config.clockShowSeconds;
			break;
		case 10:
			pvpClient.reconnect(this);
			return;
		case 11:
			mc.displayGuiScreen(parent);
			return;
		default:
			return;
		}
		pvpClient.getConfigStore().markDirty();
		updateLabels();
	}

	private PvpConfig.CoordinateFormat next(PvpConfig.CoordinateFormat current) {
		switch(current) {
		case LABELLED:
			return PvpConfig.CoordinateFormat.COMMAS;
		case COMMAS:
			return PvpConfig.CoordinateFormat.SLASHES;
		default:
			return PvpConfig.CoordinateFormat.LABELLED;
		}
	}

	private void updateLabels() {
		PvpConfig config = pvpClient.getConfigStore().getConfig();
		button(1).displayString = "Sprint " + enabled(config.toggleSprintEnabled);
		button(2).displayString = "Sneak " + enabled(config.toggleSneakEnabled);
		button(3).displayString = "Zoom " + enabled(config.zoomEnabled);
		button(4).displayString = config.zoomToggle ? "Mode: Toggle" : "Mode: Hold";
		button(5).displayString = "Zoom FOV -";
		button(6).displayString = "Zoom FOV +";
		button(7).displayString = "Coordinates: " + coordinateFormatLabel(config.coordinateFormat);
		button(8).displayString = config.clock24Hour ? "Clock 24h" : "Clock 12h";
		button(9).displayString = "Seconds " + enabled(config.clockShowSeconds);
		button(10).displayString = config.lastServerAddress == null || config.lastServerAddress.isEmpty()
				? "No Previous Server" : "Quick Reconnect";
		button(10).enabled = config.lastServerAddress != null && !config.lastServerAddress.isEmpty();
	}

	private GuiButton button(int id) {
		for(int i = 0; i < buttonList.size(); ++i) {
			GuiButton button = (GuiButton) buttonList.get(i);
			if(button.id == id) {
				return button;
			}
		}
		throw new IllegalStateException("Missing QoL settings button: " + id);
	}

	private String enabled(boolean value) {
		return value ? "On" : "Off";
	}

	private String coordinateFormatLabel(PvpConfig.CoordinateFormat format) {
		switch(format) {
		case COMMAS:
			return "120, 64, -230";
		case SLASHES:
			return "120 / 64 / -230";
		default:
			return "X: 120, Y: 64, Z: -230";
		}
	}
}
