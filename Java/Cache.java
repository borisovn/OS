/* Nikita Borisov
 * CSS430 - Operating Systems
 * 05/27/2015
 * Project04: Cache - second chance algorithm
 */

import java.util.ArrayList;
import java.util.List;

public class Cache {

	// ----------------------------------------------------------------------
	// BlockData node:
	// Represent cache block
	private class BlockEntry {

		// Block attributes:
		public boolean dirty_bit;
		public boolean reference_bit;
		public int frameID;

		public BlockEntry() {
			dirty_bit = false;
			reference_bit = false;
			frameID = -1;
		}
	}
	// ----------------------------------------------------------------------

	// class attributes:
	private final List<byte[]> pages; // list of pages;
	private int victim; // hold number of potential victim
	private final BlockEntry[] table; // table of pages
	private final int INVALID_ID = -1;

	// Constructor
	public Cache(int blockSize, int cacheBlocks) {

		victim = 0;
		pages = new ArrayList<>();
		table = new BlockEntry[cacheBlocks];

		for (int i = 0; i < table.length; i++) {
			table[i] = new BlockEntry();
			pages.add(i, new byte[blockSize]);
		}
	}

	// reads into the buffer[ ] array the cache block specified
	// by blockId from the disk cache if it is in cache, otherwise
	// reads the corresponding disk block from the disk device.
	// Upon an error, it should return false, otherwise return true.
	public synchronized boolean read(int blockId, byte buffer[]) {

		// check for valid block ID
		if (blockId < 0) {
			SysLib.cerr("Invalid blockID!\n");
			return false;
		}

		// read from the cache
		for (int i = 0; i < table.length; i++) {
			// if block is cached, read from it
			if (blockId == table[i].frameID) {
				// copy data to the buffer
				byte[] read_buffer = pages.get(i);
				System.arraycopy(read_buffer, 0, buffer, 0, buffer.length);
				// second change is active on this block
				table[i].reference_bit = true;
				return true;
			}
		}

		// cache doesn't have given block
		// find the potential victim page
		int newVictim = findOpenPage();
		writeBack(newVictim); 		            		// write back to the disk
		SysLib.rawread(blockId, buffer); 				// read from the disk
	    toCached(newVictim,buffer, blockId, false); 	// cached the block

		return true;
	}

	// writes the buffer[ ]array contents to the cache block
	// specified by blockId from the disk cache if it is in cache,
	// otherwise finds a free cache block and writes the buffer [ ]
	// contents on it. No write through. Upon an error, it should
	// return false, otherwise return true.
	public synchronized boolean write(int blockId, byte buffer[]) {

		// check for valid block ID
		if (blockId < 0) {
			SysLib.cerr("Invalid blockID!\n");
			return false;
		}

		// read from the cache
		for (int i = 0; i < table.length; i++) {
			// re-write to the buffer
			if (blockId == table[i].frameID) {
				byte[] copy_buff = new byte[buffer.length];
				System.arraycopy(buffer, 0, copy_buff, 0, buffer.length);
				pages.set(i, copy_buff);
				table[i].reference_bit = true;
				table[i].dirty_bit = true;
				return true;
			}
		}

		// cache doesn't have given block
		// find the potential victim page
		int newVictim = findOpenPage();
		writeBack(newVictim);						// write back to the disk
		toCached(newVictim,buffer, blockId, true); 	// cached the block

		return true;
	}

	// caches the block
	private void toCached(int target, byte[] buffer, int blockId, boolean isWrite) {

		byte[] new_data = new byte[buffer.length];
		System.arraycopy(buffer, 0, new_data, 0, buffer.length);
		pages.set(target, new_data);
		table[target].frameID = blockId;
		table[target].reference_bit = true;

		if(isWrite) {
			table[target].dirty_bit = true;
		}

	}

	// maintains clean block copies in Cache
	public synchronized void sync() {

		for (int i = 0; i < table.length; i++) {
			writeBack(i);
		}
		SysLib.sync();
	}

	// invalidates all cached blocks
	public synchronized void flush() {
		for (int i = 0; i < table.length; i++) {
			writeBack(i);
			table[i].reference_bit = false;
			table[i].frameID = INVALID_ID;
		}
		SysLib.sync();
	}

	// find potential victim page by traversing
	// table and its free to cache blocks
	private int findOpenPage() {

		for (int i = 0; i < table.length; i++) {
			if (INVALID_ID == table[i].frameID) {
				return i;
			}
		}
		// no open page, find victim
		// start second chance
		while (true) {
			victim = (victim + 1) % table.length;
			if (table[victim].reference_bit == false)
				return victim;
			table[victim].reference_bit = false;
		}
	}

	// If it is a victim and its dirty bit is 1, then, write back this
	// contents to the disk using disk.write(). The dirty bit is reset.
	private boolean writeBack(int target) {

		if (table[target].dirty_bit) {
			SysLib.rawwrite(table[target].frameID, pages.get(target));
			table[target].dirty_bit = false;
			table[target].frameID = INVALID_ID;
			pages.set(target, null);
			return true;
		}
		return false;
	}
}
