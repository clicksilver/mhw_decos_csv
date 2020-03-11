package exporter;

import java.lang.String;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.Arrays;

public class App {

  public static void main(String[] args)
  {
    // save data layout info
    int decoBoxOffset = 0x119690;
    int slotSize = 0x2098C0;
    int[] slotOffsets = new int[]{ 0x3010D8, 0x50AB98, 0x714658 };

    // parse the save file
    byte[] bytes;
    Path p = Paths.get(args[0]);
    try {
      bytes = Files.readAllBytes(p);
    } catch(Exception e) {
      System.out.println("Failed to read bytes.");
      return;
    }
    System.out.println("Save file: " + bytes.length + " bytes");

    int counts[] = getJewelCounts(bytes);
    return;
  }

  public static int[] getJewelCounts(byte[] bytes) {
    final int kMinJewelId = 727;
    final int kMaxJewelId = 2272;
    final int kDecoListSz = 50*10*8; // 10 page, 50 jewels, 4 bytes for ID + 4 bytes for count
    
    // output list of valid jewel counts extracted
    int counts[] = new int[kMaxJewelId - kMinJewelId + 1];

    int offset = 0;
    while (offset < (bytes.length-kDecoListSz)) {
      // searching the save data bytes for a valid jewel inventory 
      ByteBuffer buf = ByteBuffer.wrap(bytes, offset, kDecoListSz);

      boolean isValid = true;
      for (int i=0; i<kDecoListSz; i+=8) {
        int itemId = buf.getInt(i);
        
        if(itemId == 0) {
          // potentially just an empty entry in the deco list, so it doesn't mean this is an invalid set of bytes
          continue;
        }

        if (itemId < kMinJewelId || itemId > kMaxJewelId) {
          // not a valid jewel entry
          isValid = false;
          break;
        }

        int jewelCount = buf.getInt(i+4);
        if (jewelCount < 0) {
          // not a valid inventory entry
          isValid = false;
          break;
        }

        counts[itemId - kMinJewelId] = jewelCount;
      }

      if (isValid) {
        System.out.println("Found a valid inventory at offset = " + offset);
        return counts;
      } else {
        // test the next potential slice of bytes for a valid jewel counts
        Arrays.fill(counts, 0, counts.length, 0);
        offset += 1;
      }
    }
    return counts;
  }
}
