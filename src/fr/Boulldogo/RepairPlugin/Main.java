package fr.Boulldogo.RepairPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private FileConfiguration config;
    private Map<UUID, Long> playerCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
    	
        saveDefaultConfig();
        config = getConfig();

        String version = config.getString("version");
        
        VersionChecker versionChecker = new VersionChecker(this, version, "https://api.github.com/repos/Boulldog0/RepairPlugin/releases/latest");
        versionChecker.checkVersion();

        
        this.getCommand("repair").setExecutor(new RepairCommand(this));
        this.getCommand("repair-all").setExecutor((new RepairAllCommand(this)));
        
        getLogger().info("Le plugin RepairPlugin v" + version + " a été chargé avec succès !");
    }

    @Override
    public void onDisable() {

        getLogger().info("Le plugin RepairPlugin a été déchargé avec succès !");
    }
    
    public long getPlayerCooldown(UUID playerUUID) {
        return playerCooldowns.getOrDefault(playerUUID, 0L);
    }

    public void setPlayerCooldown(UUID playerUUID, long cooldownTime) {
        playerCooldowns.put(playerUUID, cooldownTime);
    }

}