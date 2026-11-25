package net.lax1dude.eaglercraft.v1_8.pvp.hud.modules;

import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudElement;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.PvpContext;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.biome.BiomeGenBase;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudRenderer;
import net.lax1dude.eaglercraft.v1_8.pvp.hud.HudAppearanceConfig;

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
		GAME_MODE
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
		}
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
