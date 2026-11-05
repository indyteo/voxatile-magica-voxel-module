package fr.theoszanto.voxatile.vox;

import fr.ign.voxatile.core.placeables.Placeable;
import fr.ign.voxatile.core.world.VoxelTile;

/**
 * Magica voxel placeable with an RGBA color.
 * @param rgba color of the voxel
 */
public record MagicaVoxel(int rgba) implements Placeable {
	/**
	 * Default voxel, fully transparent.
	 */
	public static final MagicaVoxel DEFAULT_VOXEL = new MagicaVoxel(0);

	/**
	 * {@return whether this voxel is empty (i.e. black, fully transparent)}
	 */
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
