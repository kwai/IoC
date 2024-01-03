package com.kuaishou.commercial.utility.plugin

import com.android.SdkConstants
import java.io.EOFException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

object Utils {
  private const val DEFAULT_BUFFER_SIZE = 8192

  /**
   * The maximum size of array to allocate.
   * Some VMs reserve some header words in an array.
   * Attempts to allocate larger arrays may result in
   * OutOfMemoryError: Requested array size exceeds VM limit
   */
  private const val MAX_BUFFER_SIZE = Int.MAX_VALUE - 8

  /**
   * Read exactly `length` of bytes from `in`.
   *
   *
   *  Note that this method is safe to be called with unknown large
   * `length` argument. The memory used is proportional to the
   * actual bytes available. An exception is thrown if there are not
   * enough bytes in the stream.
   *
   * @param is input stream, must not be null
   * @param length number of bytes to read
   * @return bytes read
   * @throws EOFException if there are not enough bytes in the stream
   * @throws IOException if an I/O error occurs or `length` is negative
   * @throws OutOfMemoryError if an array of the required size cannot be
   * allocated.
   */
  @Throws(IOException::class)
  fun readExactlyNBytes(`is`: InputStream, length: Int): ByteArray {
    if (length < 0) {
      throw IOException("length cannot be negative: $length")
    }
    val data = readNBytes(`is`, length)
    if (data.size < length) {
      throw EOFException()
    }
    return data
  }

  /**
   * Reads all remaining bytes from the input stream. This method blocks until
   * all remaining bytes have been read and end of stream is detected, or an
   * exception is thrown. This method does not close the input stream.
   *
   *
   *  When this stream reaches end of stream, further invocations of this
   * method will return an empty byte array.
   *
   *
   *  Note that this method is intended for simple cases where it is
   * convenient to read all bytes into a byte array. It is not intended for
   * reading input streams with large amounts of data.
   *
   *
   *  The behavior for the case where the input stream is *asynchronously
   * closed*, or the thread interrupted during the read, is highly input
   * stream specific, and therefore not specified.
   *
   *
   *  If an I/O error occurs reading from the input stream, then it may do
   * so after some, but not all, bytes have been read. Consequently the input
   * stream may not be at end of stream and may be in an inconsistent state.
   * It is strongly recommended that the stream be promptly closed if an I/O
   * error occurs.
   *
   * @implSpec
   * This method invokes [.readNBytes] with a length of
   * [Integer.MAX_VALUE].
   *
   * @param is input stream, must not be null
   * @return a byte array containing the bytes read from this input stream
   * @throws IOException if an I/O error occurs
   * @throws OutOfMemoryError if an array of the required size cannot be
   * allocated.
   *
   * @since 1.9
   */
  @Throws(IOException::class)
  fun readAllBytes(`is`: InputStream): ByteArray {
    return readNBytes(`is`, Int.MAX_VALUE)
  }

  /**
   * Reads up to a specified number of bytes from the input stream. This
   * method blocks until the requested number of bytes have been read, end
   * of stream is detected, or an exception is thrown. This method does not
   * close the input stream.
   *
   *
   *  The length of the returned array equals the number of bytes read
   * from the stream. If `len` is zero, then no bytes are read and
   * an empty byte array is returned. Otherwise, up to `len` bytes
   * are read from the stream. Fewer than `len` bytes may be read if
   * end of stream is encountered.
   *
   *
   *  When this stream reaches end of stream, further invocations of this
   * method will return an empty byte array.
   *
   *
   *  Note that this method is intended for simple cases where it is
   * convenient to read the specified number of bytes into a byte array. The
   * total amount of memory allocated by this method is proportional to the
   * number of bytes read from the stream which is bounded by `len`.
   * Therefore, the method may be safely called with very large values of
   * `len` provided sufficient memory is available.
   *
   *
   *  The behavior for the case where the input stream is *asynchronously
   * closed*, or the thread interrupted during the read, is highly input
   * stream specific, and therefore not specified.
   *
   *
   *  If an I/O error occurs reading from the input stream, then it may do
   * so after some, but not all, bytes have been read. Consequently the input
   * stream may not be at end of stream and may be in an inconsistent state.
   * It is strongly recommended that the stream be promptly closed if an I/O
   * error occurs.
   *
   * @implNote
   * The number of bytes allocated to read data from this stream and return
   * the result is bounded by `2*(long)len`, inclusive.
   *
   * @param is input stream, must not be null
   * @param len the maximum number of bytes to read
   * @return a byte array containing the bytes read from this input stream
   * @throws IllegalArgumentException if `length` is negative
   * @throws IOException if an I/O error occurs
   * @throws OutOfMemoryError if an array of the required size cannot be
   * allocated.
   *
   * @since 11
   */
  @Throws(IOException::class)
  fun readNBytes(`is`: InputStream, len: Int): ByteArray {
    require(len >= 0) { "len < 0" }
    var bufs: MutableList<ByteArray>? = null
    var result: ByteArray? = null
    var total = 0
    var remaining = len
    var n: Int
    do {
      val buf = ByteArray(Math.min(remaining, DEFAULT_BUFFER_SIZE))
      var nread = 0

      // read to EOF which may read more or less than buffer size
      while (`is`.read(
          buf, nread,
          Math.min(buf.size - nread, remaining)
        ).also { n = it } > 0
      ) {
        nread += n
        remaining -= n
      }
      if (nread > 0) {
        if (MAX_BUFFER_SIZE - total < nread) {
          throw OutOfMemoryError("Required array size too large")
        }
        total += nread
        if (result == null) {
          result = buf
        } else {
          if (bufs == null) {
            bufs = ArrayList()
            bufs.add(result)
          }
          bufs.add(buf)
        }
      }
      // if the last call to read returned -1 or the number of bytes
      // requested have been read then break
    } while (n >= 0 && remaining > 0)
    if (bufs == null) {
      if (result == null) {
        return ByteArray(0)
      }
      return if (result.size == total) result else Arrays.copyOf(result, total)
    }
    result = ByteArray(total)
    var offset = 0
    remaining = total
    for (b in bufs) {
      val count = Math.min(b.size, remaining)
      System.arraycopy(b, 0, result, offset, count)
      offset += count
      remaining -= count
    }
    return result
  }

  /**
   * Reads the requested number of bytes from the input stream into the given
   * byte array. This method blocks until `len` bytes of input data have
   * been read, end of stream is detected, or an exception is thrown. The
   * number of bytes actually read, possibly zero, is returned. This method
   * does not close the input stream.
   *
   *
   *  In the case where end of stream is reached before `len` bytes
   * have been read, then the actual number of bytes read will be returned.
   * When this stream reaches end of stream, further invocations of this
   * method will return zero.
   *
   *
   *  If `len` is zero, then no bytes are read and `0` is
   * returned; otherwise, there is an attempt to read up to `len` bytes.
   *
   *
   *  The first byte read is stored into element `b[off]`, the next
   * one in to `b[off+1]`, and so on. The number of bytes read is, at
   * most, equal to `len`. Let *k* be the number of bytes actually
   * read; these bytes will be stored in elements `b[off]` through
   * `b[off+`*k*`-1]`, leaving elements `b[off+`*k*
   * `]` through `b[off+len-1]` unaffected.
   *
   *
   *  The behavior for the case where the input stream is *asynchronously
   * closed*, or the thread interrupted during the read, is highly input
   * stream specific, and therefore not specified.
   *
   *
   *  If an I/O error occurs reading from the input stream, then it may do
   * so after some, but not all, bytes of `b` have been updated with
   * data from the input stream. Consequently the input stream and `b`
   * may be in an inconsistent state. It is strongly recommended that the
   * stream be promptly closed if an I/O error occurs.
   *
   * @param  is input stream, must not be null
   * @param  b the byte array into which the data is read
   * @param  off the start offset in `b` at which the data is written
   * @param  len the maximum number of bytes to read
   * @return the actual number of bytes read into the buffer
   * @throws IOException if an I/O error occurs
   * @throws NullPointerException if `b` is `null`
   * @throws IndexOutOfBoundsException If `off` is negative, `len`
   * is negative, or `len` is greater than `b.length - off`
   *
   * @since 1.9
   */
  @Throws(IOException::class)
  fun readNBytes(`is`: InputStream, b: ByteArray, off: Int, len: Int): Int {
    Objects.requireNonNull(b)
    if (off < 0 || len < 0 || len > b.size - off) throw IndexOutOfBoundsException()
    var n = 0
    while (n < len) {
      val count = `is`.read(b, off + n, len - n)
      if (count < 0) break
      n += count
    }
    return n
  }

  /**
   * Compatibility wrapper for third party users of
   * `sun.misc.IOUtils.readFully` following its
   * removal in JDK-8231139.
   *
   * Read up to `length` of bytes from `in`
   * until EOF is detected.
   *
   * @param is input stream, must not be null
   * @param length number of bytes to read
   * @param readAll if true, an EOFException will be thrown if not enough
   * bytes are read.
   * @return bytes read
   * @throws EOFException if there are not enough bytes in the stream
   * @throws IOException if an I/O error occurs or `length` is negative
   * @throws OutOfMemoryError if an array of the required size cannot be
   * allocated.
   */
  @Throws(IOException::class)
  fun readFully(`is`: InputStream, length: Int, readAll: Boolean): ByteArray {
    if (length < 0) {
      throw IOException("length cannot be negative: $length")
    }
    return if (readAll) {
      readExactlyNBytes(`is`, length)
    } else {
      readNBytes(`is`, length)
    }
  }


  fun dealJarFile(
    jarFile: File,
    callback: (jarEntry: JarEntry, jos: JarOutputStream, bakFile: JarFile) -> Boolean
  ) {
    val jarAbsolutePath = jarFile.absolutePath
    val bakFilePath = jarAbsolutePath.substring(
      0,
      jarAbsolutePath.length - 4
    ) + System.currentTimeMillis() + SdkConstants.DOT_JAR
    val bakFile = File(bakFilePath)
    jarFile.renameTo(bakFile)
    val bakJarFile = JarFile(bakFilePath)
    val jos = JarOutputStream(FileOutputStream(jarFile))
    for (jarEntry in bakJarFile.entries()) {
      if (!callback(jarEntry, jos, bakJarFile)) {
        try {
          jos.putNextEntry(ZipEntry(jarEntry))
          val inputStream = bakJarFile.getInputStream(jarEntry)
          jos.write(Utils.readNBytes(inputStream, inputStream.available()))
          inputStream.close()
        } catch (e: Throwable) {
          throw e
        }
      }
    }
    with(jos) {
      flush()
      finish()
      close()
    }
    bakJarFile.close()
    bakFile.delete()
  }
}