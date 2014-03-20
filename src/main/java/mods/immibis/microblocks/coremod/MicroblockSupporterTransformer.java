package mods.immibis.microblocks.coremod;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mods.immibis.core.api.APILocator;
import mods.immibis.core.api.multipart.ICoverSystem;
import mods.immibis.core.api.multipart.IMultipartTile;
import mods.immibis.core.multipart.SubhitValues;
import mods.immibis.microblocks.PacketMicroblockContainerDescription;
import mods.immibis.microblocks.api.IMicroblockCoverSystem;
import mods.immibis.microblocks.api.IMicroblockSupporterTile;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;

import com.google.common.collect.ObjectArrays;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MicroblockSupporterTransformer implements net.minecraft.launchwrapper.IClassTransformer {
	public static interface ITransformedBlock {
		public MovingObjectPosition collisionRayTrace_ImmibisMicroblockTransformer(World world, int x, int y, int z, Vec3 src, Vec3 dst);
		public float getPlayerRelativeBlockHardness_ImmibisMicroblockTransformer(EntityPlayer player, World world, int x, int y, int z);
	}
	
	public static class A {public static class Nested {}}
	public static class B extends A {}
	
	static {new B.Nested();}
	
	/** Marker interface for transformed blocks that don't use tile entities by default */
	public static interface TilelessTransformedBlock {}
	
	public static Set<String> blockClasses = new HashSet<String>();
	public static Set<String> tileClasses = new HashSet<String>();
	public static Set<String> blockClassesUsingDummyTE = new HashSet<String>();
	
	private static final FMLDeobfuscatingRemapper R = FMLDeobfuscatingRemapper.INSTANCE;
	
	private static final String AXISALIGNEDBB = R.mapType("net/minecraft/util/AxisAlignedBB");
	private static final String WORLD = R.mapType("net/minecraft/world/World");
	private static final String IBLOCKACCESS = R.mapType("net/minecraft/world/IBlockAccess");
	//private static final String ENTITY = R.mapType("net/minecraft/entity/Entity");
	private static final String TILEENTITY = R.mapType("net/minecraft/tileentity/TileEntity");
	private static final String ENTITYPLAYER = R.mapType("net/minecraft/entity/player/EntityPlayer");
	private static final String RENDERBLOCKS = R.mapType("net/minecraft/client/renderer/RenderBlocks");
	private static final String PACKET = R.mapType("net/minecraft/network/Packet");
	private static final String NBTTAGCOMPOUND = R.mapType("net/minecraft/nbt/NBTTagCompound");
	private static final String MOVINGOBJECTPOSITION = R.mapType("net/minecraft/util/MovingObjectPosition");
	//private static final String VEC3 = R.mapType("net/minecraft/util/Vec3");
	private static final String ITEMSTACK = R.mapType("net/minecraft/item/ItemStack");
	
	// matching ITransformedBlock
	private static final String BLOCK_GETPLAYERRELATIVEBLOCKHARDNESS_REPLACEMENT = "getPlayerRelativeBlockHardness_ImmibisMicroblockTransformer";
	private static final String BLOCK_COLLISIONRAYTRACE_REPLACEMENT = "collisionRayTrace_ImmibisMicroblockTransformer";
	
	private static final MethodMatcher BLOCK_ADDCOLLIDINGBLOCKTOLIST =
		new MethodMatcher("net/minecraft/block/Block", "addCollisionBoxesToList", "func_149743_a", "a", "(Lnet/minecraft/world/World;IIILnet/minecraft/util/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V");
	private static final MethodMatcher BLOCK_ONBLOCKCLICKED =
		new MethodMatcher("net/minecraft/block/Block", "onBlockClicked", "func_149699_a", "a", "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;)V");
	private static final MethodMatcher BLOCK_REMOVEBLOCKBYPLAYER =
		new MethodMatcher("net/minecraft/block/Block", "removedByPlayer", "removedByPlayer", "removedByPlayer", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;III)Z");
	private static final MethodMatcher BLOCK_GETPLAYERRELATIVEBLOCKHARDNESS =
		new MethodMatcher("net/minecraft/block/Block", "getPlayerRelativeBlockHardness", "func_149737_a", "a", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;III)F");
	private static final MethodMatcher BLOCK_GETRENDERTYPE =
		new MethodMatcher("net/minecraft/block/Block", "getRenderType", "func_149645_b", "b", "()I");
	private static final MethodMatcher TILEENTITY_GETDESCRIPTIONPACKET =
		new MethodMatcher("net/minecraft/tileentity/TileEntity", "getDescriptionPacket", "func_145844_m", "m", "()Lnet/minecraft/network/Packet;");
	private static final MethodMatcher TILEENTITY_WRITETONBT =
		new MethodMatcher("net/minecraft/tileentity/TileEntity", "writeToNBT", "func_145841_b", "b", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
	private static final MethodMatcher TILEENTITY_READFROMNBT =
		new MethodMatcher("net/minecraft/tileentity/TileEntity", "readFromNBT", "func_145839_a", "a", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
	private static final MethodMatcher BLOCK_COLLISIONRAYTRACE =
		new MethodMatcher("net/minecraft/block/Block", "collisionRayTrace", "func_149731_a", "a", "(Lnet/minecraft/world/World;IIILnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;)Lnet/minecraft/util/MovingObjectPosition;");
	private static final MethodMatcher BLOCK_GETPICKBLOCK =
		new MethodMatcher("net/minecraft/block/Block", "getPickBlock", "getPickBlock", "getPickBlock", "(Lnet/minecraft/util/MovingObjectPosition;Lnet/minecraft/world/World;III)Lnet/minecraft/item/ItemStack;");
	private static final MethodMatcher BLOCK_ISBLOCKSOLIDONSIDE = 
		new MethodMatcher("net/minecraft/block/Block", "isSideSolid", "isSideSolid", "isSideSolid", "(Lnet/minecraft/world/IBlockAccess;IIILnet/minecraftforge/common/util/ForgeDirection;)Z");
	private static final MethodMatcher BLOCK_DROPBLOCKASITEMWITHCHANCE =
		new MethodMatcher("net/minecraft/block/Block", "dropBlockAsItemWithChance", "func_149690_a", "a", "(Lnet/minecraft/world/World;IIIIFI)V");
	private static final MethodMatcher BLOCK_ADDBLOCKHITEFFECTS =
		new MethodMatcher("net/minecraft/block/Block", "addHitEffects", "addHitEffects", "addHitEffects", "(Lnet/minecraft/world/World;Lnet/minecraft/util/MovingObjectPosition;Lnet/minecraft/client/particle/EffectRenderer;)Z");
	private static final MethodMatcher BLOCK_HASTILEENTITY =
		new MethodMatcher("net/minecraft/block/Block", "hasTileEntity", "hasTileEntity", "hasTileEntity", "(I)Z");
	
	private static class ClassVisitorBase extends ClassVisitor {
		public ClassVisitorBase(int api, ClassVisitor parent) {
			super(api, parent);
		}
		
		public void generateAndTransformMethod(ClassVisitor unused, String superclass, int access, MethodMatcher m) {
			generateAndTransformMethod(unused, superclass, access, m.getName(), m.getDesc());
		}
		
		public void generateAndTransformMethod(ClassVisitor unused, String superclass, int access, String name, String desc) {
			Type methodType = Type.getMethodType(desc);
			
			MethodVisitor mv = visitMethod(access, name, desc, null, new String[0]);
			
			mv.visitCode();
			
			// push 'this'
			if((access & Opcodes.ACC_STATIC) == 0)
				mv.visitVarInsn(Opcodes.ALOAD, 0);
			
			// push arguments
			int stackPos = 1;
			for(Type pt : methodType.getArgumentTypes()) {
				switch(pt.getSort()) {
				case Type.ARRAY: case Type.OBJECT:
					mv.visitVarInsn(Opcodes.ALOAD, stackPos++);
					break;
				case Type.BOOLEAN: case Type.BYTE: case Type.CHAR: case Type.INT: case Type.SHORT:
					mv.visitVarInsn(Opcodes.ILOAD, stackPos++);
					break;
				case Type.DOUBLE:
					mv.visitVarInsn(Opcodes.DLOAD, stackPos);
					stackPos += 2;
					break;
				case Type.FLOAT:
					mv.visitVarInsn(Opcodes.FLOAD, stackPos++);
					break;
				case Type.LONG:
					mv.visitVarInsn(Opcodes.LLOAD, stackPos);
					stackPos += 2;
					break;
				}
			}
			
			// call superclass method
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superclass, name, desc);
			
			// return result
			switch(methodType.getReturnType().getSort()) {
			case Type.ARRAY: case Type.OBJECT:
				mv.visitInsn(Opcodes.ARETURN);
				break;
			case Type.BOOLEAN: case Type.BYTE: case Type.CHAR: case Type.INT: case Type.SHORT:
				mv.visitInsn(Opcodes.IRETURN);
				break;
			case Type.DOUBLE:
				mv.visitInsn(Opcodes.DRETURN);
				break;
			case Type.FLOAT:
				mv.visitInsn(Opcodes.FRETURN);
				break;
			case Type.LONG:
				mv.visitInsn(Opcodes.LRETURN);
				break;
			case Type.VOID:
				mv.visitInsn(Opcodes.RETURN);
				break;
			}
			
			int nArgs = methodType.getArgumentsAndReturnSizes() >> 2;
			int nRets = methodType.getArgumentsAndReturnSizes() & 3;
			mv.visitMaxs(nArgs + 1, Math.max(nArgs + 1, nRets));
			mv.visitEnd();
		}
	}
	
	// This basically merges the code from BlockMultipartBase into any Block class, but with extra checks that the tile is actually an IMultipartTile.
	// See BlockMultipartBase to see what it needs to add to make blocks into multipart blocks.
	// If useDefaultTE is true, it also adds the interface TilelessTransformedBlock
	private static class BlockTransformerVisitor extends ClassVisitorBase {
		private boolean useDefaultTE;
		public BlockTransformerVisitor(ClassVisitor parent, boolean useDefaultTE) {
			super(Opcodes.ASM4, parent);
			this.useDefaultTE = useDefaultTE;
		}
		
		public static class TransformMethodVisitor_getRenderType extends MethodVisitor {
			public TransformMethodVisitor_getRenderType(MethodVisitor mv) {
				super(Opcodes.ASM4, mv);
			}
			
			@Override
			public void visitInsn(int opcode) {
				if(opcode == Opcodes.IRETURN && FMLLaunchHandler.side() == Side.CLIENT)
					super.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MSTHooks", "overrideRenderType", "(I)I");
				super.visitInsn(opcode);
			}
		}
		
		public static class TransformMethodVisitor_onBlockClicked extends MethodVisitor {
			
			public TransformMethodVisitor_onBlockClicked(MethodVisitor mv) {
				super(Opcodes.ASM4, mv);
			}
			
			/*
			 * Add at start:
			 * 
			 * MSTHooks.onBlockClickedStatic(par1World, par5Player);
			 */
			
			@Override
			public void visitCode() {
				super.visitCode();
				
				super.visitVarInsn(Opcodes.ALOAD, 1);
				super.visitVarInsn(Opcodes.ILOAD, 2);
				super.visitVarInsn(Opcodes.ILOAD, 3);
				super.visitVarInsn(Opcodes.ILOAD, 4);
				super.visitVarInsn(Opcodes.ALOAD, 5);
				super.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MSTHooks", "onBlockClicked", "(L"+WORLD+";IIIL"+ENTITYPLAYER+";)V");
			}
			
			@Override
			public void visitMaxs(int maxStack, int maxLocals) {
				if(maxStack < 5)
					maxStack = 5;
				super.visitMaxs(maxStack, maxLocals);
			}
		}
		
		public static class TransformMethodVisitor_getPickBlock extends MethodVisitor {
			
			public TransformMethodVisitor_getPickBlock(MethodVisitor mv) {
				super(Opcodes.ASM4, mv);
			}
			
			/*
			 * Add at start:
			 * 
			 * ItemStack temp = Util.pickPart(par1MovingObjectPosition, par2World, par3X, par4Y, par5Z);
			 * if(temp != null)
			 * 	   return temp;
			 */
			
			@Override
			public void visitCode() {
				super.visitCode();
				
				Label ifNull = new Label();
				
				super.visitVarInsn(Opcodes.ALOAD, 1);
				super.visitVarInsn(Opcodes.ALOAD, 2);
				super.visitVarInsn(Opcodes.ILOAD, 3);
				super.visitVarInsn(Opcodes.ILOAD, 4);
				super.visitVarInsn(Opcodes.ILOAD, 5);
				super.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$Util", "pickPart", "(L"+MOVINGOBJECTPOSITION+";L"+WORLD+";III)L"+ITEMSTACK+";");
				super.visitInsn(Opcodes.DUP);
				super.visitJumpInsn(Opcodes.IFNULL, ifNull);
				super.visitInsn(Opcodes.ARETURN);
				
				super.visitLabel(ifNull);
				super.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {ITEMSTACK});
				super.visitInsn(Opcodes.POP);
			}
			
			@Override
			public void visitMaxs(int maxStack, int maxLocals) {
				if(maxStack < 5)
					maxStack = 5;
				super.visitMaxs(maxStack, maxLocals);
			}
		}
		
		public static class TransformMethodVisitor_isBlockSolidOnSide extends MethodVisitor {
			
			public TransformMethodVisitor_isBlockSolidOnSide(MethodVisitor mv) {
				super(Opcodes.ASM4, mv);
			}
			
			/*
			 * Add at start:
			 * 
			 * if(Util.isSolidOnSide(par1World, par2X, par3Y, par4Z, par5ForgeDirection))
			 *     return true;
			 */
			
			@Override
			public void visitCode() {
				super.visitCode();
				
				Label ifFalse = new Label();
				
				super.visitVarInsn(Opcodes.ALOAD, 1);
				super.visitVarInsn(Opcodes.ILOAD, 2);
				super.visitVarInsn(Opcodes.ILOAD, 3);
				super.visitVarInsn(Opcodes.ILOAD, 4);
				super.visitVarInsn(Opcodes.ALOAD, 5);
				super.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$Util", "isSolidOnSide", "(L"+IBLOCKACCESS+";IIILnet/minecraftforge/common/util/ForgeDirection;)Z");
				super.visitJumpInsn(Opcodes.IFEQ, ifFalse);
				super.visitInsn(Opcodes.ICONST_1);
				super.visitInsn(Opcodes.IRETURN);
				
				super.visitLabel(ifFalse);
				super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			}
			
			@Override
			public void visitMaxs(int maxStack, int maxLocals) {
				if(maxStack < 5)
					maxStack = 5;
				super.visitMaxs(maxStack, maxLocals);
			}
		}
		
		public static class TransformMethodVisitor_dropBlockAsItemWithChance extends MethodVisitor {
			public TransformMethodVisitor_dropBlockAsItemWithChance(MethodVisitor mv) {
				super(Opcodes.ASM4, mv);
			}
			
			/*
			 * Add at start:
			 * 
			 * if(Util.dropBlockAsItemWithChance(par1World, par2X, par3Y, par4Z, par6Float))
			 *     return;
			 */
			@Override
			public void visitCode() {
				super.visitCode();
				
				super.visitVarInsn(Opcodes.ALOAD, 1);
				super.visitVarInsn(Opcodes.ILOAD, 2);
				super.visitVarInsn(Opcodes.ILOAD, 3);
				super.visitVarInsn(Opcodes.ILOAD, 4);
				super.visitVarInsn(Opcodes.FLOAD, 6);
				super.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$Util", "dropBlockAsItemWithChance", "(L"+WORLD+";IIIF)Z");
				
				Label ifFalse = new Label();
				
				super.visitJumpInsn(Opcodes.IFEQ, ifFalse);
				super.visitInsn(Opcodes.RETURN);
				super.visitLabel(ifFalse);
				super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			}
			
			@Override
			public void visitMaxs(int maxStack, int maxLocals) {
				if(maxStack < 5)
					maxStack = 5;
				
				super.visitMaxs(maxStack, maxLocals);
			}
		}

		public static class TransformMethodVisitor_addCollidingBlockToList extends MethodVisitor {
			
			public TransformMethodVisitor_addCollidingBlockToList(MethodVisitor mv) {
				super(Opcodes.ASM4, mv);
			}
			
			/*
			 * Add at start:
			 * 
			 * Util.addCollidingBlockToList(par1World, par2X, par3Y, par4Z, par5Mask, par6List);
			 * IMultipartTile te = ((IMultipartTile)par1World.getTileEntity(par2X, par3Y, par4Z));
			 * ICoverSystem ci = te.getCoverSystem();
			 * if(ci != null)
			 * 		ci.getCollidingBoundingBoxes(par5Mask, par6List);
			 */

			@Override
			public void visitCode() {
				super.visitCode();
				
				super.visitVarInsn(Opcodes.ALOAD, 1); // par1World
				super.visitVarInsn(Opcodes.ILOAD, 2); // par2X
				super.visitVarInsn(Opcodes.ILOAD, 3); // par3Y
				super.visitVarInsn(Opcodes.ILOAD, 4); // par4Z
				super.visitVarInsn(Opcodes.ALOAD, 5); // par5Mask
				super.visitVarInsn(Opcodes.ALOAD, 6); // par6List
				super.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$Util", "addCollidingBlockToList", "(L"+WORLD+";IIIL"+AXISALIGNEDBB+";Ljava/util/List;)V");
				
			}
			
			@Override
			public void visitMaxs(int maxStack, int maxLocals) {
				if(maxStack < 6)
					maxStack = 6;
				super.visitMaxs(maxStack, maxLocals);
			}
		}
		
		public static class TransformMethodVisitor_addBlockHitEffects extends MethodVisitor {
			public TransformMethodVisitor_addBlockHitEffects(MethodVisitor parent) {
				super(Opcodes.ASM4, parent);
			}
			
			@Override
			public void visitCode() {
				super.visitCode();
				
				Label ifFalse = new Label();
				
				visitVarInsn(Opcodes.ALOAD, 1);
				visitVarInsn(Opcodes.ALOAD, 2);
				visitVarInsn(Opcodes.ALOAD, 3);
				visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MSTHooks", "addHitEffects", BLOCK_ADDBLOCKHITEFFECTS.getDesc());
				visitJumpInsn(Opcodes.IFEQ, ifFalse);
				visitInsn(Opcodes.ICONST_1);
				visitInsn(Opcodes.IRETURN);
				
				visitLabel(ifFalse);
				visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			}
			
			@Override
			public void visitMaxs(int maxStack, int maxLocals) {
				if(maxStack < 3)
					maxStack = 3;
				super.visitMaxs(maxStack, maxLocals);
			}
		}
		
		public static class TransformMethodVisitor_removeBlockByPlayer extends MethodVisitor {
			public TransformMethodVisitor_removeBlockByPlayer(MethodVisitor parent) {
				super(Opcodes.ASM4, parent);
			}
			
			@Override
			public void visitCode() {
				super.visitCode();
				
				Label ifFalse = new Label();
				
				visitVarInsn(Opcodes.ALOAD, 1);
				visitVarInsn(Opcodes.ALOAD, 2);
				visitVarInsn(Opcodes.ILOAD, 3);
				visitVarInsn(Opcodes.ILOAD, 4);
				visitVarInsn(Opcodes.ILOAD, 5);
				visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$Util", "removeBlockByPlayer", BLOCK_REMOVEBLOCKBYPLAYER.getDesc());
				visitJumpInsn(Opcodes.IFEQ, ifFalse);
				visitInsn(Opcodes.ICONST_1);
				visitInsn(Opcodes.IRETURN);
				
				visitLabel(ifFalse);
				visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			}
			
			@Override
			public void visitMaxs(int maxStack, int maxLocals) {
				if(maxStack < 5)
					maxStack = 5;
				
				super.visitMaxs(maxStack, maxLocals);
			}
		}
		
		@SuppressWarnings("unused")
		public String className, superclass;
		
		public boolean saw_addCollidingBlockToList;
		public boolean saw_removeBlockByPlayer;
		public boolean saw_onBlockClicked;
		public boolean saw_getPlayerRelativeBlockHardness;
		public boolean saw_getRenderType;
		public boolean saw_collisionRayTrace;
		public boolean saw_getPickBlock;
		public boolean saw_isBlockSolidOnSide;
		public boolean saw_dropBlockAsItemWithChance;
		public boolean saw_addBlockHitEffects;
		
		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			className = name;
			superclass = superName;
			
			String newInterfaces[] = new String[(interfaces == null ? 0 : interfaces.length) + 1 + (useDefaultTE ? 1 : 0)];
			if(interfaces != null)
				System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
			newInterfaces[newInterfaces.length - 1] = "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$ITransformedBlock";
			if(useDefaultTE)
				newInterfaces[newInterfaces.length - 2] = "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$TilelessTransformedBlock";
			
			super.visit(version, access, name, signature, superName, newInterfaces);
		}
		
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			
			if(BLOCK_GETPLAYERRELATIVEBLOCKHARDNESS.matches(name, desc)) {
				name = BLOCK_GETPLAYERRELATIVEBLOCKHARDNESS_REPLACEMENT;
				saw_getPlayerRelativeBlockHardness = true;
			}
			
			/*if(name.equals(BLOCK_GETBLOCKDROPPED_NAME) && desc.equals(BLOCK_GETBLOCKDROPPED_DESC)) {
				name = BLOCK_GETBLOCKDROPPED_REPLACEMENT;
				saw_getBlockDropped = true;
			}*/
			
			if(BLOCK_COLLISIONRAYTRACE.matches(name, desc)) {
				name = BLOCK_COLLISIONRAYTRACE_REPLACEMENT;
				saw_collisionRayTrace = true;
			}
			
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			
			if(BLOCK_ADDCOLLIDINGBLOCKTOLIST.matches(name, desc)) {
				saw_addCollidingBlockToList = true;
				mv = new TransformMethodVisitor_addCollidingBlockToList(mv);
			}
			
			if(BLOCK_ONBLOCKCLICKED.matches(name, desc)) {
				saw_onBlockClicked = true;
				mv = new TransformMethodVisitor_onBlockClicked(mv);
			}
			
			if(BLOCK_GETRENDERTYPE.matches(name, desc)) {
				saw_getRenderType = true;
				mv = new TransformMethodVisitor_getRenderType(mv);
			}
			
			if(BLOCK_GETPICKBLOCK.matches(name, desc)) {
				saw_getPickBlock = true;
				mv = new TransformMethodVisitor_getPickBlock(mv);
			}
			
			if(BLOCK_ISBLOCKSOLIDONSIDE.matches(name, desc)) {
				saw_isBlockSolidOnSide = true;
				mv = new TransformMethodVisitor_isBlockSolidOnSide(mv);
			}
			
			if(BLOCK_DROPBLOCKASITEMWITHCHANCE.matches(name, desc)) {
				saw_dropBlockAsItemWithChance = true;
				mv = new TransformMethodVisitor_dropBlockAsItemWithChance(mv);
			}
			
			if(BLOCK_ADDBLOCKHITEFFECTS.matches(name, desc)) {
				saw_addBlockHitEffects = true;
				mv = new TransformMethodVisitor_addBlockHitEffects(mv);
			}
			
			if(BLOCK_REMOVEBLOCKBYPLAYER.matches(name, desc)) {
				saw_removeBlockByPlayer = true;
				mv = new TransformMethodVisitor_removeBlockByPlayer(mv);
			}
			
			return mv;
		}
		
		@Override
		public void visitEnd() {
			
			if(!saw_addCollidingBlockToList)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, BLOCK_ADDCOLLIDINGBLOCKTOLIST);
			
			if(!saw_removeBlockByPlayer)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, BLOCK_REMOVEBLOCKBYPLAYER);
			
			if(!saw_onBlockClicked)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, BLOCK_ONBLOCKCLICKED);
			
			if(!saw_getPlayerRelativeBlockHardness)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, BLOCK_GETPLAYERRELATIVEBLOCKHARDNESS);
			
			if(!saw_getRenderType)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, BLOCK_GETRENDERTYPE);
			
			if(!saw_collisionRayTrace)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, BLOCK_COLLISIONRAYTRACE);
			
			if(!saw_getPickBlock)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, BLOCK_GETPICKBLOCK);
			
			if(!saw_isBlockSolidOnSide)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, BLOCK_ISBLOCKSOLIDONSIDE);
			
			if(!saw_dropBlockAsItemWithChance)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, BLOCK_DROPBLOCKASITEMWITHCHANCE);
			
			if(!saw_addBlockHitEffects && FMLLaunchHandler.side() == Side.CLIENT)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, BLOCK_ADDBLOCKHITEFFECTS);
			
			{
				/* public float getPlayerRelativeBlockHardness(EntityPlayer ply, World world, int x, int y, int z) {
					return Util.getPlayerRelativeBlockHardness(world, ply, x, y, z);
				} */
				
				MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, BLOCK_GETPLAYERRELATIVEBLOCKHARDNESS.getName(), BLOCK_GETPLAYERRELATIVEBLOCKHARDNESS.getDesc(), null, new String[0]);
				mv.visitCode();
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitVarInsn(Opcodes.ALOAD, 2);
				mv.visitVarInsn(Opcodes.ILOAD, 3);
				mv.visitVarInsn(Opcodes.ILOAD, 4);
				mv.visitVarInsn(Opcodes.ILOAD, 5);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$Util", "getPlayerRelativeBlockHardness", BLOCK_GETPLAYERRELATIVEBLOCKHARDNESS.getDesc());
				mv.visitInsn(Opcodes.FRETURN);
				mv.visitMaxs(5, 6);
				mv.visitEnd();
			}
			
			if(useDefaultTE)
			{
				/* public boolean hasTileEntity(int meta) {
					return true;
				} */
				MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, BLOCK_HASTILEENTITY.getName(), BLOCK_HASTILEENTITY.getDesc(), null, new String[0]);
				mv.visitCode();
				mv.visitInsn(Opcodes.ICONST_1);
				mv.visitInsn(Opcodes.IRETURN);
				mv.visitMaxs(1, 2);
				mv.visitEnd();
			}
			
			/*{
				/* public ArrayList getBlockDropped(World, int, int, int, int, int) {
					return MSTHooks.getBlockDropped();
				} * /
				
				MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, BLOCK_GETBLOCKDROPPED_NAME, BLOCK_GETBLOCKDROPPED_DESC, null, new String[0]);
				mv.visitCode();
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MSTHooks", "getBlockDropped", "()Ljava/util/ArrayList;");
				mv.visitInsn(Opcodes.ARETURN);
				mv.visitMaxs(1, 7);
				mv.visitEnd();
			} */
			
			{
				/* public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 src, Vec3 dst) {
				 	return Util.collisionRayTrace(world, x, y, z, src, dst);
				} */
				
				MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, BLOCK_COLLISIONRAYTRACE.getName(), BLOCK_COLLISIONRAYTRACE.getDesc(), null, new String[0]);
				mv.visitCode();
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitVarInsn(Opcodes.ILOAD, 2);
				mv.visitVarInsn(Opcodes.ILOAD, 3);
				mv.visitVarInsn(Opcodes.ILOAD, 4);
				mv.visitVarInsn(Opcodes.ALOAD, 5);
				mv.visitVarInsn(Opcodes.ALOAD, 6);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$Util", "collisionRayTrace", BLOCK_COLLISIONRAYTRACE.getDesc());
				mv.visitInsn(Opcodes.ARETURN);
				mv.visitMaxs(6, 7);
				mv.visitEnd();
			}
			
			super.visitEnd();
		}
	}
	
	public static final String IMCS_FIELD = "ImmibisCoreMicroblockCoverSystem";
	
	private static class TileTransformerVisitor extends ClassVisitorBase {
		public TileTransformerVisitor(ClassVisitor parent, boolean isCoverable) {
			super(Opcodes.ASM4, parent);
		}
		
		private String position = "Centre";
		
		public String className, superclass;
		
		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			interfaces = ObjectArrays.concat(interfaces, "mods/immibis/microblocks/api/IMicroblockSupporterTile2");
			super.visit(version, access, name, signature, superName, interfaces);
			
			className = name;
			superclass = superName;
			
			FieldVisitor fv = super.visitField(Opcodes.ACC_PUBLIC, IMCS_FIELD, "Lmods/immibis/microblocks/api/IMicroblockCoverSystem;", null, null);
			fv.visitEnd();
			
			MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "getPartPosition", "(I)Lmods/immibis/microblocks/api/EnumPosition;", null, new String[0]);
			mv.visitCode();
			mv.visitFieldInsn(Opcodes.GETSTATIC, "mods/immibis/microblocks/api/EnumPosition", position, "Lmods/immibis/microblocks/api/EnumPosition;");
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(1, 2);
			mv.visitEnd();
			
			{
				Label l = new Label();
				
				mv = super.visitMethod(Opcodes.ACC_PUBLIC, "isPlacementBlockedByTile", "(Lmods/immibis/microblocks/api/PartType;Lmods/immibis/microblocks/api/EnumPosition;)Z", null, new String[0]);
				mv.visitCode();
				mv.visitFieldInsn(Opcodes.GETSTATIC, "mods/immibis/microblocks/api/EnumPosition", position, "Lmods/immibis/microblocks/api/EnumPosition;");
				mv.visitVarInsn(Opcodes.ALOAD, 2);
				
				// return true if position argument = position of part
				mv.visitJumpInsn(Opcodes.IF_ACMPEQ, l);
				mv.visitInsn(Opcodes.ICONST_0);
				mv.visitInsn(Opcodes.IRETURN);
				
				mv.visitLabel(l);
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitInsn(Opcodes.ICONST_1);
				mv.visitInsn(Opcodes.IRETURN);
				
				mv.visitMaxs(2, 3);
				mv.visitEnd();
			}
			
			{
				Label l = new Label();
				
				mv = super.visitMethod(Opcodes.ACC_PUBLIC, "isPositionOccupiedByTile", "(Lmods/immibis/microblocks/api/EnumPosition;)Z", null, new String[0]);
				mv.visitCode();
				mv.visitFieldInsn(Opcodes.GETSTATIC, "mods/immibis/microblocks/api/EnumPosition", position, "Lmods/immibis/microblocks/api/EnumPosition;");
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				
				// return true if position argument = position of part
				mv.visitJumpInsn(Opcodes.IF_ACMPEQ, l);
				mv.visitInsn(Opcodes.ICONST_0);
				mv.visitInsn(Opcodes.IRETURN);
				
				mv.visitLabel(l);
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitInsn(Opcodes.ICONST_1);
				mv.visitInsn(Opcodes.IRETURN);
				
				mv.visitMaxs(2, 2);
				mv.visitEnd();
			}
			
			mv = super.visitMethod(Opcodes.ACC_PUBLIC, "getCoverSystem", "()Lmods/immibis/microblocks/api/IMicroblockCoverSystem;", null, new String[0]);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0); // 'this'
			mv.visitFieldInsn(Opcodes.GETFIELD, className, IMCS_FIELD, "Lmods/immibis/microblocks/api/IMicroblockCoverSystem;");
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
			
			mv = super.visitMethod(Opcodes.ACC_PUBLIC, "getCoverSystem", "()Lmods/immibis/core/api/multipart/ICoverSystem;", null, new String[0]);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0); // 'this'
			mv.visitFieldInsn(Opcodes.GETFIELD, className, IMCS_FIELD, "Lmods/immibis/microblocks/api/IMicroblockCoverSystem;");
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
			
			mv = super.visitMethod(Opcodes.ACC_PUBLIC, "getPlayerRelativePartHardness", "(L"+ENTITYPLAYER+";I)F", null, new String[0]);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$Util", "getPlayerRelativeTileHardness", "(L"+TILEENTITY+";L"+ENTITYPLAYER+";)F");
			mv.visitInsn(Opcodes.FRETURN);
			mv.visitMaxs(2, 3);
			mv.visitEnd();
			
			if(FMLLaunchHandler.side() == Side.CLIENT) {
				// public void renderPart(RenderBlocks rb, int) {render(rb);}
				mv = super.visitMethod(Opcodes.ACC_PUBLIC, "renderPart", "(L"+RENDERBLOCKS+";I)V", null, new String[0]);
				mv.visitCode();
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "render", "(L"+RENDERBLOCKS+";)V");
				mv.visitInsn(Opcodes.RETURN);
				mv.visitMaxs(2, 3);
				mv.visitEnd();
				
				// public void render(RenderBlocks rb) {Util.render(this, rb);}
				mv = super.visitMethod(Opcodes.ACC_PUBLIC, "render", "(L"+RENDERBLOCKS+";)V", null, new String[0]);
				mv.visitCode();
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$Util", "render", "(L"+TILEENTITY+";L"+RENDERBLOCKS+";)V");
				mv.visitInsn(Opcodes.RETURN);
				mv.visitMaxs(2, 2);
				mv.visitEnd();
			}
			
			// nothing calls getCollidingBoundingBoxes except BlockMultipartBase which isn't involved here, so we can leave it out
			// same for pickPart and isSolidOnSide
			
			mv = super.visitMethod(Opcodes.ACC_PUBLIC, "removePartByPlayer", "(L"+ENTITYPLAYER+";I)Ljava/util/List;", null, new String[0]);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$Util", "removeTileByPlayer", "(L"+TILEENTITY+";L"+ENTITYPLAYER+";)Ljava/util/List;");
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(2, 3);
			mv.visitEnd();
			
			mv = super.visitMethod(Opcodes.ACC_PUBLIC, "getPartAABBFromPool", "(I)L"+AXISALIGNEDBB+";", null, new String[0]);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$Util", "getTileAABBFromPool", "(L"+TILEENTITY+";)L"+AXISALIGNEDBB+";");
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(1, 2);
			mv.visitEnd();
		}
		
		private static class TransformMethodVisitor_NBT extends MethodVisitor {
			public String name;
			
			public TransformMethodVisitor_NBT(MethodVisitor parent, String name) {
				super(Opcodes.ASM4, parent);
				this.name = name;
			}
			
			@Override
			public void visitCode() {
				super.visitCode();
				
				visitVarInsn(Opcodes.ALOAD, 0); // this
				visitVarInsn(Opcodes.ALOAD, 1); // tag
				visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$Util", name, "(L"+TILEENTITY+";L"+NBTTAGCOMPOUND+";)V");
			}
			
			@Override
			public void visitMaxs(int maxStack, int maxLocals) {
				if(maxStack < 2)
					maxStack = 2;
				
				super.visitMaxs(maxStack, maxLocals);
			}
		}
		
		private boolean saw_constructor;
		private boolean saw_getDescriptionPacket;
		private boolean saw_readFromNBT;
		private boolean saw_writeToNBT;
		private boolean saw_onMicroblocksChanged_hook;
		
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			
			if(name.equals("<init>")) {
				// create IMicroblockCoverSystem in constructor
				mv = new MethodVisitor(Opcodes.ASM4, mv) {
					@Override
					public void visitInsn(int opcode) {
						if(opcode == Opcodes.RETURN) {
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitInsn(Opcodes.DUP);
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/api/MicroblockAPIUtils", "createMicroblockCoverSystem", "(Lmods/immibis/microblocks/api/IMicroblockSupporterTile;)Lmods/immibis/microblocks/api/IMicroblockCoverSystem;");
							super.visitFieldInsn(Opcodes.PUTFIELD, className, IMCS_FIELD, "Lmods/immibis/microblocks/api/IMicroblockCoverSystem;");
						}
						
						super.visitInsn(opcode);
					}
					
					@Override
					public void visitMaxs(int maxStack, int maxLocals) {
						if(maxStack < 2)
							maxStack = 2;
						
						super.visitMaxs(maxStack, maxLocals);
					}
				};
				saw_constructor = true;
			}
			
			if(TILEENTITY_GETDESCRIPTIONPACKET.matches(name, desc)) {
				saw_getDescriptionPacket = true;
				
				mv = new MethodVisitor(Opcodes.ASM4, mv) {
					@Override
					public void visitInsn(int opcode) {
						if(opcode == Opcodes.ARETURN) {
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MicroblockSupporterTransformer$Util", "getDescriptionPacket", "(L"+PACKET+";L"+TILEENTITY+";)L"+PACKET+";");
						}
						
						super.visitInsn(opcode);
					}
					
					@Override
					public void visitMaxs(int maxStack, int maxLocals) {
						if(maxStack < 2)
							maxStack = 2;
						
						super.visitMaxs(maxStack, maxLocals);
					}
				};
			}
			
			if(TILEENTITY_READFROMNBT.matches(name, desc)) {
				saw_readFromNBT = true;
				mv = new TransformMethodVisitor_NBT(mv, "readFromNBT");
			}
			
			if(TILEENTITY_WRITETONBT.matches(name, desc)) {
				saw_writeToNBT = true;
				mv = new TransformMethodVisitor_NBT(mv, "writeToNBT");
			}
			
			if(name.equals("ImmibisMicroblocks_onMicroblocksChanged") && desc.equals("()V")) {
				saw_onMicroblocksChanged_hook = true;
			}
			
			if(name.equals("ImmibisMicroblocks_isSideOpen") && desc.equals("(I)Z")) {
				mv.visitCode();
				mv.visitVarInsn(Opcodes.ALOAD, 0); // this
				mv.visitFieldInsn(Opcodes.GETFIELD, className, IMCS_FIELD, "Lmods/immibis/microblocks/api/IMicroblockCoverSystem;");
				mv.visitVarInsn(Opcodes.ILOAD, 1); // side
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "mods/immibis/microblocks/api/IMicroblockCoverSystem", "isSideOpen", "(I)Z");
				mv.visitInsn(Opcodes.IRETURN);
				mv.visitMaxs(2, 2);
				mv.visitEnd();
				
				mv = new MethodVisitor(Opcodes.ASM4) {};
			}
			
			
			
			return mv;
		}
		
		@Override
		public void visitEnd() {
		
			if(!saw_constructor)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, "<init>", "()V");
			
			if(!saw_getDescriptionPacket)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, TILEENTITY_GETDESCRIPTIONPACKET);
			
			if(!saw_readFromNBT)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, TILEENTITY_READFROMNBT);
			
			if(!saw_writeToNBT)
				generateAndTransformMethod(this, superclass, Opcodes.ACC_PUBLIC, TILEENTITY_WRITETONBT);
			
			{
				MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "onMicroblocksChanged", "()V", null, new String[0]);
				mv.visitCode();
				if(saw_onMicroblocksChanged_hook) {
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "ImmibisMicroblocks_onMicroblocksChanged", "()V");
				}
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/immibis/microblocks/coremod/MSTHooks", "onMicroblocksChanged", "(Lnet/minecraft/tileentity/TileEntity;)V");
				mv.visitInsn(Opcodes.RETURN);
				mv.visitMaxs(1, 1);
				mv.visitEnd();
			}
			
			super.visitEnd();
		}
	}
	
	@Deprecated private static String blockMarkerString = "ImmibissMicroblocks_TransformableBlockMarker";
	@Deprecated private static String tileMarkerString = "ImmibissMicroblocks_TransformableTileEntityMarker";
	@Deprecated private static byte[] blockMarkerBytes = blockMarkerString.getBytes(Charset.forName("utf-8"));
	@Deprecated private static byte[] tileMarkerBytes = tileMarkerString.getBytes(Charset.forName("utf-8"));
	private static String blockMarkerString2 = "ImmibisMicroblocks_TransformableBlockMarker";
	private static String tileMarkerString2 = "ImmibisMicroblocks_TransformableTileEntityMarker";
	private static String dummyTEMarkerString = "ImmibisMicroblocks_UseDummyTileMarker";
	private static byte[] blockMarkerBytes2 = blockMarkerString2.getBytes(Charset.forName("utf-8"));
	private static byte[] tileMarkerBytes2 = tileMarkerString2.getBytes(Charset.forName("utf-8"));

	@Override
	public byte[] transform(String originalName, String name, byte[] bytes) {
		
		if(bytes == null)
			return null;
		
		boolean useDummyTE = blockClassesUsingDummyTE.contains(name);
		boolean isBlock = useDummyTE || blockClasses.contains(name);
		boolean isTile = tileClasses.contains(name);
		
		if(!isBlock && !isTile) {
			if(containsBytes(bytes, blockMarkerBytes) || containsBytes(bytes, tileMarkerBytes) || containsBytes(bytes, blockMarkerBytes2) || containsBytes(bytes, tileMarkerBytes2)) {
				
				class CV extends ClassVisitor {
					boolean isBlock, isTile, useDummyTE;
					
					CV() {super(Opcodes.ASM4);}
					
					@Override
					public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
						if(name.equals(blockMarkerString) || name.equals(blockMarkerString2))
							isBlock = true;
						if(name.equals(tileMarkerString) || name.equals(tileMarkerString2))
							isTile = true;
						if(name.equals(dummyTEMarkerString))
							useDummyTE = true;
						return null;
					}
				}
				
				CV cv = new CV();
				new ClassReader(bytes).accept(cv, ClassReader.SKIP_CODE);
				isBlock = cv.isBlock;
				isTile = cv.isTile;
				useDummyTE = cv.useDummyTE;
			}
		}
		
		if(isBlock && isTile)
			throw new AssertionError("Configuration error: "+name+" is recognized as both a block class and a tile entity class");
		
		if(isBlock) {
			ClassWriter cw = new ClassWriter(0);
			new ClassReader(bytes).accept(new BlockTransformerVisitor(new CheckClassAdapter(cw), useDummyTE), 0);
			return cw.toByteArray();
		}
		
		if(isTile) {
			ClassWriter cw = new ClassWriter(0);
			new ClassReader(bytes).accept(new TileTransformerVisitor(new CheckClassAdapter(cw), true), 0);
			return cw.toByteArray();
		}
		
		return bytes;
	}
	
	
	
	private boolean containsBytes(byte[] haystack, byte[] needle) {
		int max = haystack.length - needle.length + 1;
		int i;
		
		for(int k = 0; k < max; k++) {
			for(i = 0; i < needle.length; i++)
				if(haystack[k+i] != needle[i])
					break;
			
			if(i == needle.length)
				return true;
		}
		
		return false;
	}



	public static final String NBT_FIELD_NAME = "ImmibisCoreMicroblocks";
	
	
	// called by generated code
	public static class Util {
		
		public static float getPlayerRelativeTileHardness(TileEntity te, EntityPlayer ply) throws Exception {
			Block block = te.getWorldObj().getBlock(te.xCoord, te.yCoord, te.zCoord);
			
			//Method m = block.getClass().getMethod(BLOCK_GETPLAYERRELATIVEBLOCKHARDNESS_REPLACEMENT, EntityPlayer.class, World.class, int.class, int.class, int.class);
			//return (Float)m.invoke(block, ply, te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
			return ((ITransformedBlock)block).getPlayerRelativeBlockHardness_ImmibisMicroblockTransformer(ply, te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
		}
		
		public static float getPlayerRelativeBlockHardness(EntityPlayer ply, World w, int x, int y, int z) throws Exception {
			if(w.getTileEntity(x, y, z) instanceof IMultipartTile)
				return MSTHooks.getPlayerRelativeBlockHardness(ply, w, x, y, z);
			else {
				Block block = w.getBlock(x, y, z);
				
				//Method m = block.getClass().getMethod(BLOCK_GETPLAYERRELATIVEBLOCKHARDNESS_REPLACEMENT, EntityPlayer.class, World.class, int.class, int.class, int.class);
				//return (Float)m.invoke(block, ply, w, x, y, z);
				return ((ITransformedBlock)block).getPlayerRelativeBlockHardness_ImmibisMicroblockTransformer(ply, w, x, y, z);
			}
		}
		
		@SideOnly(Side.CLIENT)
		public static void render(TileEntity te, RenderBlocks rb) {
			MSTHooks.renderBlock(rb, te.getWorldObj().getBlock(te.xCoord, te.yCoord, te.zCoord), te.xCoord, te.yCoord, te.zCoord);
		}

		
		// Marker itemstack. If the item dropped is this one, then instead of dropping it we do the normal block drops.  
		private static ItemStack dropNormalDrops = new ItemStack(Blocks.air, 0, 0);

		public static List<ItemStack> removeTileByPlayer(TileEntity te, EntityPlayer ply) throws Exception {
			Block block = te.getWorldObj().getBlock(te.xCoord, te.yCoord, te.zCoord);
			
			//List<ItemStack> rv = (List<ItemStack>)block.getClass().getMethod(BLOCK_GETBLOCKDROPPED_REPLACEMENT, World.class, int.class, int.class, int.class, int.class, int.class)
			//		.invoke(block, te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, te.getWorldObj().getBlockMetadata(te.xCoord, te.yCoord, te.zCoord), EnchantmentHelper.getFortuneModifier(ply));
			
			useDefaultRemoveBlockByPlayer = true;
			if(!block.removedByPlayer(te.getWorldObj(), ply, te.xCoord, te.yCoord, te.zCoord)) {
				useDefaultRemoveBlockByPlayer = false;
				return null;
			}
			useDefaultRemoveBlockByPlayer = false;
			
			// if removeBlockByPlayer removed the block, then un-remove it and place the microblock container block
			if(te.getWorldObj().getBlock(te.xCoord, te.yCoord, te.zCoord).isAir(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord) && te instanceof IMultipartTile) {
				ICoverSystem ci = ((IMultipartTile)te).getCoverSystem();
				if(ci != null)
					ci.convertToContainerBlock();
			}
			
			return Collections.singletonList(dropNormalDrops);
		}
		
		// return true to cancel normal block drops
		public static boolean dropBlockAsItemWithChance(World world, int x, int y, int z, float chance) {
			
			if(!(world.getTileEntity(x, y, z) instanceof IMultipartTile))
				return false; // do normal dropping
			
			List<ItemStack> drops = MSTHooks.getDrops();
			if(drops == null || drops.size() == 0)
				return true;
			
			if(drops.size() == 1 && drops.get(0) == dropNormalDrops)
				// player broke the actual block, do normal block dropping
				return false;
			
			// do microblock drops

            for (ItemStack item : drops)
            {
                if (world.rand.nextFloat() <= chance)
                {
                	if (!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))
                    {
                        float var6 = 0.7F;
                        double var7 = (double)(world.rand.nextFloat() * var6) + (double)(1.0F - var6) * 0.5D;
                        double var9 = (double)(world.rand.nextFloat() * var6) + (double)(1.0F - var6) * 0.5D;
                        double var11 = (double)(world.rand.nextFloat() * var6) + (double)(1.0F - var6) * 0.5D;
                        EntityItem var13 = new EntityItem(world, (double)x + var7, (double)y + var9, (double)z + var11, item);
                        var13.delayBeforeCanPickup = 10;
                        world.spawnEntityInWorld(var13);
                    }
                }
            }
			
			return true;
		}
		
		public static AxisAlignedBB getTileAABBFromPool(TileEntity te) {
			Block block = te.getWorldObj().getBlock(te.xCoord, te.yCoord, te.zCoord);
			
			AxisAlignedBB bb;
			if(te.getWorldObj().isRemote) {
				bb = block.getSelectedBoundingBoxFromPool(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
			} else {
				bb = block.getCollisionBoundingBoxFromPool(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
			}
			if(bb == null)
				return AxisAlignedBB.getAABBPool().getAABB(0, 0, 0, 1, 1, 1);
			else
				return bb.offset(-te.xCoord, -te.yCoord, -te.zCoord);
		}
		
		public static Packet getDescriptionPacket(Packet originalPacket, TileEntity te) {
			if(!(te instanceof IMicroblockSupporterTile))
				return originalPacket;
			
			IMicroblockCoverSystem mcs = ((IMicroblockSupporterTile)te).getCoverSystem();
			PacketMicroblockContainerDescription pmcd;
			
			/*if(originalPacket != null) {
				pmcd = new PacketMicroblockDescriptionWithWrapping();
				((PacketMicroblockDescriptionWithWrapping)pmcd).wrappedPacket = originalPacket;
			} else {
				pmcd = new PacketMicroblockContainerDescription();
			}*/
			// TODO: Make this actually work.
			
			pmcd = new PacketMicroblockContainerDescription();
			pmcd.x = te.xCoord;
			pmcd.y = te.yCoord;
			pmcd.z = te.zCoord;
			pmcd.data = mcs.writeDescriptionBytes();
			APILocator.getNetManager().sendToClientDimension(pmcd, te.getWorldObj().provider.dimensionId);
			//Packet wrapped = APILocator.getNetManager().wrap(pmcd);
			//wrapped.isChunkDataPacket = true;
			return originalPacket;
		}
		
		public static void writeToNBT(TileEntity te, NBTTagCompound tag) {
			if(te instanceof IMicroblockSupporterTile) {
				IMicroblockCoverSystem imcs = ((IMicroblockSupporterTile)te).getCoverSystem();
				
				if(imcs != null) {
					NBTTagCompound csTag = new NBTTagCompound();
					imcs.writeToNBT(csTag);
					tag.setTag(NBT_FIELD_NAME, csTag);
				}
			}
		}
		
		public static void readFromNBT(TileEntity te, NBTTagCompound tag) {
			if(te instanceof IMicroblockSupporterTile) {
				IMicroblockCoverSystem imcs = ((IMicroblockSupporterTile)te).getCoverSystem();
			
				if(imcs != null && tag.hasKey(NBT_FIELD_NAME))
					imcs.readFromNBT(tag.getCompoundTag(NBT_FIELD_NAME));
			}
		}
		
		public static MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 src, Vec3 dst) throws Throwable {
			TileEntity te = world.getTileEntity(x, y, z);
			
			MovingObjectPosition ciPos = null;	
			
			if(te instanceof IMultipartTile) {
				ICoverSystem ci = ((IMultipartTile)te).getCoverSystem();
				if(ci != null)
					ciPos = ci.collisionRayTrace(src, dst);
			}
			
			Block block = world.getBlock(x, y, z);
			
			//MovingObjectPosition tilePos = (MovingObjectPosition)block.getClass()
			//		.getMethod(BLOCK_COLLISIONRAYTRACE_REPLACEMENT, World.class, int.class, int.class, int.class, Vec3.class, Vec3.class)
			//		.invoke(block, world, x, y, z, src, dst);
			
			MovingObjectPosition tilePos = ((ITransformedBlock)block).collisionRayTrace_ImmibisMicroblockTransformer(world, x, y, z, src, dst);
			
			if(tilePos != null && SubhitValues.isCoverSystem(tilePos.subHit))
				throw new RuntimeException("MicroblockSupporterTransformer cannot be used on this block - its \"selected part\" values conflict with the ones microblocks use. Offending block was "+block+" named " + Block.blockRegistry.getNameForObject(block) +". subHit value was "+tilePos.subHit);
			
			//if(tilePos != null)
				//tilePos.subHit = 0;
			
			if(tilePos == null) return ciPos;
			if(ciPos == null) return tilePos;
			
			double ciDist = ciPos.hitVec.squareDistanceTo(src);
			double tileDist = tilePos.hitVec.squareDistanceTo(src);
			
			return ciDist < tileDist ? ciPos : tilePos;
		}
		
		public static boolean isSolidOnSide(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
			TileEntity te = world.getTileEntity(x, y, z);
			if(!(te instanceof IMultipartTile))
				return false;
			
			ICoverSystem ci = ((IMultipartTile)te).getCoverSystem();
			if(ci == null)
				return false;
			
			return ci.isSolidOnSide(side);
		}
		
		public static ItemStack pickPart(MovingObjectPosition rayTrace, World w, int x, int y, int z) {
			if(!SubhitValues.isCoverSystem(rayTrace.subHit))
				return null;
			
			TileEntity te = w.getTileEntity(x, y, z);
			if(!(te instanceof IMultipartTile))
				return null;
			
			ICoverSystem ci = ((IMultipartTile)te).getCoverSystem();
			if(ci == null)
				return null;
			return ci.pickPart(rayTrace, SubhitValues.getCSPartIndex(rayTrace.subHit));
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB mask, List list) {
			TileEntity te = w.getTileEntity(x, y, z);
			if(!(te instanceof IMultipartTile))
				return;
			
			ICoverSystem ci = ((IMultipartTile)te).getCoverSystem();
			if(ci != null)
				ci.getCollidingBoundingBoxes(mask, list);
		}
		
		private static boolean useDefaultRemoveBlockByPlayer = false;
		
		// returns false to run default code
		public static boolean removeBlockByPlayer(World w, EntityPlayer pl, int x, int y, int z) {
			if(useDefaultRemoveBlockByPlayer)
				return false;
			
			if(!(w.getTileEntity(x, y, z) instanceof IMultipartTile))
				return false;
			
			MSTHooks.onRemovedByPlayer(w, pl, x, y, z);
			return true;
		}
	}
}
