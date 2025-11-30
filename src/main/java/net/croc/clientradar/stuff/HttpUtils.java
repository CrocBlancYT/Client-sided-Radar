package net.croc.clientradar.stuff;

import net.croc.clientradar.Main;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;
import org.joml.primitives.AABBic;
import org.slf4j.Logger;
import org.valkyrienskies.core.api.ships.ClientShip;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.IOException;

import net.croc.clientradar.RegistryConfigs;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;

public class HttpUtils {
    private static Logger LOGGER = Main.LOGGER;

    private static Map<String, Object> format(AABBic b) {
        Map<String, Object> f = new HashMap<>();
        f.put("min_x", b.minX());
        f.put("min_y", b.minY());
        f.put("min_z", b.minZ());

        f.put("max_x", b.maxX());
        f.put("max_y", b.maxY());
        f.put("max_z", b.maxZ());
        return f;
    }

    private static Map<String, Object> format(Vector3dc v) {
        Map<String, Object> f = new HashMap<>();
        f.put("x", v.x());
        f.put("y", v.y());
        f.put("z", v.z());
        return f;
    }

    private static Map<String, Object> format(Quaterniondc q) {
        Map<String, Object> f = new HashMap<>();
        f.put("x", q.x());
        f.put("y", q.y());
        f.put("z", q.z());
        f.put("w", q.w());
        return f;
    }

    public static Map<String, Object> formatShip(ClientShip ship) {
        Map<String, Object> out = new HashMap<>();

        ShipTransform transform = ship.getTransform();

        if (RegistryConfigs.Config.getID_Slug.get()) {
            out.put("id", ship.getId());
            out.put("slug", ship.getSlug());
        }

        if (RegistryConfigs.Config.getOmega_Vel.get()) {
            out.put("vel", format(ship.getVelocity()));
            out.put("omega", format(ship.getOmega()));
        }
        
        out.put("world_pos", format(transform.getPositionInWorld()));
        out.put("ship_pos", format(transform.getPositionInShip()));

        out.put("quat", format(ship.getTransform().getShipToWorldRotation()));

        return out;
    }

    public static String getURL() {
        return RegistryConfigs.Config.serverUrl.get();
    }

    private static final ExecutorService HTTP_EXECUTOR = Executors.newCachedThreadPool();

    public static void send(String jsonPayload, String path) {
        HTTP_EXECUTOR.execute(() -> {
            try {
                URL url = new URL(getURL().concat(path));

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
                    LOGGER.error(" <Client Radar> : HTTP polling failed. response code: " + responseCode);
                }

                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}