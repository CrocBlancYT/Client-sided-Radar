package net.croc.clientradar.stuff;

import com.google.gson.Gson;
import net.croc.clientradar.RegistryConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class PlayerInput {
    public static double getRaycastMaxDistance() { return RegistryConfigs.Config.raycastMaxDistance.get(); }
    public static int getRaycastTicks() { return RegistryConfigs.Config.raycastTicks.get(); }
    public static boolean isRaycastEnabled() { return RegistryConfigs.Config.raycastEnabled.get(); }

    private static final Gson gson = new Gson();
    private static boolean firstHit = false;
    private static HitResult hit;
    private static int lastCheckTick = 0;

    public static String getData(ClientLevel level) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return "";

        Map<String, Object> info = new HashMap<>();

        Vec3 eyePos = player.getEyePosition();
        info.put("x", eyePos.x);
        info.put("y", eyePos.y);
        info.put("z", eyePos.z);

        info.put("yaw", player.getYRot());
        info.put("pitch", player.getXRot());

        Vec3 lookVector = player.getLookAngle();
        info.put("look_x", lookVector.x);
        info.put("look_y", lookVector.y);
        info.put("look_z", lookVector.z);

        info.put("uuid", player.getUUID().toString());

        info.put("isCrouching", player.isCrouching());
        info.put("isSeated", player.isPassenger());

        info.put("attack_button_down", mc.options.keyAttack.isDown());
        info.put("use_button_down", mc.options.keyUse.isDown());

        if (isRaycastEnabled() && player.tickCount - lastCheckTick > getRaycastTicks()) { // Update every second
            hit = player.pick(getRaycastMaxDistance(), 0.0F, false);
            lastCheckTick = player.tickCount;
            firstHit = true;
        }

        if (firstHit && hit.getType() == HitResult.Type.BLOCK) {
            Vec3 pos = hit.getLocation();

            info.put("hit_x", pos.x);
            info.put("hit_y", pos.y);
            info.put("hit_z", pos.z);
        }

        return gson.toJson(info);
    }
}