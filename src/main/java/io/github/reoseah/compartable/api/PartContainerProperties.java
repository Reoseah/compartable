package io.github.reoseah.compartable.api;

import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;

public abstract class PartContainerProperties {
    /**
     * Luminance of a part container, equals to the maximum of its parts.
     */
    public static final IntProperty LUMINANCE = IntProperty.of("luminance", 0, 15);
    /**
     * Whether a part container emits redstone power, actually it's whether redstone should connect to it
     * and unrelated to the redstone power it emits. True if any part "emits" redstone.
     */
    public static final BooleanProperty EMITS_REDSTONE = BooleanProperty.of("emits_redstone");

    private PartContainerProperties() {
    }
}
