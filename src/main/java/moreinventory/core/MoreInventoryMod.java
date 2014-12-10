package moreinventory.core;

import moreinventory.block.BlockCatchall;
import moreinventory.block.BlockStorageBox;
import moreinventory.block.BlockStorageBoxAddon;
import moreinventory.block.BlockTransportManager;
import moreinventory.handler.MIMEventHooks;
import moreinventory.handler.MIMGuiHandler;
import moreinventory.handler.MIMWorldSaveHelper;
import moreinventory.item.ItemBlockSBAddon;
import moreinventory.item.ItemBlockStorageBox;
import moreinventory.item.ItemBlockTransportManager;
import moreinventory.item.ItemChestTransporter;
import moreinventory.item.ItemNoFunction;
import moreinventory.item.ItemPlating;
import moreinventory.item.ItemPotionHolder;
import moreinventory.item.ItemPouch;
import moreinventory.item.ItemSpanner;
import moreinventory.item.ItemTorchHolder;
import moreinventory.network.CatchallMessage;
import moreinventory.network.ImporterMessage;
import moreinventory.network.PouchMessage;
import moreinventory.network.SBAddonBaseConfigMessage;
import moreinventory.network.SBAddonBaseMessage;
import moreinventory.network.StorageBoxButtonMessage;
import moreinventory.network.StorageBoxConfigMessage;
import moreinventory.network.StorageBoxContentsMessage;
import moreinventory.network.StorageBoxMessage;
import moreinventory.network.TransportManagerMessage;
import moreinventory.recipe.RecipePouch;
import moreinventory.recipe.RecipeTorchHolder;
import moreinventory.tileentity.TileEntityCatchall;
import moreinventory.tileentity.TileEntityExporter;
import moreinventory.tileentity.TileEntityImporter;
import moreinventory.tileentity.TileEntityTransportManager;
import moreinventory.tileentity.storagebox.StorageBoxType;
import moreinventory.tileentity.storagebox.addon.EnumSBAddon;
import moreinventory.util.MIMUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod
(
	modid = MoreInventoryMod.MODID,
	acceptedMinecraftVersions = "[1.7.10,)",
	dependencies = "after:InvTweaks",
	guiFactory = "moreinventory.client.config.MIMGuiFactory",
	useMetadata = true
)
public class MoreInventoryMod
{
	public static final String
	MODID = "MoreInventoryMod",
	CONFIG_LANG = "moreinv.config.";

	@Instance(MODID)
	public static MoreInventoryMod instance;

	@SidedProxy(modId = MODID, clientSide = "moreinventory.client.ClientProxy", serverSide = "moreinventory.core.CommonProxy")
	public static CommonProxy proxy;

	public static final SimpleNetworkWrapper network = new SimpleNetworkWrapper(MODID);

	public static MIMWorldSaveHelper saveHelper;

	public static Block Catchall;
	public static Block StorageBox;
	public static Block TransportManager;
	public static Block StorageBoxAddon;
	public static Item[] Torchholder = new Item[3];
	public static Item ChestTransporter;
	public static Item Pouch;
	public static Item NoFunctionItems;
	public static Item Spanner;
	public static Item Potionholder;
	public static Item Plating;

	public static final String[] MATERIALNAME = {"Leather Pack", "Brush", "Dimension Core", "Clipboard"};
	public static final String[] COLORNAME = {"White", "Orange", "Magenta", "LightBlue", "Yellow", "Lime", "Pink", "Gray", "LightGray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"};

	public static final String defaultOwner = "***Unknown***";

	public static final CreativeTabs tabMoreInventoryMod = new CreativeTabMoreInventoryMod();

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Config.syncConfig();

		/*** Blocks ***/
		Catchall = new BlockCatchall(Material.wood).setBlockName("catchall");
		StorageBox = new BlockStorageBox(Material.iron).setBlockName("containerbox");
		TransportManager = new BlockTransportManager(Material.rock).setBlockName("transportmanager");
		StorageBoxAddon = new BlockStorageBoxAddon(Material.iron);
		GameRegistry.registerBlock(Catchall, ItemBlock.class, "catchall");
		GameRegistry.registerBlock(StorageBox, ItemBlockStorageBox.class, "containerbox");
		GameRegistry.registerBlock(TransportManager, ItemBlockTransportManager.class, "transportmanager");
		GameRegistry.registerBlock(StorageBoxAddon, ItemBlockSBAddon.class, "StorageBoxAddon");

		/*** Items ***/
		ChestTransporter = new ItemChestTransporter().setUnlocalizedName("transporter");
		NoFunctionItems = new ItemNoFunction().setUnlocalizedName("itemnofunction");
		Potionholder = new ItemPotionHolder().setUnlocalizedName("potionholder");
		Spanner = new ItemSpanner().setUnlocalizedName("spanner").setCreativeTab(MoreInventoryMod.tabMoreInventoryMod);
		Plating = new ItemPlating().setUnlocalizedName("painting").setCreativeTab(MoreInventoryMod.tabMoreInventoryMod);
		Pouch = new ItemPouch().setUnlocalizedName("pouch");

		final String[] gradeName = { "Iron", "Gold", "Diamond" };
		for (int i = 0; i < 3; i++)
		{
			Torchholder[i] = new ItemTorchHolder(i).setUnlocalizedName("torchholder:" + gradeName[i]);
			GameRegistry.registerItem(Torchholder[i], "torchholder" + gradeName[i]);
		}

		GameRegistry.registerItem(ChestTransporter, "transporter");
		GameRegistry.registerItem(NoFunctionItems, "itemnofunction");
		GameRegistry.registerItem(Potionholder, "potionholder");
		GameRegistry.registerItem(Spanner, "spanner");
		GameRegistry.registerItem(Plating, "painting");
		GameRegistry.registerItem(Pouch, "pouch");

		/*** TileEntity ***/
		GameRegistry.registerTileEntity(TileEntityCatchall.class, "containerCatchall");
		GameRegistry.registerTileEntity(TileEntityTransportManager.class, "TileEntityTransportManagerMeta");
		GameRegistry.registerTileEntity(TileEntityImporter.class, "TilEntityImporter");
		GameRegistry.registerTileEntity(TileEntityExporter.class, "TileEntityExporter");

		for (StorageBoxType type : StorageBoxType.values())
		{
			GameRegistry.registerTileEntity(type.clazz, "TileEntity" + type.name() + "StorageBox");
		}

		for (int i = 0; i < EnumSBAddon.values().length; i++)
		{
			GameRegistry.registerTileEntity(EnumSBAddon.values()[i].clazz, "moreinventory." + EnumSBAddon.values()[i].name());
		}

		/*** Messages ***/
		int i = 0;
		network.registerMessage(PouchMessage.Client.class, PouchMessage.class, i++, Side.CLIENT);
		network.registerMessage(PouchMessage.Server.class, PouchMessage.class, i++, Side.SERVER);
		network.registerMessage(TransportManagerMessage.class, TransportManagerMessage.class, i++, Side.CLIENT);
		network.registerMessage(CatchallMessage.class, CatchallMessage.class, i++, Side.CLIENT);
		network.registerMessage(ImporterMessage.Client.class, ImporterMessage.class, i++, Side.CLIENT);
		network.registerMessage(ImporterMessage.Server.class, ImporterMessage.class, i++, Side.SERVER);
		network.registerMessage(StorageBoxMessage.class, StorageBoxMessage.class, i++, Side.CLIENT);
		network.registerMessage(StorageBoxContentsMessage.class, StorageBoxContentsMessage.class, i++, Side.CLIENT);
		network.registerMessage(StorageBoxConfigMessage.class, StorageBoxConfigMessage.class, i++, Side.CLIENT);
		network.registerMessage(StorageBoxButtonMessage.class, StorageBoxButtonMessage.class, i++, Side.SERVER);
		network.registerMessage(SBAddonBaseMessage.class, SBAddonBaseMessage.class, i++, Side.CLIENT);
		network.registerMessage(SBAddonBaseConfigMessage.Client.class, SBAddonBaseConfigMessage.class, i++, Side.CLIENT);
		network.registerMessage(SBAddonBaseConfigMessage.Server.class, SBAddonBaseConfigMessage.class, i++, Side.SERVER);
	}

	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new MIMGuiHandler());

		FMLCommonHandler.instance().bus().register(MIMEventHooks.instance);

		MinecraftForge.EVENT_BUS.register(MIMEventHooks.instance);

		GameRegistry.addShapedRecipe(new ItemStack(Torchholder[0], 1, ItemTorchHolder.maxDamage[0] - 2),
			"I L", "I I", "III",
			'I', new ItemStack(Items.iron_ingot),
			'L', new ItemStack(Items.leather)
		);
		GameRegistry.addShapedRecipe(new ItemStack(Torchholder[1], 1, ItemTorchHolder.maxDamage[1] - 2),
			"I L", "I I", "ISI",
			'I', new ItemStack(Items.gold_ingot),
			'L', new ItemStack(Items.leather),
			'S', new ItemStack(Items.blaze_rod)
		);
		GameRegistry.addShapedRecipe(new ItemStack(Torchholder[2], 1, ItemTorchHolder.maxDamage[2] - 2),
			"I L", "I I", "ISI",
			'I', new ItemStack(Items.diamond),
			'L', new ItemStack(Items.leather),
			'S', new ItemStack(Items.nether_star)
		);

		for (int i = 0; i < 2; ++i)
		{
			GameRegistry.addRecipe(new RecipeTorchHolder(new ItemStack(Blocks.torch), Lists.newArrayList(new ItemStack(Torchholder[i], 1, OreDictionary.WILDCARD_VALUE))));
		}

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Catchall),
			"P P", "PCP", "HHH",
			'P', "plankWood",
			'H', "slabWood",
			'C', new ItemStack(Blocks.chest)
		));

		GameRegistry.addShapedRecipe(new ItemStack(Spanner),
			"SSS", " I ", "SSS",
			'S', new ItemStack(Blocks.stone),
			'I', new ItemStack(Items.iron_ingot)
		);

		GameRegistry.addShapedRecipe(new ItemStack(Potionholder),
			"SLS", "BBB",
			'S', new ItemStack(Items.string),
			'L', new ItemStack(Items.leather),
			'B', new ItemStack(Items.glass_bottle)
		);

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ChestTransporter),
			"P P", "PHP", "HHH",
			'H', "slabWood",
			'P', "plankWood"
		));

		GameRegistry.addShapedRecipe(new ItemStack(TransportManager, 1, 0),
			" B ", "SBS", "SSS",
			'B', new ItemStack(Blocks.lapis_block),
			'S', new ItemStack(Blocks.stone)
		);
		GameRegistry.addShapedRecipe(new ItemStack(TransportManager, 1, 1),
			" B ", "SBS", "SSS",
			'B', new ItemStack(Blocks.redstone_block),
			'S', new ItemStack(Blocks.stone)
		);

		GameRegistry.addShapedRecipe(new ItemStack(Pouch),
			"PPP", "BLB", "PBP",
			'L', new ItemStack(Items.diamond),
			'P', new ItemStack(Items.leather),
			'B', new ItemStack(NoFunctionItems, 1, 0)
		);

		for (int i = 0; i < 16; i++)
		{
			GameRegistry.addRecipe(new RecipePouch(new ItemStack(Pouch, 1, i + 1), "dye" + COLORNAME[i], new ItemStack(Pouch, 1, OreDictionary.WILDCARD_VALUE)));
		}

		ItemStack upgradePouch = new ItemStack(Pouch);
		upgradePouch.setStackDisplayName(upgradePouch.getDisplayName() + "(Upgrade)");
		GameRegistry.addRecipe(new RecipePouch(upgradePouch, new ItemStack(Items.ender_pearl), new ItemStack(Pouch, 1, OreDictionary.WILDCARD_VALUE)));

		GameRegistry.addShapedRecipe(new ItemStack(NoFunctionItems, 1, 0),
			"LLL", "LSL", "LLL",
			'L', new ItemStack(Items.leather),
			'S', new ItemStack(Items.string)
		);
		GameRegistry.addShapedRecipe(new ItemStack(NoFunctionItems, 4, 1),
			" WW", " WW", "S  ",
			'W', new ItemStack(Blocks.wool),
			'S', new ItemStack(Items.stick)
		);
		GameRegistry.addShapedRecipe(new ItemStack(NoFunctionItems, 1, 2),
			"ODO", "DED", "ODO",
			'O', new ItemStack(Blocks.obsidian),
			'D', new ItemStack(Items.diamond),
			'E', new ItemStack(Items.ender_eye)
		);
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(NoFunctionItems, 1, 3),
			"WIW", "WPW", "WPW",
			'W', "slabWood",
			'I', new ItemStack(Items.iron_ingot),
			'P', new ItemStack(Items.paper)
		));

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StorageBox, 1, 0),
			"IHI", "I I", "IHI",
			'I', "logWood",
			'C', new ItemStack(StorageBox, 1, 0),
			'H', "slabWood"
		));

		ItemStack woodStorageBox = new ItemStack(StorageBox, 1, 0);
		ItemStack halfStone = new ItemStack(Blocks.stone_slab);

		for (int i = 0; i < StorageBoxType.values().length; i++)
		{
			if (StorageBoxType.values()[i].canCraft)
			{
				for (int j = 0; j < StorageBoxType.values()[i].materials.length; j++)
				{
					GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StorageBox, 3, i), true,
						"IHI", "ICI", "IHI",
						'I', StorageBoxType.values()[i].materials[j],
						'C', woodStorageBox,
						'H', halfStone
					));
				}
			}
		}

		GameRegistry.addShapedRecipe(new ItemStack(StorageBox, 32, 8),
			"IHI", "I I", "IHI",
			'H', new ItemStack(Blocks.glass_pane),
			'I', new ItemStack(Blocks.glass)
		);
		GameRegistry.addShapedRecipe(new ItemStack(StorageBox, 1, 9),
			"IHI", "I I", "IHI",
			'H', new ItemStack(Blocks.stone_slab, 1, 3),
			'I', new ItemStack(Blocks.cobblestone)
		);
		GameRegistry.addShapedRecipe(new ItemStack(StorageBox, 2, 11),
			"OOO", "CEC", "OOO",
			'O', new ItemStack(Blocks.obsidian),
			'C', new ItemStack(NoFunctionItems, 1, 2),
			'E', new ItemStack(StorageBox, 1, 10)
		);

		GameRegistry.addRecipe(new ItemStack(StorageBoxAddon, 1, 0),
			"SSS", "CEC", "SSS",
			'S', new ItemStack(Blocks.end_stone),
			'C', new ItemStack(NoFunctionItems, 1, 2),
			'E', new ItemStack(StorageBox, 1, 10)
		);

		ItemStack lava = new ItemStack(Items.lava_bucket);
		ItemStack brush = new ItemStack(NoFunctionItems, 1, 1);

		for (int i = 0; i < ItemPlating.typeIndex.length; i++)
		{
			int j = ItemPlating.typeIndex[i];

			for (int k = 0; k < StorageBoxType.values()[j].materials.length; k++)
			{
				Object ingot = StorageBoxType.values()[j].materials[k];

				GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Plating, 1, i), ingot, ingot, lava, brush));
			}
		}

		proxy.registerRenderers();

		chestRegistry();
	}

	private void chestRegistry()
	{
		ItemChestTransporter.transportableChests.put(MIMUtils.getUniqueName(Blocks.chest), -1, 0);
		ItemChestTransporter.transportableChests.put(MIMUtils.getUniqueName(Blocks.trapped_chest), -1, 0);
		ItemChestTransporter.transportableChests.put(MIMUtils.getUniqueName(StorageBox), -1, 0);

		//Config
		String chests = Joiner.on(",").skipNulls().join(Config.transportableChests).trim();

		if (chests.length() > 0)
		{
			for (String entry : chests.split(","))
			{
				String[] args = entry.split("@");
				int index = args[2].trim().length() > 0 ? Byte.parseByte(args[2].trim()) : 0;

				ItemChestTransporter.transportableChests.put(args[0].trim(), Integer.parseInt(args[1].trim()), index);
			}
		}

		//Modded

		for (int i = 0; i < 7; i++)
		{
			ItemChestTransporter.transportableChests.put("IronChest:BlockIronChest", i, i + 1);
		}

		ItemChestTransporter.transportableChests.put("MultiPageChest:multipagechest", -1, 11);
	}
}