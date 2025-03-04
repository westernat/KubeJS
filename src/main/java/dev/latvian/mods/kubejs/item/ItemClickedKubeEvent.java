package dev.latvian.mods.kubejs.item;

import dev.latvian.mods.kubejs.entity.KubeRayTraceResult;
import dev.latvian.mods.kubejs.player.KubePlayerEvent;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.ItemWrapper;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Info(value = """
	Invoked when a player right clicks with an item **without targeting anything**.
	
	Not to be confused with `BlockEvents.rightClick` or `ItemEvents.entityInteracted`.
	"""
)
public class ItemClickedKubeEvent implements KubePlayerEvent {
	private final Player player;
	private final InteractionHand hand;
	private final ItemStack item;
	private KubeRayTraceResult target;

	public ItemClickedKubeEvent(Player player, InteractionHand hand, ItemStack item) {
		this.player = player;
		this.hand = hand;
		this.item = item;
	}

	@Override
	@Info("The player that clicked with the item.")
	public Player getEntity() {
		return player;
	}

	@Info("The hand that the item was clicked with.")
	public InteractionHand getHand() {
		return hand;
	}

	@Info("The item that was clicked with.")
	public ItemStack getItem() {
		return item;
	}

	@Info("The ray trace result of the click.")
	public KubeRayTraceResult getTarget() {
		if (target == null) {
			target = player.kjs$rayTrace();
		}

		return target;
	}

	@Override
	@Nullable
	public ItemStack defaultExitValue(Context cx) {
		return item;
	}

	@Override
	@HideFromJS
	public TypeInfo getExitValueType() {
		return ItemWrapper.TYPE_INFO;
	}
}