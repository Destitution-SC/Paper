package org.bukkit.craftbukkit.block.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import io.papermc.paper.generated.GeneratedFrom;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Fire;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.jetbrains.annotations.Range;

@GeneratedFrom("1.20.4")
@SuppressWarnings("unused")
public class CraftFire extends CraftBlockData implements Fire {
    private static final IntegerProperty AGE = FireBlock.AGE;

    private static final Map<BlockFace, BooleanProperty> PROPERTY_BY_DIRECTION = Map.of(
        BlockFace.EAST, FireBlock.EAST,
        BlockFace.NORTH, FireBlock.NORTH,
        BlockFace.SOUTH, FireBlock.SOUTH,
        BlockFace.UP, FireBlock.UP,
        BlockFace.WEST, FireBlock.WEST
    );

    public CraftFire(BlockState state) {
        super(state);
    }

    @Override
    @Range(
            from = 0,
            to = 15
    )
    public int getAge() {
        return this.get(AGE);
    }

    @Override
    public void setAge(@Range(from = 0, to = 15) final int age) {
        this.set(AGE, age);
    }

    @Override
    public int getMaximumAge() {
        return AGE.max;
    }

    @Override
    public boolean hasFace(final BlockFace blockFace) {
        Preconditions.checkArgument(blockFace != null, "blockFace cannot be null!");
        BooleanProperty property = PROPERTY_BY_DIRECTION.get(blockFace);
        Preconditions.checkArgument(property != null, "Invalid %s, only %s are allowed!".formatted("blockFace", PROPERTY_BY_DIRECTION.keySet().stream().map(Enum::name).collect(Collectors.joining(", "))));
        return this.get(property);
    }

    @Override
    public void setFace(final BlockFace blockFace, final boolean face) {
        Preconditions.checkArgument(blockFace != null, "blockFace cannot be null!");
        BooleanProperty property = PROPERTY_BY_DIRECTION.get(blockFace);
        Preconditions.checkArgument(property != null, "Invalid %s, only %s are allowed!".formatted("blockFace", PROPERTY_BY_DIRECTION.keySet().stream().map(Enum::name).collect(Collectors.joining(", "))));
        this.set(property, face);
    }

    @Override
    public Set<BlockFace> getFaces() {
        ImmutableSet.Builder<BlockFace> faces = ImmutableSet.builder();
        for (Map.Entry<BlockFace, BooleanProperty> entry : PROPERTY_BY_DIRECTION.entrySet()) {
            if (this.get(entry.getValue())) {
                faces.add(entry.getKey());
            }
        }
        return faces.build();
    }

    @Override
    public Set<BlockFace> getAllowedFaces() {
        return Collections.unmodifiableSet(PROPERTY_BY_DIRECTION.keySet());
    }
}
