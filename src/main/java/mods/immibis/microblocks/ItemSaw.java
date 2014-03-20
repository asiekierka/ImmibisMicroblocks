package mods.immibis.microblocks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemSaw extends Item {

	protected ItemSaw() {
		super();
		maxStackSize = 1;
		setUnlocalizedName("immibis_microblocks.saw");
		setTextureName("immibis_microblocks:saw");
		setCreativeTab(CreativeTabs.tabTools);
	}
	
	@Override
	public boolean hasContainerItem() {
		return true;
	}
	
	@Override
	public Item getContainerItem() {
		return this;
	}

	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack is) {
		return false;
	}
	
}
