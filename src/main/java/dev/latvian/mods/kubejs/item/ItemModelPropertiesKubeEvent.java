package dev.latvian.mods.kubejs.item;

import dev.architectury.registry.item.ItemPropertiesRegistry;
import dev.latvian.mods.kubejs.event.KubeStartupEvent;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.ID;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.world.item.crafting.Ingredient;

public class ItemModelPropertiesKubeEvent implements KubeStartupEvent {

	@Info("""
		Register a model property for an item. Model properties are used to change the appearance of an item in the world.
				
		More about model properties: https://minecraft.fandom.com/wiki/Model#Item_predicates
		""")
	public void register(Ingredient ingredient, String overwriteId, ClampedItemPropertyFunction callback) {
		var id = ID.kjs(overwriteId);

		if (ingredient.kjs$isWildcard()) {
			ItemPropertiesRegistry.registerGeneric(id, callback);

		} else {
			for (var stack : ingredient.kjs$getStacks()) {
				ItemPropertiesRegistry.register(stack.getItem(), id, callback);
			}
		}
	}

	@Info("Register a model property for all items.")
	public void registerAll(String overwriteId, ClampedItemPropertyFunction callback) {
		ItemPropertiesRegistry.registerGeneric(ID.kjs(overwriteId), callback);
	}
}
