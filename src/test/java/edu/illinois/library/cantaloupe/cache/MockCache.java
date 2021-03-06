package edu.illinois.library.cantaloupe.cache;

import edu.illinois.library.cantaloupe.image.Identifier;
import edu.illinois.library.cantaloupe.image.Info;
import edu.illinois.library.cantaloupe.operation.OperationList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Optional;

class MockCache implements DerivativeCache, SourceCache {

    private boolean initializeCalled = false;
    private boolean shutdownCalled = false;

    @Override
    public Optional<Info> getInfo(Identifier identifier) {
        return Optional.empty();
    }

    @Override
    public Optional<Path> getSourceImageFile(Identifier identifier)
            throws IOException {
        return Optional.empty();
    }

    @Override
    public void initialize() {
        initializeCalled = true;
    }

    boolean isInitializeCalled() {
        return initializeCalled;
    }

    boolean isShutdownCalled() {
        return shutdownCalled;
    }

    @Override
    public InputStream newDerivativeImageInputStream(OperationList opList)
            throws IOException {
        return null;
    }

    @Override
    public OutputStream newDerivativeImageOutputStream(OperationList opList)
            throws IOException {
        return null;
    }

    @Override
    public OutputStream newSourceImageOutputStream(Identifier identifier)
            throws IOException {
        return OutputStream.nullOutputStream();
    }

    @Override
    public void purge() {}

    @Override
    public void purge(Identifier identifier) {}

    @Override
    public void purge(OperationList opList) {}

    @Override
    public void purgeInvalid() {}

    @Override
    public void put(Identifier identifier, Info imageInfo) {}

    @Override
    public void shutdown() {
        shutdownCalled = true;
    }

}