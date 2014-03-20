package mods.immibis.microblocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.util.Dir;
import mods.immibis.microblocks.api.EnumPartClass;
import mods.immibis.microblocks.api.EnumPosition;
import net.minecraft.block.Block;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class PartTypeFullGrass extends PartTypeDefault {

	public PartTypeFullGrass(int id, EnumPartClass clazz, double size, String unlocalizedNameFormat, String unlocalizedBlockName, Block modelBlock, int modelMeta) {
		super(id, clazz, size, unlocalizedNameFormat, unlocalizedBlockName, modelBlock, modelMeta);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void renderFaces(RenderBlocks rb, int bx, int by, int bz, AxisAlignedBB aabb, AxisAlignedBB rbb, boolean nx, boolean ny, boolean nz, boolean px, boolean py, boolean pz, EnumPosition pos) {
		
		int grassColour =
			(rb != null && rb.blockAccess != null)
			? Blocks.grass.colorMultiplier(rb.blockAccess, bx, by, bz)
			: Blocks.grass.getBlockColor(); // item colour
			
		double r = (grassColour >> 16 & 255) / 255.0;
        double g = (grassColour >> 8 & 255) / 255.0;
        double b = (grassColour & 255) / 255.0;
		
        setTex(Dir.PY);
        
        if(nz) {
			setBaseColour(0.8*r, 0.8*g, 0.8*b);
			getBrightnessZFace(rb, bx, by, bz, rbb.minZ, -1);
			rawRenderFaceNZ(aabb, rbb);
		}
		
		if(ny) {
			setBaseColour(0.5*r, 0.5*g, 0.5*b);
			getBrightnessYFace(rb, bx, by, bz, rbb.minY, -1);
			rawRenderFaceNY(aabb, rbb);
		}
		
		if(nx) {
			setBaseColour(0.6*r, 0.6*g, 0.6*b);
			getBrightnessXFace(rb, bx, by, bz, rbb.minX, -1);
			rawRenderFaceNX(aabb, rbb);
		}
		
		if(pz) {
			setBaseColour(0.8*r, 0.8*g, 0.8*b);
			getBrightnessZFace(rb, bx, by, bz, rbb.maxZ, 1);
			rawRenderFacePZ(aabb, rbb);
		}
		
		if(py) {
			setBaseColour(r, g, b);
			getBrightnessYFace(rb, bx, by, bz, rbb.maxY, 1);
			rawRenderFacePY(aabb, rbb);
		}
		
		if(px) {
			setBaseColour(0.6*r, 0.6*g, 0.6*b);
			getBrightnessXFace(rb, bx, by, bz, rbb.maxX, 1);
			rawRenderFacePX(aabb, rbb);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	void colorizeParticle(EntityDiggingFX fx, TileEntity te) {
		fx.applyColourMultiplier(te.xCoord, te.yCoord, te.zCoord);
	}

}
