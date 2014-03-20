package mods.immibis.microblocks.coremod;

import java.io.IOException;

import net.minecraft.launchwrapper.LaunchClassLoader;

import org.objectweb.asm.commons.Remapper;

import com.google.common.collect.BiMap;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.relauncher.ReflectionHelper;

class MethodMatcher {
	String owner_mcp;
	String owner_obf;
	String name_mcp;
	String name_searge;
	String name_obf;
	String desc_mcp;
	String desc_obf;
	
	private boolean deobfFieldsInited;
	private void initMyDeobf()
	{
		if(!deobfFieldsInited && isDeobfDataAvailable())
		{
			owner_obf = classMapper_MCPtoObf.mapType(owner_mcp);
			desc_obf = classMapper_MCPtoObf.mapMethodDesc(desc_mcp);
		}
	}
	
	// owner and desc are MCP names
	MethodMatcher(String owner, String mcp, String searge, String obf, String desc) {
		owner_mcp = owner;
		owner_obf = owner_mcp;
		name_mcp = mcp;
		name_searge = searge;
		name_obf = obf;
		desc_mcp = desc;
		desc_obf = desc_mcp;
		
		if(obf == null || obf.equals(""))
			throw new IllegalArgumentException();
	}
	
	static boolean MCP;
	static Remapper classMapper_MCPtoObf;
	static {
		try {
			MCP = ((LaunchClassLoader)MethodMatcher.class.getClassLoader()).getClassBytes("net.minecraft.world.World") != null;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		classMapper_MCPtoObf = new Remapper() {
			@Override
			public String map(String typeName) {
				String result = FMLDeobfuscatingRemapper.INSTANCE.unmap(typeName);
				//System.out.println(typeName+" -> "+result);
				return result;
			}
		};
	}
	
	boolean matches(String name, String desc) {
		
		initMyDeobf();
		
		boolean result;
		if(MCP)
			result = name.equals(name_mcp) && desc.equals(desc_mcp);
		else
			result = (name.equals(name_obf) || name.equals(name_searge)) && (desc.equals(desc_mcp) || desc.equals(desc_obf));
		
		//System.out.println(name+desc+" matches ["+name_mcp+"|"+name_searge+"|"+name_obf+"]["+desc_mcp+"|"+desc_obf+"]? "+result);
		
		return result;
	}
	
	String getName() {
		initMyDeobf();
		return MCP ? name_mcp : name_searge;
	}
	
	String getDesc() {
		initMyDeobf();
		return MCP ? desc_mcp : desc_mcp; // searge class names = MCP class names
	}
	
	private static boolean deobfDataAvailable;
	private static boolean isDeobfDataAvailable()
	{
		if(deobfDataAvailable)
			return true;
		if(ReflectionHelper.<BiMap<String, String>, FMLDeobfuscatingRemapper>getPrivateValue(FMLDeobfuscatingRemapper.class, FMLDeobfuscatingRemapper.INSTANCE, "classNameBiMap") != null) {
			deobfDataAvailable = true;
			return true;
		}
		return false;
	}
}
