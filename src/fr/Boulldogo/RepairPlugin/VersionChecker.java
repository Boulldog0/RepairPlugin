package fr.Boulldogo.RepairPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionChecker {

    private final JavaPlugin plugin;
    private final String currentVersion;
    private final String apiUrl;

    public VersionChecker(JavaPlugin plugin, String currentVersion, String apiUrl) {
        this.plugin = plugin;
        this.currentVersion = currentVersion;
        this.apiUrl = apiUrl;
    }

    public void checkVersion() {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JSONObject json = (JSONObject) JSONValue.parse(reader);

            String latestVersion = (String) json.get("tag_name");

            if (!latestVersion.equals(currentVersion)) {
                plugin.getLogger().warning("Une nouvelle version du plugin RepairPlugin est disponible : " + latestVersion);
            } else {
                plugin.getLogger().info("Le plugin RepairPlugin est à jour (version " + currentVersion + ")");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
