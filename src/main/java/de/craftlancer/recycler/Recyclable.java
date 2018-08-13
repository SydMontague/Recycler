package de.craftlancer.recycler;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class Recyclable {
    private final Material inputType;
    private final Material resultType;
    private final int resultAmount;
    private final int extraDurability;
    private final boolean calculateDurability;
    private final int burntime;
    
    private final Permission permission;
    
    public Recyclable(Material type, Material rewardType, int rewardamount, int extradura, boolean calcdura, int burntime) {
        this.inputType = type;
        this.resultType = rewardType;
        this.resultAmount = rewardamount;
        this.extraDurability = extradura;
        this.calculateDurability = calcdura;
        this.burntime = burntime;
        
        permission = new Permission("recycler.item." + inputType.name(), PermissionDefault.FALSE);
        permission.addParent(Recycler.WILDCARD_PERMISSION, true);
        
        if (Bukkit.getPluginManager().getPermission(permission.getName()) == null)
            Bukkit.getPluginManager().addPermission(permission);
    }
    
    public Material getInputType() {
        return inputType;
    }
    
    public Material getRewardType() {
        return resultType;
    }
    
    public int getRewardAmount() {
        return resultAmount;
    }
    
    public int getExtraDurability() {
        return extraDurability;
    }
    
    public boolean isCalculatingDurability() {
        return calculateDurability;
    }
    
    public int getBurntime() {
        return burntime;
    }
    
    public int calculateAmount(ItemStack src) {
        if (!isCalculatingDurability())
            return getRewardAmount();
        
        int amount = getRewardAmount() * (inputType.getMaxDurability() + extraDurability - src.getDurability()) / inputType.getMaxDurability();
        amount = Math.max(amount, 0);
        amount = Math.min(amount, getRewardAmount());
        
        return amount;
    }
    
    public Permission getPermission() {
        return permission;
    }
    
    public Recipe toFurnaceRecipe(Recycler recycler) {
        NamespacedKey key = new NamespacedKey(recycler, getInputType().name());
        return new FurnaceRecipe(key, new ItemStack(getRewardType()), getInputType(), 0, burntime);
    }
}
