package mods.immibis.microblocks;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import mods.immibis.core.api.net.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketDummyTEDesc implements IPacket {
	public int x, y, z;
	
	public PacketDummyTEDesc() {}
	public PacketDummyTEDesc(int x, int y, int z) {this.x=x; this.y=y; this.z=z;}
	
	
	@SideOnly(Side.CLIENT)
	@Override
	public void onReceived(EntityPlayer source) {
		if(source == null) {
			Minecraft.getMinecraft().theWorld.setTileEntity(x, y, z, new TileDummy());
		}
	}
	
	@Override
	public void read(ByteBuf in) throws IOException {
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
	}
	
	@Override
	public void write(ByteBuf out) throws IOException {
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
	}
}
