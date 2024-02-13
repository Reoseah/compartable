package io.github.reoseah.parts.api;

import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;

public abstract class Parts {
    /**
     * Luminance of a part container block, equals to the maximum of its parts.
     */
    public static final IntProperty LUMINANCE = IntProperty.of("luminance", 0, 15);
    /**
     * Whether a part container block emits redstone power, actually is whether redstone should connect to it
     * and unrelated to the redstone power it emits. True if any part "emits" redstone.
     */
    public static final BooleanProperty EMITS_REDSTONE = BooleanProperty.of("emits_redstone");

    private Parts() {
    }
}
