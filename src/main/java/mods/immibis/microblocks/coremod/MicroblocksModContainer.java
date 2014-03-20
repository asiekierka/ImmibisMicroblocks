package mods.immibis.microblocks.coremod;


import java.io.File;
import java.util.Arrays;

import mods.immibis.microblocks.ModProperties;

import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.client.FMLFileResourcePack;
import cpw.mods.fml.client.FMLFolderResourcePack;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.versioning.VersionParser;
import cpw.mods.fml.common.versioning.VersionRange;

public class MicroblocksModContainer extends DummyModContainer {

	public MicroblocksModContainer() {
		super(new ModMetadata());
		ModMetadata md = getMetadata();
		
		md.modId = ModProperties.MODID;
		md.name = ModProperties.MOD_NAME;
		md.version = ModProperties.MOD_VERSION;
		md.credits = "";
		md.authorList = Arrays.asList("immibis");
		md.description = "";
		md.url = "http://www.minecraftforum.net/topic/1001131-110-immibiss-mods-smp/";
		md.updateUrl = "";
		md.screenshots = new String[0];
		md.logoFile = "";
	}
	
	@Override
	public Class<?> getCustomResourcePackClass() {
		// cpw you are being silly if this is the only way to include resources in a coremod
		
		for(ModContainer mc : Loader.instance().getModList()) {
			if(mc.getModId().equals(getModId())) {
				System.out.println("IMMIBIS MICROBLOCKS DEBUG: Mod source is "+mc.getSource());
				return mc.getSource().isDirectory() ? FMLFolderResourcePack.class : FMLFileResourcePack.class;
			}
		}
		
		// if we just throw an exception, it gets swallowed AND breaks loading. genius!
		new Exception("Couldn't locate mod ID "+getModId()+" in mod list").printStackTrace();
		System.exit(1);
		return null;
	}
	
	@Override
	public VersionRange acceptableMinecraftVersionRange() {
		return VersionParser.parseRange("[1.7.2]");
	}
	
	private LoadController controller;
	
	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		this.controller = controller;
		return true;
	}
	
	@Subscribe
	public void construct(FMLConstructionEvent evt) {
		
	}
	
	@Subscribe
	public void preinit(FMLPreInitializationEvent evt) {
		BridgeClass1.isMinecraftLoaded = true;
		try {
			BridgeClass2.preinit(evt);
		} catch(Throwable t) {
			controller.errorOccurred(this, t);
            Throwables.propagateIfPossible(t);
		}
	}
	
	@Subscribe
	public void init(FMLInitializationEvent evt) {
		try {
			BridgeClass2.init(evt);
		} catch(Throwable t) {
			controller.errorOccurred(this, t);
            Throwables.propagateIfPossible(t);
		}
	}
	
	@Subscribe
	public void postinit(FMLPostInitializationEvent evt) {
		try {
			BridgeClass2.postinit(evt);
		} catch(Throwable t) {
			controller.errorOccurred(this, t);
            Throwables.propagateIfPossible(t);
		}
	}
	
	@Subscribe
	public void receiveIMC(IMCEvent evt) {
		try {
			BridgeClass2.receiveIMC(evt);
		} catch(Throwable t) {
			controller.errorOccurred(this, t);
			Throwables.propagateIfPossible(t);
		}
	}
	
	// Q: why doesn't FML keep track of this for us like it's supposed to?
	// A: because it's FML
	@Override
	public File getSource() {
		return MicroblocksCoreMod.coremodLocation;
	}
}
