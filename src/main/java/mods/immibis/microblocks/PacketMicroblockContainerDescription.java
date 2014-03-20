package mods.immibis.microblocks;


import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import mods.immibis.core.api.multipart.ICoverSystem;
import mods.immibis.core.api.multipart.IMultipartTile;
import mods.immibis.core.api.net.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketMicroblockContainerDescription implements IPacket {
	
	public int x, y, z;
	public byte[] data;

	@Override
	public void read(ByteBuf in) throws IOException {
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
		int len = in.readShort();
		data = new byte[len];
		in.readBytes(data);
	}

	@Override
	public void write(ByteBuf out) throws IOException {
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
		out.writeShort(data.length);
		out.writeBytes(data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onReceived(EntityPlayer source) {
		World w = Minecraft.getMinecraft().theWorld;
		TileEntity te = w.getTileEntity(x, y, z);
		if(te instanceof IMultipartTile) {
			ICoverSystem ci = ((IMultipartTile)te).getCoverSystem();
			if(ci instanceof MicroblockCoverSystem)
				((MicroblockCoverSystem)ci).readDescriptionBytes(data, 0);
		}
	}

}
