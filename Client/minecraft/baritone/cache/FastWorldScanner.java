/*
 * This file is part of Baritone.
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone. If not, see <https://www.gnu.org/licenses/>.
 */
package baritone.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import baritone.api.cache.IWorldScanner;
import baritone.api.utils.BlockOptionalMetaLookup;
import baritone.api.utils.IPlayerContext;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public enum FastWorldScanner implements IWorldScanner {
	INSTANCE;

	@Override
	public List<BlockPos> scanChunkRadius(IPlayerContext ctx, BlockOptionalMetaLookup filter, int max, int yLevelThreshold, int maxSearchRadius) {
		if (maxSearchRadius < 0) { throw new IllegalArgumentException("chunkRange must be >= 0"); }
		return scanChunksInternal(ctx, filter, getChunkRange(ctx.playerFeet().x >> 4, ctx.playerFeet().z >> 4, maxSearchRadius), max);
	}

	@Override
	public List<BlockPos> scanChunk(IPlayerContext ctx, BlockOptionalMetaLookup filter, ChunkCoordIntPair pos, int max, int yLevelThreshold) {
		var stream = scanChunkInternal(ctx, filter, pos);
		if (max >= 0) {
			stream = stream.limit(max);
		}
		return stream.toList();
	}

	@Override
	public int repack(IPlayerContext ctx) {
		return this.repack(ctx, 40);
	}

	@Override
	public int repack(IPlayerContext ctx, int range) {
		var chunkProvider = ctx.world().getChunkProvider();
		var cachedWorld = ctx.worldData().getCachedWorld();
		var playerPos = ctx.playerFeet();
		var playerChunkX = playerPos.getX() >> 4;
		var playerChunkZ = playerPos.getZ() >> 4;
		var minX = playerChunkX - range;
		var minZ = playerChunkZ - range;
		var maxX = playerChunkX + range;
		var maxZ = playerChunkZ + range;
		var queued = 0;
		for (var x = minX; x <= maxX; x++) {
			for (var z = minZ; z <= maxZ; z++) {
				var chunk = chunkProvider.provideChunk(x, z);
				if (chunk != null && !chunk.isEmpty()) {
					queued++;
					cachedWorld.queueForPacking(chunk);
				}
			}
		}
		return queued;
	}

	// ordered in a way that the closest blocks are generally first
	public static List<ChunkCoordIntPair> getChunkRange(int centerX, int centerZ, int chunkRadius) {
		List<ChunkCoordIntPair> chunks = new ArrayList<>();
		// spiral out
		chunks.add(new ChunkCoordIntPair(centerX, centerZ));
		for (var i = 1; i < chunkRadius; i++) {
			for (var j = 0; j <= i; j++) {
				chunks.add(new ChunkCoordIntPair(centerX - j, centerZ - i));
				if (j != 0) {
					chunks.add(new ChunkCoordIntPair(centerX + j, centerZ - i));
					chunks.add(new ChunkCoordIntPair(centerX - j, centerZ + i));
				}
				chunks.add(new ChunkCoordIntPair(centerX + j, centerZ + i));
				if (j != i) {
					chunks.add(new ChunkCoordIntPair(centerX - i, centerZ - j));
					chunks.add(new ChunkCoordIntPair(centerX + i, centerZ - j));
					if (j != 0) {
						chunks.add(new ChunkCoordIntPair(centerX - i, centerZ + j));
						chunks.add(new ChunkCoordIntPair(centerX + i, centerZ + j));
					}
				}
			}
		}
		return chunks;
	}

	private List<BlockPos> scanChunksInternal(IPlayerContext ctx, BlockOptionalMetaLookup lookup, List<ChunkCoordIntPair> chunkPositions, int maxBlocks) {
		assert ctx.world() != null;
		try {
			// p -> scanChunkInternal(ctx, lookup, p)
			Stream<BlockPos> posStream = chunkPositions.parallelStream().flatMap(p -> scanChunkInternal(ctx, lookup, p));
			if (maxBlocks >= 0) {
				// WARNING: this can be expensive if maxBlocks is large...
				// see limit's javadoc
				posStream = posStream.limit(maxBlocks);
			}
			return posStream.toList();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private Stream<BlockPos> scanChunkInternal(IPlayerContext ctx, BlockOptionalMetaLookup lookup, ChunkCoordIntPair pos) {
		var chunkProvider = ctx.world().getChunkProvider();
		// if chunk is not loaded, return empty stream
		if (!chunkProvider.chunkExists(pos.chunkXPos, pos.chunkZPos)) { return Stream.empty(); }
		var chunkX = (long) pos.chunkXPos << 4;
		var chunkZ = (long) pos.chunkZPos << 4;
		var playerSectionY = ctx.playerFeet().y >> 4;
		return collectChunkSections(lookup, chunkProvider.provideChunk(pos.chunkXPos, pos.chunkZPos), chunkX, chunkZ, playerSectionY).stream();
	}

	private List<BlockPos> collectChunkSections(BlockOptionalMetaLookup lookup, Chunk chunk, long chunkX, long chunkZ, int playerSection) {
		// iterate over sections relative to player
		List<BlockPos> blocks = new ArrayList<>();
		var sections = chunk.getBlockStorageArray();
		var l = sections.length;
		var i = playerSection - 1;
		var j = playerSection;
		for (; i >= 0 || j < l; ++j, --i) {
			if (j < l) {
				visitSection(lookup, sections[j], blocks, chunkX, chunkZ);
			}
			if (i >= 0) {
				visitSection(lookup, sections[i], blocks, chunkX, chunkZ);
			}
		}
		return blocks;
	}

	private void visitSection(BlockOptionalMetaLookup lookup, ExtendedBlockStorage section, List<BlockPos> blocks, long chunkX, long chunkZ) {
		if (section == null || section.isEmpty()) { return; }
		var sectionContainer = section.getDataArray();
		// this won't work if the PaletteStorage is of the type EmptyPaletteStorage
		if (sectionContainer == null) { return; }
		// boolean[] isInFilter = getIncludedFilterIndices(lookup, ((IBlockStateContainer) sectionContainer).getPalette());
		// if (isInFilter.length == 0) { return; }
		var arraySize = sectionContainer.length;
		var bitsPerEntry = 4;
		var yOffset = section.getYLocation();
		for (int idx = 0, kl = bitsPerEntry - 1; idx < arraySize; idx++, kl += bitsPerEntry) {
			var pos = new BlockPos(
						chunkX + (idx & 255 & 15),
						yOffset + (idx >> 8),
						chunkZ + ((idx & 255) >> 4));
			if (lookup.has(Minecraft.getMinecraft().theWorld.getBlockState(pos))) {
				blocks.add(pos);
			}
		}
	}

	private boolean[] getIncludedFilterIndices(BlockOptionalMetaLookup lookup, IBlockState palette) {
		var commonBlockFound = false;
		var paletteMap = getPalette(palette);
		var size = paletteMap.size();
		var isInFilter = new boolean[size];
		for (var i = 0; i < size; i++) {
			var state = paletteMap.getByValue(i);
			if (lookup.has(state)) {
				isInFilter[i] = true;
				commonBlockFound = true;
			} else {
				isInFilter[i] = false;
			}
		}
		if (!commonBlockFound)
			return new boolean[0];
		return isInFilter;
	}

	/**
	 * cheats to get the actual map of id -> blockstate from the various palette implementations
	 */
	private static ObjectIntIdentityMap<IBlockState> getPalette(IBlockState palette) {
		var buf = new PacketBuffer(Unpooled.buffer());
		buf.writeVarIntToBuffer(Block.BLOCK_STATE_IDS.get(palette));
		var size = buf.readVarIntFromBuffer();
		var states = new ObjectIntIdentityMap<IBlockState>();
		for (var i = 0; i < size; i++) {
			var state = Block.BLOCK_STATE_IDS.getByValue(buf.readVarIntFromBuffer());
			states.put(state, i);
		}
		return states;
	}
}