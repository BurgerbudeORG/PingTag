package org.burgerbude.labymod.addons.pingtag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.projectile.EntityEvokerFangs;

import java.util.*;

/**
 * This object represents a detector that detects the pings of all online players after some time
 *
 * @author Robby
 */
public class PingDetector {

    private final Minecraft minecraft;
    private final Map<UUID, Integer> pings;

    private long lastUpdate;

    /**
     * Default constructor
     *
     * @param minecraft The game
     */
    public PingDetector(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.pings = new HashMap<>();
    }

    /**
     * Update the pings
     */
    public void updatePing() {
        if (this.lastUpdate + 2_500L >= System.currentTimeMillis()) return;
        if (this.minecraft.player == null || this.minecraft.getConnection() == null) return;
        this.lastUpdate = System.currentTimeMillis();

        try {
            for (NetworkPlayerInfo networkPlayerInfo : this.minecraft.getConnection().getPlayerInfoMap()) {

                if (networkPlayerInfo.getGameProfile().getId().getLeastSignificantBits() == 0L) return;

                NetworkPlayerInfo playerInfo = this.networkPlayerInfo(networkPlayerInfo.getGameProfile().getName());

                if (playerInfo == null) return;

                this.pings.put(networkPlayerInfo.getGameProfile().getId(), playerInfo.getResponseTime());
            }
        } catch (ConcurrentModificationException ignored) {
        }
    }

    /**
     * Gets the ping of a player with the given unique identifier
     *
     * @param uniqueId The unique identifier of a player
     * @return the ping of a player
     */
    public int playerPing(UUID uniqueId) {
        if (this.pings.isEmpty() || this.pings.get(uniqueId) == null) return 0;
        return this.pings.get(uniqueId);
    }

    /**
     * Gets the {@link NetworkPlayerInfo} with the given name
     *
     * @param name The name of the player
     * @return a {@link NetworkPlayerInfo} or <b>null</b>
     */
    private NetworkPlayerInfo networkPlayerInfo(String name) {
        if (this.minecraft.getConnection() == null) return null;

        return this.minecraft.getConnection().getPlayerInfoMap().stream().filter(networkPlayerInfo ->
                networkPlayerInfo.getGameProfile().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Map<UUID, Integer> pings() {
        return this.pings;
    }
}
