package net.minecraft.client.gui;

import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.pvp.PvpClient;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

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
public class GuiSlider extends GuiButton {
	private float sliderPosition = 1.0F;
	public boolean isMouseDown;
	private String name;
	private final float min;
	private final float max;
	private final GuiPageButtonList.GuiResponder responder;
	private GuiSlider.FormatHelper formatHelper;

	public GuiSlider(GuiPageButtonList.GuiResponder guiResponder, int idIn, int x, int y, String name, float min,
			float max, float defaultValue, GuiSlider.FormatHelper formatter) {
		super(idIn, x, y, 150, 20, "");
		this.name = name;
		this.min = min;
		this.max = max;
		this.sliderPosition = (defaultValue - min) / (max - min);
		this.formatHelper = formatter;
		this.responder = guiResponder;
		this.displayString = this.getDisplayString();
	}

	public float func_175220_c() {
		return this.min + (this.max - this.min) * this.sliderPosition;
	}

	public void func_175218_a(float parFloat1, boolean parFlag) {
		this.sliderPosition = (parFloat1 - this.min) / (this.max - this.min);
		this.displayString = this.getDisplayString();
		if (parFlag) {
			this.responder.onTick(this.id, this.func_175220_c());
		}

	}

	public float func_175217_d() {
		return this.sliderPosition;
	}

	private String getDisplayString() {
		return this.formatHelper == null ? I18n.format(this.name, new Object[0]) + ": " + this.func_175220_c()
				: this.formatHelper.getText(this.id, I18n.format(this.name, new Object[0]), this.func_175220_c());
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
	protected void mouseDragged(Minecraft var1, int i, int var3) {
		if (this.visible) {
			if (this.isMouseDown) {
				this.sliderPosition = (float) (i - (this.xPosition + 4)) / (float) (this.width - 8);
				if (this.sliderPosition < 0.0F) {
					this.sliderPosition = 0.0F;
				}

				if (this.sliderPosition > 1.0F) {
					this.sliderPosition = 1.0F;
				}

				this.displayString = this.getDisplayString();
				this.responder.onTick(this.id, this.func_175220_c());
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
			int filled = Math.round(trackWidth * this.sliderPosition);
			if(filled > 0) {
				HudRenderer.drawRoundedRect(trackLeft, trackY, filled, 2,
						Math.min(radius, 1), accent);
			}
			int knobX = trackLeft + filled;
			HudRenderer.drawRoundedRect(knobX - 2, trackY - 2, 5, 6, 2, 0xFFF1F7F5);
		}
	}

	public void func_175219_a(float parFloat1) {
		this.sliderPosition = parFloat1;
		this.displayString = this.getDisplayString();
		this.responder.onTick(this.id, this.func_175220_c());
	}

	/**+
	 * Returns true if the mouse has been pressed on this control.
	 * Equivalent of MouseListener.mousePressed(MouseEvent e).
	 */
	public boolean mousePressed(Minecraft minecraft, int i, int j) {
		if (super.mousePressed(minecraft, i, j)) {
			this.sliderPosition = (float) (i - (this.xPosition + 4)) / (float) (this.width - 8);
			if (this.sliderPosition < 0.0F) {
				this.sliderPosition = 0.0F;
			}

			if (this.sliderPosition > 1.0F) {
				this.sliderPosition = 1.0F;
			}

			this.displayString = this.getDisplayString();
			this.responder.onTick(this.id, this.func_175220_c());
			this.isMouseDown = true;
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
		this.isMouseDown = false;
	}

	public interface FormatHelper {
		String getText(int var1, String var2, float var3);
	}

	public boolean isSliderTouchEvents() {
		return true;
	}
}