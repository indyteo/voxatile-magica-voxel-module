package fr.theoszanto.voxatile.vox;

import com.ignfab.minalac.generator.parameters.OutputFormat;
import com.ignfab.minalac.generator.parameters.ParamsParser;
import com.ignfab.minalac.generator.utils.modules.Module;

/**
 * Vox format (MagicaVoxel) module.
 * @see <a href="https://github.com/ephtracy/voxel-model/blob/master/MagicaVoxel-file-format-vox.txt">MagicaVoxel .vox File Format</a>
 */
public class MagicaVoxelModule extends Module {
	@Override
	public void registerParams(ParamsParser parser) {
		parser.registerFormat("vox", new OutputFormat(MagicaVoxelWorld::new, MagicaVoxelParams.class, MagicaVoxelParams::fromHex));
	}
}
