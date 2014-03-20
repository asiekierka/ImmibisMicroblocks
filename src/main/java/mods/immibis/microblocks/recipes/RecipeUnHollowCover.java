package mods.immibis.microblocks.recipes;


import java.util.HashMap;

import mods.immibis.microblocks.ItemMicroblock;
import mods.immibis.microblocks.MicroblockSystem;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipeUnHollowCover implements IRecipe {
	
	private static HashMap<Integer, Integer> partIDMap = new HashMap<Integer, Integer>();
	
	public static void addMap(int a, int b)
	{
		partIDMap.put(a, b);
	}

	@Override
	public boolean matches(InventoryCrafting inventorycrafting, World world) {
		return getCraftingResult(inventorycrafting) != null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
		int slot = -1;
		for(int k = 0; k < inventorycrafting.getSizeInventory(); k++)
		{
			ItemStack is = inventorycrafting.getStackInSlot(k);
			if(is == null || !(is.getItem() instanceof ItemMicroblock))
				continue;
			if(slot != -1)
				return null;
			slot = k;
		}
		if(slot == -1)
			return null;
		ItemStack is = inventorycrafting.getStackInSlot(slot);
		Integer o = partIDMap.get(ItemMicroblock.getPartTypeID(is));
		if(o == null)
			return null;
		return ItemMicroblock.getStackWithPartID(o);
	}

	@Override
	public int getRecipeSize() {
		return 1;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(MicroblockSystem.microblockContainerBlock, 1, 0);
	}

}
