package edu.illinois.library.cantaloupe.source;

import edu.illinois.library.cantaloupe.image.Format;
import org.apache.commons.io.input.NullInputStream;

public class MockStreamSource extends AbstractSource
        implements StreamSource {

    @Override
    public void checkAccess() {
    }

    @Override
    public Format getFormat() {
        return null;
    }

    @Override
    public StreamFactory newStreamFactory() {
        return () -> new NullInputStream(8);
    }

}
