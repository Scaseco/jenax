package org.aksw.commons.allocation.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import org.aksw.commons.allocation.api.Allocatable;

public class AllocatableFolder
    implements Allocatable
{
    protected Path folder;

    @Override
    public void allocate() throws IOException {
        Files.createDirectories(folder);
    }

    @Override
    public boolean isAllocated() throws IOException {
        boolean result = Files.isDirectory(folder, LinkOption.NOFOLLOW_LINKS);
        return result;
    }

    @Override
    public void deallocate() throws IOException {
        Files.delete(folder);
    }
}
