package io.github.clicksilver.exporter;

import java.io.FileWriter;
import java.lang.String;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import io.github.legendff.mhw.save.Savecrypt;

public class App {

  // Decoration item IDs
  static final int kMinJewelId = 727;
  static final int kMaxJewelId = 2275;
  static final int kNumDecos = kMaxJewelId - kMinJewelId + 1;
  
  // 10 pages, 50 jewels per page
  static final int kDecoInventorySize = 50 * 10; 

  // 8 bytes per jewel, (4 for ID + 4 for count)
  static final int kNumBytesPerDeco = 8;

  // direct offsets into the decrypted save, where each decorations list starts
  static final int kSaveSlotDecosOffsets[] = new int[]{4302696, 6439464, 8576232};

  public static void main(String[] args) {
    if (args.length == 0) {
      JFrame frame = new JFrame();
      JOptionPane.showMessageDialog(frame, "No input save file detected.\n\n" +
                                    "Drag save file onto the executable.",
                                    "ERROR", JOptionPane.INFORMATION_MESSAGE);
      System.exit(0);
    }
    byte[] bytes;
    Path p = Paths.get(args[0]);
    try {
      byte[] save = Files.readAllBytes(p);
      byte[] decrypted_save = io.github.legendff.mhw.save.Savecrypt.decryptSave(save);

      for (int i=0; i<3; ++i) {
        // Get actual decoration counts from the decrypted save.
        int[] decorationCounts = getJewelCounts(decrypted_save, kSaveSlotDecosOffsets[i]);
        
        // If there are no decorations counted, skip this save file.
        if (decorationCounts == null) { break; }

        // Write out the Honeyhunter format.
        FileWriter honeyFile;
        try {
          honeyFile = new FileWriter("honeyhunter-" + String.valueOf(i + 1) + ".txt");
          if (decorationCounts != null) {
            honeyFile
                .write("WARNING: Unequip all decorations before using this " + "otherwise the count will be wrong.");
            honeyFile.write("\n");
            honeyFile.write("\n");
            honeyFile.write(outputHoneyHunter(decorationCounts));
            honeyFile.write("\n");
          }
          honeyFile.close();
        } catch(Exception e) {
          JFrame frame = new JFrame();
          JOptionPane.showMessageDialog(frame, "Failed to write honey hunter output");
        }

        // Write out the MHW Wiki DB format.
        FileWriter wikidbFile;
        try {
          wikidbFile = new FileWriter("mhw-wiki-db-" + String.valueOf(i + 1) + ".txt");
          if (decorationCounts != null) {
            wikidbFile
                .write("WARNING: Unequip all decorations before using this" + " otherwise the count will be wrong.");
            wikidbFile.write("\n");
            wikidbFile.write("\n");
            wikidbFile.write(outputWikiDB(decorationCounts));
            wikidbFile.write("\n");
          }
          wikidbFile.close();
        } catch (Exception e) {
          JFrame frame = new JFrame();
          JOptionPane.showMessageDialog(frame, "Failed to write mhw-wiki-db output");
        }
      }

      JFrame frame = new JFrame();
      JOptionPane.showMessageDialog(frame, "Successfully exported decorations",
          "COMPLETE", JOptionPane.INFORMATION_MESSAGE);
      System.exit(0);
    } catch(Exception e) {
      e.printStackTrace();
      System.out.println(e);
      JFrame frame = new JFrame();
      JOptionPane.showMessageDialog(frame, "Not a valid save file.", "ERROR",
          JOptionPane.INFORMATION_MESSAGE);
      System.exit(0);
    }
    return;
  }

  public static void printJewels(int[] counts) {
    for (int i=0; i<counts.length; ++i) {
      String name = DecorationNames.getDecorationName(i + kMinJewelId);
      int count = counts[i];
      if(name.length() != 0 && count != 0) {
        System.out.println(name + ": " + counts[i]);
      }
    }
  }
  
  public static String outputWikiDB(int[] counts) {
    int wikiDBcounts[] = new int[WikiDB.kNumDecos];
    Arrays.fill(wikiDBcounts, 0);

    for (int i=0; i<counts.length; ++i) {
      String name = DecorationNames.getDecorationName(i + kMinJewelId);
      if (name.length() == 0) {
        continue;
      }
      int order = WikiDB.getOrderingFromName(name);
      if (order < 0) {
        continue;
      }
      wikiDBcounts[order] = counts[i];
    }

    StringBuilder contents = new StringBuilder("");
    contents.append("{");
    for (int i=0; i<wikiDBcounts.length; ++i) {
      int count = Math.max(0, wikiDBcounts[i]);
      count = Math.min(count, 7);
      contents.append("\"");
      contents.append(WikiDB.kDecoNames[i]);
      contents.append("\":");
      contents.append(count);
      contents.append(",");
    }
    contents.append("}");
    return contents.toString();
  }

  public static String outputHoneyHunter(int[] counts) {
    int hhCounts[] = new int[HoneyHunter.kNumDecos];
    Arrays.fill(hhCounts, 0);

    for (int i=0; i<counts.length; ++i) {
      String name = DecorationNames.getDecorationName(i + kMinJewelId);
      if (name.length() == 0) {
        continue;
      }
      int count = Math.min(counts[i], HoneyHunter.getMaxCountFromName(name));
      int order = HoneyHunter.getOrderingFromName(name);
      if (order < 0 || count < 0) {
        continue;
      }
      hhCounts[order] = count;
    }

    StringBuilder contents = new StringBuilder("");
    contents.append(hhCounts[0]);
    for (int i=1; i<hhCounts.length; ++i) {
      contents.append(",");
      contents.append(hhCounts[i]);
    }
    return contents.toString();
  }

  public static int[] getJewelCounts(byte[] bytes, int offset) {
    int counts[] = new int[kNumDecos];

    ByteBuffer buf = ByteBuffer.wrap(bytes, offset, kDecoInventorySize * kNumBytesPerDeco);
    
    // NOTE: Java is dumb about bytes.
    buf.order(ByteOrder.LITTLE_ENDIAN);
    
    boolean anyNonZero = false;

    for (int i=0; i<kDecoInventorySize; i++) {
      int jewelId = buf.getInt();
      int jewelCount = buf.getInt();
      if(jewelId == 0) {
        // missing owned deco, which is not an invalid deco
        continue;
      }
      if (jewelId < kMinJewelId || jewelId > kMaxJewelId) {
        System.out.println("Error parsing decorations. Index=" + i +
            " ID=" + jewelId +
            " Count=" + jewelCount);
        return null;
      }

      if (jewelCount > 0) {
        anyNonZero = true;
      }

      counts[jewelId - kMinJewelId] = jewelCount;
    }

    if (anyNonZero) {
      return counts;
    }
    return null;
  }
}
