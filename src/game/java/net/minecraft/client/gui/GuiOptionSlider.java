package net.minecraft.client.gui;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.pvp.PvpClient;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;

/**+
 * This portion of EaglercraftX contains deobfuscated Minecraft 1.8 source code.
 * 
 * Minecraft 1.8.8 bytecode is (c) 2015 Mojang AB. "Do not distribute!"
 * Mod Coder Pack v9.18 deobfuscation configs are (c) Copyright by the MCP Team
 * 
 * EaglercraftX 1.8 patch files (c) 2022-2025 lax1dude, ayunami2000. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
public class GuiOptionSlider extends GuiButton {
	public float sliderValue;
	public boolean dragging;
	private GameSettings.Options options;
	private final float field_146132_r;
	private final float field_146131_s;

	public GuiOptionSlider(int parInt1, int parInt2, int parInt3, GameSettings.Options parOptions) {
		this(parInt1, parInt2, parInt3, parOptions, 0.0F, 1.0F);
	}

	public GuiOptionSlider(int parInt1, int parInt2, int parInt3, GameSettings.Options parOptions, float parFloat1,
			float parFloat2) {
		super(parInt1, parInt2, parInt3, 150, 20, "");
		this.sliderValue = 1.0F;
		this.options = parOptions;
		this.field_146132_r = parFloat1;
		this.field_146131_s = parFloat2;
		Minecraft minecraft = Minecraft.getMinecraft();
		this.sliderValue = parOptions.normalizeValue(minecraft.gameSettings.getOptionFloatValue(parOptions));
		this.displayString = minecraft.gameSettings.getKeyBinding(parOptions);
	}

	public GameSettings.Options getEnumOptions() {
		return options;
	}

	/**+
	 * Returns 0 if the button is disabled, 1 if the mouse is NOT
	 * hovering over this button and 2 if it IS hovering over this
	 * button.
	 */
	protected int getHoverState(boolean var1) {
		return 0;
	}

	/**+
	 * Fired when the mouse button is dragged. Equivalent of
	 * MouseListener.mouseDragged(MouseEvent e).
	 */
	protected void mouseDragged(Minecraft minecraft, int i, int var3) {
		if (this.visible) {
			if (this.dragging) {
				this.sliderValue = (float) (i - (this.xPosition + 4)) / (float) (this.width - 8);
				this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
				float f = this.options.denormalizeValue(this.sliderValue);
				minecraft.gameSettings.setOptionFloatValue(this.options, f);
				this.sliderValue = this.options.normalizeValue(f);
				this.displayString = minecraft.gameSettings.getKeyBinding(this.options);
			}

			int trackY = this.yPosition + this.height - 4;
			int trackLeft = this.xPosition + 5;
			int trackWidth = Math.max(1, this.width - 10);
			int radius = 2;
			int accent = 0xFF53D6A3;
			PvpClient pvp = PvpClient.getInstance();
			if(pvp != null) {
				radius = pvp.getConfigStore().getConfig().appearance.cornerRadius;
				accent = pvp.getConfigStore().getConfig().appearance.accentColor;
			}
			HudRenderer.drawRoundedRect(trackLeft, trackY, trackWidth, 2,
					Math.min(radius, 1), 0xFF0B1119);
			int filled = Math.round(trackWidth * this.sliderValue);
			if(filled > 0) {
				HudRenderer.drawRoundedRect(trackLeft, trackY, filled, 2,
						Math.min(radius, 1), accent);
			}
			int knobX = trackLeft + filled;
			HudRenderer.drawRoundedRect(knobX - 2, trackY - 2, 5, 6, 2, 0xFFF1F7F5);
		}
	}

	/**+
	 * Returns true if the mouse has been pressed on this control.
	 * Equivalent of MouseListener.mousePressed(MouseEvent e).
	 */
	public boolean mousePressed(Minecraft minecraft, int i, int j) {
		if (super.mousePressed(minecraft, i, j)) {
			this.sliderValue = (float) (i - (this.xPosition + 4)) / (float) (this.width - 8);
			this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
			minecraft.gameSettings.setOptionFloatValue(this.options, this.options.denormalizeValue(this.sliderValue));
			this.displayString = minecraft.gameSettings.getKeyBinding(this.options);
			this.dragging = true;
			return true;
		} else {
			return false;
		}
	}

	/**+
	 * Fired when the mouse button is released. Equivalent of
	 * MouseListener.mouseReleased(MouseEvent e).
	 */
	public void mouseReleased(int var1, int var2) {
		this.dragging = false;
	}

	public boolean isSliderTouchEvents() {
		return true;
	}
}