package dev.latvian.mods.kubejs.neoforge;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.client.BlockTintFunctionWrapper;
import dev.latvian.mods.kubejs.client.ItemTintFunctionWrapper;
import dev.latvian.mods.kubejs.client.KubeJSClientEventHandler;
import dev.latvian.mods.kubejs.fluid.FluidBucketItemBuilder;
import dev.latvian.mods.kubejs.fluid.FluidBuilder;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public class KubeJSNeoForgeClient {
	public KubeJSNeoForgeClient(IEventBus bus) {
		bus.addListener(EventPriority.LOW, this::setupClient);
		bus.addListener(this::blockColors);
		bus.addListener(this::itemColors);
		//bus.addListener(this::textureStitch);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOW, this::openScreenEvent);
	}

	private void setupClient(FMLClientSetupEvent event) {
		KubeJS.PROXY.clientSetup();

		for (var builder : RegistryInfo.BLOCK) {
			if (builder instanceof BlockBuilder b) {
				switch (b.renderType) {
					case "cutout" -> ItemBlockRenderTypes.setRenderLayer(b.get(), RenderType.cutout());
					case "cutout_mipped" -> ItemBlockRenderTypes.setRenderLayer(b.get(), RenderType.cutoutMipped());
					case "translucent" -> ItemBlockRenderTypes.setRenderLayer(b.get(), RenderType.translucent());
				}
			}
		}

		for (var builder : RegistryInfo.FLUID) {
			if (builder instanceof FluidBuilder b) {
				switch (b.renderType) {
					case "cutout" -> {
						ItemBlockRenderTypes.setRenderLayer(b.get().getSource(), RenderType.cutout());
						ItemBlockRenderTypes.setRenderLayer(b.get().getFlowing(), RenderType.cutout());
					}
					case "cutout_mipped" -> {
						ItemBlockRenderTypes.setRenderLayer(b.get().getSource(), RenderType.cutoutMipped());
						ItemBlockRenderTypes.setRenderLayer(b.get().getFlowing(), RenderType.cutoutMipped());
					}
					case "translucent" -> {
						ItemBlockRenderTypes.setRenderLayer(b.get().getSource(), RenderType.translucent());
						ItemBlockRenderTypes.setRenderLayer(b.get().getFlowing(), RenderType.translucent());
					}
				}
			}
		}
	}

	private void blockColors(RegisterColorHandlersEvent.Block event) {
		for (var builder : RegistryInfo.BLOCK) {
			if (builder instanceof BlockBuilder b && b.tint != null) {
				event.register(new BlockTintFunctionWrapper(b.tint), b.get());
			}
		}
	}

	private void itemColors(RegisterColorHandlersEvent.Item event) {
		for (var builder : RegistryInfo.ITEM) {
			if (builder instanceof ItemBuilder b && b.tint != null) {
				event.register(new ItemTintFunctionWrapper(b.tint), b.get());
			}

			if (builder instanceof FluidBucketItemBuilder b && b.fluidBuilder.bucketColor != 0xFFFFFFFF) {
				event.register((stack, index) -> index == 1 ? b.fluidBuilder.bucketColor : 0xFFFFFFFF, b.get());
			}
		}
	}

	// FIXME: implement
	/*private void textureStitch(TextureStitchEvent.Pre event) {
		ClientEvents.ATLAS_SPRITE_REGISTRY.post(new AtlasSpriteRegistryEventJS(event::addSprite), event.getAtlas().location());
	}*/

	private void openScreenEvent(ScreenEvent.Opening event) {
		var s = KubeJSClientEventHandler.setScreen(event.getScreen());

		if (s != null && event.getScreen() != s) {
			event.setNewScreen(s);
		}
	}
}
