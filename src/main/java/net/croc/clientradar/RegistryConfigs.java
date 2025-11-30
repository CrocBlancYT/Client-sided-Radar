package net.croc.clientradar;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RegistryConfigs {

    @SuppressWarnings("deprecation")
    public static void register(ModLoadingContext modLoadingContext) {
        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        RegistryConfigs.Config.loadConfig(Config.CLIENT_CONFIG, Paths.get("config/web-radar-client.toml"));
    }

    public static class Config {
        public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
        public static final ForgeConfigSpec CLIENT_CONFIG;

        public static final ForgeConfigSpec.ConfigValue<String> serverUrl;

        public static final ForgeConfigSpec.ConfigValue<String> path_entity_radar;
        public static final ForgeConfigSpec.BooleanValue enabled_entity_radar;
        public static final ForgeConfigSpec.BooleanValue getID_Slug;
        public static final ForgeConfigSpec.BooleanValue getOmega_Vel;


        public static final ForgeConfigSpec.ConfigValue<String> path_ship_radar;
        public static final ForgeConfigSpec.BooleanValue enabled_ship_radar;


        public static final ForgeConfigSpec.ConfigValue<String> path_player_input;
        public static final ForgeConfigSpec.BooleanValue enabled_player_input;
        public static final ForgeConfigSpec.BooleanValue raycastEnabled;
        public static final ForgeConfigSpec.ConfigValue<Integer> raycastTicks;
        public static final ForgeConfigSpec.ConfigValue<Double> raycastMaxDistance;

        static {
            CLIENT_BUILDER.comment("Server Settings");

            serverUrl = CLIENT_BUILDER
                    .comment("\nURL of the webserver")
                    .define("url", "http://");


            enabled_entity_radar = CLIENT_BUILDER .define("enable entity radar", false);
            enabled_ship_radar   = CLIENT_BUILDER .define("enable ship radar", false);
            enabled_player_input = CLIENT_BUILDER .define("enable player input", false);

            path_entity_radar = CLIENT_BUILDER .define("path entity radar", "/entity");
            path_ship_radar   = CLIENT_BUILDER .define("path ship radar", "/ship");
            path_player_input = CLIENT_BUILDER .define("path player input", "/player");

            getID_Slug = CLIENT_BUILDER .define("id_&_slug", false);
            getOmega_Vel = CLIENT_BUILDER .define("omega_&_vel", false);

            raycastEnabled = CLIENT_BUILDER
                    .define("raycast_enabled", false);

            raycastTicks = CLIENT_BUILDER
                    .comment("\nTime in ticks between each raycast")
                    .define("raycast_cooldown", 2);

            raycastMaxDistance = CLIENT_BUILDER
                    .comment("\nMaximum distance in blocks travelled by the rays.")
                    .define("raycast_max_distance", 300.0);

            CLIENT_CONFIG = CLIENT_BUILDER.build();
        }

        public static void loadConfig(ForgeConfigSpec config, Path path) {
            final CommentedFileConfig file = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
            file.load();
            config.setConfig(file);
        }
    }
}