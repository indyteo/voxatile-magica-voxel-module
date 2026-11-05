package fr.theoszanto.voxatile.vox;

import fr.ign.voxatile.core.utils.FileHelpers;
import fr.ign.voxatile.core.utils.world2d.WorldBBox2d;
import fr.ign.voxatile.core.utils.world3d.WorldBBox3d;
import fr.ign.voxatile.core.utils.world3d.WorldCoords3d;
import fr.ign.voxatile.core.world.MapWriteException;
import fr.ign.voxatile.core.world.VoxelTile;
import fr.ign.voxatile.core.world.VoxelWorld;
import fr.ign.voxatile.core.world.VoxelWorldMetadata;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link VoxelWorld} that create a MagicaVoxel
 * model. The model uses the .vox version 150 file format.
 * This format does not support tiling. {@link #tiles(int)}
 * will always return a single tile covering the whole area.
 */
public class MagicaVoxelWorld extends VoxelWorld {
	private final File destination;

	// Not exactly hard limits, but each model coordinates is encoded as a single
	// byte and currently only a single model is included in the generated file.
	private static final WorldBBox3d MAX_LIMIT = new WorldBBox3d(
			new WorldCoords3d(Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE),
			new WorldCoords3d(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE)
	);

	/**
	 * Constructs a new {@code MagicaVoxelWorld}.
	 * The limits of the world have to be set using {@link #setLimits(WorldBBox3d)}
	 *
	 * @param destination Directory where to save .vox file to. If null nothing is saved.
	 */
	public MagicaVoxelWorld(File destination) {
		super(new VoxelWorldMetadata());
		this.destination = destination;
	}

	@Override
	public WorldBBox3d maxLimits() {
		return MAX_LIMIT;
	}

	@Override
	public VoxelTile newTile(WorldBBox3d limits) {
		return new MagicaVoxelTile(limits, generateVoxFileName());
	}

	@Override
	public void initialize() throws MapWriteException {
		if (destination == null)
			return;
		if (!FileHelpers.isReadableDirectory(destination))
			throw new MapWriteException("Directory %s can not be accessed".formatted(destination));
	}

	@Override
	public void finalizeAndSave() {}

	@Override
	public Collection<WorldBBox2d> tiles(int maxTileSize) {
		return List.of(limits().to2d());
	}

	private File generateVoxFileName() {
		if (destination == null)
			return null;
		return new File(destination, Objects.requireNonNullElse(metadata.getWorldName(), "voxatile")
				.toLowerCase()
				.replaceAll("[^a-z0-9]+", "_") + ".vox");
	}
}
