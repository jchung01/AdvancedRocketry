package zmaster587.advancedRocketry.atmosphere;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import zmaster587.advancedRocketry.api.AreaBlob;
import zmaster587.advancedRocketry.api.IAtmosphere;
import zmaster587.libVulpes.util.HashedBlockPosition;

import java.util.List;

/**
 * Listens to block changes and processes atmosphere effects on the block.
 */
public class AtmosphereBlockListener  {
    private final AtmosphereHandler handler;
    private final AtmosphereBehaviors.BlockEffect effect;

    public AtmosphereBlockListener(AtmosphereHandler handler, AtmosphereBehaviors.BlockEffect effect) {
        this.handler = handler;
        this.effect = effect;
    }

    public void notifyBlockUpdate(World world, BlockPos pos, IBlockState state, int flags) {
        IAtmosphere typeAtPos = handler.getAtmosphereType(pos);
        // Only process effect if it's the expected type (not in a blob)
        if (typeAtPos instanceof AtmosphereType && effect.canHandle((AtmosphereType) typeAtPos)) {
            boolean handled = effect.handle(world, pos, state, flags);
            // Prevent recursive calls
            if (!handled) return;
        }

        HashedBlockPosition hPos = new HashedBlockPosition(pos);
        List<AreaBlob> nearbyBlobs = handler.getBlobWithinRadius(hPos, AtmosphereHandler.MAX_BLOB_RADIUS);
        if (nearbyBlobs.isEmpty()) return;

        for (AreaBlob blob : nearbyBlobs) {
            if (blob.getBlobMaxRadius() > hPos.getDistance(blob.getRootPosition())) {
                if (world.isAirBlock(pos))
                    onBlockRemove(hPos);
                else {
                    //Place block
                    if (blob.contains(hPos) && !blob.isPositionAllowed(world, hPos, nearbyBlobs)) {
                        blob.removeBlock(hPos);
                    } else if (!blob.contains(blob.getRootPosition())) {
                        blob.addBlock(blob.getRootPosition(), nearbyBlobs);
                    } else if (!blob.contains(hPos) && blob.isPositionAllowed(world, hPos, nearbyBlobs))//isFulBlock(world, pos.getBlockPos()))
                        blob.addBlock(hPos, nearbyBlobs);
                }
            }
        }
    }

    private void onBlockRemove(HashedBlockPosition pos) {
        List<AreaBlob> blobs = handler.getBlobWithinRadius(pos, AtmosphereHandler.MAX_BLOB_RADIUS);
        for (AreaBlob blob : blobs) {
            //Make sure that a block can actually be attached to the blob
            for (EnumFacing dir : EnumFacing.VALUES)
                if (blob.contains(pos.getPositionAtOffset(dir))) {
                    blob.addBlock(pos, blobs);
                    break;
                }
        }
    }
}
