package zmaster587.advancedRocketry.api.material;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import zmaster587.advancedRocketry.api.AdvancedRocketryBlocks;
import zmaster587.advancedRocketry.api.AdvancedRocketryItems;
import zmaster587.advancedRocketry.api.util.ItemStackMapping;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class MaterialRegistry {

	static HashMap<Object, MixedMaterial> mixedMaterialList = new HashMap<Object, MixedMaterial>();

	/**
	 * Contains a list of itemtypes that are common to multiple materials
	 */
	public static enum AllowedProducts {
		DUST,
		INGOT,
		CRYSTAL,
		BOULE,
		NUGGET,
		COIL(true, AdvancedRocketryBlocks.blockCoil),
		PLATE,
		STICK,
		BLOCK(true, AdvancedRocketryBlocks.blockMetal),
		ORE(true, AdvancedRocketryBlocks.blockOre),
		FAN,
		SHEET;

		boolean isBlock;
		List<Block> blockArray;

		private AllowedProducts() {
			this.isBlock = false;
		}

		private AllowedProducts(boolean isBlock,List<Block> blockArray) {
			this.isBlock = isBlock;
			this.blockArray = blockArray;
		}

		public int getFlagValue() {
			return 1 << ordinal();
		}

		/**
		 * @param flag
		 * @return true if the flag corresponds to this type of item
		 */
		public boolean isOfType(int flag) {
			return (getFlagValue() & flag) != 0;
		}
		
		public String getName() {
			return this.name().equalsIgnoreCase("stick") ? "ROD" : this.name();
		}

		/**
		 * @return true if the itemtype is a block, IE Ore, coils, etc
		 */
		public boolean isBlock() {
			return isBlock;
		}
	}

	/**
	 * @param stack the item stack to get the material of
	 * @return {@link Materials} of the itemstack if it exists, otherwise null
	 */
	public static Materials getMaterialFromItemStack(ItemStack stack) {
		Item item = stack.getItem();

		//If items is an itemOreProduct it must have been registered

		for(Item i : AdvancedRocketryItems.itemOreProduct) {
			if(item == i) {
				return Materials.values()[stack.getItemDamage()];
			}
		}

		int[] ids = OreDictionary.getOreIDs(stack);

		//Check all ores against list
		for(Materials ore : Materials.values()) {
			for(String str : ore.getOreDictNames()) {
				for(int i : ids) {
					if(OreDictionary.getOreName(i).contains(str)) 
						return ore;
				}
			}
		}

		return null;
	}

	/**
	 * @param material
	 * @param product
	 * @return an itemstack of size one containing the product with the given material, or null if one does not exist
	 */
	public static ItemStack getItemStackFromMaterialAndType(Materials material,AllowedProducts product) {
		return getItemStackFromMaterialAndType(material, product,1);
	}

	/**
	 * @param material
	 * @param product
	 * @param amount stackSize
	 * @return an itemstack of stackSize amount containing the product with the given material, or null if one does not exist
	 */
	public static ItemStack getItemStackFromMaterialAndType(Materials ore,AllowedProducts product, int amount) {
		return new ItemStack( AdvancedRocketryItems.itemOreProduct[product.ordinal()], amount, ore.ordinal());
	}

	/**
	 * Registers a mixed material or allow to automate recipe registration
	 * @param material new mixed material to create
	 */
	public static void registerMixedMaterial(MixedMaterial material) {
		if(material.getInput() instanceof ItemStack)
			mixedMaterialList.put( new ItemStackMapping((ItemStack) material.getInput()), material);
		else
			mixedMaterialList.put( material.getInput(), material);
	}

	/**
	 * @param stack
	 * @return {@link MixedMaterial} that makes up the item, null if the item is not registered
	 */
	public MixedMaterial getMixedMaterial(ItemStack stack) {
		return mixedMaterialList.get(new ItemStackMapping(stack));
	}

	/**
	 * @param str
	 * @return mixed material corresponding to the supplied string example: "bronze"
	 */
	public MixedMaterial getMixedMaterial(String str) {
		return mixedMaterialList.get(str);
	}

	public static int getColorFromItemMaterial(ItemStack stack) {

		Materials material = getMaterialFromItemStack(stack);
		if(material == null) {
			return 0x7a7a7a;
			/*Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture);
			TextureAtlasSprite tas = (TextureAtlasSprite)stack.getIconIndex();
			
			IntBuffer pixels = BufferUtils.createIntBuffer(Math.max(tas.getIconWidth() * tas.getIconHeight(),1024));
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, tas.getOriginX(), tas.getOriginY(), tas.getIconWidth(), tas.getIconHeight(), GL11.GL_RGBA, GL11.GL_INT, pixels);
			int[] texture = new int[pixels.remaining()];
			pixels.get(texture);
			int number = 0;

			int r = 0,g = 0,b = 0;
			for (int i = 0; i < Math.sqrt(texture.length); i++) {
				for (int j = 0; j < Math.sqrt(texture.length); j++) {
					int pixel = texture[(i + (j * tas.getIconWidth()))];

					if((pixel & 0xFF) == 0xFF) {
						r += (pixel >>> 24) & 0xFF;
						g += (pixel >>> 16) & 0xFF;
						b += (pixel >>> 8) & 0xFF;
						number++;
					}
				}
			}

			return (((r / number)& 0xFF) << 16  ) | (((g / number)& 0xFF) << 8 ) | ((( b / number ) & 0xFF)   );*/

		}
		else
			return material.getColor();
	}

	/**
	 * @return Collection containing all registered mixed materials
	 */
	public static Collection<MixedMaterial> getMixedMaterialList() {
		return mixedMaterialList.values();
	}

	public static enum Materials {
		DILITHIUM("Dilithium", "pickaxe", 3, 0xddcecb, AllowedProducts.DUST.getFlagValue() | AllowedProducts.CRYSTAL.getFlagValue()),
		IRON("Iron", "pickaxe", 3, 0xafafaf, AllowedProducts.SHEET.getFlagValue() | AllowedProducts.STICK.getFlagValue() | AllowedProducts.DUST.getFlagValue() | AllowedProducts.PLATE.getFlagValue(), false),
		GOLD("Gold", "pickaxe", 3, 0xffff5d, AllowedProducts.DUST.getFlagValue() | AllowedProducts.PLATE.getFlagValue(), false),
		SILICON("Silicon", "pickaxe", 3, 0x2c2c2b, AllowedProducts.INGOT.getFlagValue() | AllowedProducts.DUST.getFlagValue() | AllowedProducts.BOULE.getFlagValue() | AllowedProducts.NUGGET.getFlagValue(), false),
		COPPER("Copper", "pickaxe", 2, 0xd55e28, AllowedProducts.COIL.getFlagValue() | AllowedProducts.BLOCK.getFlagValue() | AllowedProducts.STICK.getFlagValue() | AllowedProducts.INGOT.getFlagValue() | AllowedProducts.NUGGET.getFlagValue() | AllowedProducts.DUST.getFlagValue()),
		TIN("Tin", "pickaxe", 2, 0xcdd5d8, AllowedProducts.BLOCK.getFlagValue() | AllowedProducts.PLATE.getFlagValue() | AllowedProducts.INGOT.getFlagValue() | AllowedProducts.NUGGET.getFlagValue() | AllowedProducts.DUST.getFlagValue()),
		STEEL("Steel", "pickaxe", 2, 0x55555d, AllowedProducts.BLOCK.getFlagValue() | AllowedProducts.FAN.getFlagValue() | AllowedProducts.PLATE.getFlagValue() | AllowedProducts.INGOT.getFlagValue() | AllowedProducts.NUGGET.getFlagValue() | AllowedProducts.DUST.getFlagValue() | AllowedProducts.STICK.getFlagValue(), false),
		TITANIUM("Titanium", "pickaxe", 2, 0xb2669e, AllowedProducts.PLATE.getFlagValue() | AllowedProducts.INGOT.getFlagValue() | AllowedProducts.NUGGET.getFlagValue() | AllowedProducts.DUST.getFlagValue() | AllowedProducts.STICK.getFlagValue(), false),
		RUTILE("Rutile", "pickaxe", 2, 0xbf936a, 0);

		String unlocalizedName, tool;
		String[] oreDictNames;
		int harvestLevel;
		int allowedProducts;
		int color;

		private Materials(String unlocalizedName, String tool, int level, int color, int allowedProducts, boolean hasOre) {
			this(unlocalizedName, tool, level, color, hasOre ? AllowedProducts.ORE.getFlagValue() | allowedProducts : allowedProducts, new String[] {unlocalizedName});
		}

		private Materials(String unlocalizedName, String tool, int level, int color, int allowedProducts, MixedMaterial ... products) {
			this(unlocalizedName, tool, level, color, allowedProducts | AllowedProducts.ORE.getFlagValue(), new String[] {unlocalizedName});
		}

		private Materials(String unlocalizedName, String tool, int level, int color, int allowedProducts) {
			this(unlocalizedName, tool, level, color, allowedProducts | AllowedProducts.ORE.getFlagValue(), new String[] {unlocalizedName});
		}

		private Materials(String unlocalizedName, String tool, int level, int color, int allowedProducts, String[] oreDictNames) {
			this.unlocalizedName = unlocalizedName;
			this.tool = tool;
			this.harvestLevel = level;
			this.oreDictNames = oreDictNames;
			this.allowedProducts = allowedProducts;
			this.color = color;
		}

		/**
		 * @return true if the material is vanilla (Gold, iron)
		 */
		public boolean isVanilla() {
			return this.unlocalizedName.equals("Iron") ||  this.unlocalizedName.equals("Gold");
		}

		/**
		 * @param product
		 * @param amount
		 * @return Itemstack representing the product of this material, or null if nonexistant
		 */
		public ItemStack getProduct(AllowedProducts product, int amount) {
			if(product.isBlock()) {
				return new ItemStack(product.blockArray.get(this.ordinal()/16), amount, getMeta());
			}
			return new ItemStack(AdvancedRocketryItems.itemOreProduct[product.ordinal()], amount, getMeta());
		}
		/**
		 * @param product
		 * @return Itemstack of size 1 representing the product of this material, or null if nonexistant
		 */
		public ItemStack getProduct(AllowedProducts product) {
			return getProduct(product,1);
		}

		/**
		 * @return 32wide-bitmask corresponding to allowed products by this material
		 */
		public int getAllowedProducts() {
			return allowedProducts;
		}

		/**
		 * @return harvest level required to harvest the ore of this material
		 */
		public int getHarvestLevel() {
			return harvestLevel;
		}

		/**
		 * @return tool required to harvest the ore of this material
		 */
		public String getTool() {
			return tool;
		}

		public String getUnlocalizedName() {
			return unlocalizedName;
		}

		/**
		 * @return list of ore dictionary names for this material.  Example: {iron, pigiron}
		 */
		public String[] getOreDictNames() {
			return oreDictNames;
		}

		/**
		 * Used in rendering of the item and block
		 * @return color of the material 0xRRGGBB
		 */
		public int getColor() {
			return color;
		}

		@Deprecated
		public Block getBlock() {
			return AdvancedRocketryBlocks.blockOre.get(this.ordinal()/16);
		}

		/**
		 * @return the meta value for the itemstack representing a block of this material
		 */
		public int getMeta() {
			return this.ordinal() % 16;
		}

		/**
		 * 
		 * @param str 
		 * @return the material corresponding to the string supplied or null if non existant
		 */
		public static Materials valueOfSafe(String str) {
			try {
				return Materials.valueOf(str);
			} catch (Exception e) {
				return null;
			}
		}
	}
}
