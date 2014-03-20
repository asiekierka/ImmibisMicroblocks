package mods.immibis.microblocks.crossmod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import mods.immibis.microblocks.MicroblockSystem;
import mods.immibis.microblocks.api.IMicroblockPermissionHandler;
import mods.immibis.microblocks.api.IMicroblockSupporterTile;
import mods.immibis.microblocks.api.Part;

public class MCPCMicroblockPermissionHandler implements IMicroblockPermissionHandler {
	
	// public boolean World.canPlaceMultipart(Block block, int x, int y, int z)
	private static Method canPlaceMultipart;
	
	static {
		try {
			canPlaceMultipart = World.class.getMethod("canPlaceMultipart", Block.class, int.class, int.class, int.class);
		} catch(NoSuchMethodException e) {
			canPlaceMultipart = null;
		}
	}
	
	public static final boolean TEST = false;
	
	public static boolean isApplicable() {
		return canPlaceMultipart != null || TEST;
	}

	@Override
	public boolean doesPreventPlacing(EntityPlayer player, World w, int x, int y, int z, Part p) {
		if(TEST)
			return !w.isRemote;
		if(canPlaceMultipart == null)
			return false;
		try {
			return !(Boolean)canPlaceMultipart.invoke(w, MicroblockSystem.microblockContainerBlock, x, y, z);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean doesPreventBreaking(EntityPlayer player, World w, int x, int y, int z, IMicroblockSupporterTile tile, Part p) {
		return false;
	}
}
