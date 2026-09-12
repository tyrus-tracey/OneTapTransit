package com.example.onetaptransit.staticdata

import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.Source

/**
 * Wraps a delegate Source object, tracking the # of bytes read whenever the delegate Source
 * reads bytes from itself to a sink buffer.
 *
 * Note that it cannot just inherit from Source as that is a sealed class. So use .buffered() on
 * any instances of CountingSource() in order to convert from a RawSource into a Source
 */
class CountingSource(
    private val delegate: Source,
    private val onBytesRead: (bytesReadSoFar: Long) -> Unit
) : RawSource {
    private var totalRead : Long = 0L

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        val n = delegate.readAtMostTo(sink, byteCount)
        if (n != -1L) {
            totalRead += n
            onBytesRead(totalRead)
        }
        return n
    }

    override fun close() = delegate.close()
}