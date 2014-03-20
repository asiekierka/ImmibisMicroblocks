package mods.immibis.microblocks;


import java.util.List;

import mods.immibis.core.api.APILocator;
import mods.immibis.core.api.multipart.util.BlockMultipartBase;
import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.core.api.util.Dir;
import mods.immibis.core.multipart.SubhitValues;
import mods.immibis.microblocks.api.*;
import mods.immibis.microblocks.coremod.MicroblockSupporterTransformer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMicroblock extends ItemBlock {
	
	private BlockMultipartBase block;

	public ItemMicroblock(Block block) {
		super((BlockMultipartBase)block);
		this.block = (BlockMultipartBase)block;
	}
	
	public static class Placement {
		public final int x, y, z;
		public final EnumPosition pos;
		public Placement(int x, int y, int z, EnumPosition pos) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.pos = pos;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static Placement getPlacement(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int dir) {
		MovingObjectPosition ray = entityplayer.rayTrace(SidedProxy.instance.getPlayerReach(entityplayer), 0);
        if(ray == null) {
        	if(DEBUG) System.out.println("null raytrace");
        	return null;
        }

        x = ray.blockX;
        y = ray.blockY;
        z = ray.blockZ;
        dir = ray.sideHit;
        TileEntity rayTE = (world.getTileEntity(ray.blockX, ray.blockY, ray.blockZ));
        EnumPosition rayPos = null;
        if(rayTE instanceof IMicroblockSupporterTile) 
        {
        	 if(SubhitValues.isCoverSystem(ray.subHit))
        		 rayPos = ((IMicroblockSupporterTile)rayTE).getCoverSystem().getPartPosition(SubhitValues.getCSPartIndex(ray.subHit));
        	 else
        		 rayPos = ((IMicroblockSupporterTile)rayTE).getPartPosition(SubhitValues.getTilePartIndex(ray.subHit));
        }
        
        Block oldblock = world.getBlock(x, y, z);
        if (oldblock.equals(Blocks.snow_layer))
            dir = 0;
        else if (!oldblock.equals(Blocks.vine))
        {
        	int dx=0, dy=0, dz=0;
        	switch(dir)
        	{
        	case Dir.NX: if(rayPos == null || rayPos.x.touchesNegative()) dx=-1; break;
        	case Dir.PX: if(rayPos == null || rayPos.x.touchesPositive()) dx=1; break;
        	case Dir.NY: if(rayPos == null || rayPos.y.touchesNegative()) dy=-1; break;
        	case Dir.PY: if(rayPos == null || rayPos.y.touchesPositive()) dy=1; break;
        	case Dir.NZ: if(rayPos == null || rayPos.z.touchesNegative()) dz=-1; break;
        	case Dir.PZ: if(rayPos == null || rayPos.z.touchesPositive()) dz=1; break;
        	}
        	if(dx != 0 || dy != 0 || dz != 0)
        	{
        		x += dx;
        		y += dy;
        		z += dz;
        		rayPos = null;
        	}
        }
        if (itemstack.stackSize == 0) {
        	if(DEBUG) System.out.println("empty stack");
            return null;
        }
        
        PartType<?> type = MicroblockSystem.parts.get(getPartTypeID(itemstack));
        if(type == null) {
        	// invalid item
        	itemstack.stackSize = 0;
        	if(DEBUG) System.out.println("invalid type");
        	return null;
        }
     	EnumPosition pos;
     	EnumPartClass clazz = type.getPartClass();
    	if(clazz == EnumPartClass.Panel || clazz == EnumPartClass.HollowPanel)
    	{
    		pos = MicroblockPlacementHighlightHandler.getPanelPlacement(entityplayer, ray, rayPos);
    	}
    	else if(clazz == EnumPartClass.Corner)
    	{
    		pos = MicroblockPlacementHighlightHandler.getCornerPlacement(entityplayer, ray, rayPos);
    	}
    	else if(clazz == EnumPartClass.Strip)
    	{
    		pos = MicroblockPlacementHighlightHandler.getStripPlacement(entityplayer, ray, rayPos);
    	}
    	else {
    		if(DEBUG) System.out.println("invalid class");
            return null;
    	}
    	
    	return new Placement(x, y, z, pos);
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int dir, float x2, float y2, float z2)
    {
		if(world.isRemote) {

	        Placement pl = getPlacement(itemstack, entityplayer, world, x, y, z, dir);
	        
	        if(pl == null)
	        	return false;
	    	
    		if(!placeInBlock(world, pl.x, pl.y, pl.z, pl.pos, itemstack, true, entityplayer, true, dir)) {
    			if(DEBUG) System.out.println("placeInBlockWithPermissionCheck failed");
    			return false;
    		}
    		
    		world.playSound(x+0.5, y+0.5, z+0.5, block.stepSound.getBreakSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F, false);
    		
    		itemstack.stackSize--;
			
	        return true;
		} else {
			// server
			return false;
		}
    }
	
	private static final boolean DEBUG = Boolean.getBoolean("immibis.microblocks.debugplace");
	
	public boolean placeInBlock(World world, int x, int y, int z, EnumPosition pos, ItemStack itemstack, boolean doBlockUpdate, EntityPlayer ply, boolean doPermissionCheck, int sideClicked)
	{
		if(DEBUG) System.out.println((world.isRemote ? "client" : "server")+" placeInBlock "+x+","+y+","+z+" "+pos);
		if(world.isRemote) {
			APILocator.getNetManager().sendToServer(new PacketMicroblockPlace(x, y, z, pos.ordinal(), sideClicked));
		}
		
		int d = getPartTypeID(itemstack);
        if(!MicroblockSystem.parts.containsKey(d)) {
        	if(DEBUG) System.out.println("wrong part ID, got "+d);
        	return false;
        }
        
        PartType<?> type = MicroblockSystem.parts.get(d);
        assert type != null : "No part type with ID "+d;
        
        Part part = type.createPart(pos);
        assert part != null : type+".createPart returned null";
        
        if(doPermissionCheck)
        	for(IMicroblockPermissionHandler ph : MicroblockSystem.instance.permissionHandlers)
        		if(ph.doesPreventPlacing(ply, world, x, y, z, part)) {
        			if(DEBUG) System.out.println("permission check failed: "+MicroblockSystem.instance.getPermissionHandlerName(ph));
        			return false;
        		}
        
        TileEntity newTE = world.getTileEntity(x, y, z);
        
        boolean addedTE = false;
        if(newTE == null || !(newTE instanceof IMicroblockSupporterTile))
        {
        	Block replacing = world.getBlock(x, y, z);
        	
        	if(replacing instanceof MicroblockSupporterTransformer.TilelessTransformedBlock) {
        		newTE = new TileDummy();
        		world.setTileEntity(x, y, z, newTE);
	        	
	        } else {
	        	if(replacing != null && !replacing.isReplaceable(world, x, y, z)) {
	        		if(DEBUG) System.out.println("not replaceable");
	        		return false;
	        	}
	        	if(!block.canPlaceBlockOnSide(world, x, y, z, 0)) {
	        		if(DEBUG) System.out.println("can't place on side");
	        		return false;
	        	}
	        	
	        	world.setBlock(x, y, z, block, 0, 0);
	        	newTE = new TileMicroblockContainer();
	        	world.setTileEntity(x, y, z, newTE);
	        	addedTE = true;
        	}
	    }
        
        IMicroblockCoverSystem cover = ((IMicroblockSupporterTile)newTE).getCoverSystem();
        assert cover != null : "New tile entity has no cover system";
        
        if(!cover.addPart(part)) {
        	if(addedTE)
        		world.setBlock(x, y, z, Blocks.air, 0, 0);
        	
        	if(DEBUG) System.out.println("addPart failed");
			return false;
        }
        if(DEBUG) System.out.println("addPart ok");
        
        if(doBlockUpdate) {
	        if(newTE instanceof IMicroblockSupporterTile2)
				((IMicroblockSupporterTile2)newTE).onMicroblocksChanged();
			else {
				world.notifyBlocksOfNeighborChange(x, y, z, block);
				world.markBlockForUpdate(x, y, z);
			}
        }
		return true;
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack is) {
		PartType<?> pt = MicroblockAPIUtils.getPartTypeByID(getPartTypeID(is));
		return pt == null ? "" : pt.getLocalizedName(is);
	}
	
	@Override
	public boolean getHasSubtypes() {
		return true;
	}
	
	
	@Override
	public boolean getShareTag() {
		return true;
	}
	
	
	public static int getPartTypeID(ItemStack stack) {
		NBTTagCompound tag = stack.stackTagCompound;
		if(tag != null && tag.hasKey("MicroType"))
			return tag.getInteger("MicroType");
		return stack.getItemDamage();
	}

	public static ItemStack getStackWithPartID(int id) {
		//return new ItemStack(MicroblockSystem.microblockContainerBlock, 1, id);
		ItemStack rv = new ItemStack(MicroblockSystem.microblockContainerBlock, 1, 0);
		rv.stackTagCompound = new NBTTagCompound();
		rv.stackTagCompound.setInteger("MicroType", id);
		return rv;
	}

	public static ItemStack getStackWithPartID(int partID, int stackSize) {
		ItemStack rv = getStackWithPartID(partID);
		rv.stackSize = stackSize;
		return rv;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
		if(par4) {
			par3List.add("Part ID: "+getPartTypeID(par1ItemStack));
			par3List.add("Hex: "+Integer.toHexString(getPartTypeID(par1ItemStack)));
		}
	}
}
