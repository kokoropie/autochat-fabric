package com.kaga.autochat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import org.lwjgl.glfw.GLFW;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kaga.autochat.config.AutoChatConfig;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;

import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;
import java.util.Date;

@Environment(EnvType.CLIENT)
public class AutoChat implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("autochat");
	private static final ConfigHolder<AutoChatConfig> CONFIG = AutoChatConfig.init();
	
	private static AutoChat instance;
	private KeyBinding autoChatKeyBinding;
	private KeyBinding configAutoChatKeyBinding;
	public boolean enabled = false;
	public long timeMillis = 0L;
	private static Timer timer = new Timer();

	@Override
	public void onInitialize() {
		if (instance == null) {
			instance = this;
		}

		autoChatKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("Start/Stop AutoChat", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "AutoChat"));
		configAutoChatKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open Config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "AutoChat"));
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	public void tick(MinecraftClient client) {
        if (configAutoChatKeyBinding.wasPressed())
			client.setScreen(AutoConfig.getConfigScreen(AutoChatConfig.class, client.currentScreen).get());
        if (autoChatKeyBinding.wasPressed()) {
			enabled = !enabled;
			if (enabled) {
				timer = new Timer();
			} else {
				timer.cancel();
			}
			client.player.sendMessage(Text.of("AutoChat: " + (enabled ? "Enabled" : "Disabled")), true);
			schedule(client);
		}
		if (client.world == null || client.player == null) {
			enabled = false;
		}
    }

	public void schedule(MinecraftClient client) {
		if (client.world != null && client.player != null && enabled) {
			timeMillis = Util.getMeasuringTimeMs();

			AutoChatConfig config = getConfig();
			long delay = config.delay * 60 * 1000;
			String message = config.message;

			TimerTask doAsynchronousTask = new TimerTask() {       
				@Override
				public void run() {
					SimpleDateFormat dt = new SimpleDateFormat("hh:mm:ss");
					client.player.sendMessage(Text.of("Sent at %s".formatted(dt.format(new Date()))), false);
					if (message.startsWith("/")) {
						client.getNetworkHandler().sendCommand(message.substring(1));
					} else {
						client.getNetworkHandler().sendChatMessage(message);
					}
				}
			};
			timer.schedule(doAsynchronousTask, 0, delay); 
		} else {
			enabled = false;
			timer.cancel();
		}
	}

	public static AutoChatConfig getConfig() {
		return CONFIG.getConfig();
	}

	public static AutoChat getInstance() {
		return instance;
	}
}