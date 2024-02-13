package com.kaga.autochat.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

@Config(name = "AutoChat")
public class AutoChatConfig implements ConfigData {

    public String message = "Hello, world!";
    
    public int delay = 5;

    public static ConfigHolder<AutoChatConfig> init()
    {
        ConfigHolder<AutoChatConfig> holder = AutoConfig.register(AutoChatConfig.class, GsonConfigSerializer::new);

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((s, m) -> AutoConfig.getConfigHolder(AutoChatConfig.class).load());

        return holder;
    }
}
