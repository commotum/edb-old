package datomic.impl;

import java.io.IOException;
import java.io.OutputStream;
import org.fressian.impl.BytesOutputStream;
import org.slf4j.Logger;

/**
 * Converts newline-delimited byte output into debug log records. Bytes are
 * buffered until a line feed arrives, then the completed line is emitted and
 * the buffer is reused.
 */
public class LoggedOutputStream
extends OutputStream {
    BytesOutputStream bos = new BytesOutputStream();
    Logger logger;

    public LoggedOutputStream(Logger l) {
        this.logger = l;
    }

    @Override
    public void write(int i) throws IOException {
        if (i == 10) {
            this.logger.debug(new String(this.bos.internalBuffer()));
            this.bos.reset();
        } else {
            this.bos.write(i);
        }
    }
}
