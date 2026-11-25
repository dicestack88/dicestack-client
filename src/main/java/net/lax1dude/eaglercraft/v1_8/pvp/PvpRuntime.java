package net.lax1dude.eaglercraft.v1_8.pvp;

import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.minecraft.client.Minecraft;

public final class PvpRuntime {

	private static final int CLICK_HISTORY_SIZE = 128;
	private static final long CPS_WINDOW_MILLIS = 1000L;

	private final long[] leftClicks = new long[CLICK_HISTORY_SIZE];
	private final long[] rightClicks = new long[CLICK_HISTORY_SIZE];
	private int leftClickIndex;
	private int rightClickIndex;
	private long sessionStartedAt;
	private long lastPositionAt;
	private double lastX;
	private double lastY;
	private double lastZ;
	private double speed;
	private boolean trackingPlayer;

	public void tick(Minecraft minecraft) {
		if(minecraft.theWorld == null || minecraft.thePlayer == null) {
			trackingPlayer = false;
			speed = 0.0d;
			sessionStartedAt = 0L;
			lastPositionAt = 0L;
			return;
		}

		long now = EagRuntime.steadyTimeMillis();
		if(!trackingPlayer) {
			trackingPlayer = true;
			sessionStartedAt = now;
			lastPositionAt = now;
			lastX = minecraft.thePlayer.posX;
			lastY = minecraft.thePlayer.posY;
			lastZ = minecraft.thePlayer.posZ;
			speed = 0.0d;
			return;
		}

		long elapsed = now - lastPositionAt;
		if(elapsed > 0L && elapsed <= 2000L) {
			double dx = minecraft.thePlayer.posX - lastX;
			double dy = minecraft.thePlayer.posY - lastY;
			double dz = minecraft.thePlayer.posZ - lastZ;
			double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
			speed = distance <= 5.0d ? distance * 1000.0d / elapsed : 0.0d;
		}else {
			speed = 0.0d;
		}
		lastPositionAt = now;
		lastX = minecraft.thePlayer.posX;
		lastY = minecraft.thePlayer.posY;
		lastZ = minecraft.thePlayer.posZ;
	}

	public double getSpeed() {
		return speed;
	}

	public long getSessionSeconds() {
		return !trackingPlayer ? 0L
				: Math.max(0L, EagRuntime.steadyTimeMillis() - sessionStartedAt) / 1000L;
	}

	public void recordMouseClick(int button) {
		if(button == 0) {
			recordClick(leftClicks, leftClickIndex);
			leftClickIndex = (leftClickIndex + 1) % CLICK_HISTORY_SIZE;
		}else if(button == 1) {
			recordClick(rightClicks, rightClickIndex);
			rightClickIndex = (rightClickIndex + 1) % CLICK_HISTORY_SIZE;
		}
	}

	private void recordClick(long[] clicks, int index) {
		clicks[index] = EagRuntime.steadyTimeMillis();
	}

	public int getLeftCps() {
		return countRecent(leftClicks);
	}

	public int getRightCps() {
		return countRecent(rightClicks);
	}

	private int countRecent(long[] clicks) {
		long now = EagRuntime.steadyTimeMillis();
		int count = 0;
		for(int i = 0; i < clicks.length; ++i) {
			long clickTime = clicks[i];
			if(clickTime != 0L && now - clickTime >= 0L && now - clickTime <= CPS_WINDOW_MILLIS) {
				++count;
			}
		}
		return count;
	}

}
