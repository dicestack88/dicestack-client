# Dicestack remaining expansion audit

Status legend: **[x]** already implemented before this branch; **[+]** implemented
in this branch; **[~]** partial/alternative; **[ ]** not implemented;
**[!]** unsupported safely by the current architecture.

## Camera and movement visuals

- [x] No Dynamic FOV; [x] No Hurt Tilt.
- [x] View Bobbing and Smooth Camera use the existing vanilla settings.
- [ ] Camera Shake controls.
- [+] Custom FOV, applied locally without changing the saved vanilla FOV.
- [ ] Improved Third-Person Distance controls.
- [~] Zoom enable/mode/strength are configurable; smoothness is not.
- [!] Freelook: player rotation is shared by local targeting and network movement;
  no verified local-only camera rotation path is available.

## World visuals

- [x] Fullbright, No Fog, local rain/snow hiding, vanilla particle presets,
  entity-shadow removal and vignette hiding.
- [x] Fire, Pumpkin and Portal overlay controls.
- [ ] Block Shadows, Clear Water, Custom Fog Colour and Damage Flash controls.
- [~] Custom Sky can be supplied by existing compatible resource packs; there is
  no Dicestack custom-sky setting.
- [!] Time/Weather Changers: safely changing the visible world requires a
  render-only time/weather abstraction; mutating world state is explicitly
  avoided.

## Item visuals and animations

- [ ] Item Drop Physics, Custom Item Animations, Held Item Size/Position,
  Enchantment Glint Intensity/Colour and Totem Animation Adjustments.
- [ ] Explosion Particle and Block-Breaking Particle Adjustments.
- [ ] Potion Effect Icon and Armor HUD Icon Styling.
- [ ] Custom Hit Colour and Custom Block-Selection Outline.
- [~] Vanilla reduced-particle settings exist, but do not expose per-effect
  animation controls.

## Crosshair

- [x] Vanilla, Dot, Circle, Cross and Circle-with-Dot styles.
- [x] Size, Thickness, Gap, Colour, Opacity, Outline and Dynamic Target Colour.
- [x] Hide in Menus/Third Person; settings apply immediately and preserve vanilla
  crosshair when custom rendering is off.

## Privacy and account protection

- [!] Password Hider/Reveal: no client-owned password-entry screen is exposed
  through the current Dicestack settings architecture; credentials are not
  stored or surfaced by this feature.
- [ ] Streamer Mode and Username Hider.
- [~] Server Address Hider is available for the Dicestack HUD module, not every
  client-owned screen.
- [~] Chat Privacy can hide the HUD chat display; selective filtering/replacement
  is not implemented.
- [!] Screenshot Privacy: the supported screenshot call delegates directly to
  the platform capture API, with no image-processing callback. External OS
  screenshots, recordings and capture software cannot be protected here.

## Chat

- [+] Optional local-time timestamps, 12/24-hour formatting and mention
  highlighting; these affect rendering only and do not alter sent/received
  message components.
- [~] Chat Opacity uses the persisted vanilla opacity setting.
- [ ] Dicestack Chat Background Customisation, Compact Chat Mode and general
  configurable message highlighting. Mention highlighting is the available
  focused alternative.
- [x] Whole-chat hide control from the previous milestone.

## General client QoL

- [x] Toggle Sprint, Toggle Sneak, Zoom, Quick Reconnect, Copy Coordinates,
  Coordinate Format, In-game Clock Formatting and Temporary HUD Hide.
- [+] Searchable QoL settings and favourite HUD modules.
- [+] Named HUD profiles store module visibility, position, scale, shared and
  per-module appearance, and layout settings. Applying a profile leaves global
  QoL settings and favourites untouched.
- [~] Profiles can be selected/applied from the HUD module manager; no dedicated
  profile hotkey is provided.
- [ ] Individual module keybinds, reset-by-category, Notification Controls and
  Screenshot Mode.
- [!] Configuration Import/Export: the current browser bridge provides clipboard
  writing but no verified clipboard read or safe file import/export API. No
  nonfunctional import/export controls are exposed.
- [x] GUI Scale is available through the existing vanilla Video Settings.
- [~] Existing vanilla tooltips remain; no Dicestack-specific tooltip system was
  added.

## Performance and comfort

- [x] Vanilla particle reduction presets and optional entity-shadow removal.
- [~] Reduced visual effects are covered by existing local visual toggles, not a
  grouped profile.
- [~] FPS HUD value is available and inherits shared styling/scale controls.
- [ ] Animation Reduction, FPS Limit Presets, Performance Profile and
  Lightweight HUD Rendering Mode.

## HUD modules

- [x] Potion Effects, Saturation, Arrow Counter, Target HUD, Low Health Warning,
  Sprint Status, Sneak Status, Block Information, Day Counter and Light Level.
- [+] Hunger/Saturation panel exposes actual food, saturation and exhaustion
  values; held-food restoration is labelled as an estimate.
- [+] Held Item Counter totals the selected item type in the player's inventory.
- [~] Armor Status and Armor Durability are present; no icon-based preview was
  added.
- [ ] Inventory Preview.
- [!] Attack Indicator: this client does not expose a reliable cooldown value.
- [!] Combo Counter and Hit Accuracy: there is no dependable local hit-event
  source from which to compute these statistics; CPS is not treated as hits.
- [~] FPS is available, but memory usage is not exposed as a portable client
  metric in the shared WASM-GC architecture.
- [x] Existing FPS, Coordinates, CPS, Keystrokes, Compass, Speedometer, Biome,
  World Clock, Session Playtime, Health, Hunger, Armor, Held Item/Durability,
  Server Address and Game Mode modules remain.

## Styling, migration and validation

- [x] Modern Glass and Minimal Competitive presets and the existing per-module
  overrides remain supported.
- [+] Configuration schema v6 migrates v5 data by retaining known settings and
  applying defaults for new settings.
- [+] Migration, profile isolation, and v6 JSON round-trip checks passed.
- [x] Current controls continue to use existing Eaglercraft rendering/settings
  APIs; no Fabric mod or new network protocol is introduced.
