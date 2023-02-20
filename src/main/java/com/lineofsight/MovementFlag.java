package com.lineofsight;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.CollisionDataFlag;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
enum MovementFlag
{
    BLOCK_MOVEMENT_NORTH_WEST(CollisionDataFlag.BLOCK_MOVEMENT_NORTH_WEST),
    BLOCK_MOVEMENT_NORTH(CollisionDataFlag.BLOCK_MOVEMENT_NORTH),
    BLOCK_MOVEMENT_NORTH_EAST(CollisionDataFlag.BLOCK_MOVEMENT_NORTH_EAST),
    BLOCK_MOVEMENT_EAST(CollisionDataFlag.BLOCK_MOVEMENT_EAST),
    BLOCK_MOVEMENT_SOUTH_EAST(CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_EAST),
    BLOCK_MOVEMENT_SOUTH(CollisionDataFlag.BLOCK_MOVEMENT_SOUTH),
    BLOCK_MOVEMENT_SOUTH_WEST(CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_WEST),
    BLOCK_MOVEMENT_WEST(CollisionDataFlag.BLOCK_MOVEMENT_WEST),

    BLOCK_MOVEMENT_OBJECT(CollisionDataFlag.BLOCK_MOVEMENT_OBJECT),
    BLOCK_MOVEMENT_FLOOR_DECORATION(CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION),
    BLOCK_MOVEMENT_FLOOR(CollisionDataFlag.BLOCK_MOVEMENT_FLOOR),
    BLOCK_MOVEMENT_FULL(CollisionDataFlag.BLOCK_MOVEMENT_FULL),

    BLOCK_LINE_OF_SIGHT_NORTH(CollisionDataFlag.BLOCK_LINE_OF_SIGHT_NORTH), // 0x400
    BLOCK_LINE_OF_SIGHT_EAST(CollisionDataFlag.BLOCK_LINE_OF_SIGHT_EAST), // 0x1000
    BLOCK_LINE_OF_SIGHT_SOUTH(CollisionDataFlag.BLOCK_LINE_OF_SIGHT_SOUTH), // 0x4000
    BLOCK_LINE_OF_SIGHT_WEST(CollisionDataFlag.BLOCK_LINE_OF_SIGHT_WEST), // 0x10000
    BLOCK_LINE_OF_SIGHT_FULL(CollisionDataFlag.BLOCK_LINE_OF_SIGHT_FULL);


    @Getter
    private int flag;

    /**
     * @param collisionData The tile collision flags.
     * @return The set of {@link MovementFlag}s that have been set.
     */
    public static Set<MovementFlag> getSetFlags(int collisionData)
    {
        return Arrays.stream(values())
                .filter(movementFlag -> (movementFlag.flag & collisionData) != 0)
                .collect(Collectors.toSet());
    }
}
