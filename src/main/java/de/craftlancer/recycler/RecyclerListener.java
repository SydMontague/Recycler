package de.craftlancer.recycler;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class RecyclerListener implements Listener {
    private static final int INPUT_SLOT = 0;
    
    private Recycler plugin;
    
    public RecyclerListener(Recycler instance) {
        plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSmelt(FurnaceSmeltEvent event) {
        ItemStack source = event.getSource();
        
        if (!plugin.hasRecycleable(source.getType()))
            return;
        
        Recyclable rec = plugin.getRecycleable(source.getType());
        
        int amount = rec.calculateAmount(source);
        event.setResult(new ItemStack(rec.getRewardType(), amount));
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void putInFurnace(InventoryClickEvent event) {
        if (!event.getInventory().getType().equals(InventoryType.FURNACE))
            return;
        
        ItemStack item = null;
        
        if (event.getRawSlot() == INPUT_SLOT)
            item = event.getCursor();
        else if (event.isShiftClick())
            item = event.getCurrentItem();
        
        if (item == null || !plugin.hasRecycleable(item.getType()))
            return;
        
        Recyclable rec = plugin.getRecycleable(item.getType());
        
        if (!event.getWhoClicked().hasPermission(rec.getPermission()))
            event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent e) {
        if (!plugin.isHopperDisabled())
            return;
        
        InventoryType source = e.getSource().getType();
        
        if (!source.equals(InventoryType.HOPPER) && !source.equals(InventoryType.DROPPER))
            return;
        
        if (!e.getDestination().getType().equals(InventoryType.FURNACE))
            return;
        
        if (!plugin.hasRecycleable(e.getItem().getType()))
            return;
        
        e.setCancelled(true);
    }
}
