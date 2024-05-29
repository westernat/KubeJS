package dev.latvian.mods.kubejs.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.schema.DynamicRecipeComponent;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.desc.DescriptionContext;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;
import dev.latvian.mods.kubejs.util.ID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public record RegistryComponent<T>(RegistryInfo<T> registry) implements RecipeComponent<T> {
	@SuppressWarnings("unchecked")
	public static final DynamicRecipeComponent DYNAMIC = new DynamicRecipeComponent(TypeDescJS.object(1).add("registry", TypeDescJS.STRING.or(DescriptionContext.DEFAULT.javaType(RegistryInfo.class))), (ctx, scope, args) -> {
		Object registry = args.get("registry");
		RegistryInfo<?> regInfo;
		if (registry instanceof RegistryInfo<?> registryInfo) {
			regInfo = registryInfo;
		} else if (registry instanceof ResourceKey<?> resourceKey) {
			regInfo = RegistryInfo.of((ResourceKey) resourceKey);
		} else {
			regInfo = RegistryInfo.of(ResourceKey.createRegistryKey(new ResourceLocation(String.valueOf(registry))));
		}

		return new RegistryComponent<>(regInfo);
	});

	@Override
	public String componentType() {
		return "registry_element";
	}

	@Override
	public TypeDescJS constructorDescription(DescriptionContext ctx) {
		return TypeDescJS.STRING.or(ctx.javaType(registry.objectBaseClass));
	}

	@Override
	public Class<?> componentClass() {
		return registry.objectBaseClass;
	}

	@Override
	public JsonElement write(KubeRecipe recipe, T value) {
		return new JsonPrimitive(registry.getId(value).toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public T read(KubeRecipe recipe, Object from) {
		if (registry.objectBaseClass != Object.class && registry.objectBaseClass.isInstance(from)) {
			return (T) from;
		} else if (!(from instanceof CharSequence) && !(from instanceof JsonPrimitive) && !(from instanceof ResourceLocation)) {
			if (registry == RegistryInfo.ITEM) {
				if (from instanceof ItemStack is) {
					return (T) is.getItem();
				} else {
					return (T) recipe.readOutputItem(from).item.getItem();
				}
			} else if (registry == RegistryInfo.FLUID) {
				if (from instanceof FluidStack fs) {
					return (T) fs.getFluid();
				}
			}
		}

		return registry.getValue(ID.mc(from));
	}

	@Override
	public boolean hasPriority(KubeRecipe recipe, Object from) {
		return (registry.objectBaseClass != Object.class && registry.objectBaseClass.isInstance(from)) || (from instanceof CharSequence && ID.mc(from.toString()) != null) || (from instanceof JsonPrimitive json && json.isString() && ID.mc(json.getAsString()) != null);
	}

	@Override
	public String toString() {
		return "%s{%s}".formatted(componentType(), registry);
	}
}
