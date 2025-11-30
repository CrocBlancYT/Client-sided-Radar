package net.croc.clientradar;

import com.mojang.logging.LogUtils;
import net.croc.clientradar.stuff.EntityRadar;
import net.croc.clientradar.stuff.PlayerInput;
import net.croc.clientradar.stuff.ShipRadar;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import net.croc.clientradar.stuff.HttpUtils;

@Mod(Main.MODID)
@Mod.EventBusSubscriber(modid = net.croc.clientradar.Main.MODID, value = Dist.CLIENT)
public class Main {
    public static final String MODID = "clientradar";
    public static final Logger LOGGER = LogUtils.getLogger();


    @SubscribeEvent
    @SuppressWarnings("unused")
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) return;
        if (event.phase != TickEvent.Phase.START) return;

        if (isEntityRadarEnabled()) {
            String data = EntityRadar.getData(mc.level);
            HttpUtils.send(data, getEntityRadarPath());
        }

        if (isShipRadarEnabled()) {
            String data = ShipRadar.getData(mc.level);
            HttpUtils.send(data, getShipRadarPath());
        }

        if (isControlEnabled()) {
            String data = PlayerInput.getData(mc.level);
            HttpUtils.send(data, getPlayerInputPath());
        }
    }

    public Main() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        MinecraftForge.EVENT_BUS.register(this);
        RegistryConfigs.register(modLoadingContext);
    }

    public static boolean isEntityRadarEnabled() { return RegistryConfigs.Config.enabled_entity_radar.get(); }
    public static boolean isShipRadarEnabled() { return RegistryConfigs.Config.enabled_ship_radar.get(); }
    public static boolean isControlEnabled() { return RegistryConfigs.Config.enabled_player_input.get(); }

    public static String getEntityRadarPath() { return RegistryConfigs.Config.path_entity_radar.get(); }
    public static String getShipRadarPath() { return RegistryConfigs.Config.path_ship_radar.get(); }
    public static String getPlayerInputPath() { return RegistryConfigs.Config.path_player_input.get(); }

}