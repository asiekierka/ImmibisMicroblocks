package mods.immibis.microblocks;

import java.io.DataInput;
import java.lang.reflect.Field;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import mods.immibis.core.api.util.Dir;
import mods.immibis.microblocks.api.EnumAxisPosition;
import mods.immibis.microblocks.api.EnumPartClass;
import mods.immibis.microblocks.api.EnumPosition;
import mods.immibis.microblocks.api.MicroblockAPIUtils;
import mods.immibis.microblocks.api.Part;
import mods.immibis.microblocks.api.PartType;

public class PartTypeDefault implements PartType<Part> {
	@Override
	public boolean canHarvest(EntityPlayer ply, Part part) {
		return modelBlock.canHarvestBlock(ply, modelMeta);
	}
	
	@Override
	public EnumPartClass getPartClass() {
		return clazz;
	}

	@Override
	public double getSize() {
		return size;
	}

	private String getLocalizedName2(ItemStack stack) {
		return I18n.format(unlocalizedNameFormat, I18n.format(unlocalizedBlockName));
	}
	
	@Override
	public String getLocalizedName(ItemStack stack) {
		try {
			return getLocalizedName2(stack);
		} catch(Error e) {
			// on a server
			return unlocalizedNameFormat+" "+unlocalizedBlockName;
		}
	}
	
	@Override
	public int getID() {
		return id;
	}

	@Override
	public ItemStack getDroppedStack(Part part, EntityPlayer ply) {
		return MicroblockAPIUtils.getMicroblockSystem().partTypeIDToItemStack(id, 1);
	}
	
	@Override
	public float getPlayerRelativeHardness(Part part, EntityPlayer ply) {

		float hardness;
		
		try {
			hardness = modelBlock.getBlockHardness(null, 0, 0, 0);
		} catch(Throwable t) {
			return 0.1f;
		}
		
		if(hardness < 0)
			return 0;
		if(!canHarvest(ply, part))
			return 0.01F / hardness;
		// modelMeta - TODO
		return ply.getCurrentPlayerStrVsBlock(modelBlock, false) / hardness / 30F;
	}
	
	@Override
	public ItemStack getPickItem(Part part) {
		return getDroppedStack(part, null);
	}
	
	@Override
	public Part createPart(EnumPosition pos) {
		return new PartDefault(this, pos);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Part createPart(EnumPosition pos, DataInput data) {
		return createPart(pos);
	}
	
	@Override
	public Part createPart(EnumPosition pos, NBTTagCompound c) {
		return createPart(pos);
	}
	
	private final EnumPartClass clazz;
	private final double size;
	private final String unlocalizedNameFormat, unlocalizedBlockName;
	private final int id;
	
	public PartTypeDefault(int id, EnumPartClass clazz, double size, String unlocalizedNameFormat, String unlocalizedBlockName, Block modelBlock, int modelMeta) {
		this.clazz = clazz;
		this.size = size;
		this.id = id;
		this.unlocalizedNameFormat = unlocalizedNameFormat;
		this.unlocalizedBlockName = unlocalizedBlockName;
		this.modelBlock = modelBlock;
		this.modelMeta = modelMeta;
	}
	
	public Block modelBlock;
	public int modelMeta;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private int alpha = 255;
	
	@SideOnly(Side.CLIENT)
	private float uMin, uMax, vMin, vMax;
	@SideOnly(Side.CLIENT)
	private IIcon icon, overrideIcon;
	
	@SideOnly(Side.CLIENT)
	protected void setTex(IIcon i) {
		if(overrideIcon != null)
			i = overrideIcon;
		icon = i;
		uMin = i.getMinU();
		uMax = i.getMaxU();
		vMin = i.getMinV();
		vMax = i.getMaxV();
	}
	
	@SideOnly(Side.CLIENT)
	protected void setTex(int side) {
		setTex(modelBlock.getIcon(side, modelMeta));
	}
	
	Field rbf = null;
	
	@SideOnly(Side.CLIENT)
	private void getRenderBlocks() {
		String[] names =  {"renderBlocksRg", "field_147592_B", "B"};
		for(String name: names) {
			try {
				Field f = RenderGlobal.class.getDeclaredField(name);
				if(f == null || !(f.getType().isAssignableFrom(RenderBlocks.class))) continue;
				if(!f.isAccessible()) f.setAccessible(true);
				rbf = f;
				return;
			} catch(Exception e) { }
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderPreview(RenderGlobal context, EnumPosition pos, ItemStack stack) {
		RenderBlocks rb = null;
		if(rbf == null) getRenderBlocks();
		
		try {
			rb = (RenderBlocks) rbf.get(context);
		} catch(Exception e) {
			return;
		}
		
		GL11.glEnable(GL11.GL_BLEND);
		alpha = 127;
		
		overrideIcon = null;
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		
		Tessellator t = Tessellator.instance;
		t.startDrawingQuads();
		renderPartWorld(rb, createPart(pos), 0, 0, 0, new boolean[6]);
		//renderQuads(t, pos);
		t.draw();
		
		alpha = 255;
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	private static final double HOLLOW_EDGE_SIZE = 0.25;
	
	@SideOnly(Side.CLIENT) private void vertXY(Tessellator t, double x, double y, double z) {	
		t.addVertexWithUV(x, y, z, uMin + (uMax-uMin)*x, vMax + (vMin - vMax)*y);
	}
	
	@SideOnly(Side.CLIENT) private void vertXZ(Tessellator t, double x, double y, double z) {	
		t.addVertexWithUV(x, y, z, uMin + (uMax-uMin)*x, vMin + (vMax - vMin)*z);
	}
	
	@SideOnly(Side.CLIENT) private void vertYZ(Tessellator t, double x, double y, double z) {	
		t.addVertexWithUV(x, y, z, uMin + (uMax-uMin)*z, vMax + (vMin - vMax)*y);
	}
	
	@SideOnly(Side.CLIENT) private void renderHollowPanelYZ(Tessellator t, AxisAlignedBB aabb) {
		setTex(modelBlock.getIcon(Dir.NX, modelMeta));
		t.setNormal(-1, 0, 0);
		
		vertYZ(t, aabb.minX, 0+HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.minX, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.minX, 1, 0);
		vertYZ(t, aabb.minX, 0, 0);
		
		vertYZ(t, aabb.minX, 0, 1);
		vertYZ(t, aabb.minX, 1, 1);
		vertYZ(t, aabb.minX, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.minX, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		
		vertYZ(t, aabb.minX, 0, 1);
		vertYZ(t, aabb.minX, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.minX, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.minX, 0, 0);
		
		vertYZ(t, aabb.minX, 1, 0);
		vertYZ(t, aabb.minX, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.minX, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.minX, 1, 1);
		
		setTex(modelBlock.getIcon(Dir.PX, modelMeta));
		t.setNormal(1, 0, 0);
		
		vertYZ(t, aabb.maxX, 0, 0);
		vertYZ(t, aabb.maxX, 1, 0);
		vertYZ(t, aabb.maxX, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.maxX, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		
		vertYZ(t, aabb.maxX, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.maxX, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.maxX, 1, 1);
		vertYZ(t, aabb.maxX, 0, 1);
		
		vertYZ(t, aabb.maxX, 0, 0);
		vertYZ(t, aabb.maxX, HOLLOW_EDGE_SIZE, 0+HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.maxX, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.maxX, 0, 1);
		
		vertYZ(t, aabb.maxX, 1, 1);
		vertYZ(t, aabb.maxX, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.maxX, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		vertYZ(t, aabb.maxX, 1, 0);
		
		setTex(modelBlock.getIcon(Dir.NY, modelMeta));
		t.setNormal(0, -1, 0);
		vertXZ(t, aabb.minX, 0, 0);
		vertXZ(t, aabb.maxX, 0, 0);
		vertXZ(t, aabb.maxX, 0, 1);
		vertXZ(t, aabb.minX, 0, 1);
		
		vertXZ(t, aabb.minX, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		vertXZ(t, aabb.maxX, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		vertXZ(t, aabb.maxX, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		vertXZ(t, aabb.minX, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		
		setTex(modelBlock.getIcon(Dir.PY, modelMeta));
		t.setNormal(0, 1, 0);
		vertXZ(t, aabb.minX, 1, 1);
		vertXZ(t, aabb.maxX, 1, 1);
		vertXZ(t, aabb.maxX, 1, 0);
		vertXZ(t, aabb.minX, 1, 0);
		
		vertXZ(t, aabb.minX, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		vertXZ(t, aabb.maxX, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		vertXZ(t, aabb.maxX, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		vertXZ(t, aabb.minX, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		
		setTex(modelBlock.getIcon(Dir.NZ, modelMeta));
		t.setNormal(0, 0, -1);
		vertXY(t, aabb.minX, 0, 0);
		vertXY(t, aabb.minX, 1, 0);
		vertXY(t, aabb.maxX, 1, 0);
		vertXY(t, aabb.maxX, 0, 0);
		
		vertXY(t, aabb.minX, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		vertXY(t, aabb.minX, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		vertXY(t, aabb.maxX, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		vertXY(t, aabb.maxX, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE);
		
		setTex(modelBlock.getIcon(Dir.PZ, modelMeta));
		t.setNormal(0, 0, 1);
		vertXY(t, aabb.minX, 1, 1);
		vertXY(t, aabb.minX, 0, 1);
		vertXY(t, aabb.maxX, 0, 1);
		vertXY(t, aabb.maxX, 1, 1);
		
		vertXY(t, aabb.minX, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		vertXY(t, aabb.minX, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		vertXY(t, aabb.maxX, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
		vertXY(t, aabb.maxX, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE);
	}
	
	@SideOnly(Side.CLIENT) private void renderHollowPanelXZ(Tessellator t, AxisAlignedBB aabb) {
		setTex(Dir.NY);
		t.setNormal(0, -1, 0);
		vertXZ(t, HOLLOW_EDGE_SIZE, aabb.minY, HOLLOW_EDGE_SIZE);
		vertXZ(t, HOLLOW_EDGE_SIZE, aabb.minY, 1-HOLLOW_EDGE_SIZE);
		vertXZ(t, 0, aabb.minY, 1);
		vertXZ(t, 0, aabb.minY, 0);
		
		vertXZ(t, 1-HOLLOW_EDGE_SIZE, aabb.minY, 1-HOLLOW_EDGE_SIZE);
		vertXZ(t, 1-HOLLOW_EDGE_SIZE, aabb.minY, HOLLOW_EDGE_SIZE);
		vertXZ(t, 1, aabb.minY, 0);
		vertXZ(t, 1, aabb.minY, 1);
		
		vertXZ(t, 0, aabb.minY, 0);
		vertXZ(t, 1, aabb.minY, 0);
		vertXZ(t, 1-HOLLOW_EDGE_SIZE, aabb.minY, HOLLOW_EDGE_SIZE);
		vertXZ(t, HOLLOW_EDGE_SIZE, aabb.minY, HOLLOW_EDGE_SIZE);
		
		vertXZ(t, HOLLOW_EDGE_SIZE, aabb.minY, 1-HOLLOW_EDGE_SIZE);
		vertXZ(t, 1-HOLLOW_EDGE_SIZE, aabb.minY, 1-HOLLOW_EDGE_SIZE);
		vertXZ(t, 1, aabb.minY, 1);
		vertXZ(t, 0, aabb.minY, 1);
		
		setTex(Dir.PY);
		t.setNormal(0, 1, 0);
		vertXZ(t, 0, aabb.maxY, 0);
		vertXZ(t, 0, aabb.maxY, 1);
		vertXZ(t, HOLLOW_EDGE_SIZE, aabb.maxY, 1-HOLLOW_EDGE_SIZE);
		vertXZ(t, HOLLOW_EDGE_SIZE, aabb.maxY, HOLLOW_EDGE_SIZE);
		
		vertXZ(t, 1, aabb.maxY, 1);
		vertXZ(t, 1, aabb.maxY, 0);
		vertXZ(t, 1-HOLLOW_EDGE_SIZE, aabb.maxY, HOLLOW_EDGE_SIZE);
		vertXZ(t, 1-HOLLOW_EDGE_SIZE, aabb.maxY, 1-HOLLOW_EDGE_SIZE);
		
		vertXZ(t, HOLLOW_EDGE_SIZE, aabb.maxY, HOLLOW_EDGE_SIZE);
		vertXZ(t, 1-HOLLOW_EDGE_SIZE, aabb.maxY, HOLLOW_EDGE_SIZE);
		vertXZ(t, 1, aabb.maxY, 0);
		vertXZ(t, 0, aabb.maxY, 0);
		
		vertXZ(t, 0, aabb.maxY, 1);
		vertXZ(t, 1, aabb.maxY, 1);
		vertXZ(t, 1-HOLLOW_EDGE_SIZE, aabb.maxY, 1-HOLLOW_EDGE_SIZE);
		vertXZ(t, HOLLOW_EDGE_SIZE, aabb.maxY, 1-HOLLOW_EDGE_SIZE);
		
		setTex(Dir.NX);
		t.setNormal(-1, 0, 0);
		vertYZ(t, 0, aabb.minY, 0);
		vertYZ(t, 0, aabb.minY, 1);
		vertYZ(t, 0, aabb.maxY, 1);
		vertYZ(t, 0, aabb.maxY, 0);
		
		vertYZ(t, HOLLOW_EDGE_SIZE, aabb.minY, 1-HOLLOW_EDGE_SIZE);
		vertYZ(t, HOLLOW_EDGE_SIZE, aabb.minY, HOLLOW_EDGE_SIZE);
		vertYZ(t, HOLLOW_EDGE_SIZE, aabb.maxY, HOLLOW_EDGE_SIZE);
		vertYZ(t, HOLLOW_EDGE_SIZE, aabb.maxY, 1-HOLLOW_EDGE_SIZE);
		
		setTex(Dir.PX);
		t.setNormal(1, 0, 0);
		vertYZ(t, 1, aabb.minY, 1);
		vertYZ(t, 1, aabb.minY, 0);
		vertYZ(t, 1, aabb.maxY, 0);
		vertYZ(t, 1, aabb.maxY, 1);
		
		vertYZ(t, 1-HOLLOW_EDGE_SIZE, aabb.maxY, 1-HOLLOW_EDGE_SIZE);
		vertYZ(t, 1-HOLLOW_EDGE_SIZE, aabb.maxY, HOLLOW_EDGE_SIZE);
		vertYZ(t, 1-HOLLOW_EDGE_SIZE, aabb.minY, HOLLOW_EDGE_SIZE);
		vertYZ(t, 1-HOLLOW_EDGE_SIZE, aabb.minY, 1-HOLLOW_EDGE_SIZE);
		
		setTex(Dir.NZ);
		t.setNormal(0, 0, -1);
		vertXY(t, 0, aabb.minY, 0);
		vertXY(t, 0, aabb.maxY, 0);
		vertXY(t, 1, aabb.maxY, 0);
		vertXY(t, 1, aabb.minY, 0);
		
		vertXY(t, 1-HOLLOW_EDGE_SIZE, aabb.minY, HOLLOW_EDGE_SIZE);
		vertXY(t, 1-HOLLOW_EDGE_SIZE, aabb.maxY, HOLLOW_EDGE_SIZE);
		vertXY(t, HOLLOW_EDGE_SIZE, aabb.maxY, HOLLOW_EDGE_SIZE);
		vertXY(t, HOLLOW_EDGE_SIZE, aabb.minY, HOLLOW_EDGE_SIZE);
		
		setTex(Dir.PZ);
		t.setNormal(0, 0, 1);
		vertXY(t, 1, aabb.minY, 1);
		vertXY(t, 1, aabb.maxY, 1);
		vertXY(t, 0, aabb.maxY, 1);
		vertXY(t, 0, aabb.minY, 1);
		
		vertXY(t, HOLLOW_EDGE_SIZE, aabb.minY, 1-HOLLOW_EDGE_SIZE);
		vertXY(t, HOLLOW_EDGE_SIZE, aabb.maxY, 1-HOLLOW_EDGE_SIZE);
		vertXY(t, 1-HOLLOW_EDGE_SIZE, aabb.maxY, 1-HOLLOW_EDGE_SIZE);
		vertXY(t, 1-HOLLOW_EDGE_SIZE, aabb.minY, 1-HOLLOW_EDGE_SIZE);
	}
		
	@SideOnly(Side.CLIENT) private void renderHollowPanelXY(Tessellator t, AxisAlignedBB aabb) {
		setTex(Dir.NZ);
		t.setNormal(0, 0, -1);
		vertXY(t, 0, 0, aabb.minZ);
		vertXY(t, 0, 1, aabb.minZ);
		vertXY(t, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.minZ);
		vertXY(t, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.minZ);
		
		vertXY(t, 1, 1, aabb.minZ);
		vertXY(t, 1, 0, aabb.minZ);
		vertXY(t, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.minZ);
		vertXY(t, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.minZ);
		
		vertXY(t, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.minZ);
		vertXY(t, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.minZ);
		vertXY(t, 1, 0, aabb.minZ);
		vertXY(t, 0, 0, aabb.minZ);
		
		vertXY(t, 0, 1, aabb.minZ);
		vertXY(t, 1, 1, aabb.minZ);
		vertXY(t, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.minZ);
		vertXY(t, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.minZ);
		
		setTex(Dir.PZ);
		t.setNormal(0, 0, 1);
		vertXY(t, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertXY(t, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertXY(t, 0, 1, aabb.maxZ);
		vertXY(t, 0, 0, aabb.maxZ);
		
		vertXY(t, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertXY(t, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertXY(t, 1, 0, aabb.maxZ);
		vertXY(t, 1, 1, aabb.maxZ);
		
		vertXY(t, 0, 0, aabb.maxZ);
		vertXY(t, 1, 0, aabb.maxZ);
		vertXY(t, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertXY(t, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.maxZ);
		
		vertXY(t, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertXY(t, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertXY(t, 1, 1, aabb.maxZ);
		vertXY(t, 0, 1, aabb.maxZ);
		
		setTex(Dir.NX);
		t.setNormal(-1, 0, 0);
		vertYZ(t, 0, 1, aabb.minZ);
		vertYZ(t, 0, 0, aabb.minZ);
		vertYZ(t, 0, 0, aabb.maxZ);
		vertYZ(t, 0, 1, aabb.maxZ);
		
		vertYZ(t, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.minZ);
		vertYZ(t, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.minZ);
		vertYZ(t, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertYZ(t, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.maxZ);
		
		setTex(Dir.PX);
		t.setNormal(1, 0, 0);
		vertYZ(t, 1, 0, aabb.minZ);
		vertYZ(t, 1, 1, aabb.minZ);
		vertYZ(t, 1, 1, aabb.maxZ);
		vertYZ(t, 1, 0, aabb.maxZ);
		
		vertYZ(t, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertYZ(t, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertYZ(t, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.minZ);
		vertYZ(t, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.minZ);
		
		setTex(Dir.NY);
		t.setNormal(0, -1, 0);
		vertXZ(t, 1, 0, aabb.minZ);
		vertXZ(t, 1, 0, aabb.maxZ);
		vertXZ(t, 0, 0, aabb.maxZ);
		vertXZ(t, 0, 0, aabb.minZ);
		
		vertXZ(t, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.minZ);
		vertXZ(t, HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertXZ(t, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertXZ(t, 1-HOLLOW_EDGE_SIZE, HOLLOW_EDGE_SIZE, aabb.minZ);
		
		setTex(Dir.PY);
		t.setNormal(0, 1, 0);
		vertXZ(t, 0, 1, aabb.minZ);
		vertXZ(t, 0, 1, aabb.maxZ);
		vertXZ(t, 1, 1, aabb.maxZ);
		vertXZ(t, 1, 1, aabb.minZ);
		
		vertXZ(t, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.minZ);
		vertXZ(t, 1-HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertXZ(t, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.maxZ);
		vertXZ(t, HOLLOW_EDGE_SIZE, 1-HOLLOW_EDGE_SIZE, aabb.minZ);
	}
	
	@SideOnly(Side.CLIENT) private void renderQuads(Tessellator t, EnumPosition pos) {
		AxisAlignedBB aabb = Part.getBoundingBoxFromPool(pos, size);
		
		if(getPartClass() == EnumPartClass.HollowPanel)
		{
			if(pos.x != EnumAxisPosition.Span)
				renderHollowPanelYZ(t, aabb);
			else if(pos.y != EnumAxisPosition.Span)
				renderHollowPanelXZ(t, aabb);
			else if(pos.z != EnumAxisPosition.Span)
				renderHollowPanelXY(t, aabb);
			return;
		}
		
		renderAABB(t, aabb, null, 0, 0, 0, null, new boolean[6], pos);/*/
		
		overrideIcon = null;
		
		float i = pos.ordinal() / 100000.0f; // unnoticeable position-dependent inset, to reduce flickering
		
		if(clazz == EnumPartClass.HollowPanel)
		{
			float hes = 0.25f; //(float)HOLLOW_EDGE_SIZE;
			float s = (float)size;
			
			switch(pos)
			{
			case FaceNX:
				renderAABB(render, x, y, z, i, i, i, s, hes, 1-i, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, i, 1-hes, i, s, 1-i, 1-i, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, i, hes, i, s, 1-hes, hes, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, i, hes, 1-hes, s, 1-hes, 1-i, modelBlock, dontRenderFaces);
				break;
			case FacePX:
				renderAABB(render, x, y, z, 1-s, i, i, 1-i, hes, 1-i, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, 1-s, 1-hes, i, 1-i, 1-i, 1-i, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, 1-s, hes, i, 1-i, 1-hes, hes, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, 1-s, hes, 1-hes, 1-i, 1-hes, 1-i, modelBlock, dontRenderFaces);
				break;
			case FaceNY:
				renderAABB(render, x, y, z, i, i, i, hes, s, 1-i, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, 1-hes, i, i, 1-i, s, 1-i, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, hes, i, i, 1-hes, s, hes, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, hes, i, 1-hes, 1-hes, s, 1-i, modelBlock, dontRenderFaces);
				break;
			case FacePY:
				renderAABB(render, x, y, z, i, 1-s, i, hes, 1-i, 1-i, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, 1-hes, 1-s, i, 1-i, 1-i, 1-i, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, hes, 1-s, i, 1-hes, 1-i, hes, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, hes, 1-s, 1-hes, 1-hes, 1-i, 1-i, modelBlock, dontRenderFaces);
				break;
			case FaceNZ:
				renderAABB(render, x, y, z, i, i, i, hes, 1-i, s, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, 1-hes, i, i, 1-i, 1-i, s, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, hes, i, i, 1-hes, hes, s, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, hes, 1-hes, 0, 1-hes, 1-i, s, modelBlock, dontRenderFaces);
				break;
			case FacePZ:
				renderAABB(render, x, y, z, i, i, 1-s, hes, 1-i, 1-i, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, 1-hes, i, 1-s, 1-i, 1-i, 1-i, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, hes, i, 1-s, 1-hes, hes, 1-i, modelBlock, dontRenderFaces);
				renderAABB(render, x, y, z, hes, 1-hes, 1-s, 1-hes, 1-i, 1-i, modelBlock, dontRenderFaces);
				break;
			default:
				// shouldn't happen
				System.err.println("hollow panel placed at invalid position "+p.pos+" in block "+x+","+y+","+z);
			}
		}
		else
		{
			AxisAlignedBB bb = p.getBoundingBoxFromPool();
			if(bb.minX == 0) bb.minX = i;
			if(bb.minY == 0) bb.minY = i;
			if(bb.minZ == 0) bb.minZ = i;
			if(bb.maxX == 1) bb.maxX -= i;
			if(bb.maxY == 1) bb.maxY -= i;
			if(bb.maxZ == 1) bb.maxZ -= i;
			renderAABB(render, x, y, z, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, modelBlock, dontRenderFaces);
		}// */
	}
	
	
	// colour and brightness values for the corners of the face currently being rendered
	@SideOnly(Side.CLIENT) private int colNN, colPN, colNP, colPP;
	@SideOnly(Side.CLIENT) private int briNN, briPN, briNP, briPP;
	
	@SideOnly(Side.CLIENT) private float baseColR, baseColG, baseColB;
	
	@SideOnly(Side.CLIENT) private static double interp(double a, double b, double i) {
		return a + (b - a) * i;
	}
	@SideOnly(Side.CLIENT) private static int rgb_component(double a) {
		if(a < 0)
			return 0;
		if(a > 1)
			return 255;
		return (int)(255 * a);
	}
	@SideOnly(Side.CLIENT) private static int rgb_r(int a) {return (a >> 16) & 255;}
	@SideOnly(Side.CLIENT) private static int rgb_g(int a) {return (a >> 8) & 255;}
	@SideOnly(Side.CLIENT) private static int rgb_b(int a) {return a & 255;}
	@SideOnly(Side.CLIENT) private static int interpRGB(int a, int b, double i) {
		return rgb(interp(rgb_r(a), rgb_r(b), i)/255.0, interp(rgb_g(a), rgb_g(b), i)/255.0, interp(rgb_b(a), rgb_b(b), i)/255.0);
	}
	@SideOnly(Side.CLIENT) private static int scaleRGB(int a, double sc) {
		sc /= 255;
		return rgb(rgb_r(a)*sc, rgb_g(a)*sc, rgb_b(a)*sc);
	}
	@SideOnly(Side.CLIENT) private static int interpBrightness(int a, int b, double i) {
		int AH = a >> 16;
		int AL = a & 65535;
		int BH = b >> 16;
		int BL = b & 65535;
		int H = (int)(AH + (BH - AH) * i);
		int L = (int)(AL + (BL - AL) * i);
		return (H << 16) | L;
	}
	
	@SideOnly(Side.CLIENT) private void setColourAndBrightness(Tessellator t, double x, double y) {
		int col = interpRGB(interpRGB(colNN, colPN, x), interpRGB(colNP, colPP, x), y);
		int bri = interpBrightness(interpBrightness(briNN, briPN, x), interpBrightness(briNP, briPP, x), y);
		t.setColorRGBA_I(col, alpha);
		t.setBrightness(bri);
	}
	
	@SideOnly(Side.CLIENT) public float getAmbientOcclusionLightValue(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		return par1IBlockAccess.getBlock(par2, par3, par4).isNormalCube(par1IBlockAccess, par2, par3, par4) ? 0.2F : 1.0F;
	}
	
	@SideOnly(Side.CLIENT) public int getMixedBrightnessForBlock(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
        return par1IBlockAccess.getLightBrightnessForSkyBlocks(par2, par3, par4, 0 /* emitted light level */);
    }
	
	@SideOnly(Side.CLIENT) public int getAoBrightness(int par4, int par1, int par2, int par3)
    {
        if (par1 == 0)
        {
            par1 = par4;
        }

        if (par2 == 0)
        {
            par2 = par4;
        }

        if (par3 == 0)
        {
            par3 = par4;
        }

        return par1 + par2 + par3 + par4 >> 2 & 16711935;
    }
	@SideOnly(Side.CLIENT)
	protected void getBrightnessXFace(RenderBlocks rb, int bx, int by, int bz, double f_dx, int dir_dx) {
		int dx = (f_dx >= 0.995 ? 1 : f_dx <= 0.005 ? -1 : 0);
        if(rb == null || rb.blockAccess == null) {
			briNN = briNP = briPN = briPP = 0x00F000F0;
		} else if(!Minecraft.isAmbientOcclusionEnabled()) {
			briNN = briNP = briPN = briPP = rb.blockAccess.getLightBrightnessForSkyBlocks(bx+dx, by, bz, 0);
		
		} else {
			bx += dx;
			float shadeNX = getAmbientOcclusionLightValue(rb.blockAccess, bx, by, bz - 1);
			float shadeNY = getAmbientOcclusionLightValue(rb.blockAccess, bx, by - 1, bz);
			float shadePY = getAmbientOcclusionLightValue(rb.blockAccess, bx, by + 1, bz);
			float shadePX = getAmbientOcclusionLightValue(rb.blockAccess, bx, by, bz + 1);
            int briNX = getMixedBrightnessForBlock(rb.blockAccess, bx, by, bz - 1);
            int briNY = getMixedBrightnessForBlock(rb.blockAccess, bx, by - 1, bz);
            int briPY = getMixedBrightnessForBlock(rb.blockAccess, bx, by + 1, bz);
            int briPX = getMixedBrightnessForBlock(rb.blockAccess, bx, by, bz + 1);
            boolean flagPX = rb.blockAccess.getBlock(bx + dir_dx, by, bz + 1).getCanBlockGrass();
            boolean flagNX = rb.blockAccess.getBlock(bx + dir_dx, by, bz - 1).getCanBlockGrass();
            boolean flagPY = rb.blockAccess.getBlock(bx + dir_dx, by + 1, bz).getCanBlockGrass();
            boolean flagNY = rb.blockAccess.getBlock(bx + dir_dx, by - 1, bz).getCanBlockGrass();
            
            float shadeNN = (!flagNX && !flagNY ? shadeNX : getAmbientOcclusionLightValue(rb.blockAccess, bx, by - 1, bz - 1));
            float shadeNP = (!flagNX && !flagPY ? shadeNX : getAmbientOcclusionLightValue(rb.blockAccess, bx, by + 1, bz - 1));
            float shadePN = (!flagPX && !flagNY ? shadePX : getAmbientOcclusionLightValue(rb.blockAccess, bx, by - 1, bz + 1));
            float shadePP = (!flagPX && !flagPY ? shadePX : getAmbientOcclusionLightValue(rb.blockAccess, bx, by + 1, bz + 1));
            int briNN = (!flagNX && !flagNY ? briNX : getMixedBrightnessForBlock(rb.blockAccess, bx, by - 1, bz - 1));
            int briNP = (!flagNX && !flagPY ? briNX : getMixedBrightnessForBlock(rb.blockAccess, bx, by + 1, bz - 1));
            int briPN = (!flagPX && !flagNY ? briPX : getMixedBrightnessForBlock(rb.blockAccess, bx, by - 1, bz + 1));
            int briPP = (!flagPX && !flagPY ? briPX : getMixedBrightnessForBlock(rb.blockAccess, bx, by + 1, bz + 1));
            bx -= dx;
			
            int briThisFace =
            	(dx != 0 || !rb.blockAccess.getBlock(bx + dir_dx, by, bz).isOpaqueCube())
                ? getMixedBrightnessForBlock(rb.blockAccess, bx + dir_dx, by, bz)
                : getMixedBrightnessForBlock(rb.blockAccess, bx, by, bz);
                
            setShading(
            	shadeNN, shadePN, shadeNP, shadePP,
            	shadeNY, shadeNX, shadePY, shadePX,
            	briNN, briPN, briNP, briPP,
            	briNY, briNX, briPY, briPX,
            	briThisFace, rb, bx, by, bz);
		}
	}
	
	@SideOnly(Side.CLIENT)
	protected void getBrightnessYFace(RenderBlocks rb, int bx, int by, int bz, double f_dy, int dir_dy) {
		int dy = (f_dy >= 0.995 ? 1 : f_dy <= 0.005 ? -1 : 0);
        if(rb == null || rb.blockAccess == null) {
			briNN = briNP = briPN = briPP = 0x00F000F0;
		} else if(!Minecraft.isAmbientOcclusionEnabled()) {
			briNN = briNP = briPN = briPP = rb.blockAccess.getLightBrightnessForSkyBlocks(bx, by+dy, bz, 0);
			
		} else {
			by += dy;
			float shadeNX = getAmbientOcclusionLightValue(rb.blockAccess, bx - 1, by, bz);
			float shadeNY = getAmbientOcclusionLightValue(rb.blockAccess, bx, by, bz - 1);
			float shadePY = getAmbientOcclusionLightValue(rb.blockAccess, bx, by, bz + 1);
			float shadePX = getAmbientOcclusionLightValue(rb.blockAccess, bx + 1, by, bz);
            int briNX = getMixedBrightnessForBlock(rb.blockAccess, bx - 1, by, bz);
            int briNY = getMixedBrightnessForBlock(rb.blockAccess, bx, by, bz - 1);
            int briPY = getMixedBrightnessForBlock(rb.blockAccess, bx, by, bz + 1);
            int briPX = getMixedBrightnessForBlock(rb.blockAccess, bx + 1, by, bz);
            boolean flagPX = rb.blockAccess.getBlock(bx + 1, by + dir_dy, bz).getCanBlockGrass();
            boolean flagNX = rb.blockAccess.getBlock(bx - 1, by + dir_dy, bz).getCanBlockGrass();
            boolean flagPY = rb.blockAccess.getBlock(bx, by + dir_dy, bz + 1).getCanBlockGrass();
            boolean flagNY = rb.blockAccess.getBlock(bx, by + dir_dy, bz - 1).getCanBlockGrass();
            
            float shadeNN = (!flagNX && !flagNY ? shadeNX : getAmbientOcclusionLightValue(rb.blockAccess, bx - 1, by, bz - 1));
            float shadeNP = (!flagNX && !flagPY ? shadeNX : getAmbientOcclusionLightValue(rb.blockAccess, bx - 1, by, bz + 1));
            float shadePN = (!flagPX && !flagNY ? shadePX : getAmbientOcclusionLightValue(rb.blockAccess, bx + 1, by, bz - 1));
            float shadePP = (!flagPX && !flagPY ? shadePX : getAmbientOcclusionLightValue(rb.blockAccess, bx + 1, by, bz + 1));
            int briNN = (!flagNX && !flagNY ? briNX : getMixedBrightnessForBlock(rb.blockAccess, bx - 1, by, bz - 1));
            int briNP = (!flagNX && !flagPY ? briNX : getMixedBrightnessForBlock(rb.blockAccess, bx - 1, by, bz + 1));
            int briPN = (!flagPX && !flagNY ? briPX : getMixedBrightnessForBlock(rb.blockAccess, bx + 1, by, bz - 1));
            int briPP = (!flagPX && !flagPY ? briPX : getMixedBrightnessForBlock(rb.blockAccess, bx + 1, by, bz + 1));
            by -= dy;
			
            int briThisFace =
            	(dy != 0 || !rb.blockAccess.getBlock(bx, by + dir_dy, bz).isOpaqueCube())
                ? getMixedBrightnessForBlock(rb.blockAccess, bx, by + dir_dy, bz)
                : getMixedBrightnessForBlock(rb.blockAccess, bx, by, bz);
			
            setShading(
            	shadeNN, shadeNP, shadePN, shadePP,
            	shadeNX, shadeNY, shadePX, shadePY,
            	briNN, briNP, briPN, briPP,
            	briNX, briNY, briPX, briPY,
            	briThisFace, rb, bx, by, bz);
		}
	}
	
	@SideOnly(Side.CLIENT)
	protected void getBrightnessZFace(RenderBlocks rb, int bx, int by, int bz, double f_dz, int dir_dz) {
		int dz = (f_dz >= 0.995 ? 1 : f_dz <= 0.005 ? -1 : 0);
        if(rb == null || rb.blockAccess == null) {
			briNN = briNP = briPN = briPP = 0x00F000F0;
        } else if(!Minecraft.isAmbientOcclusionEnabled()) {
        	briNN = briNP = briPN = briPP = rb.blockAccess.getLightBrightnessForSkyBlocks(bx, by, bz+dz, 0);
		
        } else {
			bz += dz;
			float shadeNX = getAmbientOcclusionLightValue(rb.blockAccess, bx - 1, by, bz);
			float shadeNY = getAmbientOcclusionLightValue(rb.blockAccess, bx, by - 1, bz);
			float shadePY = getAmbientOcclusionLightValue(rb.blockAccess, bx, by + 1, bz);
			float shadePX = getAmbientOcclusionLightValue(rb.blockAccess, bx + 1, by, bz);
            int briNX = getMixedBrightnessForBlock(rb.blockAccess, bx - 1, by, bz);
            int briNY = getMixedBrightnessForBlock(rb.blockAccess, bx, by - 1, bz);
            int briPY = getMixedBrightnessForBlock(rb.blockAccess, bx, by + 1, bz);
            int briPX = getMixedBrightnessForBlock(rb.blockAccess, bx + 1, by, bz);
            boolean flagPX = rb.blockAccess.getBlock(bx + 1, by, bz + dir_dz).getCanBlockGrass();
            boolean flagNX = rb.blockAccess.getBlock(bx - 1, by, bz + dir_dz).getCanBlockGrass();
            boolean flagPY = rb.blockAccess.getBlock(bx, by + 1, bz + dir_dz).getCanBlockGrass();
            boolean flagNY = rb.blockAccess.getBlock(bx, by - 1, bz + dir_dz).getCanBlockGrass();
            
            float shadeNN = (!flagNX && !flagNY ? shadeNX : getAmbientOcclusionLightValue(rb.blockAccess, bx - 1, by - 1, bz));
            float shadeNP = (!flagNX && !flagPY ? shadeNX : getAmbientOcclusionLightValue(rb.blockAccess, bx - 1, by + 1, bz));
            float shadePN = (!flagPX && !flagNY ? shadePX : getAmbientOcclusionLightValue(rb.blockAccess, bx + 1, by - 1, bz));
            float shadePP = (!flagPX && !flagPY ? shadePX : getAmbientOcclusionLightValue(rb.blockAccess, bx + 1, by + 1, bz));
            int briNN = (!flagNX && !flagNY ? briNX : getMixedBrightnessForBlock(rb.blockAccess, bx - 1, by - 1, bz));
            int briNP = (!flagNX && !flagPY ? briNX : getMixedBrightnessForBlock(rb.blockAccess, bx - 1, by + 1, bz));
            int briPN = (!flagPX && !flagNY ? briPX : getMixedBrightnessForBlock(rb.blockAccess, bx + 1, by - 1, bz));
            int briPP = (!flagPX && !flagPY ? briPX : getMixedBrightnessForBlock(rb.blockAccess, bx + 1, by + 1, bz));
            bz -= dz;
			
            int briThisFace =
            	(dz != 0 || !rb.blockAccess.getBlock(bx, by, bz + dir_dz).isOpaqueCube())
                ? getMixedBrightnessForBlock(rb.blockAccess, bx, by, bz + dir_dz)
                : getMixedBrightnessForBlock(rb.blockAccess, bx, by, bz);
			
            setShading(
            	shadeNN, shadeNP, shadePN, shadePP,
            	shadeNX, shadeNY, shadePX, shadePY,
            	briNN, briNP, briPN, briPP,
            	briNX, briNY, briPX, briPY,
            	briThisFace, rb, bx, by, bz);
		}
	}
	
	private void setShading(
		float shadeNN, float shadeNP, float shadePN, float shadePP,
		float shadeNX, float shadeNY, float shadePX, float shadePY,
		int briNN, int briNP, int briPN, int briPP,
		int briNX, int briNY, int briPX, int briPY,
		int briThisFace, RenderBlocks rb, int bx, int by, int bz) {
		
		float shadeThisBlock = getAmbientOcclusionLightValue(rb.blockAccess, bx, by, bz);
        
        float shadeUseNP = (shadeThisBlock + shadeNP + shadeNX + shadePY) / 4.0F;
        float shadeUsePP = (shadeThisBlock + shadePP + shadePX + shadePY) / 4.0F;
        float shadeUsePN = (shadeThisBlock + shadePN + shadePX + shadeNY) / 4.0F;
        float shadeUseNN = (shadeThisBlock + shadeNN + shadeNX + shadeNY) / 4.0F;
        this.briNP = getAoBrightness(briThisFace, briNP, briNX, briPY);
        this.briPP = getAoBrightness(briThisFace, briPP, briPX, briPY);
        this.briPN = getAoBrightness(briThisFace, briPN, briPX, briNY);
        this.briNN = getAoBrightness(briThisFace, briNN, briNX, briNY);
        
        float colorRedTopLeft, colorGreenTopLeft, colorBlueTopLeft;
        float colorRedBottomLeft, colorRedBottomRight, colorRedTopRight;
        float colorGreenBottomRight, colorGreenBottomLeft, colorGreenTopRight;
        float colorBlueTopRight, colorBlueBottomRight, colorBlueBottomLeft;
        
        /*if (flag1)
        {
            colorRedTopLeft = colorRedBottomLeft = colorRedBottomRight = colorRedTopRight = par5 * 0.8F;
            colorGreenTopLeft = colorGreenBottomLeft = colorGreenBottomRight = colorGreenTopRight = par6 * 0.8F;
            colorBlueTopLeft = colorBlueBottomLeft = colorBlueBottomRight = colorBlueTopRight = par7 * 0.8F;
        }
        else
        {*/
            colorRedTopLeft = colorRedBottomLeft = colorRedBottomRight = colorRedTopRight = baseColR;
            colorGreenTopLeft = colorGreenBottomLeft = colorGreenBottomRight = colorGreenTopRight = baseColG;
            colorBlueTopLeft = colorBlueBottomLeft = colorBlueBottomRight = colorBlueTopRight = baseColB;
        //}

        colorRedTopLeft *= shadeUseNP;
        colorGreenTopLeft *= shadeUseNP;
        colorBlueTopLeft *= shadeUseNP;
        colorRedBottomLeft *= shadeUsePP;
        colorGreenBottomLeft *= shadeUsePP;
        colorBlueBottomLeft *= shadeUsePP;
        colorRedBottomRight *= shadeUsePN;
        colorGreenBottomRight *= shadeUsePN;
        colorBlueBottomRight *= shadeUsePN;
        colorRedTopRight *= shadeUseNN;
        colorGreenTopRight *= shadeUseNN;
        colorBlueTopRight *= shadeUseNN;
        
        colNN = rgb(colorRedTopRight, colorGreenTopRight, colorBlueTopRight);
        colNP = rgb(colorRedTopLeft, colorGreenTopLeft, colorBlueTopLeft);
        colPN = rgb(colorRedBottomRight, colorGreenBottomRight, colorBlueBottomRight);
        colPP = rgb(colorRedBottomLeft, colorGreenBottomLeft, colorBlueBottomLeft);
	}

	@SideOnly(Side.CLIENT) private static int rgb(float r, float g, float b) {
		int ir = (int)(r*255);
		int ig = (int)(g*255);
		int ib = (int)(b*255);
		return (ir<<16) | (ig<<8) | ib;
	}
	
	@SideOnly(Side.CLIENT) private static int rgb(double r, double g, double b) {
		return rgb((float)r, (float)g, (float)b);
	}
	
	@SideOnly(Side.CLIENT)
	protected void setBaseColour(double r, double g, double b) {
		colNN = colNP = colPN = colPP = rgb(r, g, b);
		baseColR = (float)r;
		baseColG = (float)g;
		baseColB = (float)b;
	}
	
	// texture offsets from fractional coordinates - to avoid squashing texture
	@SideOnly(Side.CLIENT) protected double txmin, txmax, tymin, tymax, tzmin, tzmax;

	@SideOnly(Side.CLIENT) private void renderAABB(Tessellator t, AxisAlignedBB aabb, RenderBlocks rb, int bx, int by, int bz, Block par1Block, boolean[] dontRenderFaces, EnumPosition pos) {
		
		// block-relative AABB
		AxisAlignedBB rbb = aabb.getOffsetBoundingBox(-bx, -by, -bz);
		
		txmin = (((aabb.minX % 1) + 1) % 1);
		txmax = (((aabb.maxX % 1) + 1) % 1);
		tymin = (((aabb.minY % 1) + 1) % 1);
		tymax = (((aabb.maxY % 1) + 1) % 1);
		tzmin = (((aabb.minZ % 1) + 1) % 1);
		tzmax = (((aabb.maxZ % 1) + 1) % 1);
		
		if(txmax == 0) txmax = 1;
		if(tymax == 0) tymax = 1;
		if(tzmax == 0) tzmax = 1;
		
		// minimum and maximum X/Y/Z values for dontRenderFaces to not apply
		// (anything below DRFMIN is treated as 0, anything above DRFMAX treated as 1)
		final double DRFMIN = 0.005;
		final double DRFMAX = 0.995;
		
		renderFaces(rb, bx, by, bz, aabb, rbb,
			txmin > DRFMIN || !dontRenderFaces[Dir.NX],
			tymin > DRFMIN || !dontRenderFaces[Dir.NY],
			tzmin > DRFMIN || !dontRenderFaces[Dir.NZ],
			txmax < DRFMAX || !dontRenderFaces[Dir.PX],
			tymax < DRFMAX || !dontRenderFaces[Dir.PY],
			tzmax < DRFMAX || !dontRenderFaces[Dir.PZ],
			pos);
	}
	
	@SideOnly(Side.CLIENT)
	protected void renderFaces(RenderBlocks rb, int bx, int by, int bz, AxisAlignedBB aabb, AxisAlignedBB rbb, boolean nx, boolean ny, boolean nz, boolean px, boolean py, boolean pz, EnumPosition pos) {
		if(nz) {
			setTex(Dir.NZ);
			setBaseColour(0.8, 0.8, 0.8);
			getBrightnessZFace(rb, bx, by, bz, rbb.minZ, -1);
			rawRenderFaceNZ(aabb, rbb);
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
		}
		
		if(pz) {
			setTex(Dir.PZ);
			setBaseColour(0.8, 0.8, 0.8);
			getBrightnessZFace(rb, bx, by, bz, rbb.maxZ, 1);
			rawRenderFacePZ(aabb, rbb);
		}
		
		if(py) {
			setTex(Dir.PY);
			setBaseColour(1.0, 1.0, 1.0);
			getBrightnessYFace(rb, bx, by, bz, rbb.maxY, 1);
			rawRenderFacePY(aabb, rbb);
		}
		
		if(px) {
			setTex(Dir.PX);
			setBaseColour(0.6, 0.6, 0.6);
			getBrightnessXFace(rb, bx, by, bz, rbb.maxX, 1);
			rawRenderFacePX(aabb, rbb);
		}
	}

	@SideOnly(Side.CLIENT)
	protected void rawRenderFacePX(AxisAlignedBB aabb, AxisAlignedBB rbb) {
		Tessellator t = Tessellator.instance;
		t.setNormal(1, 0, 0);
		setColourAndBrightness(t, rbb.maxY, rbb.minZ);
		t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, uMax+tzmin*(uMin-uMax), vMax+tymax*(vMin-vMax));
		setColourAndBrightness(t, rbb.maxY, rbb.maxZ);
		t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, uMax+tzmax*(uMin-uMax), vMax+tymax*(vMin-vMax));
		setColourAndBrightness(t, rbb.minY, rbb.maxZ);
		t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, uMax+tzmax*(uMin-uMax), vMax+tymin*(vMin-vMax));
		setColourAndBrightness(t, rbb.minY, rbb.minZ);
		t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, uMax+tzmin*(uMin-uMax), vMax+tymin*(vMin-vMax));
	}

	@SideOnly(Side.CLIENT)
	protected void rawRenderFacePY(AxisAlignedBB aabb, AxisAlignedBB rbb) {
		Tessellator t = Tessellator.instance;
		t.setNormal(0, 1, 0);
		setColourAndBrightness(t, rbb.minX, rbb.minZ);
		t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, uMin+txmin*(uMax-uMin), vMin+tzmin*(vMax-vMin));
		setColourAndBrightness(t, rbb.minX, rbb.maxZ);
		t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, uMin+txmin*(uMax-uMin), vMin+tzmax*(vMax-vMin));
		setColourAndBrightness(t, rbb.maxX, rbb.maxZ);
		t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, uMin+txmax*(uMax-uMin), vMin+tzmax*(vMax-vMin));
		setColourAndBrightness(t, rbb.maxX, rbb.minZ);
		t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, uMin+txmax*(uMax-uMin), vMin+tzmin*(vMax-vMin));
	}

	@SideOnly(Side.CLIENT)
	protected void rawRenderFacePZ(AxisAlignedBB aabb, AxisAlignedBB rbb) {
		Tessellator t = Tessellator.instance;
		t.setNormal(0, 0, 1);
		setColourAndBrightness(t, rbb.maxX, rbb.minY);
		t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, uMin+txmax*(uMax-uMin), vMax+tymin*(vMin-vMax));
		setColourAndBrightness(t, rbb.maxX, rbb.maxY);
		t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, uMin+txmax*(uMax-uMin), vMax+tymax*(vMin-vMax));
		setColourAndBrightness(t, rbb.minX, rbb.maxY);
		t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, uMin+txmin*(uMax-uMin), vMax+tymax*(vMin-vMax));
		setColourAndBrightness(t, rbb.minX, rbb.minY);
		t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, uMin+txmin*(uMax-uMin), vMax+tymin*(vMin-vMax));
	}

	@SideOnly(Side.CLIENT)
	protected void rawRenderFaceNX(AxisAlignedBB aabb, AxisAlignedBB rbb) {
		Tessellator t = Tessellator.instance;
		t.setNormal(-1, 0, 0);
		setColourAndBrightness(t, rbb.minY, rbb.minZ);
		t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, uMin+tzmin*(uMax-uMin), vMax+tymin*(vMin-vMax));
		setColourAndBrightness(t, rbb.minY, rbb.maxZ);
		t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, uMin+tzmax*(uMax-uMin), vMax+tymin*(vMin-vMax));
		setColourAndBrightness(t, rbb.maxY, rbb.maxZ);
		t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, uMin+tzmax*(uMax-uMin), vMax+tymax*(vMin-vMax));
		setColourAndBrightness(t, rbb.maxY, rbb.minZ);
		t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, uMin+tzmin*(uMax-uMin), vMax+tymax*(vMin-vMax));
	}
	
	@SideOnly(Side.CLIENT)
	protected void rawRenderFaceNZ(AxisAlignedBB aabb, AxisAlignedBB rbb) {
		Tessellator t = Tessellator.instance;
		t.setNormal(0, 0, -1);
		setColourAndBrightness(t, rbb.minX, rbb.minY);
		t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, uMax+txmin*(uMin-uMax), vMax+tymin*(vMin-vMax));
		setColourAndBrightness(t, rbb.minX, rbb.maxY);
		t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, uMax+txmin*(uMin-uMax), vMax+tymax*(vMin-vMax));
		setColourAndBrightness(t, rbb.maxX, rbb.maxY);
		t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, uMax+txmax*(uMin-uMax), vMax+tymax*(vMin-vMax));
		setColourAndBrightness(t, rbb.maxX, rbb.minY);
		t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, uMax+txmax*(uMin-uMax), vMax+tymin*(vMin-vMax));
	}
	
	@SideOnly(Side.CLIENT)
	protected void rawRenderFaceNY(AxisAlignedBB aabb, AxisAlignedBB rbb) {
		Tessellator t = Tessellator.instance;
		t.setNormal(0, -1, 0);
		setColourAndBrightness(t, rbb.maxX, rbb.minZ);
		t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, uMin+txmax*(uMax-uMin), vMin+tzmin*(vMax-vMin));
		setColourAndBrightness(t, rbb.maxX, rbb.maxZ);
		t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, uMin+txmax*(uMax-uMin), vMin+tzmax*(vMax-vMin));
		setColourAndBrightness(t, rbb.minX, rbb.maxZ);
		t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, uMin+txmin*(uMax-uMin), vMin+tzmax*(vMax-vMin));
		setColourAndBrightness(t, rbb.minX, rbb.minZ);
		t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, uMin+txmin*(uMax-uMin), vMin+tzmin*(vMax-vMin));
	}
	
	// renders centred on 0.5,0.5,0.5
	@Override
	@SideOnly(Side.CLIENT)
	public void renderPartInv(RenderBlocks render, ItemStack stack) {
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GL11.glPushMatrix();
		
		EnumPosition pos;
		switch(clazz)
		{
		case Centre:
			pos = EnumPosition.Centre;
			break;
		case Panel: case HollowPanel:
			pos = EnumPosition.FaceNZ;
			GL11.glTranslatef(0, 0, 0.5f-(float)size/2);
			break;
		case Strip:
			pos = EnumPosition.EdgeNXNZ;
			GL11.glTranslatef(0.5f-(float)size/2, 0, 0.5f-(float)size/2);
			break;
		case Corner:
			pos = EnumPosition.CornerNXNYNZ;
			GL11.glTranslatef(0.5f-(float)size/2, 0.5f-(float)size/2, 0.5f-(float)size/2);
			break;
		default:
			pos = EnumPosition.Centre;
		}
		
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		overrideIcon = null;
		
		Tessellator t = Tessellator.instance;
		t.startDrawingQuads();
		
		
		renderPartWorld(render, createPart(pos), 0, 0, 0, new boolean[6]);
		//renderQuads(t, pos, render);
		
		t.draw();
		GL11.glPopMatrix();
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	private void renderAABB(RenderBlocks render, int x, int y, int z, double minx, double miny, double minz, double maxx, double maxy, double maxz, Block block, boolean[] dontRenderFaces, EnumPosition pos) {
		renderAABB(Tessellator.instance,
			AxisAlignedBB.getAABBPool().getAABB(minx, miny, minz, maxx, maxy, maxz).offset(x, y, z),
			render,
			x, y, z,
			block,
			dontRenderFaces,
			pos);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderPartWorld(RenderBlocks render, Part p, int x, int y, int z, boolean[] dontRenderFaces) {
		overrideIcon = render.overrideBlockTexture;
		
		float i = p.pos.ordinal() / 100000.0f; // unnoticeable position-dependent inset, to reduce flickering
		
		if(clazz == EnumPartClass.HollowPanel)
		{
			float hes = 0.25f; //(float)HOLLOW_EDGE_SIZE;
			float s = (float)size;
			
			switch(p.pos)
			{
			case FaceNX:
				renderAABB(render, x, y, z, i, i, i, s, hes, 1-i, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, i, 1-hes, i, s, 1-i, 1-i, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, i, hes, i, s, 1-hes, hes, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, i, hes, 1-hes, s, 1-hes, 1-i, modelBlock, dontRenderFaces, p.pos);
				break;
			case FacePX:
				renderAABB(render, x, y, z, 1-s, i, i, 1-i, hes, 1-i, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, 1-s, 1-hes, i, 1-i, 1-i, 1-i, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, 1-s, hes, i, 1-i, 1-hes, hes, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, 1-s, hes, 1-hes, 1-i, 1-hes, 1-i, modelBlock, dontRenderFaces, p.pos);
				break;
			case FaceNY:
				renderAABB(render, x, y, z, i, i, i, hes, s, 1-i, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, 1-hes, i, i, 1-i, s, 1-i, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, hes, i, i, 1-hes, s, hes, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, hes, i, 1-hes, 1-hes, s, 1-i, modelBlock, dontRenderFaces, p.pos);
				break;
			case FacePY:
				renderAABB(render, x, y, z, i, 1-s, i, hes, 1-i, 1-i, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, 1-hes, 1-s, i, 1-i, 1-i, 1-i, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, hes, 1-s, i, 1-hes, 1-i, hes, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, hes, 1-s, 1-hes, 1-hes, 1-i, 1-i, modelBlock, dontRenderFaces, p.pos);
				break;
			case FaceNZ:
				renderAABB(render, x, y, z, i, i, i, hes, 1-i, s, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, 1-hes, i, i, 1-i, 1-i, s, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, hes, i, i, 1-hes, hes, s, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, hes, 1-hes, 0, 1-hes, 1-i, s, modelBlock, dontRenderFaces, p.pos);
				break;
			case FacePZ:
				renderAABB(render, x, y, z, i, i, 1-s, hes, 1-i, 1-i, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, 1-hes, i, 1-s, 1-i, 1-i, 1-i, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, hes, i, 1-s, 1-hes, hes, 1-i, modelBlock, dontRenderFaces, p.pos);
				renderAABB(render, x, y, z, hes, 1-hes, 1-s, 1-hes, 1-i, 1-i, modelBlock, dontRenderFaces, p.pos);
				break;
			default:
				// shouldn't happen
				System.err.println("hollow panel placed at invalid position "+p.pos+" in block "+x+","+y+","+z);
			}
		}
		else
		{
			AxisAlignedBB bb = p.getBoundingBoxFromPool();
			if(bb.minX == 0) bb.minX = i;
			if(bb.minY == 0) bb.minY = i;
			if(bb.minZ == 0) bb.minZ = i;
			if(bb.maxX == 1) bb.maxX -= i;
			if(bb.maxY == 1) bb.maxY -= i;
			if(bb.maxZ == 1) bb.maxZ -= i;
			renderAABB(render, x, y, z, bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, modelBlock, dontRenderFaces, p.pos);
		}
	}

	@Override
	public boolean isOpaque() {
		return modelBlock.isOpaqueCube();
	}
	
	@SideOnly(Side.CLIENT)
	void colorizeParticle(EntityDiggingFX fx, TileEntity te) {
		fx.setRBGColorF(1, 1, 1);
	}
}
