package de.craftlancer.recycler;

import java.io.File;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Recipe;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

public class Recycler extends JavaPlugin {
    private static final String INPUT = "input";
    private static final String RESULT = "result";
    private static final String RESULT_AMOUNT = "resultamount";
    private static final String EXTRA_DURABILITY = "extradura";
    private static final String CALCULATE_DURA = "calcdura";
    private static final String BURNTIME = "burntime";
    
    public static final Permission WILDCARD_PERMISSION = new Permission("recycler.item.*", PermissionDefault.FALSE);
    
    // used to let MC know, that a recipe accepts all data
    // this is implementation specific and may cause problems in older or future versions!
    public static final int MATCH_ALL_DATA = 32767;
    
    private Map<Material, Recyclable> map = new EnumMap<>(Material.class);
    private boolean preventHoppers = true;
    
    @Override
    public void onEnable() {
        loadConfig();
        getServer().getPluginManager().addPermission(WILDCARD_PERMISSION);
        getServer().getPluginManager().registerEvents(new RecyclerListener(this), this);
    }
    
    @Override
    public void onDisable() {
        map.clear();
    }
    
    private void loadConfig() {
        if (!new File(getDataFolder().getPath(), "config.yml").exists())
            saveDefaultConfig();
        
        reloadConfig();
        
        FileConfiguration config = getConfig();
        ConfigurationSection recipesConfig = config.getConfigurationSection("recipes");
        map.clear();
        
        preventHoppers = config.getBoolean("general.disableHopper", true);
        
        for (String key : recipesConfig.getKeys(false)) {
            ConfigurationSection recipeConfig = recipesConfig.getConfigurationSection(key);
            
            Material inputType = Material.matchMaterial(recipeConfig.getString(INPUT));
            Material rewardType = Material.matchMaterial(recipeConfig.getString(RESULT));
            int amount = recipeConfig.getInt(RESULT_AMOUNT, 0);
            int extradura = recipeConfig.getInt(EXTRA_DURABILITY, 0);
            boolean calcdura = recipeConfig.getBoolean(CALCULATE_DURA, true);
            int burntime = recipeConfig.getInt(BURNTIME, 200);
            
            if (inputType == null)
                getLogger().warning(() -> "Invalid Material: " + recipeConfig.getString(INPUT));
            else if (rewardType == null)
                getLogger().warning(() -> "Invalid Material: " + recipeConfig.getString(RESULT));
            else if (map.put(inputType, new Recyclable(inputType, rewardType, amount, extradura, calcdura, burntime)) != null)
                getLogger().warning(() -> "You have 2 configs for " + inputType.name() + "! Using the last one.");
        }
        
        for (Recyclable rec : map.values())
            if (!getServer().addRecipe(rec.toFurnaceRecipe(this)))
                getLogger().warning("Failed to add Recipe for " + rec.getInputType());
            
        getLogger().info(() -> map.size() + " recyclables loaded.");
    }
    
    public boolean isHopperDisabled() {
        return preventHoppers;
    }
    
    public Map<Material, Recyclable> getRecyleMap() {
        return map;
    }
    
    public boolean hasRecycleable(Material mat) {
        return map.containsKey(mat);
    }
    
    public Recyclable getRecycleable(Material mat) {
        return map.get(mat);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!sender.hasPermission("recycler.admin"))
            return false;
        
        // remove plugin recipes
        Iterator<Recipe> itr = getServer().recipeIterator();
        while (itr.hasNext()) {
            Recipe next = itr.next();
            
            if (!(next instanceof Keyed))
                continue;
            
            if (((Keyed) next).getKey().getNamespace().equalsIgnoreCase(this.getName()))
                itr.remove();
        }
        
        loadConfig();
        
        return true;
    }
}
