package com.dotkntrell.mc.terraformer.listener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TeleportaionListener implements Listener {

    //FIELDS
    private final World world_;

    //CONSTRUCTORS
    public TeleportaionListener(World world) {
        this.world_ = world;
    }

    //EVENT HANDLERS
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (e.getPlayer().getWorld().equals(this.world_)) { return; }
        e.getPlayer().teleport(this.world_.getSpawnLocation());
        Bukkit.getLogger().info("Teleported player to generated world");
    }

}
