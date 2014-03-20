package mods.immibis.microblocks.coremod;

public class DisableMicroblocksTransformer implements net.minecraft.launchwrapper.IClassTransformer {

	@Override
	public byte[] transform(String originalName, String name, byte[] bytes) {
		if(name.startsWith("mods.immibis.microblocks.api"))
			return bytes;
		if(name.startsWith("mods.immibis.microblocks.coremod"))
			return bytes;
		if(name.equals("mods.immibis.microblocks.MicroblocksNonCoreMod"))
			return bytes;
		if(name.startsWith("mods.immibis.microblocks"))
			return null;
		return bytes;
	}

}
