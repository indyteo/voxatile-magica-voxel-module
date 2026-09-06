package fr.theoszanto.voxatile.vox;

import com.ignfab.minalac.generator.utils.world3d.WorldBBox3d;
import com.ignfab.minalac.generator.world.MapWriteException;
import com.ignfab.minalac.generator.world.VoxelTile;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MagicaVoxelTile extends VoxelTile {
	private final File voxFile;
	private final byte[] data = new byte[256 * 256 * 256];
	private final Palette palette = new Palette();
	private int minX = Integer.MAX_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int minZ = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int maxY = Integer.MIN_VALUE;
	private int maxZ = Integer.MIN_VALUE;
	private int nVoxels = 0;

	private static final byte ID_EMPTY = (byte) 0;

	public MagicaVoxelTile(WorldBBox3d limits, File voxFile) {
		super(limits);
		this.voxFile = voxFile;
	}

	private static void write(OutputStream stream, String str) throws IOException {
		stream.write(str.getBytes(StandardCharsets.UTF_8));
	}

	private static void write(OutputStream stream, int i, boolean bigEndian) throws IOException {
		if (bigEndian) {
			stream.write(i >>> 24);
			stream.write(i >>> 16);
			stream.write(i >>> 8);
			stream.write(i);
		} else {
			stream.write(i);
			stream.write(i >>> 8);
			stream.write(i >>> 16);
			stream.write(i >>> 24);
		}
	}

	private static void write(OutputStream stream, int i) throws IOException {
		write(stream, i, false);
	}

	@Override
	public void save() throws MapWriteException {
		if (voxFile == null)
			return;
		if (nVoxels == 0) {
			minX = 0;
			minY = 0;
			minZ = 0;
			maxX = -1;
			maxY = -1;
			maxZ = -1;
		}

		try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(voxFile))) {
			write(stream, "VOX ");
			write(stream, 150); // Version

			int chunkHeaderSize = 4 * 3;
			int sizeChunkSize = 4 * 3;
			int xyziChunkSize = 4 + 4 * nVoxels;
			int rgbaChunkSize = 4 * 256;

			write(stream, "MAIN");
			write(stream, 0); // Content size
			write(stream, 3 * chunkHeaderSize + sizeChunkSize + xyziChunkSize + rgbaChunkSize); // Children size

			// Size chunk
			write(stream, "SIZE");
			write(stream, sizeChunkSize); // Content size
			write(stream, 0); // Children size
			write(stream, maxX - minX + 1); // Size X
			write(stream, maxY - minY + 1); // Size Y
			write(stream, maxZ - minZ + 1); // Size Z

			// XYZI chunk
			write(stream, "XYZI");
			write(stream, xyziChunkSize); // Content size
			write(stream, 0); // Children size
			write(stream, nVoxels); // Number of voxels
			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					for (int z = minZ; z <= maxZ; z++) {
						byte id = data[index(x, y, z)];
						if (id != ID_EMPTY) {
							stream.write(x - minX); // X
							stream.write(y - minY); // Y
							stream.write(z - minZ); // Z
							stream.write(id); // Color ID
						}
					}
				}
			}

			// RGBA chunk
			write(stream, "RGBA");
			write(stream, rgbaChunkSize); // Content size
			write(stream, 0); // Children size
			for (int rgba : palette.data())
				write(stream, rgba, true);
		} catch (IOException e) {
			throw new MapWriteException("Unable to write " + voxFile.getAbsolutePath() + " file", e);
		}
	}

	@Override
	protected void updateHeightmaps(int x, int y, int z) {
		super.updateHeightmaps(x, y, z);
		minX = Math.min(minX, x);
		minY = Math.min(minY, y);
		minZ = Math.min(minZ, z);
		maxX = Math.max(maxX, x);
		maxY = Math.max(maxY, y);
		maxZ = Math.max(maxZ, z);
	}

	/* package-private */ void setVoxel(int x, int y, int z, MagicaVoxel voxel) {
		if (isOutOfLimits(x, y, z))
			return;

		int i = index(x, y, z);
		boolean wasEmpty = data[i] == ID_EMPTY;
		if (voxel.isEmpty()) {
			if (!wasEmpty) {
				nVoxels--;
				data[i] = ID_EMPTY;
			}
			return;
		}

		if (wasEmpty)
			nVoxels++;
		data[i] = palette.getOrInsert(voxel.rgba());
		updateHeightmaps(x, y, z);
	}

	@Override
	public MagicaVoxel getVoxel(int x, int y, int z) {
		byte id = data[index(x, y, z)];
		return id == ID_EMPTY ? MagicaVoxel.DEFAULT_VOXEL : new MagicaVoxel(palette.getRgba(id));
	}

	private boolean isOutOfLimits(int x, int y, int z) {
		return !limits().contains(x, y, z);
	}

	private int index(int x, int y, int z) {
		return (x + 128) * 65536 + (y + 128) * 256 + z + 128;
	}

	private static class Palette {
		// Symmetric mapping to allow bidirectional quering
		private final int[] id2rgba = new int[256];
		// Inverse map is synchronized on the same reference to avoid out-of-sync!
		private final Int2IntMap rgba2id = Int2IntMaps.synchronize(new Int2IntOpenHashMap(), id2rgba);

		private void insert(int rgba, byte id) {
			id2rgba[(id & 0xFF) - 1] = rgba;
			rgba2id.put(rgba, id);
		}

		private byte nextId() {
			int id = rgba2id.size();
			if (id >= 255)
				throw new IllegalStateException("Palette is full!");
			return (byte) (id + 1);
		}

		public byte getOrInsert(int rgba) {
			byte id = getId(rgba);
			if (id != -1)
				return id;

			synchronized (id2rgba) {
				id = getId(rgba);
				if (id != -1)
					return id;

				id = nextId();
				insert(rgba, id);
				return id;
			}
		}

		public byte getId(int rgba) {
			return (byte) rgba2id.getOrDefault(rgba, -1);
		}

		public int getRgba(byte id) {
			return id2rgba[(id & 0xFF) - 1];
		}

		public int[] data() {
			return id2rgba;
		}
	}
}
