package zmaster587.advancedRocketry.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class chunkdata {
    public int x;
    public int z;
    public TerraformingType type;


    // chunk can be populated if itself and the 3 chunks with (x+1), (z+1) and (x+1,z+1) are fully generated in the world


    // if a chunks terrain is fully generated and does not need any future updates - causes it to set the IBlockState[][][] to null to free memory
    // if the terrain is fully generated it allows for calling populate()
    public boolean terrain_fully_generated;

    // will be set by BiomeHandler after calling the populate method, makes the chunk fully generated and it does not need any future updates
    // as long as the atmosphere does not change, chunks that are fully generated will not register their blocks in the terraforming queue
    public boolean chunk_fully_generated;

    // if the y values of new chunk and chunk in world match, terrain at this position is considered fully generated
    public boolean[][] fully_generated;

    // if all positions have been decorated
    public boolean[][] fully_decorated;


    public IBlockState[][][] blockStates;

    World world; // used to call populate after decoration is finished
    TerraformingHelper helper;

    public chunkdata(int x, int z, IBlockState[][][] blockStates, World world, TerraformingHelper helper) {
        this.x = x;
        this.world = world;
        this.helper = helper;
        this.z = z;
        this.blockStates = blockStates;
        this.type = TerraformingType.ALLOWED;
        chunk_fully_generated = false;
        terrain_fully_generated = false;
        fully_generated = new boolean[16][16];
        fully_decorated = new boolean[16][16];
    }

    /*
    sets if a blockpos is fully generated and computed if the chunk is fully generated
    called from BiomeHandler
     */
    public void set_position_fully_generated(int x, int z){
        fully_generated[x][z] = true;
        boolean all_generated = true;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                if (fully_generated[i][j] == false) {
                    all_generated = false;
                }
            }
        }
        if (all_generated){
            terrain_fully_generated = true;
            this.blockStates = null; // no longer needed, gc should collect them now
            helper.check_can_decorate(x,z);
        }
    }

    public void set_position_decorated(int x, int z){

        fully_decorated[x][z] = true;
        boolean all_decorated = true;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                if (fully_decorated[i][j] == false) {
                    all_decorated = false;
                }
            }
        }
        if (all_decorated){
            // populate uses the biome at blockpos 0,0, in the chunk x+1,z+1, that's why we need the chunks next to it generated
            world.provider.createChunkGenerator().populate(this.x, this.z);
            helper.setChunkFullyGenerated(this.x,this.z);
        }


    }

}
