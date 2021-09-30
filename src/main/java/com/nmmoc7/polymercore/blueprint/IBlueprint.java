package com.nmmoc7.polymercore.blueprint;

import com.nmmoc7.polymercore.blueprint.type.IBlueprintType;
import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public interface IBlueprint {
    Map<BlockPos, IBlueprintType> getMap();
}
