/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.utils.schematic.format.defaults;

import baritone.utils.schematic.StaticSchematic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author Brady
 * @since 12/27/2019
 */
public final class MCEditSchematic extends StaticSchematic {

    public MCEditSchematic(NBTTagCompound schematic) {
        String type = schematic.getString("Materials");
        if (!type.equals("Alpha")) {
            throw new IllegalStateException("bad schematic " + type);
        }
        this.x = schematic.getInteger("Width");
        this.y = schematic.getInteger("Height");
        this.z = schematic.getInteger("Length");
        byte[] blocks = schematic.getByteArray("Blocks");
        byte[] metadata = schematic.getByteArray("Data");

        byte[] additional = null;
        if (schematic.hasKey("AddBlocks")) {
            byte[] addBlocks = schematic.getByteArray("AddBlocks");
            additional = new byte[addBlocks.length * 2];
            for (int i = 0; i < addBlocks.length; i++) {
                additional[i * 2] = (byte) ((addBlocks[i] >> 4) & 0xF); // lower nibble
                additional[i * 2 + 1] = (byte) ((addBlocks[i]) & 0xF); // upper nibble
            }
        }
        this.states = new IBlockState[this.x][this.z][this.y];
        for (int y = 0; y < this.y; y++) {
            for (int z = 0; z < this.z; z++) {
                for (int x = 0; x < this.x; x++) {
                    int blockInd = (y * this.z + z) * this.x + x;

                    int blockID = blocks[blockInd] & 0xFF;
                    if (additional != null) {
                        // additional is 0 through 15 inclusive since it's & 0xF above
                        blockID |= additional[blockInd] << 8;
                    }
                    Block block = Block.blockRegistry.getObjectById(blockID);
                    int meta = metadata[blockInd] & 0xFF;
                    this.states[x][z][y] = block.getStateFromMeta(meta);
                }
            }
        }
    }
}
