package org.aksw.commons.util.docker;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface CLib extends Library {
    CLib INSTANCE = Native.load("c", CLib.class);

    int pipe(int[] fds);      // fds[0] = read end, fds[1] = write end
    int close(int fd);
    int write(int fd, byte[] buf, int count);
    int read(int fd, byte[] buf, int count);
}
