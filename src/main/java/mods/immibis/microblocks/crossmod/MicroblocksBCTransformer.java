package mods.immibis.microblocks.crossmod;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import mods.immibis.microblocks.coremod.MicroblockSupporterTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class MicroblocksBCTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2) {
		if(arg0.equals("buildcraft.transport.TileGenericPipe")) {
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			new ClassReader(arg2).accept(new ClassVisitor(Opcodes.ASM4, cw) {
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
					if(name.equals("canPipeConnect")) {
						if(!desc.startsWith("(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraftforge/common/util/ForgeDirection;")) {
							new Exception("Immibis's Microblocks BC compatibility will not work on this version of BC. This is not a critical error, but microblocks will not stop BC pipes from connecting. You should report this. Technical data: canPipeConnect desc="+desc).printStackTrace();
							return super.visitMethod(access, name, desc, signature, exceptions);
						}
						
						return new MethodVisitor(Opcodes.ASM4, super.visitMethod(access, name, desc, signature, exceptions)) {
							@Override
							public void visitMethodInsn(int opcode, String owner, String name, String desc) {
								
								if(owner.equals("buildcraft/transport/TileGenericPipe") && name.equals("hasPlug")) {
									if(!desc.equals("(Lnet/minecraftforge/common/util/ForgeDirection;)Z"))
										new Exception("Immibis's Microblocks BC compatibility will not work on this version of BC. This is not a critical error, but microblocks will not stop BC pipes from connecting. You should report this. Technical data: hasPlug desc="+desc).printStackTrace();
									else
										name = "ImmibisMicroblocksBCCompat_hasPlugOrCover";
								}
								
								super.visitMethodInsn(opcode, owner, name, desc);
							}
						};
					}
					
					if(name.equals("onMicroblocksChanged")) { // this exists because we run after MicroblockSupporterTransformer
						return new MethodVisitor(Opcodes.ASM4, super.visitMethod(access, name, desc, signature, exceptions)) {
							@Override
							public void visitCode() {
								super.visitCode();
								super.visitVarInsn(Opcodes.ALOAD, 0);
								super.visitInsn(Opcodes.ICONST_1);
								super.visitFieldInsn(Opcodes.PUTFIELD, "buildcraft/transport/TileGenericPipe", "blockNeighborChange", "Z");
							}
						};
					}
					
					return super.visitMethod(access, name, desc, signature, exceptions);
				}
				
				@Override
				public void visitEnd() {
					
					Label retTrueLbl = new Label();
					
					MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "ImmibisMicroblocksBCCompat_hasPlugOrCover", "(Lnet/minecraftforge/common/util/ForgeDirection;)Z", null, new String[0]);
					mv.visitCode();
					
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "buildcraft/transport/TileGenericPipe", "hasPlug", "(Lnet/minecraftforge/common/util/ForgeDirection;)Z");
					mv.visitJumpInsn(Opcodes.IFNE, retTrueLbl);
					
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/transport/TileGenericPipe", MicroblockSupporterTransformer.IMCS_FIELD, "Lmods/immibis/microblocks/api/IMicroblockCoverSystem;");
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraftforge/common/util/ForgeDirection", "ordinal", "()I");
					mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "mods/immibis/microblocks/api/IMicroblockCoverSystem", "isSideOpen", "(I)Z");
					mv.visitInsn(Opcodes.ICONST_1);
					mv.visitInsn(Opcodes.IXOR);
					mv.visitInsn(Opcodes.IRETURN);
					
					mv.visitLabel(retTrueLbl);
					mv.visitInsn(Opcodes.ICONST_1);
					mv.visitInsn(Opcodes.IRETURN);
					
					mv.visitMaxs(3, 2);
					mv.visitEnd();
					super.visitEnd();
				}
				
			}, 0);
			return cw.toByteArray();
		}
		
		return arg2;
	}

}
