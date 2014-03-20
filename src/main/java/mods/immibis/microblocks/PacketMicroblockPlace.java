package mods.immibis.microblocks;


import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import mods.immibis.core.api.net.IPacket;
import mods.immibis.microblocks.api.EnumPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PacketMicroblockPlace implements IPacket {
	
	public int x, y, z, posid, sideClicked;

	public PacketMicroblockPlace(int x, int y, int z, int posid, int sideClicked) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.posid = posid;
		this.sideClicked = sideClicked;
	}
	
	public PacketMicroblockPlace() {
		this(0, 0, 0, 0, 0);
	}

	@Override
	public void read(ByteBuf in) throws IOException {
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
		posid = in.readInt();
		sideClicked = in.readInt();
	}

	@Override
	public void write(ByteBuf out) throws IOException {
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
		out.writeInt(posid);
		out.writeInt(sideClicked);
	}

	@Override
	public void onReceived(EntityPlayer source) {
		if(source != null) {
			if(posid < 0 || posid >= EnumPosition.values().length) {
				System.out.println("wrong position");
				source.worldObj.markBlockForUpdate(x, y, z);
				return;
			}
			EnumPosition pos = EnumPosition.values()[posid];
			ItemStack h = source.getCurrentEquippedItem();
			if(h == null || !(h.getItem() instanceof ItemMicroblock)) {
				System.out.println("wrong item equipped");
				source.worldObj.markBlockForUpdate(x, y, z);
				return;
			}
			ItemMicroblock i = (ItemMicroblock)h.getItem();

			if(i.placeInBlock(source.worldObj, x, y, z, pos, h, true, source, true, sideClicked) && !source.capabilities.isCreativeMode) {
				h.stackSize--;
				if(h.stackSize == 0)
					source.destroyCurrentEquippedItem();
			}
			
			source.worldObj.markBlockForUpdate(x, y, z);
		}
	}
}
