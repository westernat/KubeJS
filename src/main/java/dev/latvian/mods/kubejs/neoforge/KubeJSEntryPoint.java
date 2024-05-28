package dev.latvian.mods.kubejs.neoforge;

import dev.latvian.mods.kubejs.CommonProperties;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.bindings.event.StartupEvents;
import dev.latvian.mods.kubejs.gui.KubeJSMenus;
import dev.latvian.mods.kubejs.ingredient.KubeJSIngredients;
import dev.latvian.mods.kubejs.item.creativetab.CreativeTabCallback;
import dev.latvian.mods.kubejs.item.creativetab.CreativeTabKubeEvent;
import dev.latvian.mods.kubejs.item.creativetab.KubeJSCreativeTabs;
import dev.latvian.mods.kubejs.net.KubeJSNet;
import dev.latvian.mods.kubejs.recipe.KubeJSRecipeSerializers;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.Cast;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Mod(KubeJS.MOD_ID)
public class KubeJSEntryPoint {
	private static final ThreadLocal<IEventBus> BUS = new ThreadLocal<>();

	public KubeJSEntryPoint(IEventBus bus, Dist dist) throws Throwable {
		bus.addListener(EventPriority.LOW, KubeJSEntryPoint::loadComplete);
		bus.addListener(EventPriority.LOW, KubeJSEntryPoint::initRegistries);
		bus.addListener(EventPriority.LOW, KubeJSEntryPoint::commonSetup);
		bus.addListener(EventPriority.LOW, KubeJSEntryPoint::creativeTab);
		bus.addListener(KubeJSNet::register);

		BUS.set(bus);

		KubeJS.instance = new KubeJS(bus);
		KubeJS.instance.setup();

		if (CommonProperties.get().serverOnly) {
			// FIXME ModLoadingContext.get().registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> DisplayTest.IGNORESERVERONLY, (a, b) -> true));
		} else {
			NeoForgeMod.enableMilkFluid();
			KubeJSIngredients.REGISTRY.register(bus);
			KubeJSCreativeTabs.REGISTRY.register(bus);
			// KubeJSComponents.REGISTRY.register(bus);
			KubeJSRecipeSerializers.REGISTRY.register(bus);
			KubeJSMenus.REGISTRY.register(bus);
		}

		if (dist == Dist.CLIENT) {
			new KubeJSNeoForgeClient(bus);
		}
	}

	private static void initRegistries(RegisterEvent event) {
		var info = RegistryInfo.of(event.getRegistryKey());
		info.registerObjects((id, supplier) -> event.register(Cast.to(info.key), id, supplier));
	}

	private static void commonSetup(FMLCommonSetupEvent event) {
	}

	private record CreativeTabCallbackForge(BuildCreativeModeTabContentsEvent event) implements CreativeTabCallback {
		@Override
		public void addAfter(ItemStack order, ItemStack[] items, CreativeModeTab.TabVisibility visibility) {
			for (var item : items) {
				event.accept(item, visibility);
			}
		}

		@Override
		public void addBefore(ItemStack order, ItemStack[] items, CreativeModeTab.TabVisibility visibility) {
			for (var item : items) {
				event.accept(item, visibility);
			}
		}

		@Override
		public void remove(Ingredient filter, boolean removeDisplay, boolean removeSearch) {
			var entries = new ArrayList<Map.Entry<ItemStack, CreativeModeTab.TabVisibility>>();

			for (var entry : event.getEntries()) {
				if (filter.test(entry.getKey())) {
					var visibility = entry.getValue();

					if (removeDisplay && removeSearch) {
						visibility = null;
					}

					if (removeDisplay && visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS) {
						visibility = CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY;
					}

					if (removeSearch && visibility == CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS) {
						visibility = CreativeModeTab.TabVisibility.PARENT_TAB_ONLY;
					}

					entries.add(new AbstractMap.SimpleEntry<>(entry.getKey(), visibility));
				}
			}

			for (var entry : entries) {
				if (entry.getValue() == null) {
					event.getEntries().remove(entry.getKey());
				} else {
					event.getEntries().put(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
		var tabId = event.getTabKey().location();

		if (StartupEvents.MODIFY_CREATIVE_TAB.hasListeners(tabId)) {
			StartupEvents.MODIFY_CREATIVE_TAB.post(ScriptType.STARTUP, tabId, new CreativeTabKubeEvent(event.getTab(), event.hasPermissions(), new CreativeTabCallbackForge(event)));
		}
	}

	private static void loadComplete(FMLLoadCompleteEvent event) {
		KubeJS.instance.loadComplete();
	}

	public static Optional<IEventBus> eventBus() {
		return Optional.ofNullable(BUS.get());
	}
}
