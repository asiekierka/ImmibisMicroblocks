package mods.immibis.microblocks.coremod;

import java.util.Collections;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import mods.immibis.core.api.APILocator;

import mods.immibis.core.api.multipart.IMultipartSystem;
import mods.immibis.core.api.multipart.IMultipartTile;
import mods.immibis.core.api.multipart.IPartContainer;
import mods.immibis.core.api.multipart.IPartContainer2;
import mods.immibis.core.api.multipart.PartCoordinates;

public abstract class MSTHooks {
	private MSTHooks() {}
	
	private static IMultipartSystem ims;
	
	public static void init() {
		ims = APILocator.getMultipartSystem();
	}
	
	public static int overrideRenderType(int base) {
		if(ims == null) return base;
		
		boolean _3d = RenderBlocks.renderItemIn3d(base);
		return ims.overrideRenderType(base, _3d);
	}
	
	public static void onBlockClicked(World w, int x, int y, int z, EntityPlayer ply) {
		if(ims != null)
			ims.onBlockClicked(w, x, y, z, ply);
	}

	public static float getPlayerRelativeBlockHardness(EntityPlayer ply, World w, int x, int y, int z) {
		if(ims == null)
			return -1;
		return ims.getPlayerRelativeBlockHardness(ply, w, x, y, z);
	}

	public static void renderBlock(RenderBlocks rb, Block block, int xCoord, int yCoord, int zCoord) {
		if(ims != null)
			ims.renderBlockInWorldUsingOverriddenRenderType(rb, block, xCoord, yCoord, zCoord);
	}

	public static List<ItemStack> getDrops() {
		if(ims == null)
			return Collections.emptyList();
		return ims.getDrops();
	}

	public static void onRemovedByPlayer(World w, EntityPlayer pl, int x, int y, int z) {
		if(ims != null)
			ims.onRemoveBlockByPlayer(w, pl, x, y, z);
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean addHitEffects(World w, MovingObjectPosition ray, EffectRenderer er) {
		if(ims == null)
			return false;
		
		PartCoordinates p = ims.getBreakingPart(Minecraft.getMinecraft().thePlayer);
		if(p == null)
			return false; // something weird happened
		
		if(p.x != ray.blockX || p.y != ray.blockY || p.z != ray.blockZ)
			return true; // something weird happened
		
		if(!p.isCoverSystemPart)
			return false; // let transformed block handle it
		
		TileEntity t = w.getTileEntity(p.x, p.y, p.z);
		if(!(t instanceof IMultipartTile))
			return false; // no tile entity? obviously doesn't have microblocks
		
		IPartContainer ipc = ((IMultipartTile)t).getCoverSystem();
		if(!(ipc instanceof IPartContainer2))
			return true;
		
		((IPartContainer2)ipc).addPartHitEffects(p.part, ray.sideHit, er);
		
        return true;
	}
	
	public static void onMicroblocksChanged(TileEntity te) {
		te.getWorldObj().markBlockForUpdate(te.xCoord, te.yCoord, te.zCoord);
		te.getWorldObj().notifyBlocksOfNeighborChange(te.xCoord, te.yCoord, te.zCoord, te.getBlockType());
	}
}
