# MagicaVoxel output format

MagicaVoxel output format exports world in [.vox file version 150](https://github.com/ephtracy/voxel-model/blob/master/MagicaVoxel-file-format-vox.txt).

A single generation cannot exceed 256x256x256 size, centered on (0, 0, 0).

## Table of contents

* [Output format description](#output-format-description)
* [Voxel placeable description](#voxel-placeable-description)
  * [Full format](#full-format)
  * [Short format](#short-format)

## Output format description

```yaml
format: vox
```

The generated model will be saved as a `.vox` file in the destination directory. The file will be named after the `worldName` parameter (with special characters replaced by underscores).

## Voxel placeable description

Voxels in MagicaVoxel are colors, stored in a 255-color palette. As a result, no more than 255 different colors can be used in a single generation!

#### Full format

Example of a Magica voxel description (RGBA packed integer) for `place` field:

```yaml
place:
  rgba: 0xFF8000FF
```

Example of a Magica voxel description (separate red, green, blue, alpha values) for `place` field:

```yaml
place:
  r: 0xFF # 255
  g: 0x80 # 128
  b: 0x00 # 0
```

Fields:
- `rgba`: Integer value with 8 bits for each red, green, blue and alpha channel (required)

  *OR*

- `r`: Red channel value (8-bit integer, optional, default `0`)
- `g`: Green channel value (8-bit integer, optional, default `0`)
- `b`: Blue channel value (8-bit integer, optional, default `0`)
- `a`: Alpha channel value (8-bit integer, optional, default `255`)

Alpha channel is the opacity, with `0` being fully transparent and `255` fully opaque. Be careful when using `rgba` to not forget the alpha channel!

Numbers in YAML can be written in hexadecimal using `0x` prefix. This is handy to write `rgba` values. Decimal numbers work, but might be harder to read for a human.

#### Short format

The Magica voxel short form string is interpreted as a `#`-prefixed hexadecimal color. It must start with a `#`, followed by either 1, 3, 6 or 8 hexadecimal digits.

The color is created depending on the number of hex digits:

| Digits | Short | Full (rgba) |
|---|---|---|
| 1 | `#x` | `#xxxxxxff` |
| 3 | `#xyz` | `#xxyyzzff` |
| 6 | `#xyzuvw` | `#xyzuvwff` |
| 8 | `#xyzuvwst` | *unchanged* |

The special value `empty` stands for `#00000000` and represents an empty (fully transparent) voxel.

Be careful when writing hexadecimal colors in YAML, as the `#` symbol is used for comments. For example, `place: #360` is the same as `place:` (with no value, which may produce unexpected results). Write instead `place: "#360"` with quotes (either single or double).
