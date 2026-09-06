package fr.theoszanto.voxatile.vox;

import com.ignfab.minalac.generator.placeables.Placeable;
import com.ignfab.minalac.generator.world.VoxelTile;

public record MagicaVoxel(int rgba) implements Placeable {
	public static final MagicaVoxel DEFAULT_VOXEL = new MagicaVoxel(0);

	public boolean isEmpty() {
		return rgba == 0;
	}

	@Override
	public void place(VoxelTile tile, int x, int y, int z) {
		if (!(tile instanceof MagicaVoxelTile mvTile))
			throw new IllegalArgumentException("Magica voxel can only be placed in a MagicaVoxel world");
		mvTile.setVoxel(x, y, z, this);
	}
}
