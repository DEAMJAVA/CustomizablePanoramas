# Customizable Panoramas

A Fabric client mod for Minecraft that lets you capture your own in-game panoramas and use them as your main menu background, instead of the default one or using resourcepacks to do so.

## What it does

- Capture a 360 panorama from wherever you're standing in the world, using a keybind.
- Give each captured panorama a name.
- Manage your saved panoramas.
- The selected panorama replaces the vanilla title screen background.

## How it works

Press the capture keybind while in a world. The mod will briefly take over your camera, hide your HUD, and turn to face all six directions to take a screenshot of each. Once that's done, you'll be asked to name the panorama, and it gets saved to disk.

From then on, you can pick it as your active panorama from the "Panoramas" button in the options screen. Selecting one applies it immediately as your menu background; deselecting it brings back the default Minecraft panorama.


## Usage

1. Open your keybind settings and assign a key to "Capture Panorama".
2. Go into a world, stand somewhere you'd like to capture, and press that key.
3. Wait for the capture to finish, then give the panorama a name.
4. Open the options screen and click "Manage Panoramas" to select it.

> NOTE: take panoramans from on ground only as taking it in air causes visible seams in the captured panoramas

Panoramas are stored per-instance under your config folder, so they carry over between worlds but not between separate game installs unless you copy the files over.


> NOTE: This is a client-side mod. It doesn't need to be installed on servers.

## License

This project is licensed under the GNU General Public [License](LICENSE) v3.0 (GPLv3).