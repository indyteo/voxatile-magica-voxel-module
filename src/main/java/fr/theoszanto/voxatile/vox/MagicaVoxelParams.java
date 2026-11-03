package fr.theoszanto.voxatile.vox;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import fr.ign.voxatile.core.parameters.placeables.PlaceableParams;
import fr.ign.voxatile.core.utils.random.Seed;

import java.beans.ConstructorProperties;

public class MagicaVoxelParams extends PlaceableParams {
	@JsonSetter(nulls = Nulls.FAIL)
	public int rgba;

	@ConstructorProperties("rgba")
	public MagicaVoxelParams(int rgba) {
		this.rgba = rgba;
	}

	@Override
	public MagicaVoxel create(Seed seed) {
		return new MagicaVoxel(rgba);
	}

	public static MagicaVoxelParams fromHex(String hex) {
		if (hex.equalsIgnoreCase("empty"))
			return new MagicaVoxelParams(0);
		if (!hex.startsWith("#"))
			throw new IllegalArgumentException("Hex color must start with '#'");
		String val = hex.substring(1);
		return new MagicaVoxelParams(Integer.parseUnsignedInt(switch (val.length()) {
			case 3 -> "" + val.charAt(0) + val.charAt(0) + val.charAt(1) + val.charAt(1) + val.charAt(2) + val.charAt(2) + "ff";
			case 6 -> val + "ff";
			case 8 -> val;
			default -> throw new IllegalArgumentException("Invalid hex color value: " + val);
		}, 16));
	}
}
