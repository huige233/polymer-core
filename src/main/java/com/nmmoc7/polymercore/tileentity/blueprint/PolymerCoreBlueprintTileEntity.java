package com.nmmoc7.polymercore.tileentity.blueprint;

import com.nmmoc7.polymercore.RegisterHandler;
import com.nmmoc7.polymercore.blueprint.IBlueprint;
import com.nmmoc7.polymercore.blueprint.type.IBlueprintType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class PolymerCoreBlueprintTileEntity extends TileEntity implements IPolymerCoreBlueprintTileEntity {
    IBlueprint blueprint;

    public PolymerCoreBlueprintTileEntity() {
        super(RegisterHandler.BLUEPRINT_TILE);
    }

    public void setBlueprint(IBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    @Override
    public IBlueprint getBlueprint() {
        return blueprint;
    }

    @Override
    public boolean test() {
        for (Map.Entry<BlockPos, IBlueprintType> entry : getBlueprint().getMap().entrySet()) {
            if (entry.getValue().test(world.getBlockState(entry.getKey().add(getPos())))) {
                return false;
            }
        }

        return true;
    }
}
