package mods.immibis.microblocks.coremod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.launchwrapper.LaunchClassLoader;
import mods.immibis.core.api.CoremodMarker;
import mods.immibis.core.api.FMLModInfo;
import mods.immibis.microblocks.ModProperties;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@FMLModInfo(
	modid=ModProperties.MODID,
	name=ModProperties.MOD_NAME,
	url="http://www.minecraftforum.net/topic/1001131-110-immibiss-mods-smp/",
	description="",
	authors="immibis"
	)
@CoremodMarker(version=ModProperties.MOD_VERSION)
public class MicroblocksCoreMod implements IFMLLoadingPlugin {

	public final static boolean MCP = MicroblocksCoreMod.class.getClassLoader().getResource("mods/immibis/microblocks/MicroblockSystem.class").toString().endsWith("/bin/mods/immibis/microblocks/MicroblockSystem.class");
	
	// if true, the mod will pretend not to exist, and will prevent all non-API classes from loading.
	public final static boolean TEST_DISABLED = MCP && false;

	@Override
	public String[] getASMTransformerClass() {
		if(TEST_DISABLED)
			return new String[] {
				"mods.immibis.microblocks.coremod.MicroblockSupporterTransformer",
				"mods.immibis.microblocks.coremod.DisableMicroblocksTransformer"
			};
		else {
			List<String> s = new ArrayList<String>();
			s.add("mods.immibis.microblocks.coremod.MicroblockSupporterTransformer");
			
			// can't add these here, getASMTransformerClass is called before injectData
			//s.addAll(CoreModOptions.transformerClasses);
			
			return (String[])s.toArray(new String[s.size()]);
		}
	}

	@Override
	public String getModContainerClass() {
		if(TEST_DISABLED)
			return null;
		else
			return "mods.immibis.microblocks.coremod.MicroblocksModContainer";
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		if(!TEST_DISABLED) {
			CoreModOptions.load((java.io.File)data.get("mcLocation"));
			
			// XXX hack, see above comment in getASMTransformerClass
			for(String s : CoreModOptions.transformerClasses)
				((LaunchClassLoader)MicroblocksCoreMod.class.getClassLoader()).registerTransformer(s);
		} 
		coremodLocation = (File)data.get("coremodLocation");
	}
	
	public static File coremodLocation;

	@Override
	public String getAccessTransformerClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
