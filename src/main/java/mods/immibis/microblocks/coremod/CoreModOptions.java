package mods.immibis.microblocks.coremod;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import mods.immibis.microblocks.coremod.OptionsFile.BooleanOption;
import mods.immibis.microblocks.coremod.OptionsFile.ItemAndMetaListOption;
import mods.immibis.microblocks.coremod.OptionsFile.StringSetOption;
import mods.immibis.microblocks.coremod.OptionsFile.ItemListOption.ItemID;

import com.google.common.collect.ImmutableSet;

public class CoreModOptions {
	public static boolean autoDetectCuttableBlocks;
	public static List<ItemID> manualCuttableBlocks;
	public static Collection<? extends String> transformerClasses;
	public static boolean enableNEI;
	public static boolean forceDisableMCPCCompat;
	//public static boolean replaceVanillaBlocks;
	
	public static final ImmutableSet<String> DEFAULT_BLOCK_CLASSES = ImmutableSet.of(
		"buildcraft.transport.BlockGenericPipe",
		"ic2.core.block.wiring.BlockCable",
		"appeng.common.base.AppEngMultiBlock",
		"powercrystals.minefactoryreloaded.block.BlockRedNetCable",
		"micdoodle8.mods.galacticraft.core.blocks.GCCoreBlockOxygenPipe",
		"thermalexpansion.block.conduit.BlockConduit"
		//"net.minecraft.block.BlockChest",
		//"net.minecraft.block.BlockSign",
		//"net.minecraft.block.BlockSkull",
		//"net.minecraft.block.BlockEnderChest"
	);
	public static final ImmutableSet<String> DEFAULT_TILE_CLASSES = ImmutableSet.of(
		"buildcraft.transport.TileGenericPipe",
		"ic2.core.block.wiring.TileEntityCable",
		"appeng.me.tile.TileCable",
		"appeng.me.tile.TileDarkCable",
		"appeng.me.tile.TileInputCable",
		"appeng.me.tile.TileOutputCable",
		"appeng.me.tile.TileLevelEmitter",
		"appeng.me.tile.TileStorageBus",
		"appeng.me.tile.TileColorlessCable",
		"powercrystals.minefactoryreloaded.tile.rednet.TileEntityRedNetCable",
		"micdoodle8.mods.galacticraft.core.tile.GCCoreTileEntityOxygenPipe",
		"thermalexpansion.block.conduit.energy.TileConduitEnergy",
		"thermalexpansion.block.conduit.fluid.TileConduitFluid",
		"thermalexpansion.block.conduit.item.TileConduitItem",
		"mods.immibis.microblocks.TileMicroblockContainerDefaultTE"
		//"net.minecraft.tileentity.TileEntityChest",
		//"net.minecraft.tileentity.TileEntitySign",
		//"net.minecraft.tileentity.TileEntitySkull",
		//"net.minecraft.tileentity.TileEntityEnderChest"
	);
	public static final ImmutableSet<String> DEFAULT_TRANSFORMERS = ImmutableSet.of(
		"mods.immibis.microblocks.crossmod.MicroblocksBCTransformer"
	);
	public static final ImmutableSet<String> DEFAULT_BLOCKS_USING_DUMMY_TE = ImmutableSet.of(
		//"net.minecraft.block.BlockTorch",
		//"net.minecraft.block.BlockFlowerPot",
		//"net.minecraft.block.BlockLadder"
	);
	
	private static boolean loaded;
	private static File configFile;
	private static File configDir;
	
	private static OptionsFile of;
	
	private static void updateConfig() throws IOException {
		File oldConfigFile = new File(configDir, "immibis-coremod.cfg");
		if(oldConfigFile.exists()) {
			Properties props = new Properties();
			
			try {
				FileReader fr = new FileReader(oldConfigFile);
				try {
					props.load(fr);
				} finally {
					fr.close();
				}
			} catch(IOException ex) {
				throw new RuntimeException(ex);
			}
			
			StringSetOption blockClasses = new StringSetOption("blockClass");
			StringSetOption tileClasses = new StringSetOption("tileEntityClass");
			BooleanOption autoDetect = new BooleanOption("autoDetectCuttableBlocks");
			
			OptionsFile of = new OptionsFile();
			of.addOption(blockClasses);
			of.addOption(tileClasses);
			of.addOption(autoDetect);
			
			blockClasses.addValues(Arrays.asList(props.getProperty("microblockTransformer.blockClasses").split(";")));
			tileClasses.addValues(Arrays.asList(props.getProperty("microblockTransformer.tileEntityClasses").split(";")));
			autoDetect.set(props.getProperty("autoDetectCuttableBlocks", "false").equals("true"));
			
			of.write(configFile);
			
			oldConfigFile.delete();
		}
	}
	
	public static void load(File minecraftDir) {
		if(loaded)
			return;
		
		loaded = true;
		
		configDir = new File(minecraftDir, "config");
		if(!configDir.exists() && !configDir.mkdirs())
			throw new RuntimeException("Couldn't create directory: "+configDir);
		configFile = new File(configDir, "immibis-microblocks.cfg");
		
		try {
			updateConfig();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		StringSetOption blockClasses = new StringSetOption("blockClass", DEFAULT_BLOCK_CLASSES);
		StringSetOption tileClasses = new StringSetOption("tileEntityClass", DEFAULT_TILE_CLASSES);
		StringSetOption transformers = new StringSetOption("transformer", DEFAULT_TRANSFORMERS);
		StringSetOption blockClassesDummyTE = new StringSetOption("blockClassUsingDummyTile", DEFAULT_BLOCKS_USING_DUMMY_TE);
		BooleanOption autoDetect = new BooleanOption("autoDetectCuttableBlocks", false);
		BooleanOption enableNEIOption = new BooleanOption("enableNEI", true);
		ItemAndMetaListOption cuttableBlocks = new ItemAndMetaListOption("cuttableBlock");
		BooleanOption forceDisableMCPCCompatOption = new BooleanOption("forceDisableMCPCCompat", false);
		//BooleanOption replaceVanillaBlocksOption = new BooleanOption("replaceVanillaBlocks", false);
		
		of = new OptionsFile();
		of.addOption(blockClasses);
		of.addOption(tileClasses);
		of.addOption(transformers);
		of.addOption(blockClassesDummyTE);
		of.addOption(autoDetect);
		of.addOption(enableNEIOption);
		of.addOption(cuttableBlocks);
		of.addOption(forceDisableMCPCCompatOption);
		//of.addOption(replaceVanillaBlocksOption);
		
		if(configFile.exists()) {
			try {
				of.read(configFile);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		manualCuttableBlocks = cuttableBlocks.get();
		autoDetectCuttableBlocks = autoDetect.get();
		transformerClasses = transformers.get();
		enableNEI = enableNEIOption.get();
		forceDisableMCPCCompat = forceDisableMCPCCompatOption.get();
		//replaceVanillaBlocks = replaceVanillaBlocksOption.get();
		MicroblockSupporterTransformer.blockClasses.addAll(blockClasses.get());
		MicroblockSupporterTransformer.tileClasses.addAll(tileClasses.get());
		MicroblockSupporterTransformer.blockClassesUsingDummyTE.addAll(blockClassesDummyTE.get());
		
		save();
	}
	
	public static void save() {
		of.<ItemAndMetaListOption>getOption("cuttableBlock").set(manualCuttableBlocks);
		of.<BooleanOption>getOption("autoDetectCuttableBlocks").set(autoDetectCuttableBlocks);
		if(OptionsFile.DEBUG)
			System.out.println("[Immibis's Microblocks DEBUG] Saving config file");
		try {
			of.write(configFile);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
}
