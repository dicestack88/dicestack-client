package net.lax1dude.eaglercraft.v1_8.pvp.hud.modules;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.PvpContext;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;
import net.lax1dude.eaglercraft.v1_8.pvp.PvpClient;
import net.lax1dude.eaglercraft.v1_8.pvp.PvpUiFontRenderer;

public final class TextHud implements HudElement {

	public enum Type {
		COMPASS,
		SPEED,
		BIOME,
		WORLD_CLOCK,
		SESSION_TIME,
		HEALTH,
		HUNGER,
		ARMOR,
		HELD_ITEM,
		HELD_DURABILITY,
		ARMOR_DURABILITY,
		SERVER_ADDRESS,
		GAME_MODE,
		POTION_EFFECTS,
		SATURATION,
		ARROW_COUNT,
		SPRINT_STATUS,
		SNEAK_STATUS,
		TARGET,
		BLOCK_INFO,
		DAY_COUNTER,
		LIGHT_LEVEL,
		LOW_HEALTH,
		FOOD_INFO,
		ITEM_COUNT,
		INVENTORY_PREVIEW
	}

	private final Minecraft minecraft;
	private final String id;
	private final Type type;
	private long cachedValue = Long.MIN_VALUE;
	private String cachedDetail;
	private ItemStack cachedStack;
	private WorldClient cachedBiomeWorld;
	private int cachedBiomeX = Integer.MIN_VALUE;
	private int cachedBiomeY = Integer.MIN_VALUE;
	private int cachedBiomeZ = Integer.MIN_VALUE;
	private String cachedText = "";
	private int cachedWidth;
	private boolean cachedClock24Hour;
	private boolean cachedClockSeconds;
	private long cachedPotionSecond = Long.MIN_VALUE;
	private WorldClient cachedPotionWorld;
	private final String[] cachedPotionRows = new String[3];
	private int[] cachedPotionIds = new int[3];
	private int cachedPotionCount;
	private int cachedLowHealthThreshold = Integer.MIN_VALUE;
	private Entity cachedTarget;
	private String cachedTargetName;
	private int cachedTargetHealth = Integer.MIN_VALUE;
	private String cachedTargetHealthText = "";
	private String cachedTargetDisplaySource;
	private String cachedTargetDisplayName;
	private int cachedTargetDisplayWidth = -1;
	private String cachedArmorValue = "0";
	private WorldClient cachedBlockWorld;
	private int cachedBlockX = Integer.MIN_VALUE;
	private int cachedBlockY = Integer.MIN_VALUE;
	private int cachedBlockZ = Integer.MIN_VALUE;
	private String cachedBlockName;
	private WorldClient cachedLightWorld;
	private int cachedLightX = Integer.MIN_VALUE;
	private int cachedLightY = Integer.MIN_VALUE;
	private int cachedLightZ = Integer.MIN_VALUE;
	private BlockPos cachedLightPosition;
	private ItemStack cachedCountStack;
	private int cachedItemCount = Integer.MIN_VALUE;
	private int cachedFood = Integer.MIN_VALUE;
	private int cachedFoodSaturation = Integer.MIN_VALUE;
	private int cachedFoodExhaustion = Integer.MIN_VALUE;
	private int cachedFoodEstimate = Integer.MIN_VALUE;
	private int cachedFoodSaturationEstimate = Integer.MIN_VALUE;
	private String cachedFoodEstimateText = "";
	private String cachedFoodValueText = "0/20";
	private String cachedFoodSaturationText = "0.0";
	private String cachedFoodExhaustionText = "0.0";
	private ItemStack cachedFoodStack;
	private final ItemStack[] cachedInventoryStacks = new ItemStack[6];
	private final int[] cachedInventoryCounts = new int[6];
	private final String[] cachedInventoryCountLabels = new String[6];
	private int cachedInventoryCount;
	private boolean inventoryPreviewInitialized;
	private float animatedProgress;
	private boolean progressInitialized;

	public TextHud(Minecraft minecraft, String id, Type type) {
		this.minecraft = minecraft;
		this.id = id;
		this.type = type;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getWidth(PvpContext context) {
		refresh(context);
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		int width = cachedWidth;
		switch(type) {
		case COMPASS: width = Math.max(width, 92); break;
		case SPEED: width = Math.max(width, 72); break;
		case HEALTH:
		case HUNGER:
		case LOW_HEALTH:
		case LIGHT_LEVEL:
			width = Math.max(width, 88);
			break;
		case SATURATION:
			width = Math.max(width, 104);
			break;
		case ARMOR_DURABILITY:
			width = Math.max(width, 108);
			break;
		case HELD_DURABILITY:
			width = Math.max(width, 108) + 20;
			break;
		case FOOD_INFO:
			width = 112;
			break;
		case ARMOR: width = Math.max(width, 80); break;
		case HELD_ITEM:
		case ITEM_COUNT: width = Math.max(width, 92) + 20; break;
		case ARROW_COUNT: width = Math.max(width, 72) + 20; break;
		case POTION_EFFECTS: width = Math.max(width, 112); break;
		case TARGET: width = Math.max(width, 100); break;
		case INVENTORY_PREVIEW: width = 114; break;
		default: width = Math.max(width, 72); break;
		}
		return width + style.padding * 2 - 4;
	}

	@Override
	public int getHeight(PvpContext context) {
		refresh(context);
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		int height = 16;
		switch(type) {
		case COMPASS:
		case SPEED:
			height = 22;
			break;
		case HEALTH:
		case HUNGER:
		case SATURATION:
		case ARMOR_DURABILITY:
		case HELD_DURABILITY:
		case TARGET:
		case ARROW_COUNT:
		case LIGHT_LEVEL:
			height = 24;
			break;
		case FOOD_INFO:
			height = 38;
			break;
		case HELD_ITEM:
		case ITEM_COUNT:
			height = 24;
			break;
		case POTION_EFFECTS:
			height = 12 + Math.max(1, cachedPotionCount) * 18;
			break;
		case INVENTORY_PREVIEW:
			height = 24;
			break;
		case LOW_HEALTH:
			height = 18;
			break;
		case ARMOR:
			height = 30;
			break;
		default:
			height = 18;
			break;
		}
		return height + style.padding * 2;
	}

	@Override
	public void render(PvpContext context, int x, int y) {
		refresh(context);
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		int width = getWidth(context);
		int height = getHeight(context);
		int left = x + style.padding + 4;
		int top = y + style.padding;
		HudRenderer.drawPanel(context, getId(), x, y, width, height);
		HudRenderer.drawAccent(x, y + style.padding, height - style.padding * 2, style);
		if(minecraft.thePlayer == null && type != Type.SESSION_TIME) {
			HudRenderer.drawText(context, getId(), minecraft, cachedText, left, top + 4,
					Math.max(1, width - style.padding * 2 - 4));
			return;
		}

		switch(type) {
		case COMPASS:
			renderCompass(context, style, left, top, width);
			break;
		case SPEED:
			renderSpeed(context, style, left, top, width, height);
			break;
		case HEALTH:
			renderHealth(context, style, left, top, width);
			break;
		case HUNGER:
			renderHunger(context, style, left, top, width);
			break;
		case ARMOR:
			renderArmor(context, style, left, top);
			break;
		case ARMOR_DURABILITY:
		case HELD_DURABILITY:
			renderDurability(context, style, left, top, width);
			break;
		case HELD_ITEM:
		case ITEM_COUNT:
			renderHeldItem(context, style, left, top, width);
			break;
		case POTION_EFFECTS:
			renderPotions(context, style, x + style.padding, top);
			break;
		case SATURATION:
			renderSaturation(context, style, left, top, width);
			break;
		case SPRINT_STATUS:
		case SNEAK_STATUS:
			renderStatus(context, style, left, top, width);
			break;
		case TARGET:
			renderTarget(context, style, left, top, width);
			break;
		case LOW_HEALTH:
			renderLowHealth(context, style, left, top, width);
			break;
		case FOOD_INFO:
			renderFoodInfo(context, style, left, top, width);
			break;
		case INVENTORY_PREVIEW:
			renderInventoryPreview(context, style, x + style.padding, top);
			break;
		case ARROW_COUNT:
			renderArrowCount(context, style, left, top, width);
			break;
		case LIGHT_LEVEL:
			renderLightLevel(context, style, left, top, width);
			break;
		default:
			renderCompact(context, style, left, top, width);
			break;
		}
	}

	private void renderCompass(PvpContext context, HudAppearanceConfig style, int left, int top, int width) {
		HudRenderer.drawLabel(context, getId(), minecraft, "COMPASS", left, top);
		HudRenderer.drawText(context, getId(), minecraft, cachedText, left, top + 9, width - style.padding * 2);
		int lineY = top + 20;
		int center = left + Math.max(12, (width - style.padding * 2) / 2);
		net.minecraft.client.gui.Gui.drawRect(left, lineY, left + width - style.padding * 2, lineY + 1, 0x997D91A5);
		for(int tick = 0; tick < 7; ++tick) {
			int tickX = left + tick * Math.max(1, (width - style.padding * 2) / 6);
			net.minecraft.client.gui.Gui.drawRect(tickX, lineY - (tick == 3 ? 3 : 1), tickX + 1, lineY + 2,
					tick == 3 ? style.accentColor : 0xAA7D91A5);
		}
		if(minecraft.thePlayer != null) {
			float yaw = (minecraft.thePlayer.rotationYaw % 360.0f + 360.0f) % 360.0f;
			int markerX = center + Math.round((float) Math.sin(Math.toRadians(yaw)) * 14.0f);
			net.minecraft.client.gui.Gui.drawRect(markerX - 2, lineY - 4, markerX + 3, lineY + 1,
					style.accentColor);
		}
	}

	private void renderSpeed(PvpContext context, HudAppearanceConfig style, int left, int top, int width, int height) {
		HudRenderer.drawLabel(context, getId(), minecraft, "SPEED", left, top);
		HudRenderer.drawText(context, getId(), minecraft, cachedText, left, top + 9, width - style.padding * 2);
		float target = context.runtime == null ? 0.0f : (float) Math.min(1.0d, context.runtime.getSpeed() / 15.0d);
		drawAnimatedBar(left, top + height - style.padding * 2 - 2,
				width - style.padding * 2 - 4, target, style.accentColor, style);
	}

	private void renderHealth(PvpContext context, HudAppearanceConfig style, int left, int top, int width) {
		HudRenderer.drawLabel(context, getId(), minecraft, "HEALTH", left, top);
		HudRenderer.drawText(context, getId(), minecraft, cachedText, left, top + 9, width - style.padding * 2);
		if(minecraft.thePlayer != null) {
			float maximum = Math.max(1.0f, minecraft.thePlayer.getMaxHealth());
			float health = Math.max(0.0f, Math.min(maximum, minecraft.thePlayer.getHealth()));
			drawAnimatedBar(left, top + 20, width - style.padding * 2 - 4, health / maximum,
					health <= context.config.lowHealthThreshold ? 0xFFFF7777 : style.accentColor, style);
		}
	}

	private void renderHunger(PvpContext context, HudAppearanceConfig style, int left, int top, int width) {
		HudRenderer.drawLabel(context, getId(), minecraft, "HUNGER", left, top);
		HudRenderer.drawText(context, getId(), minecraft, cachedText, left, top + 9, width - style.padding * 2);
		if(minecraft.thePlayer == null) {
			return;
		}
		int food = minecraft.thePlayer.getFoodStats().getFoodLevel();
		int x = left;
		int y = top + 20;
		for(int i = 0; i < 10; ++i) {
			int value = Math.max(0, Math.min(2, food - i * 2));
			net.minecraft.client.gui.Gui.drawRect(x + i * 7, y, x + i * 7 + 5, y + 3, 0x990A1018);
			if(value > 0) {
				net.minecraft.client.gui.Gui.drawRect(x + i * 7, y, x + i * 7 + (value == 2 ? 5 : 3),
						y + 3, value == 2 ? 0xFF53D6A3 : 0xFFB7E8D3);
			}
		}
	}

	private void renderArmor(PvpContext context, HudAppearanceConfig style, int left, int top) {
		HudRenderer.drawLabel(context, getId(), minecraft, "ARMOR", left, top);
		HudRenderer.drawText(context, getId(), minecraft, cachedArmorValue, left + 34, top, 24);
		if(minecraft.thePlayer == null) {
			return;
		}
		ItemStack[] armor = minecraft.thePlayer.inventory.armorInventory;
		for(int i = 0; i < armor.length; ++i) {
			ItemStack stack = armor[i];
			if(stack == null) {
				continue;
			}
			int itemX = left + i * 18;
			HudRenderer.drawItem(minecraft, stack, itemX, top + 10);
			if(stack.isItemStackDamageable()) {
				float durability = (stack.getMaxDamage() - stack.getItemDamage())
						/ (float) stack.getMaxDamage();
				int color = durability > 0.5f ? 0xFF53D6A3 : durability > 0.2f ? 0xFFFFD166 : 0xFFFF7777;
				HudRenderer.drawProgressBar(itemX + 1, top + 25, 14, 2, durability, color);
			}
		}
	}

	private void renderDurability(PvpContext context, HudAppearanceConfig style, int left, int top, int width) {
		HudRenderer.drawLabel(context, getId(), minecraft, type == Type.ARMOR_DURABILITY
				? "ARMOR CONDITION" : "ITEM CONDITION", left, top);
		if(type == Type.HELD_DURABILITY && cachedStack != null) {
			HudRenderer.drawItem(minecraft, cachedStack, left, top + 7);
			left += 19;
			width -= 19;
		}
		HudRenderer.drawText(context, getId(), minecraft, cachedText, left, top + 9,
				Math.max(1, width - style.padding * 2));
		if(cachedValue >= 0L) {
			float percent = Math.max(0.0f, Math.min(1.0f, cachedValue / 100.0f));
			int color = percent > 0.5f ? 0xFF53D6A3 : percent > 0.2f ? 0xFFFFD166 : 0xFFFF7777;
			drawAnimatedBar(left, top + 21, Math.max(1, width - style.padding * 2 - 4), percent, color, style);
		}
	}

	private void renderHeldItem(PvpContext context, HudAppearanceConfig style, int left, int top, int width) {
		HudRenderer.drawLabel(context, getId(), minecraft, type == Type.HELD_ITEM ? "HELD ITEM" : "ITEM COUNT",
				left, top);
		if(type == Type.HELD_ITEM && cachedStack != null) {
			HudRenderer.drawItem(minecraft, cachedStack, left, top + 7);
		}else if(type == Type.ITEM_COUNT && cachedCountStack != null) {
			HudRenderer.drawItem(minecraft, cachedCountStack, left, top + 7);
		}
		HudRenderer.drawText(context, getId(), minecraft, cachedText, left + 19, top + 9,
				Math.max(1, width - style.padding * 2 - 19));
	}

	private void renderPotions(PvpContext context, HudAppearanceConfig style, int left, int top) {
		HudRenderer.drawLabel(context, getId(), minecraft, "EFFECTS", left + 1, top);
		int rowY = top + 10;
		for(int i = 0; i < cachedPotionCount; ++i) {
			int potionId = cachedPotionIds[i];
			if(potionId < 0 || potionId >= Potion.potionTypes.length) {
				continue;
			}
			Potion potion = Potion.potionTypes[potionId];
			HudRenderer.drawPotionIcon(minecraft, potion, left, rowY + i * 18);
			HudRenderer.drawText(context, getId(), minecraft, cachedPotionRows[i], left + 19,
					rowY + i * 18 + 5, getWidth(context) - style.padding * 2 - 22);
		}
		if(cachedPotionCount == 0) {
			HudRenderer.drawText(context, getId(), minecraft, "No active effects", left + 1, rowY + 4,
					getWidth(context) - style.padding * 2 - 4);
		}
	}

	private void renderSaturation(PvpContext context, HudAppearanceConfig style, int left, int top, int width) {
		HudRenderer.drawLabel(context, getId(), minecraft, "SATURATION", left, top);
		HudRenderer.drawText(context, getId(), minecraft, cachedText, left, top + 9, width - style.padding * 2);
		float saturation = Math.max(0.0f, Math.min(20.0f, cachedValue / 10.0f));
		for(int i = 0; i < 10; ++i) {
			float amount = Math.max(0.0f, Math.min(2.0f, saturation - i * 2.0f)) / 2.0f;
			int tickX = left + i * 7;
			net.minecraft.client.gui.Gui.drawRect(tickX, top + 20, tickX + 5, top + 23, 0x990A1018);
			if(amount > 0.0f) {
				net.minecraft.client.gui.Gui.drawRect(tickX, top + 20, tickX + Math.max(1, Math.round(5 * amount)),
						top + 23, 0xFFEFCB62);
			}
		}
	}

	private void renderStatus(PvpContext context, HudAppearanceConfig style, int left, int top, int width) {
		boolean active = cachedValue != 0L;
		HudRenderer.drawLabel(context, getId(), minecraft, type == Type.SPRINT_STATUS ? "SPRINT" : "SNEAK",
				left, top);
		int color = active ? style.performanceGoodColor : 0xFF8090A0;
		HudRenderer.drawStatusIndicator(left, top + 10, color, active);
		HudRenderer.drawTextColor(context, getId(), minecraft, active ? "ACTIVE" : "INACTIVE",
				left + 9, top + 9, width - style.padding * 2 - 9, color);
	}

	private void renderTarget(PvpContext context, HudAppearanceConfig style, int left, int top, int width) {
		HudRenderer.drawLabel(context, getId(), minecraft, "TARGET", left, top);
		if(cachedTarget == null) {
			HudRenderer.drawText(context, getId(), minecraft, "No target", left, top + 9,
					width - style.padding * 2);
			return;
		}
		String name = cachedTargetName == null ? cachedTarget.getName() : cachedTargetName;
		int contentWidth = width - style.padding * 2;
		net.minecraft.client.gui.FontRenderer font = PvpUiFontRenderer.get(minecraft);
		int healthWidth = font.getStringWidth(cachedTargetHealthText);
		int nameWidth = cachedTargetHealthText.isEmpty() ? contentWidth : contentWidth - healthWidth - 4;
		nameWidth = Math.max(1, nameWidth);
		if(cachedTargetDisplayName == null || cachedTargetDisplayWidth != nameWidth
				|| !name.equals(cachedTargetDisplaySource)) {
			cachedTargetDisplaySource = name;
			cachedTargetDisplayWidth = nameWidth;
			cachedTargetDisplayName = font.trimStringToWidth(name, nameWidth);
		}
		HudRenderer.drawText(context, getId(), minecraft, cachedTargetDisplayName, left, top + 9, nameWidth);
		if(!cachedTargetHealthText.isEmpty()) {
			HudRenderer.drawTextColor(context, getId(), minecraft, cachedTargetHealthText,
					left + contentWidth - healthWidth, top + 9, healthWidth, style.textColor);
		}
		if(cachedTarget instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase) cachedTarget;
			float maximum = Math.max(1.0f, living.getMaxHealth());
			float health = Math.max(0.0f, Math.min(maximum, living.getHealth()));
			int color = health / maximum > 0.5f ? 0xFF53D6A3
					: health / maximum > 0.2f ? 0xFFFFD166 : 0xFFFF7777;
			drawAnimatedBar(left, top + 21, width - style.padding * 2 - 4, health / maximum, color, style);
		}
	}

	private void renderLowHealth(PvpContext context, HudAppearanceConfig style, int left, int top, int width) {
		boolean low = cachedValue <= cachedLowHealthThreshold;
		HudRenderer.drawStatusIndicator(left, top + 5, low ? 0xFFFF5555 : style.performanceGoodColor, low);
		HudRenderer.drawTextColor(context, getId(), minecraft, cachedText, left + 10, top + 4,
				width - style.padding * 2 - 10, low ? 0xFFFF7777 : style.textColor);
	}

	private void renderFoodInfo(PvpContext context, HudAppearanceConfig style, int left, int top, int width) {
		HudRenderer.drawLabel(context, getId(), minecraft, "FOOD / SAT", left, top);
		int barWidth = width - style.padding * 2 - 40;
		HudRenderer.drawProgressBar(left, top + 11, barWidth, 3, cachedFood / 20.0f, 0xFF53D6A3);
		if(cachedFoodEstimate > 0) {
			int currentWidth = Math.round(barWidth * cachedFood / 20.0f);
			int predictedWidth = Math.round(barWidth * (cachedFood + cachedFoodEstimate) / 20.0f);
			net.minecraft.client.gui.Gui.drawRect(left + currentWidth, top + 11,
					left + predictedWidth, top + 14, 0xB853D6A3);
		}
		HudRenderer.drawText(context, getId(), minecraft, cachedFoodValueText,
				left + barWidth + 4, top + 8, 32);
		HudRenderer.drawProgressBar(left, top + 21, barWidth, 3, cachedFoodSaturation / 200.0f, 0xFFEFCB62);
		if(cachedFoodSaturationEstimate > cachedFoodSaturation) {
			int currentWidth = Math.round(barWidth * cachedFoodSaturation / 200.0f);
			int predictedWidth = Math.round(barWidth * cachedFoodSaturationEstimate / 200.0f);
			net.minecraft.client.gui.Gui.drawRect(left + currentWidth, top + 21,
					left + predictedWidth, top + 24, 0xB3EFCB62);
		}
		HudRenderer.drawText(context, getId(), minecraft, cachedFoodSaturationText,
				left + barWidth + 4, top + 18, 32);
		HudRenderer.drawProgressBar(left, top + 31, barWidth, 2, cachedFoodExhaustion / 40.0f, 0xFFFF7777);
		HudRenderer.drawText(context, getId(), minecraft, cachedFoodExhaustionText,
				left + barWidth + 4, top + 28, 32);
		if(cachedFoodEstimate > 0) {
			int textWidth = PvpUiFontRenderer.get(minecraft).getStringWidth(cachedFoodEstimateText);
			HudRenderer.drawTextColor(context, getId(), minecraft, cachedFoodEstimateText,
					left + width - style.padding * 2 - textWidth, top, textWidth, 0xFF53D6A3);
		}
	}

	private void renderInventoryPreview(PvpContext context, HudAppearanceConfig style, int left, int top) {
		for(int i = 0; i < cachedInventoryCount; ++i) {
			int itemX = left + i * 18;
			HudRenderer.drawItem(minecraft, cachedInventoryStacks[i], itemX, top + 4);
			String count = cachedInventoryCountLabels[i];
			if(count != null) {
				net.minecraft.client.gui.FontRenderer font = PvpUiFontRenderer.get(minecraft);
				int countX = itemX + 16 - font.getStringWidth(count);
				font.drawStringWithShadow(count, countX, top + 14, 0xFFFFFFFF);
			}
		}
		if(cachedInventoryCount == 0) {
			HudRenderer.drawText(context, getId(), minecraft, "Empty", left, top + 6, getWidth(context));
		}
	}

	private void renderArrowCount(PvpContext context, HudAppearanceConfig style, int left, int top, int width) {
		HudRenderer.drawLabel(context, getId(), minecraft, "ARROWS", left, top);
		ItemStack arrowStack = null;
		if(minecraft.thePlayer != null) {
			for(int i = 0; i < minecraft.thePlayer.inventory.mainInventory.length; ++i) {
				ItemStack stack = minecraft.thePlayer.inventory.mainInventory[i];
				if(stack != null && stack.getItem() == Items.arrow) {
					arrowStack = stack;
					break;
				}
			}
		}
		if(arrowStack != null) {
			HudRenderer.drawItem(minecraft, arrowStack, left, top + 6);
		}else {
			HudRenderer.drawStatusIndicator(left + 4, top + 11, style.accentColor, cachedValue > 0L);
		}
		HudRenderer.drawText(context, getId(), minecraft, cachedText, left + 20, top + 9,
				width - style.padding * 2 - 20);
	}

	private void renderLightLevel(PvpContext context, HudAppearanceConfig style, int left, int top, int width) {
		HudRenderer.drawLabel(context, getId(), minecraft, "LIGHT LEVEL", left, top);
		HudRenderer.drawText(context, getId(), minecraft, cachedText, left, top + 9, width - style.padding * 2);
		drawAnimatedBar(left, top + 20, width - style.padding * 2 - 4,
				Math.max(0.0f, Math.min(15.0f, cachedValue)) / 15.0f,
				cachedValue <= 7L ? 0xFFFFD166 : style.accentColor, style);
	}

	private void renderCompact(PvpContext context, HudAppearanceConfig style, int left, int top, int width) {
		HudRenderer.drawLabel(context, getId(), minecraft, getWidgetLabel(), left, top);
		HudRenderer.drawText(context, getId(), minecraft, cachedText, left, top + 9,
				Math.max(1, width - style.padding * 2));
	}

	private String getWidgetLabel() {
		switch(type) {
		case BIOME: return "BIOME";
		case WORLD_CLOCK: return "WORLD TIME";
		case SESSION_TIME: return "PLAYTIME";
		case SERVER_ADDRESS: return "SERVER";
		case GAME_MODE: return "GAME MODE";
		case BLOCK_INFO: return "BLOCK";
		case DAY_COUNTER: return "WORLD DAY";
		case LIGHT_LEVEL: return "LIGHT";
		default: return "STATUS";
		}
	}

	private void drawAnimatedBar(int x, int y, int width, float target, int color, HudAppearanceConfig style) {
		if(!progressInitialized || !style.animationsEnabled) {
			animatedProgress = target;
			progressInitialized = true;
		}else {
			animatedProgress += (target - animatedProgress) * 0.22f;
		}
		HudRenderer.drawProgressBar(x, y, width, 3, animatedProgress, color);
	}

	private void refresh(PvpContext context) {
		if(minecraft.thePlayer == null && type != Type.SESSION_TIME) {
			cachedValue = Long.MIN_VALUE;
			cachedDetail = null;
			cachedStack = null;
			cachedTarget = null;
			cachedCountStack = null;
			cachedItemCount = Integer.MIN_VALUE;
			cachedFood = Integer.MIN_VALUE;
			cachedFoodSaturation = Integer.MIN_VALUE;
			cachedFoodExhaustion = Integer.MIN_VALUE;
			cachedFoodEstimate = Integer.MIN_VALUE;
			cachedFoodSaturationEstimate = Integer.MIN_VALUE;
			cachedFoodStack = null;
			cachedBiomeWorld = null;
			cachedBlockWorld = null;
			cachedBlockName = null;
			cachedLightWorld = null;
			cachedPotionWorld = null;
			cachedPotionSecond = Long.MIN_VALUE;
			cachedPotionCount = 0;
			cachedInventoryCount = 0;
			inventoryPreviewInitialized = false;
			for(int i = 0; i < cachedInventoryStacks.length; ++i) {
				cachedInventoryStacks[i] = null;
				cachedInventoryCounts[i] = 0;
				cachedInventoryCountLabels[i] = null;
			}
			setText("Unavailable");
			return;
		}

		switch(type) {
		case COMPASS:
			int yaw = ((int) Math.floor((minecraft.thePlayer.rotationYaw % 360.0f + 360.0f) % 360.0f));
			if(yaw != cachedValue) {
				cachedValue = yaw;
				String direction = yaw < 45 || yaw >= 315 ? "S" : yaw < 135 ? "W" : yaw < 225 ? "N" : "E";
				setText("Direction: " + direction + " (" + yaw + ")");
			}
			break;
		case SPEED:
			long speed = context.runtime == null ? 0L : Math.round(context.runtime.getSpeed() * 10.0d);
			if(speed != cachedValue) {
				cachedValue = speed;
				setText("Speed: " + (speed / 10L) + "." + Math.abs(speed % 10L) + " b/s");
			}
			break;
		case BIOME:
			if(minecraft.theWorld == null) {
				cachedDetail = null;
				setText("Biome: Unavailable");
				break;
			}
			int biomeX = (int) Math.floor(minecraft.thePlayer.posX);
			int biomeY = (int) Math.floor(minecraft.thePlayer.posY);
			int biomeZ = (int) Math.floor(minecraft.thePlayer.posZ);
			if(cachedBiomeWorld != minecraft.theWorld || biomeX != cachedBiomeX || biomeY != cachedBiomeY
					|| biomeZ != cachedBiomeZ) {
				cachedBiomeWorld = minecraft.theWorld;
				cachedBiomeX = biomeX;
				cachedBiomeY = biomeY;
				cachedBiomeZ = biomeZ;
				BiomeGenBase biome = minecraft.theWorld.getBiomeGenForCoords(new BlockPos(biomeX, biomeY, biomeZ));
				cachedDetail = biome == null ? "Unknown" : biome.biomeName;
				setText("Biome: " + cachedDetail);
			}
			break;
		case WORLD_CLOCK:
			if(minecraft.theWorld == null) {
				cachedValue = Long.MIN_VALUE;
				setText("World Time: Unavailable");
				break;
			}
			long worldTime = minecraft.theWorld.getWorldTime();
			boolean showSeconds = context.config.clockShowSeconds;
			long tick = showSeconds ? worldTime : worldTime / 20L;
			if(tick != cachedValue || context.config.clock24Hour != cachedClock24Hour
					|| context.config.clockShowSeconds != cachedClockSeconds) {
				cachedValue = tick;
				cachedClock24Hour = context.config.clock24Hour;
				cachedClockSeconds = context.config.clockShowSeconds;
				long daytime = (worldTime + 6000L) % 24000L;
				if(daytime < 0L) {
					daytime += 24000L;
				}
				int hours24 = (int) (daytime / 1000L);
				int minutes = (int) (daytime % 1000L * 60L / 1000L);
				int seconds = (int) (daytime % 1000L * 3600L / 1000L % 60L);
				int displayHours = context.config.clock24Hour ? hours24 : hours24 % 12 == 0 ? 12 : hours24 % 12;
				String clock = twoDigits(displayHours) + ":" + twoDigits(minutes);
				if(showSeconds) {
					clock += ":" + twoDigits(seconds);
				}
				if(!context.config.clock24Hour) {
					clock += hours24 < 12 ? " AM" : " PM";
				}
				setText("World Time: " + clock);
			}
			break;
		case SESSION_TIME:
			long sessionSeconds = context.runtime == null ? 0L : context.runtime.getSessionSeconds();
			if(sessionSeconds != cachedValue) {
				cachedValue = sessionSeconds;
				setText("Session: " + formatDuration(sessionSeconds));
			}
			break;
		case HEALTH:
			int health = Math.round(minecraft.thePlayer.getHealth() * 10.0f);
			if(health != cachedValue) {
				cachedValue = health;
				setText("Health: " + (health / 10) + "." + Math.abs(health % 10));
			}
			break;
		case HUNGER:
			int hunger = minecraft.thePlayer.getFoodStats().getFoodLevel();
			if(hunger != cachedValue) {
				cachedValue = hunger;
				setText("Hunger: " + hunger);
			}
			break;
		case ARMOR:
			int armor = minecraft.thePlayer.getTotalArmorValue();
			if(armor != cachedValue) {
				cachedValue = armor;
				cachedArmorValue = Integer.toString(armor);
				setText("Armor: " + armor);
			}
			break;
		case HELD_ITEM:
			ItemStack held = minecraft.thePlayer.getHeldItem();
			int count = held == null ? 0 : held.stackSize;
			if(held != cachedStack || count != cachedValue) {
				cachedStack = held;
				cachedValue = count;
				cachedDetail = held == null ? "None" : held.getDisplayName();
				setText(held == null ? "Held Item: None" : "Held Item: " + cachedDetail + " x" + count);
			}
			break;
		case HELD_DURABILITY:
			refreshDurability(minecraft.thePlayer.getHeldItem(), "Held Durability: ");
			break;
		case ARMOR_DURABILITY:
			ItemStack[] armorStacks = minecraft.thePlayer.inventory.armorInventory;
			int durability = 0;
			int maximum = 0;
			for(int i = 0; i < armorStacks.length; ++i) {
				ItemStack stack = armorStacks[i];
				if(stack != null && stack.isItemStackDamageable()) {
					maximum += stack.getMaxDamage();
					durability += stack.getMaxDamage() - stack.getItemDamage();
				}
			}
			int percentage = maximum == 0 ? -1 : durability * 100 / maximum;
			if(percentage != cachedValue) {
				cachedValue = percentage;
				setText(percentage < 0 ? "Armor Durability: N/A" : "Armor Durability: " + percentage + "%");
			}
			break;
		case SERVER_ADDRESS:
			if(context.config.hideServerAddress || context.config.streamerMode) {
				if(!"Private".equals(cachedDetail)) {
					cachedDetail = "Private";
					setText("Server: Private");
				}
				break;
			}
			String address;
			if(minecraft.getCurrentServerData() != null) {
				address = minecraft.getCurrentServerData().serverIP;
			}else if(minecraft.theWorld != null && !minecraft.theWorld.isRemote) {
				address = "Singleplayer";
			}else {
				address = "Unavailable";
			}
			if(address == null || address.length() == 0) {
				address = "Unavailable";
			}
			if(cachedDetail == null || !cachedDetail.equals(address)) {
				cachedDetail = address;
				setText("Server: " + address);
			}
			break;
		case GAME_MODE:
			String mode = minecraft.playerController == null ? "Unavailable"
					: minecraft.playerController.getCurrentGameType().getName();
			if(cachedDetail == null || !cachedDetail.equals(mode)) {
				cachedDetail = mode;
				setText("Game Mode: " + mode);
			}
			break;
		case POTION_EFFECTS:
			refreshPotionEffects(context);
			break;
		case SATURATION:
			float saturation = minecraft.thePlayer.getFoodStats().getSaturationLevel();
			int saturationTenths = Math.round(saturation * 10.0f);
			if(saturationTenths != cachedValue) {
				cachedValue = saturationTenths;
				setText("Saturation: " + saturationTenths / 10.0f);
			}
			break;
		case ARROW_COUNT:
			int arrows = 0;
			for(int i = 0; i < minecraft.thePlayer.inventory.mainInventory.length; ++i) {
				ItemStack item = minecraft.thePlayer.inventory.mainInventory[i];
				if(item != null && item.getItem() == Items.arrow) {
					arrows += item.stackSize;
				}
			}
			if(arrows != cachedValue) {
				cachedValue = arrows;
				setText("Arrows: " + arrows);
			}
			break;
		case SPRINT_STATUS:
			boolean sprinting = minecraft.thePlayer.isSprinting();
			if((sprinting ? 1 : 0) != cachedValue) {
				cachedValue = sprinting ? 1 : 0;
				setText("Sprint: " + (sprinting ? "On" : "Off"));
			}
			break;
		case SNEAK_STATUS:
			boolean sneaking = minecraft.thePlayer.isSneaking();
			if((sneaking ? 1 : 0) != cachedValue) {
				cachedValue = sneaking ? 1 : 0;
				setText("Sneak: " + (sneaking ? "On" : "Off"));
			}
			break;
		case TARGET:
			Entity target = !PvpClient.isFreelookActive() && minecraft.objectMouseOver != null
					&& minecraft.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
							? minecraft.objectMouseOver.entityHit : null;
			if(target != cachedTarget) {
				cachedTarget = target;
				cachedTargetName = null;
				cachedTargetHealth = Integer.MIN_VALUE;
				cachedTargetHealthText = "";
			}
			if(target == null) {
				if(!"Target: None".equals(cachedText)) {
					setText("Target: None");
				}
			}else {
				String targetName = PvpClient.getPrivateName(target.getName());
				int targetHealth = target instanceof EntityLivingBase
						? Math.round(((EntityLivingBase) target).getHealth() * 10.0f) : Integer.MIN_VALUE;
				if(!targetName.equals(cachedTargetName) || targetHealth != cachedTargetHealth) {
					cachedTargetName = targetName;
					cachedTargetHealth = targetHealth;
					cachedTargetHealthText = targetHealth == Integer.MIN_VALUE
							? "" : (targetHealth / 10.0f) + " HP";
					setText(targetHealth == Integer.MIN_VALUE ? "Target: " + targetName
							: "Target: " + targetName + " " + (targetHealth / 10.0f) + " HP");
				}
			}
			break;
		case BLOCK_INFO:
			if(!PvpClient.isFreelookActive() && minecraft.objectMouseOver != null
					&& minecraft.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
					&& minecraft.theWorld != null) {
				BlockPos pos = minecraft.objectMouseOver.getBlockPos();
				int blockX = pos.getX();
				int blockY = pos.getY();
				int blockZ = pos.getZ();
				String blockName = minecraft.theWorld.getBlockState(pos).getBlock().getLocalizedName();
				if(minecraft.theWorld != cachedBlockWorld || blockX != cachedBlockX || blockY != cachedBlockY
						|| blockZ != cachedBlockZ || !blockName.equals(cachedBlockName)) {
					cachedBlockWorld = minecraft.theWorld;
					cachedBlockX = blockX;
					cachedBlockY = blockY;
					cachedBlockZ = blockZ;
					cachedBlockName = blockName;
					setText("Block: " + blockName + " " + blockX + " " + blockY + " " + blockZ);
				}
			}else if(!"Block: None".equals(cachedText)) {
				cachedBlockWorld = null;
				cachedBlockName = null;
				setText("Block: None");
			}
			break;
		case DAY_COUNTER:
			long day = minecraft.theWorld == null ? 0L : Math.max(0L, minecraft.theWorld.getWorldTime() / 24000L) + 1L;
			if(day != cachedValue) {
				cachedValue = day;
				setText("Day: " + day);
			}
			break;
		case LIGHT_LEVEL:
			int lightX = (int) Math.floor(minecraft.thePlayer.posX);
			int lightY = (int) Math.floor(minecraft.thePlayer.posY);
			int lightZ = (int) Math.floor(minecraft.thePlayer.posZ);
			if(minecraft.theWorld != cachedLightWorld || lightX != cachedLightX || lightY != cachedLightY
					|| lightZ != cachedLightZ) {
				cachedLightWorld = minecraft.theWorld;
				cachedLightX = lightX;
				cachedLightY = lightY;
				cachedLightZ = lightZ;
				cachedLightPosition = new BlockPos(lightX, lightY, lightZ);
			}
			int light = minecraft.theWorld == null ? 0 : minecraft.theWorld.getLightFromNeighbors(cachedLightPosition);
			if(light != cachedValue) {
				cachedValue = light;
				setText("Light: " + light + "/15");
			}
			break;
		case LOW_HEALTH:
			int healthPercent = Math.round(minecraft.thePlayer.getHealth() * 10.0f);
			int thresholdTenths = Math.round(context.config.lowHealthThreshold * 10.0f);
			if(healthPercent != cachedValue || thresholdTenths != cachedLowHealthThreshold) {
				cachedValue = healthPercent;
				cachedLowHealthThreshold = thresholdTenths;
				setText(healthPercent <= thresholdTenths ? "LOW HEALTH: " + healthPercent / 10.0f : "Health OK");
			}
			break;
		case FOOD_INFO:
			refreshFoodInfo();
			break;
		case ITEM_COUNT:
			refreshItemCount();
			break;
		case INVENTORY_PREVIEW:
			refreshInventoryPreview();
			break;
		}
	}

	private void refreshInventoryPreview() {
		int shown = 0;
		boolean changed = false;
		for(int i = 0; i < minecraft.thePlayer.inventory.mainInventory.length && shown < 6; ++i) {
			ItemStack item = minecraft.thePlayer.inventory.mainInventory[i];
			if(item == null) {
				continue;
			}
			if(cachedInventoryStacks[shown] != item || cachedInventoryCounts[shown] != item.stackSize) {
				changed = true;
			}
			++shown;
		}
		if(shown != cachedInventoryCount) {
			changed = true;
		}
		if(!changed && inventoryPreviewInitialized) {
			return;
		}

		StringBuilder text = new StringBuilder("Inventory: ");
		shown = 0;
		for(int i = 0; i < minecraft.thePlayer.inventory.mainInventory.length && shown < 6; ++i) {
			ItemStack item = minecraft.thePlayer.inventory.mainInventory[i];
			if(item == null) {
				continue;
			}
			cachedInventoryStacks[shown] = item;
			cachedInventoryCounts[shown] = item.stackSize;
			cachedInventoryCountLabels[shown] = item.stackSize > 1 ? Integer.toString(item.stackSize) : null;
			if(shown > 0) {
				text.append(", ");
			}
			text.append(item.getDisplayName());
			if(item.stackSize > 1) {
				text.append(" x").append(item.stackSize);
			}
			++shown;
		}
		for(int i = shown; i < cachedInventoryStacks.length; ++i) {
			cachedInventoryStacks[i] = null;
			cachedInventoryCounts[i] = 0;
			cachedInventoryCountLabels[i] = null;
		}
		cachedInventoryCount = shown;
		inventoryPreviewInitialized = true;
		if(shown == 0) {
			text.append("Empty");
		}
		String inventory = text.toString();
		setText(inventory);
	}

	private void refreshFoodInfo() {
		net.minecraft.util.FoodStats foodStats = minecraft.thePlayer.getFoodStats();
		int food = foodStats.getFoodLevel();
		int saturation = Math.round(foodStats.getSaturationLevel() * 10.0f);
		int exhaustion = Math.round(foodStats.getFoodExhaustionLevel() * 10.0f);
		ItemStack held = minecraft.thePlayer.getHeldItem();
		int foodEstimate = 0;
		int saturationEstimate = saturation;
		if(held != null && held.getItem() instanceof ItemFood) {
			ItemFood itemFood = (ItemFood) held.getItem();
			foodEstimate = Math.min(20, food + itemFood.getHealAmount(held)) - food;
			saturationEstimate = Math.round(Math.min(food + foodEstimate,
					foodStats.getSaturationLevel() + itemFood.getHealAmount(held)
							* itemFood.getSaturationModifier(held) * 2.0f) * 10.0f);
		}
		if(food != cachedFood || saturation != cachedFoodSaturation || exhaustion != cachedFoodExhaustion
				|| held != cachedFoodStack || foodEstimate != cachedFoodEstimate
				|| saturationEstimate != cachedFoodSaturationEstimate) {
			cachedFood = food;
			cachedFoodSaturation = saturation;
			cachedFoodExhaustion = exhaustion;
			cachedFoodStack = held;
			cachedFoodEstimate = foodEstimate;
			cachedFoodSaturationEstimate = saturationEstimate;
			cachedFoodEstimateText = foodEstimate > 0 ? "+" + foodEstimate : "";
			cachedFoodValueText = food + "/20";
			cachedFoodSaturationText = saturation / 10 + "." + Math.abs(saturation % 10);
			cachedFoodExhaustionText = exhaustion / 10 + "." + Math.abs(exhaustion % 10);
			String text = "Food: " + food + "/20  Saturation: " + saturation / 10.0f
					+ "  Exhaustion: " + exhaustion / 10.0f;
			if(foodEstimate > 0) {
				text += "  Held food estimate: +" + foodEstimate + " food, saturation "
						+ saturationEstimate / 10.0f;
			}
			setText(text);
		}
	}

	private void refreshItemCount() {
		ItemStack held = minecraft.thePlayer.getHeldItem();
		int count = 0;
		if(held != null) {
			for(int i = 0; i < minecraft.thePlayer.inventory.mainInventory.length; ++i) {
				ItemStack item = minecraft.thePlayer.inventory.mainInventory[i];
				if(item != null && item.getItem() == held.getItem()
						&& item.getMetadata() == held.getMetadata()) {
					count += item.stackSize;
				}
			}
		}
		if(held != cachedCountStack || count != cachedItemCount) {
			cachedCountStack = held;
			cachedItemCount = count;
			setText(held == null ? "Item Count: None" : "Item Count: " + count);
		}
	}

	private void refreshPotionEffects(PvpContext context) {
		long second = context.minecraft.theWorld == null ? 0L : context.minecraft.theWorld.getTotalWorldTime() / 20L;
		if(context.minecraft.theWorld == cachedPotionWorld && second == cachedPotionSecond) {
			return;
		}
		cachedPotionWorld = context.minecraft.theWorld;
		cachedPotionSecond = second;
		cachedPotionCount = 0;
		StringBuilder text = new StringBuilder("Effects: ");
		java.util.List<PotionEffect> effects = minecraft.thePlayer.getActivePotionEffectsList();
		if(effects.isEmpty()) {
			text.append("None");
		}else {
			int shown = 0;
			for(int i = 0; i < effects.size() && shown < 3; ++i) {
				PotionEffect effect = effects.get(i);
				int id = effect.getPotionID();
				if(id < 0 || id >= Potion.potionTypes.length || Potion.potionTypes[id] == null) {
					continue;
				}
				if(shown > 0) {
					text.append(", ");
				}
				String effectName = I18n.format(effect.getEffectName());
				int rowIndex = shown++;
				StringBuilder row = new StringBuilder(effectName);
				if(effect.getAmplifier() > 0) {
					row.append(" ").append(effect.getAmplifier() + 1);
				}
				if(rowIndex > 0) {
					row.append(" ");
				}
				cachedPotionIds[rowIndex] = id;
				text.append(effectName);
				if(effect.getAmplifier() > 0) {
					text.append(" ").append(effect.getAmplifier() + 1);
				}
				int totalSeconds = Math.max(0, effect.getDuration() / 20);
				String duration = totalSeconds / 60 + ":" + (totalSeconds % 60 < 10 ? "0" : "")
						+ totalSeconds % 60;
				row.append(" ").append(duration);
				cachedPotionRows[rowIndex] = row.toString();
				text.append(" ").append(duration);
			}
			cachedPotionCount = shown;
			if(shown == 0) {
				text.append("None");
			}else if(effects.size() > 3) {
				text.append(" +").append(effects.size() - 3);
			}
		}
		if(cachedPotionCount == 0) {
			for(int i = 0; i < cachedPotionRows.length; ++i) {
				cachedPotionRows[i] = null;
			}
		}
		setText(text.toString());
	}

	private void refreshDurability(ItemStack stack, String label) {
		if(stack == null || !stack.isItemStackDamageable()) {
			if(stack != cachedStack || cachedValue != -1L) {
				cachedStack = stack;
				cachedValue = -1L;
				setText(label + "N/A");
			}
			return;
		}
		int percentage = (stack.getMaxDamage() - stack.getItemDamage()) * 100 / stack.getMaxDamage();
		if(stack != cachedStack || percentage != cachedValue) {
			cachedStack = stack;
			cachedValue = percentage;
			setText(label + percentage + "%");
		}
	}

	private void setText(String text) {
		if(!text.equals(cachedText)) {
			cachedText = text;
			cachedWidth = PvpUiFontRenderer.get(minecraft).getStringWidth(text) + 4;
		}
	}

	private String twoDigits(int value) {
		return value < 10 ? "0" + value : Integer.toString(value);
	}

	private String formatDuration(long seconds) {
		long hours = seconds / 3600L;
		long minutes = seconds / 60L % 60L;
		long remaining = seconds % 60L;
		return hours > 0L ? hours + ":" + twoDigits((int) minutes) + ":" + twoDigits((int) remaining)
				: minutes + ":" + twoDigits((int) remaining);
	}

}
