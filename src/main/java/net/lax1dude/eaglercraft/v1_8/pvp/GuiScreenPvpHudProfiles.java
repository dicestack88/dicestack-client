package net.lax1dude.eaglercraft.v1_8.pvp;

import java.util.List;

import net.lax1dude.eaglercraft.v1_8.pvp.config.PvpConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.IDicestackBackgroundScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;

public final class GuiScreenPvpHudProfiles extends GuiScreen implements IDicestackBackgroundScreen {

	private final PvpClient pvpClient;
	private final GuiScreen parent;
	private GuiTextField nameField;
	private int selectedIndex;
	private String status = "";
	private String selectedProfileName = "";
	private String profileNameText = "";
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;

	public GuiScreenPvpHudProfiles(PvpClient pvpClient, GuiScreen parent) {
		this.pvpClient = pvpClient;
		this.parent = parent;
	}

	@Override
	public void initGui() {
		fontRendererObj = PvpUiFontRenderer.get(mc);
		buttonList.clear();
		panelWidth = Math.min(380, Math.max(1, width - 12));
		panelHeight = Math.min(210, Math.max(1, height - 12));
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;
		nameField = new GuiTextField(100, fontRendererObj, panelX + 16, panelY + 57, panelWidth - 32, 20);
		nameField.setMaxStringLength(32);
		nameField.setText(profileNameText);
		List<String> profiles = profiles();
		String active = pvpClient.getConfigStore().getConfig().activeHudProfile;
		if(selectedProfileName.isEmpty()) {
			selectedProfileName = active;
		}
		selectedIndex = profiles.indexOf(selectedProfileName);
		if(selectedIndex < 0) {
			selectedIndex = profiles.indexOf(active);
		}
		if(selectedIndex < 0 && !profiles.isEmpty()) {
			selectedIndex = 0;
		}
		if(!profiles.isEmpty()) {
			selectedProfileName = profiles.get(selectedIndex);
		}
		int buttonWidth = (panelWidth - 40) / 3;
		int rowY = panelY + 100;
		buttonList.add(new PvpGuiButton(1, panelX + 12, rowY, buttonWidth, 20, "Save / Update"));
		buttonList.add(new PvpGuiButton(2, panelX + 16 + buttonWidth, rowY, buttonWidth, 20, "< Profile"));
		buttonList.add(new PvpGuiButton(3, panelX + 20 + buttonWidth * 2, rowY, buttonWidth, 20, "Profile >"));
		int secondY = rowY + 28;
		buttonList.add(new PvpGuiButton(4, panelX + 12, secondY, buttonWidth, 20, "Apply"));
		buttonList.add(new PvpGuiButton(5, panelX + 16 + buttonWidth, secondY, buttonWidth, 20, "Delete"));
		buttonList.add(new PvpGuiButton(6, panelX + 20 + buttonWidth * 2, secondY, buttonWidth, 20, "Back"));
		for(int i = 1; i < buttonList.size() - 1; ++i) {
			buttonList.get(i).enabled = !profiles.isEmpty();
		}
	}

	@Override
	public void updateScreen() {
		nameField.updateCursorCounter();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		HudAppearanceConfig appearance = pvpClient.getConfigStore().getConfig().appearance;
		HudRenderer.drawPanel(appearance, panelX, panelY, panelWidth, panelHeight);
		drawCenteredString(fontRendererObj, "DICESTACK  /  HUD PROFILES", width / 2, panelY + 14, 0xFFFFFFFF);
		drawString(fontRendererObj, "Profile name", panelX + 16, panelY + 43, 0xFF9BA8B8);
		nameField.drawTextBox();
		List<String> profiles = profiles();
		String selected = profiles.isEmpty() ? "No saved profiles" : profiles.get(selectedIndex);
		int textWidth = Math.max(0, panelWidth - 24);
		drawCenteredString(fontRendererObj, fontRendererObj.trimStringToWidth("Selected: " + selected, textWidth),
				width / 2, panelY + 86, 0xFFE9EFF7);
		drawCenteredString(fontRendererObj, fontRendererObj.trimStringToWidth(status, textWidth),
				width / 2, panelY + panelHeight - 22, 0xFF53D6A3);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		PvpConfig config = pvpClient.getConfigStore().getConfig();
		List<String> profiles = profiles();
		switch(button.id) {
		case 1:
			profileNameText = nameField.getText();
			if(config.saveHudProfile(profileNameText)) {
				selectedProfileName = config.activeHudProfile;
				status = "HUD profile saved";
				pvpClient.getConfigStore().markDirty();
			}else {
				String name = profileNameText.trim();
				if(name.isEmpty()) {
					status = "Enter a profile name";
				}else if(name.length() > 32) {
					status = "Profile names are limited to 32 characters";
				}else if(!config.getHudProfileNames().contains(name)
						&& config.getHudProfileNames().size() >= 8) {
					status = "Profile limit reached (8)";
				}else {
					status = "Could not save profile";
				}
			}
			break;
		case 2:
			if(!profiles.isEmpty()) {
				selectedIndex = (selectedIndex + profiles.size() - 1) % profiles.size();
				selectedProfileName = profiles.get(selectedIndex);
			}
			break;
		case 3:
			if(!profiles.isEmpty()) {
				selectedIndex = (selectedIndex + 1) % profiles.size();
				selectedProfileName = profiles.get(selectedIndex);
			}
			break;
		case 4:
			if(!profiles.isEmpty() && config.applyHudProfile(profiles.get(selectedIndex))) {
				status = "HUD profile applied";
				selectedProfileName = profiles.get(selectedIndex);
				profileNameText = selectedProfileName;
				pvpClient.getConfigStore().markDirty();
			}else {
				status = "Profile data is invalid";
			}
			break;
		case 5:
			if(!profiles.isEmpty() && config.removeHudProfile(profiles.get(selectedIndex))) {
				selectedIndex = Math.max(0, selectedIndex - 1);
				status = "HUD profile deleted";
				selectedProfileName = config.getHudProfileNames().isEmpty() ? ""
						: config.getHudProfileNames().get(selectedIndex);
				pvpClient.getConfigStore().markDirty();
			}
			break;
		case 6:
			mc.displayGuiScreen(parent);
			return;
		}
		initGui();
	}

	@Override
	protected void keyTyped(char character, int keyCode) {
		if(keyCode == 1 || keyCode == mc.gameSettings.keyBindClose.getKeyCode()) {
			mc.displayGuiScreen(parent);
			return;
		}
		if(nameField.textboxKeyTyped(character, keyCode)) {
			profileNameText = nameField.getText();
			return;
		}
		super.keyTyped(character, keyCode);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		nameField.mouseClicked(mouseX, mouseY, button);
		super.mouseClicked(mouseX, mouseY, button);
	}

	private List<String> profiles() {
		return pvpClient.getConfigStore().getConfig().getHudProfileNames();
	}
}
