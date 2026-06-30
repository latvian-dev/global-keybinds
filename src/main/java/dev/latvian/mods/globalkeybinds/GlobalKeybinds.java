package dev.latvian.mods.globalkeybinds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Options;
import net.minecraft.util.Util;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class GlobalKeybinds {
	public static final Gson GSON = new GsonBuilder().create();

	private static <T> Supplier<T> lazy(Supplier<T> supplier) {
		var mutable = new MutableObject<T>(null);

		return () -> {
			if (mutable.get() == null) {
				mutable.setValue(supplier.get());
			}

			return mutable.get();
		};
	}

	public static final Supplier<Path> PATH = lazy(() -> {
		var override = System.getenv("MC_GLOBAL_KEYBINDS_PATH");

		if (override == null || override.isEmpty()) {
			var userHome = Util.getPlatform() == Util.OS.WINDOWS ? System.getenv("APPDATA") : System.getProperty("user.home");
			return Path.of(userHome).resolve("latvian.dev").resolve("minecraft-global-keybinds.json");
		} else {
			return Path.of(override);
		}
	});

	private static final Supplier<JsonObject> JSON = lazy(() -> {
		var path = PATH.get();

		if (path != null && Files.exists(path)) {
			try (var reader = Files.newBufferedReader(path)) {
				if (GSON.fromJson(reader, JsonObject.class) instanceof JsonObject json) {
					return json;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return new JsonObject();
	});

	@Nullable
	public static String get(String key) {
		var json = JSON.get();

		if (json.has(key)) {
			var k = json.get(key).getAsString();
			return k.isEmpty() ? "key.keyboard.unknown" : k;
		}

		return null;
	}

	public static void saveKeybinds(Options options) {
		var json = JSON.get();

		for (var key : options.keyMappings) {
			var k = key.saveString();

			if (key.isDefault()) {
				json.remove(key.getName());
			} else {
				if (key.getKeyModifier() != KeyModifier.NONE) {
					k += ":" + key.getKeyModifier();
				}

				if (k.equals("key.keyboard.unknown")) {
					k = "";
				}

				json.addProperty(key.getName(), k);
			}
		}

		var path = PATH.get();

		if (path != null) {
			try {
				var parent = path.getParent();

				if (Files.notExists(parent)) {
					Files.createDirectories(parent);
				}

				try (var writer = new JsonWriter(Files.newBufferedWriter(path))) {
					writer.setIndent("\t");
					GSON.toJson(json, JsonElement.class, writer);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static InputConstants.Key modifyDefaultKeys(String name, InputConstants.Key original) {
		return switch (name) {
			case "key.saveToolbarActivator",
			     "key.loadToolbarActivator",
			     "key.socialInteractions",
			     "key.atlasviewer.open_viewer",
			     "key.curios.open.desc",
			     "key.bridgingmod.toggle_bridging",
			     "key.voice_chat_group",
			     "key.disable_voice_chat",
			     "key.hide_icons",
			     "key.advancements",
			     "iris.keybind.reload" -> InputConstants.UNKNOWN;
			case "key.jei.bookmark" -> InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_EQUALS);
			case "key.push_to_talk" -> InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_C);
			case "key.mute_microphone" -> InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_F7);
			case "key.ok_zoomer.zoom" -> InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_Z);
			case "gui.xaero_open_map" -> InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_M);
			default -> original;
		};
	}
}
