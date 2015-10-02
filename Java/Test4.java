/* Nikita Borisov
 * CSS430 - Operating Systems
 * 05/27/2015
 * Project04: Cache - second chance algorithm
 */

import java.util.Date;
import java.util.Random;

public class Test4 extends Thread {

	// class attributes:
	private static final int BUFFER_SIZE = 512;
	private static final int ITERATION = 200;
	private int testNum;
	private Random random;
	private boolean enbaledCache;
	private long start;
	private long end;
	private boolean toStop = false;

	// constructor
	public Test4(String[] arg) {

		// check second argument
		if (arg.length != 2) {
			SysLib.cerr("Not enought argumennts!\n");
			SysLib.exit();
		}

		// check fist argument
		if (arg[0].equals("enable")) {
			enbaledCache = true;
		} else if (arg[0].equals("disable")) {
			enbaledCache = false;
		}  else {
			SysLib.cerr("Invalid Input!\n");
			toStop = true;
		}
		testNum = Integer.parseInt(arg[1]);
		random = new Random();
	}

	// thread
	public void run() {

		if (!toStop) {

			switch (testNum) {
			case 1:
				start = new Date().getTime();
				randomAccessTest();
				end = new Date().getTime() - start;
				SysLib.cerr("Execution Time: " + end + "\n\n");
				break;
			case 2:
				start = new Date().getTime();
				localizedAcessTest();
				end = new Date().getTime() - start;
				SysLib.cerr("Execution Time: " + end + "\n\n");
				break;
			case 3:
				start = new Date().getTime();
				mixedAccessTest();
				end = new Date().getTime() - start;
				SysLib.cerr("Execution Time: " + end + "\n\n");
				break;
			case 4:
				start = new Date().getTime();
				adversaryAccessTest();
				end = new Date().getTime() - start;
				SysLib.cerr("Execution Time: " + end + "\n\n");
				break;
			case 5:
				runAll();
				break;
			default:
				SysLib.cerr("Invalid test case!\n\n");

			}
			SysLib.exit();
		} else {
			SysLib.cerr("Test4 has been stopped!\n\n");
			SysLib.exit();
		}
	}

	private void runAll() {

		// run random
		start = new Date().getTime();
		randomAccessTest();
		end = new Date().getTime() - start;
		SysLib.cerr("Execution Time: " + end + "\n\n");

		// run localized
		start = new Date().getTime();
		localizedAcessTest();
		end = new Date().getTime() - start;
		SysLib.cerr("Execution Time: " + end + "\n\n");

		// run mixed
		start = new Date().getTime();
		mixedAccessTest();
		end = new Date().getTime() - start;
		SysLib.cerr("Execution Time: " + end + "\n\n");

		// run adversary
		start = new Date().getTime();
		adversaryAccessTest();
		end = new Date().getTime() - start;
		SysLib.cerr("Execution Time: " + end + "\n\n");

	}

	// randomAcessTest
	private void randomAccessTest() {

		SysLib.cerr("Rendom Access Test is running!\n");

		// allocate array of disk blocks' indexes
		int[] arrayDisk = new int[ITERATION];
		for (int i = 0; i < ITERATION; i++) {
			arrayDisk[i] = Math.abs(random.nextInt() % 512);
		}

		// allocate buffers for read and write
		byte[] read_buffer = new byte[BUFFER_SIZE];
		byte[] write_buffer = new byte[BUFFER_SIZE];

		// do random write
		for (int i = 0; i < ITERATION; i++) {
			for (int j = 0; j < BUFFER_SIZE; j++) {
				write_buffer[j] = (byte) j;
			}
			write(arrayDisk[i], write_buffer);
		}

		// do read
		for (int i = 0; i < ITERATION; i++) {
			read(arrayDisk[i], read_buffer);
			for (int j = 0; j < BUFFER_SIZE; j++) {
				if (read_buffer[j] != write_buffer[j]) {
					SysLib.cerr("Error in Random access (Read vs Write)!\n");
				}
			}
		}
	}

	// localized Access
	private void localizedAcessTest() {

		SysLib.cerr("Localized Access Test is running!\n");

		// allocate buffers for read and write
		byte[] read_buffer = new byte[BUFFER_SIZE];
		byte[] write_buffer = new byte[BUFFER_SIZE];

		// allocate array of disk blocks' indexes
		int[] arrayDisk = new int[ITERATION];
		for (int i = 0; i < ITERATION; i++) {
			arrayDisk[i] = Math.abs(random.nextInt() % 512);
		}

		// write and read at localized access
		for (int i = 0; i < ITERATION; i++) {
			// build write buffer
			for (int j = 0; j < BUFFER_SIZE; j++) {
				write_buffer[j] = (byte) j;
			}

			// write --> read
			write(arrayDisk[i], write_buffer);
			read(arrayDisk[i], read_buffer);

			// check if data match
			for (int j = 0; j < BUFFER_SIZE; j++) {
				if (read_buffer[j] != write_buffer[j]) {
					SysLib.cerr("Error in Localized Access (Read vs Write)!\n");
				}
			}
		}

	}

	// mixed Access
	private void mixedAccessTest() {

		SysLib.cerr("*****************************\n");
		SysLib.cerr("Mixed Access Test is running!\n");

		for (int i = 0; i < 10; ++i) {
			int number = Math.abs(random.nextInt() % 10);
			// go to random test
			if (number > 8) {
				randomAccessTest();

			} // otherwise, local test
			else {
				localizedAcessTest();
			}
		}

		SysLib.cerr("Mixed Access Test is done!\n");
		SysLib.cerr("*****************************\n");
	}

	// adversary Access
	private void adversaryAccessTest() {

		SysLib.cerr("Adversary Access Test is running!\n");

		// allocate buffers for read and write
		byte[] read_buffer = new byte[BUFFER_SIZE];
		byte[] write_buffer = new byte[BUFFER_SIZE];

		// write to buffer
		for (int i = 0; i < 20; i++) {
			// build write buffer
			for (int j = 0; j < BUFFER_SIZE; j++) {
				write_buffer[j] = (byte) j;
			}
			for (int j = 0; j < 10; j++) {
				write(i * 10 + j, write_buffer);
			}
		}

		// read from the buffer
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 10; j++) {
				read(i * 10 + j, read_buffer);
				// check if data match
				for (int x = 0; x < BUFFER_SIZE; x++) {
					if (read_buffer[x] != write_buffer[x]) {
						SysLib.cerr("Error in Localized Access (Read vs Write)!\n");
					}
				}
			}
		}
	}

	// write to cache of to the disk
	private void write(int blockID, byte[] buffer) {
		if (enbaledCache) {
			SysLib.cwrite(blockID, buffer);
		} else {
			SysLib.rawwrite(blockID, buffer);
		}
	}

	// read from disk or cache
	private void read(int blockID, byte[] buffer) {
		if (enbaledCache) {
			SysLib.cread(blockID, buffer);
		} else {
			SysLib.rawread(blockID, buffer);
		}
	}

}
