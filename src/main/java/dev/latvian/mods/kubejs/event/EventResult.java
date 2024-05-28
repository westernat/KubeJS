package dev.latvian.mods.kubejs.event;

import dev.architectury.event.CompoundEventResult;
import dev.latvian.mods.kubejs.util.Cast;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EventResult {
	public enum Type {
		ERROR(dev.architectury.event.EventResult.pass()),
		PASS(dev.architectury.event.EventResult.pass()),
		INTERRUPT_DEFAULT(dev.architectury.event.EventResult.interruptDefault()),
		INTERRUPT_FALSE(dev.architectury.event.EventResult.interruptFalse()),
		INTERRUPT_TRUE(dev.architectury.event.EventResult.interruptTrue());

		public final EventResult defaultResult;
		public final dev.architectury.event.EventResult defaultArchResult;
		public final EventExit defaultExit;

		Type(dev.architectury.event.EventResult defaultArchResult) {
			this.defaultResult = new EventResult(this, null);
			this.defaultArchResult = defaultArchResult;
			this.defaultExit = new EventExit(this.defaultResult);
		}

		public EventExit exit(@Nullable Object value) {
			return value == null ? defaultExit : new EventExit(new EventResult(this, value));
		}
	}

	public static final EventResult PASS = Type.PASS.defaultResult;

	private final Type type;
	private final Object value;

	private EventResult(Type type, @Nullable Object value) {
		this.type = type;
		this.value = value;
	}

	public Type type() {
		return type;
	}

	public Object value() {
		return value;
	}

	public boolean override() {
		return type != Type.PASS;
	}

	public boolean pass() {
		return type == Type.PASS;
	}

	public boolean error() {
		return type == Type.ERROR;
	}

	public boolean interruptDefault() {
		return type == Type.INTERRUPT_DEFAULT;
	}

	public boolean interruptFalse() {
		return type == Type.INTERRUPT_FALSE;
	}

	public boolean interruptTrue() {
		return type == Type.INTERRUPT_TRUE;
	}

	public dev.architectury.event.EventResult arch() {
		return type.defaultArchResult;
	}

	public <T> CompoundEventResult<T> archCompound() {
		return switch (type) {
			case INTERRUPT_DEFAULT -> CompoundEventResult.interruptDefault(Cast.to(value));
			case INTERRUPT_FALSE -> CompoundEventResult.interruptFalse(Cast.to(value));
			case INTERRUPT_TRUE -> CompoundEventResult.interruptTrue(Cast.to(value));
			default -> CompoundEventResult.pass();
		};
	}

	public boolean applyCancel(ICancellableEvent event) {
		if (interruptFalse()) {
			event.setCanceled(true);
			return true;
		}

		return false;
	}

	public void applyTristate(Consumer<TriState> consumer) {
		if (interruptFalse()) {
			consumer.accept(TriState.FALSE);
		} else if (interruptTrue()) {
			consumer.accept(TriState.TRUE);
		}
	}
}
