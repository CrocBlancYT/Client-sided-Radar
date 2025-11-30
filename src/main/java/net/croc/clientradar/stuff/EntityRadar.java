package net.croc.clientradar.stuff;

import com.google.gson.Gson;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityRadar {
    private static final Gson gson = new Gson();

    public static String getData(ClientLevel level) {
        Iterable<Entity> nearbyEntities = level.entitiesForRendering();
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

        return gson.toJson(entityList);
    }
}