package zmaster587.advancedRocketry.fuckin_bs_integrated_server_and_client_variable_sharing_crap_fix_fuckit_Im_in_rage;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import zmaster587.advancedRocketry.util.TerraformingHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class serverlists implements Afuckinginterface {
    Map<Integer, dimensionTerraformingInfo> terraforminginfolists;

    public serverlists(){
        this.terraforminginfolists = new HashMap<>();
    }

    public void initdim(int dim){
        if (terraforminginfolists.get(dim) != null){
            terraforminginfolists.get(dim).terraformingChunksDone = new ArrayList<>();
            terraforminginfolists.get(dim).terraformingProtectedBlocks = new ArrayList<>();
        }else{
            dimensionTerraformingInfo info = new dimensionTerraformingInfo();
            info.terraformingChunksDone = new ArrayList<>();
            info.terraformingProtectedBlocks = new ArrayList<>();
            terraforminginfolists.put(dim,info);
        }
    }
    public boolean isinitialized(int dim){
        return (terraforminginfolists.get(dim) != null);
    }
    public List<BlockPos> getProtectingBlocksForDimension(int dim){
        if (terraforminginfolists.get(dim) == null)return null;
        return terraforminginfolists.get(dim).terraformingProtectedBlocks;
    }
    public void setProtectingBlocksForDimension(int dim, List<BlockPos> blocks){
        if (terraforminginfolists.get(dim) == null)return;
        terraforminginfolists.get(dim).terraformingProtectedBlocks = blocks;
    }

    public void setChunksFullyTerraformed(int dim, List<ChunkPos> lpos){
        if (terraforminginfolists.get(dim) == null)return;
        terraforminginfolists.get(dim).terraformingChunksDone = lpos;
    }
    public List<ChunkPos> getChunksFullyTerraformed(int dim){
        if (terraforminginfolists.get(dim) == null)return null;
        return terraforminginfolists.get(dim).terraformingChunksDone;
    }

    public void sethelper(int dim, TerraformingHelper helper){
        if (terraforminginfolists.get(dim) == null)return;
        terraforminginfolists.get(dim).terraformingHelper= helper;
    }
    public TerraformingHelper gethelper(int dim){
        if (terraforminginfolists.get(dim) == null)return null;
        return terraforminginfolists.get(dim).terraformingHelper;
    }
}
