package mods.immibis.microblocks;

import java.util.Random;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.util.Dir;
import mods.immibis.microblocks.api.EnumPosition;
import mods.immibis.microblocks.api.IMicroblockSupporterTile;
import mods.immibis.microblocks.api.Part;
import mods.immibis.microblocks.api.PartType;

public class PartDefault extends Part {

	public PartDefault(PartType<?> type, EnumPosition pos) {
		super(type, pos);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addPartDestroyEffects(IMicroblockSupporterTile tile, EffectRenderer er) {
		byte b0 = 4;
		
		AxisAlignedBB aabb = getBoundingBoxFromPool();
		
		TileEntity t = (TileEntity)tile;
		PartTypeDefault pt = (PartTypeDefault)type;

        for (int j1 = 0; j1 < b0; ++j1)
        {
            for (int k1 = 0; k1 < b0; ++k1)
            {
                for (int l1 = 0; l1 < b0; ++l1)
                {
                    double d0 = ((double)j1 + 0.5D) / (double)b0;
                    double d1 = ((double)k1 + 0.5D) / (double)b0;
                    double d2 = ((double)l1 + 0.5D) / (double)b0;
                    d0 = aabb.minX + d0 * (aabb.maxX - aabb.minX);
                    d1 = aabb.minY + d1 * (aabb.maxY - aabb.minY);
                    d2 = aabb.minZ + d2 * (aabb.maxZ - aabb.minZ);
                    EntityDiggingFX fx = new EntityDiggingFX(t.getWorldObj(), d0+t.xCoord, d1+t.yCoord, d2+t.zCoord, d0 - (aabb.maxX-aabb.minX)/2, d1 - (aabb.maxY-aabb.minY)/2, d2 - (aabb.maxZ-aabb.minZ)/2, pt.modelBlock, pt.modelMeta);
                    pt.colorizeParticle(fx, t);
                    er.addEffect(fx);
                }
            }
        }
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addPartHitEffects(IMicroblockSupporterTile tile, int sideHit, EffectRenderer er) {
		TileEntity t = (TileEntity)tile;
		AxisAlignedBB aabb = getBoundingBoxFromPool();
		PartTypeDefault pt = (PartTypeDefault)type;
		
		Random rand = t.getWorldObj().rand;

		float f = 0.1F;
        double d0 = rand.nextDouble() * (aabb.maxX - aabb.minX - f*2) + f + aabb.minX;
        double d1 = rand.nextDouble() * (aabb.maxY - aabb.minY - f*2) + f + aabb.minY;
        double d2 = rand.nextDouble() * (aabb.maxZ - aabb.minZ - f*2) + f + aabb.minZ;
        
        switch(sideHit) {
        case Dir.NX: d0 = aabb.minX - f; break;
        case Dir.PX: d0 = aabb.maxX + f; break;
        case Dir.NY: d1 = aabb.minY - f; break;
        case Dir.PY: d1 = aabb.maxY + f; break;
        case Dir.NZ: d2 = aabb.minZ - f; break;
        case Dir.PZ: d2 = aabb.maxZ + f; break;
        }
        
        EntityDiggingFX fx = (EntityDiggingFX)(new EntityDiggingFX(t.getWorldObj(), d0+t.xCoord, d1+t.yCoord, d2+t.zCoord, 0.0D, 0.0D, 0.0D, pt.modelBlock, pt.modelMeta, sideHit)).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F);
        pt.colorizeParticle(fx, t);
        er.addEffect(fx);
	}
	
}
