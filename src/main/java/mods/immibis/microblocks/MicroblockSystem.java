package mods.immibis.microblocks;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import mods.immibis.core.BlockMetaPair;
import mods.immibis.core.ImmibisCore;
import mods.immibis.core.api.APILocator;
import mods.immibis.core.api.multipart.util.BlockMultipartBase;
import mods.immibis.core.api.net.IPacket;
import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.microblocks.api.EnumPartClass;
import mods.immibis.microblocks.api.IMicroblockCoverSystem;
import mods.immibis.microblocks.api.IMicroblockPermissionHandler;
import mods.immibis.microblocks.api.IMicroblockSupporterTile;
import mods.immibis.microblocks.api.IMicroblockSystem2;
import mods.immibis.microblocks.api.Part;
import mods.immibis.microblocks.api.PartType;
import mods.immibis.microblocks.coremod.BridgeClass1;
import mods.immibis.microblocks.coremod.CoreModOptions;
import mods.immibis.microblocks.coremod.MSTHooks;
import mods.immibis.microblocks.coremod.OptionsFile.ItemListOption.ItemID;
import mods.immibis.microblocks.crossmod.MCPCMicroblockPermissionHandler;
import mods.immibis.microblocks.recipes.RecipeCombineSeveral;
import mods.immibis.microblocks.recipes.RecipeCombineTwo;
import mods.immibis.microblocks.recipes.RecipeHollowCover;
import mods.immibis.microblocks.recipes.RecipeHorizontalCut;
import mods.immibis.microblocks.recipes.RecipeUnHollowCover;
import mods.immibis.microblocks.recipes.RecipeVerticalCut;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.FMLRelaunchLog;

public class MicroblockSystem implements IMicroblockSystem2 {
	
	/*
	 * Part IDs are bitfields.
	 * 
	 * Bits 20-31 are block ID. (12 bits)
	 * If block ID is nonzero, part is an auto-detected or third-party one.
	 * 		10-19 are ID-specific. (10 bits)
	 * 		bit 9 is 1 for parts added by the system, 0 for other parts.
	 * 		bits 5-8 are reserved.
	 * 		bits 3-4 are part type.
	 * 		bits 0-2 are part size. 
	 * 
	 * If block ID is zero, part is one that was manually added in postinit.
	 * 		bits 5-19 are manually chosen ID.
	 * 		bits 3-4 are part type.
	 * 		bits 0-2 are part size.
	 * 
	 * All vanilla-based parts are manually added in postinit.
	 */
	public final static HashMap<Integer, PartType<?>> parts = new HashMap<Integer, PartType<?>>();
	
	public static BlockMultipartBase microblockContainerBlock;
	public static ItemSaw itemSaw;
	
	public static final String CHANNEL = "ImmibisMicro";
	
	public static final byte PKT_S2C_MICROBLOCK_CONTAINER_DESCRIPTION = 0;
	public static final byte PKT_S2C_MICROBLOCK_DESCRIPTION_WITH_WRAPPING = 1;
	public static final byte PKT_C2S_MICROBLOCK_PLACE = 2;
	public static final byte PKT_S2C_DUMMY_TE_DESC = 3;
	public static final byte PKT_S2C_DUMMY_TE_DESTROY = 4;
	
	
	public static MicroblockSystem instance;
	
	public static ArrayList<Integer> neiPartIDs = new ArrayList<Integer>();
	public static int neiMaxDamage = 0;
	
	public void postinit() {
		registerManualParts(1, Blocks.stone);
		registerManualParts(2, Blocks.grass, PartTypeGrass.class);
		registerManualParts(3, Blocks.dirt);
		registerManualParts(4, Blocks.cobblestone);
		registerManualParts(5, Blocks.planks, 0);
		registerManualParts(6, Blocks.planks, 1);
		registerManualParts(7, Blocks.planks, 2);
		registerManualParts(8, Blocks.planks, 3);
		registerManualParts(9, Blocks.bedrock);
		registerManualParts(10, Blocks.sand);
		registerManualParts(11, Blocks.gravel);
		registerManualParts(12, Blocks.gold_ore);
		registerManualParts(13, Blocks.iron_ore);
		registerManualParts(14, Blocks.coal_ore);
		registerManualParts(15, Blocks.planks, 0);
		registerManualParts(16, Blocks.planks, 1);
		registerManualParts(17, Blocks.planks, 2);
		registerManualParts(18, Blocks.planks, 3);
		registerManualParts(19, Blocks.leaves, 0, PartTypeLeaves.class);
		registerManualParts(20, Blocks.leaves, 1, PartTypeLeaves.class);
		registerManualParts(21, Blocks.leaves, 2, PartTypeLeaves.class);
		registerManualParts(22, Blocks.leaves, 3, PartTypeLeaves.class);
		registerManualParts(23, Blocks.sponge);
		registerManualParts(24, Blocks.glass);
		registerManualParts(25, Blocks.lapis_ore);
		registerManualParts(26, Blocks.lapis_block);
		registerManualParts(27, Blocks.dispenser);
		registerManualParts(28, Blocks.sandstone);
		registerManualParts(29, Blocks.noteblock);
		registerManualParts(30, Blocks.sticky_piston);
		registerManualParts(31, Blocks.piston);
		registerManualParts(32, Blocks.wool, 0);
		registerManualParts(33, Blocks.wool, 1);
		registerManualParts(34, Blocks.wool, 2);
		registerManualParts(35, Blocks.wool, 3);
		registerManualParts(36, Blocks.wool, 4);
		registerManualParts(37, Blocks.wool, 5);
		registerManualParts(38, Blocks.wool, 6);
		registerManualParts(39, Blocks.wool, 7);
		registerManualParts(40, Blocks.wool, 8);
		registerManualParts(41, Blocks.wool, 9);
		registerManualParts(42, Blocks.wool, 10);
		registerManualParts(43, Blocks.wool, 11);
		registerManualParts(44, Blocks.wool, 12);
		registerManualParts(45, Blocks.wool, 13);
		registerManualParts(46, Blocks.wool, 14);
		registerManualParts(47, Blocks.wool, 15);
		registerManualParts(48, Blocks.gold_block);
		registerManualParts(49, Blocks.iron_block);
		registerManualParts(50, Blocks.brick_block);
		registerManualParts(51, Blocks.tnt);
		registerManualParts(52, Blocks.bookshelf);
		registerManualParts(53, Blocks.mossy_cobblestone);
		registerManualParts(54, Blocks.obsidian);
		registerManualParts(55, Blocks.mob_spawner);
		registerManualParts(56, Blocks.diamond_ore);
		registerManualParts(57, Blocks.diamond_block);
		registerManualParts(58, Blocks.crafting_table);
		registerManualParts(59, Blocks.furnace);
		registerManualParts(60, Blocks.redstone_ore);
		registerManualParts(61, Blocks.snow);
		registerManualParts(62, Blocks.clay);
		registerManualParts(63, Blocks.jukebox);
		registerManualParts(64, Blocks.pumpkin);
		registerManualParts(65, Blocks.netherrack);
		registerManualParts(66, Blocks.soul_sand);
		registerManualParts(67, Blocks.glowstone);
		registerManualParts(68, Blocks.pumpkin);
		registerManualParts(69, Blocks.stonebrick);
		registerManualParts(70, Blocks.melon_block);
		registerManualParts(71, Blocks.mycelium);
		registerManualParts(72, Blocks.nether_brick);
		registerManualParts(73, Blocks.end_stone, 0);
		registerManualParts(74, Blocks.end_stone, 1);
		registerManualParts(75, Blocks.emerald_ore);
		registerManualParts(76, Blocks.emerald_block);
		registerManualParts(77, Blocks.command_block);
		registerManualParts(78, Blocks.sandstone, 1);
		registerManualParts(79, Blocks.sandstone, 2);
		registerManualParts(80, Blocks.redstone_lamp);
		registerManualParts(81, Blocks.stonebrick, 1);
		registerManualParts(82, Blocks.stonebrick, 2);
		registerManualParts(83, Blocks.stonebrick, 3);
		registerManualParts(84, Blocks.redstone_block);
		registerManualParts(85, Blocks.quartz_ore);
		registerManualParts(86, Blocks.quartz_block, 0);
		registerManualParts(87, Blocks.quartz_block, 1);
		registerManualParts(88, Blocks.quartz_block, 2);
		registerManualParts(89, Blocks.dropper);
		registerManualParts(90, Blocks.double_stone_slab, 0, Blocks.double_stone_slab, 0, PartTypeDefault.class);
		//registerManualParts(91, Blocks.stoneDoubleSlab, 1, Blocks.stoneSingleSlab, 1);
		//registerManualParts(92, Blocks.stoneDoubleSlab, 3, Blocks.stoneSingleSlab, 3);
		//registerManualParts(93, Blocks.stoneDoubleSlab, 4, Blocks.stoneSingleSlab, 4);
		//registerManualParts(94, Blocks.stoneDoubleSlab, 5, Blocks.stoneSingleSlab, 5);
		//registerManualParts(95, Blocks.stoneDoubleSlab, 6, Blocks.stoneSingleSlab, 6);
		//registerManualParts(96, Blocks.stoneDoubleSlab, 7, Blocks.stoneSingleSlab, 7);
		//registerManualParts(97, Blocks.planksDoubleSlab, 0, Blocks.planksSingleSlab, 0);
		//registerManualParts(98, Blocks.planksDoubleSlab, 1, Blocks.planksSingleSlab, 1);
		//registerManualParts(99, Blocks.planksDoubleSlab, 2, Blocks.planksSingleSlab, 2);
		//registerManualParts(100, Blocks.planksDoubleSlab, 3, Blocks.planksSingleSlab, 3);
		registerManualParts(101, Blocks.stained_hardened_clay, 0);
		registerManualParts(102, Blocks.stained_hardened_clay, 1);
		registerManualParts(103, Blocks.stained_hardened_clay, 2);
		registerManualParts(104, Blocks.stained_hardened_clay, 3);
		registerManualParts(105, Blocks.stained_hardened_clay, 4);
		registerManualParts(106, Blocks.stained_hardened_clay, 5);
		registerManualParts(107, Blocks.stained_hardened_clay, 6);
		registerManualParts(108, Blocks.stained_hardened_clay, 7);
		registerManualParts(109, Blocks.stained_hardened_clay, 8);
		registerManualParts(110, Blocks.stained_hardened_clay, 9);
		registerManualParts(111, Blocks.stained_hardened_clay, 10);
		registerManualParts(112, Blocks.stained_hardened_clay, 11);
		registerManualParts(113, Blocks.stained_hardened_clay, 12);
		registerManualParts(114, Blocks.stained_hardened_clay, 13);
		registerManualParts(115, Blocks.stained_hardened_clay, 14);
		registerManualParts(116, Blocks.stained_hardened_clay, 15);
		registerManualParts(117, Blocks.hay_block);
		registerManualParts(118, Blocks.hardened_clay);
		registerManualParts(119, Blocks.coal_block);
		// 1.7.2
		registerManualParts(120, Blocks.packed_ice);
		for(int i = 0; i < 16; i++) // 121-135
			registerManualParts(121+i, Blocks.stained_glass, i);
		
		
		if(CoreModOptions.autoDetectCuttableBlocks) {
			CoreModOptions.manualCuttableBlocks.clear();
			autoDetectParts();
			CoreModOptions.autoDetectCuttableBlocks = false;
			CoreModOptions.save();
			
		} else {
			for(ItemID id : CoreModOptions.manualCuttableBlocks) {
				if(Block.getBlockFromName(id.id) == null)
					FMLRelaunchLog.warning("ImmibisMicroblocks: cuttableBlock "+id.id+":"+id.meta+" specifies block name "+id.id+" which is not a registered block");
				else
					addCuttableBlock(Block.getBlockFromName(id.id), id.meta);
			}
		}
	}
	
	private boolean isSanelyTexturedNonVanillaBlock(ItemStack is) {
		try {
			Block b = Block.getBlockFromItem(is.getItem());
			if(b == null)
				return false;
			
			if(b.getClass().getName().startsWith("net.minecraft."))
				return false; // vanilla block
			
			String nameKey = is.getItem().getUnlocalizedName(is);
			if(nameKey == null || nameKey.equals("") || nameKey.equals("item."))
				return false;
			
			return true;
		} catch(Throwable e) {
			//e.printStackTrace();
			return false;
		}
	}
	
	private void autoDetectParts() {
		List<ItemStack> itemList = new ArrayList<ItemStack>();
		for(Object o : Item.itemRegistry.getKeys()) {
			Item i = (Item)Item.itemRegistry.getObject(o);
			if(i != null) {
				try {
					for(CreativeTabs tab : i.getCreativeTabs())
						i.getSubItems(i, tab, itemList);
				} catch(NoSuchMethodError e) {
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		for(ItemStack is : itemList) {
			int meta = is.getItemDamage();
			if(meta > 1023 || meta < 0 || !isSanelyTexturedNonVanillaBlock(is)) {
				continue;
			}
			
			Block b = Block.getBlockFromItem(is.getItem());
			if(!b.isOpaqueCube()) {
				continue;
			}
			
			try {
				addCuttableBlock(b, meta);
				CoreModOptions.manualCuttableBlocks.add(new ItemID(is.getItem(), meta));
			} catch(PartIDInUseException e) {
			}
		}
	}
	
	public void preinit() {
		// ID requests should be in pre-init, but this is a special case because we are loaded before Immibis Core.
		microblockContainerBlock = new BlockMicroblockContainer(Material.rock);
		GameRegistry.registerBlock(microblockContainerBlock, ItemMicroblock.class, "MicroblockContainer");

		BridgeClass1.isMinecraftLoaded = true;
		itemSaw = new ItemSaw();
		GameRegistry.registerItem(itemSaw, "immibis.microblocksaw");
		
		GameRegistry.registerTileEntity(TileMicroblockContainer.class, "immibis.multipart");
		GameRegistry.registerTileEntity(TileDummy.class, "immibis.multipart2");
	}
	
	public void init() {
		if(!SidedProxy.instance.isDedicatedServer())
			MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(microblockContainerBlock), new MicroblockItemRenderer());
		
		GameRegistry.addRecipe(new RecipeHollowCover());
		GameRegistry.addRecipe(new RecipeUnHollowCover());
		GameRegistry.addRecipe(new RecipeVerticalCut());
		GameRegistry.addRecipe(new RecipeHorizontalCut());
		GameRegistry.addRecipe(new RecipeCombineTwo());
		GameRegistry.addRecipe(new RecipeCombineSeveral());
		
		RecipeSorter.register("microblocks.hollowCover", RecipeHollowCover.class, Category.SHAPELESS, "");
		RecipeSorter.register("microblocks.unhollowCover", RecipeUnHollowCover.class, Category.SHAPELESS, "");
		RecipeSorter.register("microblocks.cutVertical", RecipeVerticalCut.class, Category.SHAPELESS, "");
		RecipeSorter.register("microblocks.cutHorizontal", RecipeHorizontalCut.class, Category.SHAPELESS, "");
		RecipeSorter.register("microblocks.combineTwo", RecipeCombineTwo.class, Category.SHAPELESS, "");
		RecipeSorter.register("microblocks.combineSeveral", RecipeCombineSeveral.class, Category.SHAPELESS, "");
		
		APILocator.getNetManager().registerPacket(PacketDummyTEDesc.class);
		APILocator.getNetManager().registerPacket(PacketDummyTEDestroy.class);
		APILocator.getNetManager().registerPacket(PacketMicroblockContainerDescription.class);
		APILocator.getNetManager().registerPacket(PacketMicroblockPlace.class);
		
		GameRegistry.addRecipe(new ItemStack(itemSaw), new Object[] {
			"III",
			"DDI",
			'I', Items.iron_ingot,
			'D', Items.diamond
		});
		
		MSTHooks.init();
		
		if(!SidedProxy.instance.isDedicatedServer()) {
			MinecraftForge.EVENT_BUS.register(new MicroblockPlacementHighlightHandler());
		}
		
		if(!CoreModOptions.forceDisableMCPCCompat) {
			if(MCPCMicroblockPermissionHandler.isApplicable()) {
				addPermissionHandler("MCPC+ (built-in)", new MCPCMicroblockPermissionHandler());
			}
		}
	}
	
	private void registerManualParts(int n, Block block, int blockMeta, Class<? extends PartTypeDefault> clazz) {
		registerManualParts(n, block, blockMeta, block, blockMeta, clazz);
	}
	
	private void registerManualParts(int n, Block block, int blockMeta) {
		registerManualParts(n, block, blockMeta, PartTypeDefault.class);
	}
	
	private void registerManualParts(int n, Block block) {
		registerManualParts(n, block, 0);
	}
	
	private void registerManualParts(int n, Block block, Class<? extends PartTypeDefault> clazz) {
		registerManualParts(n, block, 0, clazz);
	}
	
	private static class PartRegistrationType {
		public EnumPartClass clazz;
		public double size;
		public PartRegistrationType(EnumPartClass c, double s)
		{
			clazz = c;
			size = s;
		}
	}
	
	private static PartRegistrationType blockparts[] = new PartRegistrationType[] {
		new PartRegistrationType(EnumPartClass.Panel, 1.0/8.0),
		new PartRegistrationType(EnumPartClass.Panel, 2.0/8.0),
		new PartRegistrationType(EnumPartClass.Panel, 3.0/8.0),
		new PartRegistrationType(EnumPartClass.Panel, 4.0/8.0),
		new PartRegistrationType(EnumPartClass.Panel, 5.0/8.0),
		new PartRegistrationType(EnumPartClass.Panel, 6.0/8.0),
		new PartRegistrationType(EnumPartClass.Panel, 7.0/8.0),
		null,
		new PartRegistrationType(EnumPartClass.Strip, 1.0/8.0),
		new PartRegistrationType(EnumPartClass.Strip, 2.0/8.0),
		new PartRegistrationType(EnumPartClass.Strip, 3.0/8.0),
		new PartRegistrationType(EnumPartClass.Strip, 4.0/8.0),
		new PartRegistrationType(EnumPartClass.Strip, 5.0/8.0),
		new PartRegistrationType(EnumPartClass.Strip, 6.0/8.0),
		new PartRegistrationType(EnumPartClass.Strip, 7.0/8.0),
		null,
		new PartRegistrationType(EnumPartClass.Corner, 1.0/8.0),
		new PartRegistrationType(EnumPartClass.Corner, 2.0/8.0),
		new PartRegistrationType(EnumPartClass.Corner, 3.0/8.0),
		new PartRegistrationType(EnumPartClass.Corner, 4.0/8.0),
		new PartRegistrationType(EnumPartClass.Corner, 5.0/8.0),
		new PartRegistrationType(EnumPartClass.Corner, 6.0/8.0),
		new PartRegistrationType(EnumPartClass.Corner, 7.0/8.0),
		null,
		new PartRegistrationType(EnumPartClass.HollowPanel, 1.0/8.0),
		new PartRegistrationType(EnumPartClass.HollowPanel, 2.0/8.0),
		new PartRegistrationType(EnumPartClass.HollowPanel, 3.0/8.0),
		new PartRegistrationType(EnumPartClass.HollowPanel, 4.0/8.0),
		new PartRegistrationType(EnumPartClass.HollowPanel, 5.0/8.0),
		new PartRegistrationType(EnumPartClass.HollowPanel, 6.0/8.0),
		new PartRegistrationType(EnumPartClass.HollowPanel, 7.0/8.0),
		null,
	};
	
	private void registerManualParts(int n, Block block, int meta, Block craftingBlock, int craftingMeta, Class<? extends PartTypeDefault> clazz) {
		registerParts(n*64, block, meta, craftingBlock, craftingMeta, false, clazz);
	}
	
	private void registerParts(int partIDBase, Block block, int meta, Block craftingBlock, int craftingMeta, boolean ignoreNameCheck, Class<? extends PartTypeDefault> clazz) {
		assert(blockparts.length == 32);
		
		String unlocalizedBlockName = new ItemStack(block, 1, meta).getUnlocalizedName() + ".name";
		
		for(int k = 0; k < 7; k++)
		{
			// making hollow covers
			RecipeHollowCover.addMap(partIDBase + k, partIDBase + k + 24);
			// reverting hollow covers
			RecipeUnHollowCover.addMap(partIDBase + k + 24, partIDBase + k);
			
			// cutting panels into strips
			RecipeHorizontalCut.addMap(new BlockMetaPair(microblockContainerBlock, partIDBase + k), ItemMicroblock.getStackWithPartID(partIDBase + k + 8, 2));
			
			// cutting strips into corners
			RecipeHorizontalCut.addMap(new BlockMetaPair(microblockContainerBlock, partIDBase + k + 8), ItemMicroblock.getStackWithPartID(partIDBase + k + 16, 2));
			
			// combining corners into strips
			RecipeCombineTwo.addMap(partIDBase + k + 16, partIDBase + k + 8);
			
			// combining strips into panels
			RecipeCombineTwo.addMap(partIDBase + k + 8, partIDBase + k);
		}
		
		// combining multiple panels
		RecipeCombineSeveral.addMap(partIDBase, new ItemStack(craftingBlock, 1, craftingMeta));
		
		// combining multiple hollow panels
		RecipeCombineSeveral.addMap(partIDBase + 24, new ItemStack(craftingBlock, 1, craftingMeta));
		
		// cutting full blocks/slabs/panels
		RecipeVerticalCut.addMap(new BlockMetaPair(craftingBlock, craftingMeta), ItemMicroblock.getStackWithPartID(partIDBase+3, 2));
		RecipeVerticalCut.addMap(new BlockMetaPair(microblockContainerBlock, partIDBase+3), ItemMicroblock.getStackWithPartID(partIDBase+1, 2));
		RecipeVerticalCut.addMap(new BlockMetaPair(microblockContainerBlock, partIDBase+1), ItemMicroblock.getStackWithPartID(partIDBase+0, 2));
		
		// cutting hollow slabs/panels
		RecipeVerticalCut.addMap(new BlockMetaPair(microblockContainerBlock, partIDBase+27), ItemMicroblock.getStackWithPartID(partIDBase+25, 2));
		RecipeVerticalCut.addMap(new BlockMetaPair(microblockContainerBlock, partIDBase+25), ItemMicroblock.getStackWithPartID(partIDBase+24, 2));
		
		for(int k = 0; k < blockparts.length; k++)
			if(blockparts[k] != null)
			{
				String unlocalizedNameFormat = "immibis_microblocks.nameformat."+k;
				
				PartType<Part> type;
				try {
					//type = new DefaultPartType(
					type = clazz.getConstructor(int.class, EnumPartClass.class, double.class, String.class, String.class, Block.class, int.class).newInstance(
						partIDBase+k,
						blockparts[k].clazz,
						blockparts[k].size,
						unlocalizedNameFormat, unlocalizedBlockName,
						block,
						meta
					);
				} catch (Exception e) {
					throw new RuntimeException("While registering parts for "+block+":"+meta, e);
				}
				registerPartType(type);
			}
	}
	
	@Override
	public void registerPartType(PartType<?> type) {
		int id = type.getID();
		if(parts.containsKey(id))
			throw new PartIDInUseException(id, parts.get(id), type);
		parts.put(id, type);
		neiPartIDs.add(id);
	}

	@Override
	public IMicroblockCoverSystem createMicroblockCoverSystem(IMicroblockSupporterTile tile) {
		return new MicroblockCoverSystem(tile);
	}

	@Override
	public void addCuttableBlock(Block block, int meta) {
		if(meta < 0 || meta > 1023)
			throw new IllegalArgumentException("meta must be between 0 and 1023 inclusive");
		registerParts(((Block.getIdFromBlock(block) & 4095) << 20) | ((meta & 1023) << 10), block, meta, block, meta, true, PartTypeDefault.class);
	}

	@Override
	public PartType<?> getPartTypeByID(int id) {
		return parts.get(id);
	}

	@Override
	public Block getMicroblockContainerBlock() {
		return microblockContainerBlock;
	}

	@Override
	public ItemStack partTypeIDToItemStack(int id, int stackSize) throws IllegalArgumentException {
		if(!parts.containsKey(id))
			throw new IllegalArgumentException("No part with ID "+id+" (hex: "+Integer.toHexString(id)+")");
		return ItemMicroblock.getStackWithPartID(id, stackSize);
	}

	@Override
	public int itemStackToPartID(ItemStack stack) throws NullPointerException, IllegalArgumentException {
		if(!stack.getItem().equals(Item.getItemFromBlock(microblockContainerBlock)))
			throw new IllegalArgumentException("Not a stack of microblocks");
		return ItemMicroblock.getPartTypeID(stack);
	}

	public void receiveIMC(IMCEvent evt) {
		
	}
	
	
	
	
	
	List<IMicroblockPermissionHandler> permissionHandlers = new ArrayList<IMicroblockPermissionHandler>();
	private Map<String, IMicroblockPermissionHandler> permissionHandlerNames = new HashMap<String, IMicroblockPermissionHandler>();
	
	@Override
	public void addPermissionHandler(String name, IMicroblockPermissionHandler handler) {
		if(name == null) throw new IllegalArgumentException("name is null");
		if(handler == null) throw new IllegalArgumentException("handler is null");
		synchronized(permissionHandlers) {
			
			for(IMicroblockPermissionHandler registered : permissionHandlers)
				if(registered == handler)
					throw new IllegalArgumentException("permission handler already registered");
			
			if(permissionHandlerNames.containsKey(name))
				throw new IllegalArgumentException("name already registered to "+permissionHandlerNames.get(name));
			
			permissionHandlers.add(handler);
			permissionHandlerNames.put(name, handler);
		}
	}
	
	@Override
	public void removePermissionHandler(IMicroblockPermissionHandler handler) {
		synchronized(permissionHandlers) {
			
			{
				boolean foundInList = false;
				Iterator<IMicroblockPermissionHandler> it = permissionHandlers.iterator();
				while(it.hasNext()) 
					if(it.next() == handler) {
						if(foundInList)
							throw new AssertionError("internal error: handler appears twice in list");
	
						it.remove();
						foundInList = true;
					}
				
				if(!foundInList)
					throw new AssertionError("internal error: handler not found in list");
			}
			
			{
				String name = null;
				for(Map.Entry<String, IMicroblockPermissionHandler> e : permissionHandlerNames.entrySet()) {
					if(e.getValue() == handler) {
						if(name == null)
							name = e.getKey();
						else
							throw new AssertionError("internal error: handler appears twice in map. keys: "+name+", "+e.getKey());
					}
				}
				if(name == null)
					throw new AssertionError("internal error: handler not found in map");
				permissionHandlerNames.remove(name);
			}
		}
	}

	String getPermissionHandlerName(IMicroblockPermissionHandler ph) {
		for(Map.Entry<String, IMicroblockPermissionHandler> e : permissionHandlerNames.entrySet())
			if(e.getValue() == ph)
				return e.getKey();
		return "<unknown>";
	}
}
