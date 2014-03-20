package mods.immibis.microblocks;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import mods.immibis.core.api.net.IPacket;

public class PacketDummyTEDestroy implements IPacket {
	public int x, y, z;
	
	@SideOnly(Side.CLIENT)
	@Override
	public void onReceived(EntityPlayer source) {
		if(source == null) {
			Minecraft.getMinecraft().theWorld.setTileEntity(x, y, z, null);
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
