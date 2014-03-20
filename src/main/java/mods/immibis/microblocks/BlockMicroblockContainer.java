package mods.immibis.microblocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.multipart.util.BlockMultipartBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockMicroblockContainer extends BlockMultipartBase {
	protected BlockMicroblockContainer(Material mat) {
		super(mat);
		setBlockName("immibis_microblocks.container");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileMicroblockContainer();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
	}
	
	@Override
	public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4)
    {
        Block block = par1World.getBlock(par2, par3, par4);
        return block == null || block == this || block.isReplaceable(par1World, par2, par3, par4);
    }
}
