package mods.immibis.microblocks;

import mods.immibis.core.api.APILocator;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;

/**
 * Used for transformed blocks that don't normally have tile entities e.g. torches
 */
public class TileDummy extends TileEntity {
	public static Void ImmibisMicroblocks_TransformableTileEntityMarker;
	
	@Override public boolean canUpdate() {return false;}

	public void removeTile() {
		PacketDummyTEDestroy packet = new PacketDummyTEDestroy();
		packet.x = xCoord; packet.y = yCoord; packet.z = zCoord;
		APILocator.getNetManager().sendToClientDimension(packet, worldObj.provider.dimensionId);
		
		worldObj.setTileEntity(xCoord, yCoord, zCoord, null);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
	}
	
	@Override
	public Packet getDescriptionPacket() {
		return APILocator.getNetManager().wrap(new PacketDummyTEDesc(xCoord, yCoord, zCoord));
	}
}
