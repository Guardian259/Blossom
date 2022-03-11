package com.yurisuika.blossom;

import com.yurisuika.blossom.block.FloweringLeavesBlock;
import com.yurisuika.blossom.registry.BlossomRegistry;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Blossom implements ModInitializer {

	public static final String MOD_ID = "blossom";

	public static final Logger LOGGER = LogManager.getLogger("Blossom");

	public static final Block FLOWERING_OAK_LEAVES = new FloweringLeavesBlock(Blocks.OAK_LEAVES, FabricBlockSettings.copy(Blocks.OAK_LEAVES));

	@Override
	public void onInitialize() {
		LOGGER.info("Loading Blossom!");

		PolymerRPUtils.addAssetSource(MOD_ID);
		PolymerRPUtils.markAsRequired();
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "flowering_oak_leaves"), FLOWERING_OAK_LEAVES);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "flowering_oak_leaves"), new BlockItem(FLOWERING_OAK_LEAVES, new Item.Settings()));

		BlossomRegistry.registerFlammables();
		BlossomRegistry.registerCompostables();
	}

}