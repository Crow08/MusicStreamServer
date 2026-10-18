package de.crow08.musicstreamserver.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the chapters of an MP4 file from the Nero chapter box ({@code moov/udta/chpl}), which ffmpeg writes when
 * chapters are copied into an MP4 (convert-video.ps1 uses {@code -map_chapters 0}). Browsers do not expose MP4
 * chapters, so the player gets them from the server.
 */
public final class Mp4ChapterReader {

  /** The chpl box is small; anything larger is not a chapter list written by ffmpeg. */
  private static final long MAX_CHPL_SIZE = 1 << 20;
  /** Start times in the chpl box are in units of 100 ns. */
  private static final double CHPL_TIME_SCALE = 10_000_000d;

  public record Chapter(double start, String title) {
  }

  private Mp4ChapterReader() {
  }

  /** Returns the chapters ordered by start time, or an empty list if the file has none. */
  public static List<Chapter> read(Path file) throws IOException {
    try (RandomAccessFile in = new RandomAccessFile(file.toFile(), "r")) {
      long[] moov = findBox(in, 0, in.length(), "moov");
      long[] udta = moov == null ? null : findBox(in, moov[0], moov[1], "udta");
      long[] chpl = udta == null ? null : findBox(in, udta[0], udta[1], "chpl");
      if (chpl == null || chpl[1] - chpl[0] > MAX_CHPL_SIZE) {
        return List.of();
      }
      byte[] content = new byte[(int) (chpl[1] - chpl[0])];
      in.seek(chpl[0]);
      in.readFully(content);
      return parseChpl(content);
    }
  }

  /**
   * Searches the boxes between start and end (the content of the parent box) for the first box of the given type.
   *
   * @return start and end offset of the content of the box, or null if there is none
   */
  private static long[] findBox(RandomAccessFile in, long start, long end, String type) throws IOException {
    long position = start;
    while (position + 8 <= end) {
      in.seek(position);
      long size = Integer.toUnsignedLong(in.readInt());
      byte[] typeBytes = new byte[4];
      in.readFully(typeBytes);
      long headerSize = 8;
      if (size == 1) {
        size = in.readLong();
        headerSize = 16;
      } else if (size == 0) {
        size = end - position;
      }
      if (size < headerSize || position + size > end) {
        return null;
      }
      if (type.equals(new String(typeBytes, StandardCharsets.ISO_8859_1))) {
        return new long[] {position + headerSize, position + size};
      }
      position += size;
    }
    return null;
  }

  /** Parses the content of a chpl box, in the layout ffmpeg reads and writes it (libavformat/mov.c). */
  private static List<Chapter> parseChpl(byte[] content) {
    List<Chapter> chapters = new ArrayList<>();
    int version = content.length > 0 ? content[0] : 0;
    int position = 4 + (version != 0 ? 4 : 0);
    if (position >= content.length) {
      return chapters;
    }
    int count = content[position++] & 0xff;
    for (int i = 0; i < count && position + 9 <= content.length; i++) {
      long start = 0;
      for (int b = 0; b < 8; b++) {
        start = (start << 8) | (content[position++] & 0xff);
      }
      int titleLength = content[position++] & 0xff;
      if (position + titleLength > content.length) {
        break;
      }
      String title = new String(content, position, titleLength, StandardCharsets.UTF_8);
      position += titleLength;
      chapters.add(new Chapter(start / CHPL_TIME_SCALE, title));
    }
    chapters.sort((a, b) -> Double.compare(a.start(), b.start()));
    return chapters;
  }
}
