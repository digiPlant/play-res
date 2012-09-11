package se.digiplant.resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.test.FakeApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

public class ResourcePluginTest {

    final Logger logger = LoggerFactory.getLogger(ResourcePluginTest.class);

    static ArrayList<String> plugins = new ArrayList<String>();
    Map<String, String> config = new HashMap<String, String>();

    @BeforeClass
    public static void before() {
        plugins.add("se.digiplant.resource.ResourcePlugin");
    }

    @Before
    public void beforeEach() {
        config.clear();
    }

    @Test
    public void shouldStart() {
        config.put("res.default", "res");
        final FakeApplication app = fakeApplication(config, plugins);
        running(app, new Runnable() {
            @Override
            public void run() {
                File dir = app.getWrappedApplication().getFile(config.get("res.default"));
                try { FileUtils.forceMkdir(dir); } catch (IOException e) {}
                ResourcePlugin plug = app.getWrappedApplication().plugin(ResourcePlugin.class).get();
                assertTrue("Resource plugin is started", plug != null);
            }
        });
    }

    @Test
    public void shouldSave() {
        config.put("res.default", "tmp/res");
        final FakeApplication app = fakeApplication(config, plugins);
        running(app, new Runnable() {
            @Override
            public void run() {

                // Copy testfile to tmp/upload.jpg
                File digiPlant = new File("src/test/resources/digiPlant.jpg");
                File testFile = new File("tmp/upload.jpg");
                try { FileUtils.copyFile(digiPlant, testFile); } catch (IOException e) {}

                File dir = app.getWrappedApplication().getFile(config.get("res.default"));

                ResourcePlugin plug = app.getWrappedApplication().plugin(ResourcePlugin.class).get();
                String filename = null;
                try { filename = plug.save(testFile); } catch (IOException e) {}

                assertEquals("filename is hash of file", filename, "5564ac5e3968e77b4022f55a23d36630bdeb0274.jpg");
            }
        });
    }

    @Test
    public void shouldGet() {
        config.put("res.default", "tmp/res");
        final FakeApplication app = fakeApplication(config, plugins);
        running(app, new Runnable() {
            @Override
            public void run() {

                File digiPlant = new File("src/test/resources/digiPlant.jpg");

                ResourcePlugin plug = app.getWrappedApplication().plugin(ResourcePlugin.class).get();
                File testFile = plug.get("5564ac5e3968e77b4022f55a23d36630bdeb0274.jpg");

                boolean filesEqual = false;
                try {
                    filesEqual = IOUtils.contentEquals(new FileInputStream(digiPlant), new FileInputStream(testFile));
                } catch (IOException e) {}

                assertTrue("files should be equal", filesEqual);
            }
        });
    }
}