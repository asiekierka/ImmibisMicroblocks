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
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;

public class PartTypeLeaves extends PartTypeDefault {

	public PartTypeLeaves(int id, EnumPartClass clazz, double size, String unlocalizedNameFormat, String unlocalizedBlockName, Block modelBlock, int modelMeta) {
		super(id, clazz, size, unlocalizedNameFormat, unlocalizedBlockName, modelBlock, modelMeta);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	protected void renderFaces(RenderBlocks rb, int bx, int by, int bz, AxisAlignedBB aabb, AxisAlignedBB rbb, boolean nx, boolean ny, boolean nz, boolean px, boolean py, boolean pz, EnumPosition pos) {
		
		int grassColour =
			(rb != null && rb.blockAccess != null)
			? getLeafColour(rb.blockAccess, bx, by, bz, modelMeta)
			: Blocks.leaves.getRenderColor(modelMeta); // item colour
			
		double r = (grassColour >> 16 & 255) / 255.0;
        double g = (grassColour >> 8 & 255) / 255.0;
        double b = (grassColour & 255) / 255.0;
		
		tymin = 1 - tymax;
		tymax = 1;
		
		if(nz) {
			setTex(Dir.NZ);
			setBaseColour(0.8*r, 0.8*g, 0.8*b);
			getBrightnessZFace(rb, bx, by, bz, rbb.minZ, -1);
			rawRenderFaceNZ(aabb, rbb);
		}
		
		if(ny) {
			setTex(Dir.NY);
			setBaseColour(0.5*r, 0.5*g, 0.5*b);
			getBrightnessYFace(rb, bx, by, bz, rbb.minY, -1);
			rawRenderFaceNY(aabb, rbb);
		}
		
		if(nx) {
			setTex(Dir.NX);
			setBaseColour(0.6*r, 0.6*g, 0.6*b);
			getBrightnessXFace(rb, bx, by, bz, rbb.minX, -1);
			rawRenderFaceNX(aabb, rbb);
		}
		
		if(pz) {
			setTex(Dir.PZ);
			setBaseColour(0.8*r, 0.8*g, 0.8*b);
			getBrightnessZFace(rb, bx, by, bz, rbb.maxZ, 1);
			rawRenderFacePZ(aabb, rbb);
		}
		
		if(py) {
			setTex(Dir.PY);
			setBaseColour(r, g, b);
			getBrightnessYFace(rb, bx, by, bz, rbb.maxY, 1);
			rawRenderFacePY(aabb, rbb);
		}
		
		if(px) {
			setTex(Dir.PX);
			setBaseColour(0.6*r, 0.6*g, 0.6*b);
			getBrightnessXFace(rb, bx, by, bz, rbb.maxX, 1);
			rawRenderFacePX(aabb, rbb);
		}
	}

	private int getLeafColour(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int l) {
		if ((l & 3) == 1)
        {
            return ColorizerFoliage.getFoliageColorPine();
        }
        else if ((l & 3) == 2)
        {
            return ColorizerFoliage.getFoliageColorBirch();
        }
        else
        {
            int i1 = 0;
            int j1 = 0;
            int k1 = 0;

            for (int l1 = -1; l1 <= 1; ++l1)
            {
                for (int i2 = -1; i2 <= 1; ++i2)
                {
                    int j2 = par1IBlockAccess.getBiomeGenForCoords(par2 + i2, par4 + l1).getBiomeFoliageColor(par2, par3, par4);
                    i1 += (j2 & 16711680) >> 16;
                    j1 += (j2 & 65280) >> 8;
                    k1 += j2 & 255;
                }
            }

            return (i1 / 9 & 255) << 16 | (j1 / 9 & 255) << 8 | k1 / 9 & 255;
        }
	}

	@Override
	@SideOnly(Side.CLIENT)
	void colorizeParticle(EntityDiggingFX fx, TileEntity te) {
		fx.applyColourMultiplier(te.xCoord, te.yCoord, te.zCoord);
	}
}
