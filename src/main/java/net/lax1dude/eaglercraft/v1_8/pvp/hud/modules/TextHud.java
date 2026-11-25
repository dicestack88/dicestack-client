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
	private int cachedLowHealthThreshold = Integer.MIN_VALUE;
	private Entity cachedTarget;
	private String cachedTargetName;
	private int cachedTargetHealth = Integer.MIN_VALUE;
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
	private ItemStack cachedFoodStack;
	private String cachedInventory;

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
		return cachedWidth + style.padding * 2 - 4;
	}

	@Override
	public int getHeight(PvpContext context) {
		refresh(context);
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		return 9 + style.padding * 2;
	}

	@Override
	public void render(PvpContext context, int x, int y) {
		refresh(context);
		HudAppearanceConfig style = HudRenderer.resolveAppearance(context, getId());
		HudRenderer.drawPanel(context, getId(), x, y, getWidth(context), getHeight(context));
		HudRenderer.drawText(context, getId(), minecraft, cachedText, x + style.padding, y + style.padding,
				Math.max(1, getWidth(context) - style.padding * 2));
	}

	private void refresh(PvpContext context) {
		if(minecraft.thePlayer == null && type != Type.SESSION_TIME) {
			cachedValue = Long.MIN_VALUE;
			cachedDetail = null;
			cachedStack = null;
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
			Entity target = minecraft.objectMouseOver != null
					&& minecraft.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
							? minecraft.objectMouseOver.entityHit : null;
			if(target != cachedTarget) {
				cachedTarget = target;
				cachedTargetName = null;
				cachedTargetHealth = Integer.MIN_VALUE;
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
					setText(targetHealth == Integer.MIN_VALUE ? "Target: " + targetName
							: "Target: " + targetName + " " + (targetHealth / 10.0f) + " HP");
				}
			}
			break;
		case BLOCK_INFO:
			if(minecraft.objectMouseOver != null
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
		StringBuilder text = new StringBuilder("Inventory: ");
		int shown = 0;
		for(int i = 0; i < minecraft.thePlayer.inventory.mainInventory.length && shown < 6; ++i) {
			ItemStack item = minecraft.thePlayer.inventory.mainInventory[i];
			if(item == null) {
				continue;
			}
			if(shown++ > 0) {
				text.append(", ");
			}
			text.append(item.getDisplayName());
			if(item.stackSize > 1) {
				text.append(" x").append(item.stackSize);
			}
		}
		if(shown == 0) {
			text.append("Empty");
		}
		String inventory = text.toString();
		if(!inventory.equals(cachedInventory)) {
			cachedInventory = inventory;
			setText(inventory);
		}
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
				if(shown++ > 0) {
					text.append(", ");
				}
				text.append(I18n.format(effect.getEffectName()));
				if(effect.getAmplifier() > 0) {
					text.append(" ").append(effect.getAmplifier() + 1);
				}
				int totalSeconds = Math.max(0, effect.getDuration() / 20);
				text.append(" ").append(totalSeconds / 60).append(":")
						.append(totalSeconds % 60 < 10 ? "0" : "").append(totalSeconds % 60);
			}
			if(shown == 0) {
				text.append("None");
			}else if(effects.size() > 3) {
				text.append(" +").append(effects.size() - 3);
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
			cachedWidth = minecraft.fontRendererObj.getStringWidth(text) + 4;
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
