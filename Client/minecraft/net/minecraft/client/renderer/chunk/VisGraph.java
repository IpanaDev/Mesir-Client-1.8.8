package net.minecraft.client.renderer.chunk;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import optifine.IntegerCache;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Set;

public class VisGraph {
    private static final int DX = (int) Math.pow(16.0D, 0.0D);
    private static final int DZ = (int) Math.pow(16.0D, 1.0D);
    private static final int DY = (int) Math.pow(16.0D, 2.0D);
    private final BitSet bitSet = new BitSet(4096);
    private static final int[] INDEX_OF_EDGES = new int[1352];
    private int empty = 4096;

    static {
        int i = 0;

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        INDEX_OF_EDGES[i++] = getIndex(j, k, l);
                    }
                }
            }
        }
    }

    public void setOpaqueCube(BlockPos pos) {
        this.bitSet.set(getIndex(pos), true);
        --this.empty;
    }

    private static int getIndex(BlockPos pos) {
        return getIndex(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }

    private static int getIndex(int x, int y, int z) {
        return x | y << 8 | z << 4;
    }

    public SetVisibility computeVisibility() {
        SetVisibility setvisibility = new SetVisibility();

        if (4096 - this.empty < 256) {
            setvisibility.setAllVisible(true);
        } else if (this.empty == 0) {
            setvisibility.setAllVisible(false);
        } else {
            for (int i : INDEX_OF_EDGES) {
                if (!this.bitSet.get(i)) {
                    setvisibility.setManyVisible(this.floodFill(i));
                }
            }
        }

        return setvisibility;
    }

    /*public Set func_178609_b(BlockPos pos)
    {
        return this.floodFill(getIndex(pos));
    }

     */

    private Set<EnumFacing> floodFill(int pos) {
        EnumSet<EnumFacing> enumset = EnumSet.noneOf(EnumFacing.class);
        ArrayDeque<Integer> arraydeque = new ArrayDeque<>(384);
        arraydeque.add(IntegerCache.valueOf(pos));
        this.bitSet.set(pos, true);

        while (!arraydeque.isEmpty()) {
            int i = arraydeque.poll();
            this.addEdges(i, enumset);

            for (EnumFacing enumfacing : EnumFacing.VALUES) {
                int j = this.func_178603_a(i, enumfacing);

                if (j >= 0 && !this.bitSet.get(j)) {
                    this.bitSet.set(j, true);
                    arraydeque.add(IntegerCache.valueOf(j));
                }
            }
        }

        return enumset;
    }

    private void addEdges(int pos, Set<EnumFacing> setFacings) {
        int i = pos & 15;

        if (i == 0) {
            setFacings.add(EnumFacing.WEST);
        } else if (i == 15) {
            setFacings.add(EnumFacing.EAST);
        }

        int j = pos >> 8 & 15;

        if (j == 0) {
            setFacings.add(EnumFacing.DOWN);
        } else if (j == 15) {
            setFacings.add(EnumFacing.UP);
        }

        int k = pos >> 4 & 15;

        if (k == 0) {
            setFacings.add(EnumFacing.NORTH);
        } else if (k == 15) {
            setFacings.add(EnumFacing.SOUTH);
        }
    }

    private int func_178603_a(int pos, EnumFacing facing) {
        switch (facing) {
            case DOWN -> {
                if ((pos >> 8 & 15) == 0) {
                    return -1;
                }
                return pos - DY;
            }
            case UP -> {
                if ((pos >> 8 & 15) == 15) {
                    return -1;
                }
                return pos + DY;
            }
            case NORTH -> {
                if ((pos >> 4 & 15) == 0) {
                    return -1;
                }
                return pos - DZ;
            }
            case SOUTH -> {
                if ((pos >> 4 & 15) == 15) {
                    return -1;
                }
                return pos + DZ;
            }
            case WEST -> {
                if ((pos & 15) == 0) {
                    return -1;
                }
                return pos - DX;
            }
            case EAST -> {
                if ((pos & 15) == 15) {
                    return -1;
                }
                return pos + DX;
            }
            default -> {
                return -1;
            }
        }
    }
}
