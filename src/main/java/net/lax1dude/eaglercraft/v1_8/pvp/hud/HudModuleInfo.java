package net.lax1dude.eaglercraft.v1_8.pvp.hud;

public final class HudModuleInfo {

	public enum Category {
		CORE("Core"),
		PLAYER("Player"),
		PVP("PvP"),
		WORLD("World"),
		CUSTOM("Custom");

		private final String displayName;

		Category(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	public final HudElement element;
	public final String name;
	public final Category category;

	public HudModuleInfo(HudElement element, String name, Category category) {
		this.element = element;
		this.name = name;
		this.category = category;
	}

}
