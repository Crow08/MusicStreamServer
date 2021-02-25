package de.crow08.musicstreamserver;

import org.jaudiotagger.audio.mp3.MP3AudioHeader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TrimmedAudioInputStream extends AudioInputStream {

  private final AudioInputStream stream;
  private final long startByte;
  private long prefixBytes = 0;
  private long t_bytesRead = 0;

  public TrimmedAudioInputStream(AudioInputStream audioInputStream, long startMilli) {
    super(new ByteArrayInputStream(new byte[0]), audioInputStream.getFormat(), AudioSystem.NOT_SPECIFIED);
    stream = audioInputStream;
    //calculate where to start and where to end
    startByte = (long) ((startMilli / 1000) * stream.getFormat().getFrameRate() * stream.getFormat().getFrameSize());
  }

  public TrimmedAudioInputStream(AudioInputStream audioInputStream, long startMilli, MP3AudioHeader mp3AudioHeader) {
    super(new ByteArrayInputStream(new byte[0]), audioInputStream.getFormat(), AudioSystem.NOT_SPECIFIED);
    stream = audioInputStream;
    //calculate where to start and where to end
    System.out.println("144 *" + mp3AudioHeader.getBitRateAsNumber() + "/" + mp3AudioHeader.getSampleRateAsNumber() + "+1");
    long calcFrameSize = (144 * (mp3AudioHeader.getBitRateAsNumber() * 1000) / mp3AudioHeader.getSampleRateAsNumber() + 1);
    prefixBytes = mp3AudioHeader.getMp3StartByte();
    startByte = (long) ((startMilli / 1000) * stream.getFormat().getFrameRate() * calcFrameSize);
  }

  @Override
  public int available() throws IOException {
    return (int) (super.available() - startByte);
  }

  public int read(byte[] abData, int nOffset, int nLength) throws IOException {
    int bytesRead = 0;
    if (t_bytesRead < prefixBytes) {
      bytesRead = stream.read(abData, 0, (int) (prefixBytes - t_bytesRead));
      t_bytesRead += bytesRead;
    }
    if (t_bytesRead >= prefixBytes && t_bytesRead < (startByte + prefixBytes)) {
      do {
        int bytesSkipped = (int) skip((startByte + prefixBytes) - t_bytesRead);
        t_bytesRead += bytesSkipped;
      } while (t_bytesRead < (startByte + prefixBytes));
    }

    if (nLength - bytesRead > 0) {
      bytesRead = stream.read(abData, 0, nLength - bytesRead);
      if (bytesRead == -1)
        return -1;
      else if (bytesRead == 0)
        return 0;

      t_bytesRead += bytesRead;
    }
    return bytesRead;
  }

  @Override
  public long skip(long n) throws IOException {
    // make sure not to skip fractional frames
    final long reminder = n % frameSize;
    if (reminder != 0) {
      n -= reminder;
    }
    if (n <= 0) {
      return 0;
    }

    if (frameLength != AudioSystem.NOT_SPECIFIED) {
      // don't skip more than our set length in frames.
      if ((n / frameSize) > (frameLength - framePos)) {
        n = (frameLength - framePos) * frameSize;
      }
    }
    long remaining = n;
    while (remaining > 0) {
      // Some input streams like FileInputStream can return more bytes,
      // when EOF is reached.
      long ret = Math.min(stream.skip(remaining), remaining);
      if (ret == 0) {
        // EOF or not? we need to check.
        if (stream.read() == -1) {
          break;
        }
        ret = 1;
      }
      remaining -= ret;
    }
    final long temp = n - remaining;

    // if no error, update our position.
    if (temp % frameSize != 0) {
      // Throw an IOException if we've skipped a fractional number of frames
      throw new IOException("Could not skip an integer number of frames.");
    }
    framePos += temp / frameSize;
    return temp;
  }
}
