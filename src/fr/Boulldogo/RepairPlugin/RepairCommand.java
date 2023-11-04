package fr.Boulldogo.RepairPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

public class RepairCommand implements CommandExecutor {

    private final Main plugin;
    private Economy economy; 
    
    public RepairCommand(Main plugin) {
        this.plugin = plugin;
        this.economy = null;
        setupEconomy();
    }
    
    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (economyProvider != null) {
                this.economy = economyProvider.getProvider();
            }
        }

        if (this.economy == null) {
            plugin.getLogger().warning("Vault (économie) n'a pas été trouvé. Le plugin ne prendra pas en compte les coûts en économie.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean usePrefix = plugin.getConfig().getBoolean("use-prefix");
        String prefix = usePrefix ? ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix")) : "";
        int repairCost = plugin.getConfig().getInt("repair-cost", 0);
        String repairCostString = String.valueOf(repairCost);
        if (!(sender instanceof Player)) {
            String onlyPlayer = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.only-player-can-execute"));
            sender.sendMessage(onlyPlayer);
            return true;
        }

        Player player = (Player) sender;
        String successMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.repair-success-message").replace("%price%", repairCostString));
        String anyItem = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.repair-any-item-in-hand"));

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            player.sendMessage(prefix + anyItem);
            return true;
        }

        if (itemInHand.getDurability() == 0) {
            String alreadyrepair = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.already-repair"));
            player.sendMessage(prefix + alreadyrepair);
            return true;
        }

        if (repairCost > 0 && !player.hasPermission("repair.bypasscost-repair")) {
            if (economy != null && !economy.has(player, repairCost)) {
                String nomoney = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.money-missing").replace("%price%", repairCostString));
                player.sendMessage(prefix + nomoney);
                return true;
            }

            if (economy != null && !economy.withdrawPlayer(player, repairCost).transactionSuccess()) {
                String Economyerror = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.economy-error"));
                player.sendMessage(prefix + Economyerror);
                return true;
            }
        }

        if (!player.hasPermission("repair.bypasscooldown-repair")) {
            long lastRepairTime = plugin.getPlayerCooldown(player.getUniqueId());
            long currentTime = System.currentTimeMillis();
            int cooldownSeconds = plugin.getConfig().getInt("cooldown-repair", 60);

            if (currentTime - lastRepairTime < cooldownSeconds * 1000) {
                String cooldown = String.valueOf(cooldownSeconds - ((currentTime - lastRepairTime) / 1000));
                String cooldownMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.cooldown-message").replace("%cooldown%", cooldown));
                player.sendMessage(prefix + cooldownMessage);
                return true;
            }

            plugin.setPlayerCooldown(player.getUniqueId(), currentTime);
        }

        itemInHand.setDurability((short) 0);
        player.sendMessage(prefix + successMessage);

        return true;
    }
}
