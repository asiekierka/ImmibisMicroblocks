package mods.immibis.microblocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.util.Dir;
import mods.immibis.microblocks.api.EnumPartClass;
import mods.immibis.microblocks.api.EnumPosition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class PartTypeGrass extends PartTypeDefault {

	public PartTypeGrass(int id, EnumPartClass clazz, double size, String unlocalizedNameFormat, String unlocalizedBlockName, Block modelBlock, int modelMeta) {
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
		
		boolean fancyGrass = RenderBlocks.fancyGrass && (rb == null || !rb.hasOverrideBlockTexture());
		
		boolean dirtOnTop = false;
		
		if(getPartClass() != EnumPartClass.HollowPanel || pos == EnumPosition.FaceNY || pos == EnumPosition.FacePY) {
			// for side hollow panels, don't shift the texture vertically,
			// because then each segment of the hollow panel gets a different texture shift and it looks stupid
			// (the grassy top part of the side texture renders in 4 different places)
			
			tymin = 1 - (tymax - tymin);
			tymax = 1;
		
		} else {
			if(rbb.maxY < 0.5) {
				// when rendering the bottom part (facing up) of the inside cutout section,
				// use a dirt texture instead of a grass texture.
				dirtOnTop = true;
			}
		}
		
		if(nz) {
			setTex(Dir.NZ);
			setBaseColour(0.8, 0.8, 0.8);
			getBrightnessZFace(rb, bx, by, bz, rbb.minZ, -1);
			rawRenderFaceNZ(aabb, rbb);
			
			if(fancyGrass) {
				setBaseColour(0.8*r, 0.8*g, 0.8*b);
				setTex(BlockGrass.getIconSideOverlay());
				rawRenderFaceNZ(aabb, rbb);
			}
		}
		
		if(ny) {
			setTex(Dir.NY);
			setBaseColour(0.5, 0.5, 0.5);
			getBrightnessYFace(rb, bx, by, bz, rbb.minY, -1);
			rawRenderFaceNY(aabb, rbb);
		}
		
		if(nx) {
			setTex(Dir.NX);
			setBaseColour(0.6, 0.6, 0.6);
			getBrightnessXFace(rb, bx, by, bz, rbb.minX, -1);
			rawRenderFaceNX(aabb, rbb);
			
			if(fancyGrass) {
				setBaseColour(0.8*r, 0.8*g, 0.8*b);
				setTex(BlockGrass.getIconSideOverlay());
				rawRenderFaceNX(aabb, rbb);
			}
		}
		
		if(pz) {
			setTex(Dir.PZ);
			setBaseColour(0.8, 0.8, 0.8);
			getBrightnessZFace(rb, bx, by, bz, rbb.maxZ, 1);
			rawRenderFacePZ(aabb, rbb);
			
			if(fancyGrass) {
				setBaseColour(0.8*r, 0.8*g, 0.8*b);
				setTex(BlockGrass.getIconSideOverlay());
				rawRenderFacePZ(aabb, rbb);
			}
		}
		
		if(py) {
			if(dirtOnTop) {
				setTex(Dir.NY);
				setBaseColour(1, 1, 1);
			} else {
				setTex(Dir.PY);
				setBaseColour(r, g, b);
			}
			getBrightnessYFace(rb, bx, by, bz, rbb.maxY, 1);
			rawRenderFacePY(aabb, rbb);
		}
		
		if(px) {
			setTex(Dir.PX);
			setBaseColour(0.6, 0.6, 0.6);
			getBrightnessXFace(rb, bx, by, bz, rbb.maxX, 1);
			rawRenderFacePX(aabb, rbb);
			
			if(fancyGrass) {
				setBaseColour(0.8*r, 0.8*g, 0.8*b);
				setTex(BlockGrass.getIconSideOverlay());
				rawRenderFacePX(aabb, rbb);
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	void colorizeParticle(EntityDiggingFX fx, TileEntity te) {
		fx.applyColourMultiplier(te.xCoord, te.yCoord, te.zCoord);
	}

}
