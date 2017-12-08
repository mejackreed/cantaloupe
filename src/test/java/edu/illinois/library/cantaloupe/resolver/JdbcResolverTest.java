package edu.illinois.library.cantaloupe.resolver;

import edu.illinois.library.cantaloupe.config.Configuration;
import edu.illinois.library.cantaloupe.config.Key;
import edu.illinois.library.cantaloupe.image.Format;
import edu.illinois.library.cantaloupe.image.Identifier;
import edu.illinois.library.cantaloupe.resource.RequestContext;
import edu.illinois.library.cantaloupe.test.BaseTest;
import edu.illinois.library.cantaloupe.test.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.Assert.*;

public class JdbcResolverTest extends BaseTest {

    private static final Identifier IDENTIFIER =
            new Identifier("jpg-rgb-64x56x8-baseline.jpg");

    private JdbcResolver instance;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        Configuration config = Configuration.getInstance();
        // Use an in-memory H2 database.
        config.setProperty(Key.JDBCRESOLVER_JDBC_URL, "jdbc:h2:mem:test");
        config.setProperty(Key.JDBCRESOLVER_USER, "sa");
        config.setProperty(Key.JDBCRESOLVER_PASSWORD, "");
        config.setProperty(Key.DELEGATE_SCRIPT_ENABLED, true);
        config.setProperty(Key.DELEGATE_SCRIPT_PATHNAME,
                TestUtil.getFixture("delegates.rb").toString());

        try (Connection conn = JdbcResolver.getConnection()) {
            // create the table
            String sql = "CREATE TABLE IF NOT EXISTS items (" +
                    "filename VARCHAR(255)," +
                    "media_type VARCHAR(255)," +
                    "image BLOB);";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.execute();
            }

            // insert some images
            sql = "INSERT INTO items (filename, media_type, image) VALUES (?, ?, ?)";

            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, "jpg.jpg");
                statement.setString(2, "image/jpeg");
                statement.setBinaryStream(3,
                        Files.newInputStream(TestUtil.getImage(IDENTIFIER.toString())));
                statement.executeUpdate();
            }
        }

        instance = new JdbcResolver();
        instance.setIdentifier(IDENTIFIER);
        instance.setContext(new RequestContext());
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        try (Connection conn = JdbcResolver.getConnection()) {
            String sql = "DROP TABLE items;";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.execute();
            }
        }
    }

    /* checkAccess() */

    @Test
    public void testCheckAccessWithPresentImage() throws Exception {
        instance.setIdentifier(new Identifier("jpg.jpg"));
        instance.checkAccess();
    }

    @Test(expected = NoSuchFileException.class)
    public void testCheckAccessWithMissingImage() throws Exception {
        instance.setIdentifier(new Identifier("bogus"));
        instance.checkAccess();
    }

    /* getSourceFormat() */

    @Test
    public void testGetSourceFormatWithPresentImage() throws Exception {
        instance.setIdentifier(new Identifier("jpg.jpg"));
        assertEquals(Format.JPG, instance.getSourceFormat());
    }

    @Test
    public void testGetSourceFormatWithMissingImage() throws Exception {
        instance.setIdentifier(new Identifier("bogus"));
        assertEquals(Format.UNKNOWN, instance.getSourceFormat());
    }

    /* getDatabaseIdentifier() */

    @Test
    public void testGetDatabaseIdentifier() throws Exception {
        instance.setIdentifier(new Identifier("cats.jpg"));
        String result = instance.getDatabaseIdentifier();
        assertEquals("cats.jpg", result);
    }

    /* getLookupSQL() */

    @Test
    public void testGetLookupSQL() throws Exception {
        String result = instance.getLookupSQL();
        assertEquals("SELECT image FROM items WHERE filename = ?", result);
    }

    /* getMediaType() */

    @Test
    public void testGetMediaType() throws Exception {
        instance.setIdentifier(new Identifier("cats.jpg"));
        String result = instance.getMediaType();
        assertEquals("SELECT media_type FROM items WHERE filename = ?", result);
    }

    /* newStreamSource() */

    @Test
    public void testNewStreamSourceWithPresentImage() throws Exception {
        instance.setIdentifier(new Identifier("jpg.jpg"));
        assertNotNull(instance.newStreamSource());
    }

}
