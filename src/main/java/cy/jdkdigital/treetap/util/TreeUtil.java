package cy.jdkdigital.treetap.util;

import cy.jdkdigital.treetap.TreeTap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TreeUtil
{
    private static List<BlockPos> findLogs(LevelAccessor level, BlockPos pos) {
        final Set<BlockPos> seen = new HashSet<>(64);
        final List<BlockPos> logs = new ArrayList<>(16);
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        logs.add(pos);
        for (int i = 0; i < logs.size(); i++) {
            final BlockPos log = logs.get(i);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        cursor.setWithOffset(log, dx, dy, dz);
                        if (!seen.contains(cursor)) {
                            final BlockPos cursorPos = cursor.immutable();
                            final BlockState cursorState = level.getBlockState(cursorPos);

                            if (cursorState.is(BlockTags.LOGS)) {
                                logs.add(cursorPos);
                            }
                            seen.add(cursorPos);
                        }
                    }
                }
            }
        }

        Collections.reverse(logs);
        return logs;
    }

    public static boolean isValidTree(Level level, BlockPos pos, int requiredBlocks) {
        return findLogs(level, pos).size() >= requiredBlocks;
    }

    public static float adjustTapModifier(Level level, BlockPos pos, float modifier) {
        // Check for other taps on same trunk
        AtomicInteger taps = new AtomicInteger(0);
        while (level.getBlockState(pos.below()).is(TreeTap.TAPPABLE)) {
            pos = pos.below();
        }
        findLogs(level, pos).forEach(blockPos -> {
            for (Direction direction : Direction.values()) {
                var sideState = level.getBlockState(blockPos.relative(direction));
                if (sideState.is(TreeTap.TAP.get()) && sideState.hasProperty(BlockStateProperties.ATTACHED) && sideState.getValue(BlockStateProperties.ATTACHED)) {
                    taps.getAndIncrement();
                }
            }
        });
        return modifier / Math.max(1, taps.get() - 1);
    }
}
