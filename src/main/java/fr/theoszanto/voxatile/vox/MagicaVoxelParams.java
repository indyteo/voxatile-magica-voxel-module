package fr.theoszanto.voxatile.vox;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import fr.ign.voxatile.core.parameters.placeables.PlaceableParams;
import fr.ign.voxatile.core.utils.random.Seed;

import java.beans.ConstructorProperties;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
		@JsonSubTypes.Type(MagicaVoxelParams.PackedMagicaVoxelParams.class),
		@JsonSubTypes.Type(MagicaVoxelParams.SplitMagicaVoxelParams.class),
})
public abstract class MagicaVoxelParams extends PlaceableParams {
	protected abstract int rgba();

	@Override
	public MagicaVoxel create(Seed seed) {
		return new MagicaVoxel(rgba());
	}

	public static MagicaVoxelParams fromHex(String hex) {
		if (hex.equalsIgnoreCase("empty"))
			return new PackedMagicaVoxelParams(0);
		if (!hex.startsWith("#"))
			throw new IllegalArgumentException("Hex color must start with '#'");
		String val = hex.substring(1);
		return new PackedMagicaVoxelParams(Integer.parseUnsignedInt(switch (val.length()) {
			case 1 -> val.repeat(6) + "ff";
			case 3 -> "" + val.charAt(0) + val.charAt(0) + val.charAt(1) + val.charAt(1) + val.charAt(2) + val.charAt(2) + "ff";
			case 6 -> val + "ff";
			case 8 -> val;
			default -> throw new IllegalArgumentException("Invalid hex color value: " + val);
		}, 16));
	}

	public static class PackedMagicaVoxelParams extends MagicaVoxelParams {
		@JsonSetter(nulls = Nulls.FAIL)
		public int rgba;

		@ConstructorProperties("rgba")
		public PackedMagicaVoxelParams(int rgba) {
			this.rgba = rgba;
		}

		@Override
		protected int rgba() {
			return rgba;
		}
	}

	public static class SplitMagicaVoxelParams extends MagicaVoxelParams {
		@JsonSetter(nulls = Nulls.SKIP)
		public int r = 0;

		@JsonSetter(nulls = Nulls.SKIP)
		public int g = 0;

		@JsonSetter(nulls = Nulls.SKIP)
		public int b = 0;

		@JsonSetter(nulls = Nulls.SKIP)
		public int a = 0xFF;

		@Override
		protected int rgba() {
			return r << 24 | g << 16 | b << 8 | a;
		}

		@Override
		public void validate() throws IllegalArgumentException {
			check(r, "red");
			check(g, "green");
			check(b, "blue");
			check(a, "alpha");
		}

		private void check(int channel, String name) {
			if ((channel & 0xFF) != channel)
				throw new IllegalArgumentException(name + " value must be between 0 and 255");
		}
	}
}
