package com.example.entityradar;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;

import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import net.minecraftforge.common.ForgeConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import java.util.*;

import java.util.concurrent.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "entityradar";
    private static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("deprecation")
    public Main() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        Config.loadConfig(Config.CLIENT_CONFIG, Paths.get("config/entityradar-client.toml"));

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Entity Detection Mod: Client setup initialized");
    }

    private static final ExecutorService HTTP_EXECUTOR = Executors.newCachedThreadPool();

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID)
    public static class ClientTickHandler {
        private static final Gson gson = new Gson();

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                scanForEntities();
            }
        }

        public static void scanForEntities() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null || minecraft.player == null) {return;}

            Iterable<Entity> nearbyEntities = minecraft.level.entitiesForRendering();
            List<Map<String, Object>> entityList = new ArrayList<>();

            nearbyEntities.forEach(entity -> {
                Map<String, Object> entityData = new HashMap<>();

                entityData.put("Type", entity.getType().toString());

                entityData.put("X", entity.getX());
                entityData.put("Y", entity.getY());
                entityData.put("Z", entity.getZ());

                entityData.put("Velocity_X", entity.getDeltaMovement().x());
                entityData.put("velocity_Y", entity.getDeltaMovement().y());
                entityData.put("velocity_Z", entity.getDeltaMovement().z());

                entityData.put("Yaw", entity.getYRot());
                entityData.put("Pitch", entity.getXRot());
                entityData.put("UUID", entity.getStringUUID());

                if (entity instanceof AbstractClientPlayer) {
                    AbstractClientPlayer player = (AbstractClientPlayer) entity;
                    entityData.put("Name", player.getName().getString());

                    if (entity instanceof LocalPlayer) {
                        entityData.put("isLocalPlayer", true);
                    }
                }

                entityList.add(entityData);
            });

            String data = gson.toJson(entityList);

            sendEntitiesAsync(data);
        }

        public static void sendEntitiesAsync(String jsonPayload) {
            HTTP_EXECUTOR.execute(() -> {
                try {
                    // load url from config
                    String urlString = Config.serverUrl.get();
                    URL url = new URL(urlString);

                    // start connection
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setConnectTimeout(1000); // 1 second timeout
                    conn.setReadTimeout(1000);

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    // ready payload
                    byte[] payloadBytes = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    conn.setRequestProperty("Content-Length", String.valueOf(payloadBytes.length));

                    // send payload
                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(payloadBytes);

                    int responseCode = conn.getResponseCode();

                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        System.err.println("HTTP polling failed. response code: " + responseCode);
                    }

                    conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static class Config {
        public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec CLIENT_CONFIG;

        public static final ForgeConfigSpec.ConfigValue<String> serverUrl;

        static {
            CLIENT_BUILDER.comment("Client Settings");

            serverUrl = CLIENT_BUILDER
                    .comment("URL of the server to send entities data")
                    .define("serverUrl", "http(s)://host(:port)");

            CLIENT_CONFIG = CLIENT_BUILDER.build();
        }

        public static void loadConfig(ForgeConfigSpec config, Path path) {
            final CommentedFileConfig file = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
            file.load();
            config.setConfig(file);
        }
    }
}
