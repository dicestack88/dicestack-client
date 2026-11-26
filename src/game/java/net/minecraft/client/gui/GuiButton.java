package net.minecraft.client.gui;

import static net.lax1dude.eaglercraft.v1_8.opengl.RealOpenGLEnums.*;

import net.lax1dude.eaglercraft.v1_8.Mouse;
import net.lax1dude.eaglercraft.v1_8.internal.EnumCursorType;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.pvp.PvpClient;
import net.lax1dude.eaglercraft.v1_8.pvp.PvpUiFontRenderer;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;

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
public class GuiButton extends Gui {
	protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");
	public int width;
	public int height;
	public int xPosition;
	public int yPosition;
	public String displayString;
	public int id;
	public boolean enabled;
	public boolean visible;
	protected boolean hovered;
	public float fontScale = 1.0f;

	public GuiButton(int buttonId, int x, int y, String buttonText) {
		this(buttonId, x, y, 200, 20, buttonText);
	}

	public GuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
		this.width = 200;
		this.height = 20;
		this.enabled = true;
		this.visible = true;
		this.id = buttonId;
		this.xPosition = x;
		this.yPosition = y;
		this.width = widthIn;
		this.height = heightIn;
		this.displayString = buttonText;
	}

	/**+
	 * Returns 0 if the button is disabled, 1 if the mouse is NOT
	 * hovering over this button and 2 if it IS hovering over this
	 * button.
	 */
	protected int getHoverState(boolean mouseOver) {
		byte b0 = 1;
		if (!this.enabled) {
			b0 = 0;
		} else if (mouseOver) {
			b0 = 2;
		}

		return b0;
	}

	/**+
	 * Draws this button to the screen.
	 */
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width
					&& mouseY < this.yPosition + this.height;
			if (this.enabled && this.hovered) {
				Mouse.showCursor(EnumCursorType.HAND);
			}
			int radius = 4;
			int accent = 0xFF53D6A3;
			PvpClient pvp = PvpClient.getInstance();
			if(pvp != null) {
				radius = pvp.getConfigStore().getConfig().appearance.cornerRadius;
				accent = pvp.getConfigStore().getConfig().appearance.accentColor;
			}
			radius = Math.min(radius, Math.min(this.width, this.height) / 2);
			int fill = !this.enabled ? 0xD0161F29
					: this.hovered ? 0xF02A3A4C : 0xE51E2B3A;
			HudRenderer.drawRoundedRect(this.xPosition, this.yPosition, this.width, this.height, radius, fill);
			HudRenderer.drawRoundedBorder(this.xPosition, this.yPosition, this.width, this.height, radius, 1,
					!this.enabled ? 0x662E3A48
							: this.hovered ? accent : 0x88566A7D);
			this.mouseDragged(mc, mouseX, mouseY);
			int j = 0xFFE2EAF1;
			if (!this.enabled) {
				j = 0xFF7F8C9A;
			} else if (this.hovered) {
				j = 0xFFFFFFFF;
			}
			FontRenderer fontrenderer = PvpUiFontRenderer.get(mc);
			int availableWidth = Math.max(0, this.width - 10);
			String label = this.displayString == null ? "" : this.displayString;
			if(fontrenderer.getStringWidth(label) > availableWidth) {
				String suffix = availableWidth >= fontrenderer.getStringWidth("...") ? "..." : "";
				int textLimit = Math.max(0, availableWidth - fontrenderer.getStringWidth(suffix));
				label = fontrenderer.trimStringToWidth(label, textLimit) + suffix;
			}
			this.drawCenteredString(fontrenderer, label, this.xPosition + this.width / 2,
					this.yPosition + (this.height - 8) / 2, j);
		}
	}

	/**+
	 * Fired when the mouse button is dragged. Equivalent of
	 * MouseListener.mouseDragged(MouseEvent e).
	 */
	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
	}

	/**+
	 * Fired when the mouse button is released. Equivalent of
	 * MouseListener.mouseReleased(MouseEvent e).
	 */
	public void mouseReleased(int mouseX, int mouseY) {
	}

	/**+
	 * Returns true if the mouse has been pressed on this control.
	 * Equivalent of MouseListener.mousePressed(MouseEvent e).
	 */
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition
				&& mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
	}

	/**+
	 * Whether the mouse cursor is currently over the button.
	 */
	public boolean isMouseOver() {
		return this.hovered;
	}

	public void drawButtonForegroundLayer(int mouseX, int mouseY) {
	}

	public void playPressSound(SoundHandler soundHandlerIn) {
		soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
	}

	public int getButtonWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isSliderTouchEvents() {
		return false;
	}

}