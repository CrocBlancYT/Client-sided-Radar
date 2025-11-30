package net.croc.clientradar.stuff;

import com.google.gson.Gson;
import net.minecraft.client.multiplayer.ClientLevel;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.QueryableShipData;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.ArrayList;
import java.util.List;

public class ShipRadar {
    private static QueryableShipData<ClientShip> getAllShips(ClientLevel level) {
        return VSGameUtilsKt.getShipObjectWorld(level).getAllShips();
    }

    private static final Gson gson = new Gson();

    public static String getData(ClientLevel level) {
        QueryableShipData<ClientShip> ships = ShipRadar.getAllShips(level);

        List<Object> output = new ArrayList();

        ships.forEach((ClientShip ship) -> {
            output.add(HttpUtils.formatShip(ship));
        });

        return gson.toJson(output);
    }
}