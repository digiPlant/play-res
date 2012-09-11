package se.digiplant.resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import play.Application;
import play.Configuration;
import play.Plugin;
import com.typesafe.config.ConfigException;

import java.io.*;
import java.util.*;

public class ResourcePlugin extends Plugin {

    public final Application application;
    public final Configuration config;
    public final Map<String, File> resourceDirectories = new HashMap<String, File>();

    public ResourcePlugin(Application application) {
        this.application = application;
        try {
            this.config = application.configuration().getConfig("res");
        } catch (ConfigException.Missing ex) {
            throw application.configuration().reportError("res", "res configuration missing.", ex);
        }
    }

    @Override
    public void onStart() {
        for (String key : config.subKeys()) {
            String dir = config.getString(key);
            File file = application.getFile(dir);
            try {
                FileUtils.forceMkdir(file);
            } catch (IOException e) {
                throw application.configuration().reportError("res." + key, file.getAbsolutePath() + " doesn't exist", null);
            }
            resourceDirectories.put(key, file);
        }
    }

    @Override
    public void onStop() {
    }

    @Override
    public boolean enabled() {
        return (config != null);
    }

    public File get(String filename, String source) {
        File resourceDir = resourceDirectories.get(source);
        if (resourceDir == null)
            throw new IllegalArgumentException("source doesn't exist");

        return FileUtils.getFile(resourceDir, hashAsDirectories(filename), filename);
    }

    public File get(String filename) {
        return get(filename, "default");
    }

    public String save(File file, String source) throws IOException {
        FileInputStream input = new FileInputStream(file);
        String hash = DigestUtils.shaHex(input);

        File resourceDir = resourceDirectories.get(source);
        if (resourceDir == null)
            throw new IllegalArgumentException("source doesn't exist");

        File base = new File(resourceDir, hashAsDirectories(hash));
        if (!base.exists()) {
            base.mkdirs();
        }

        String filename = hash + "." + FilenameUtils.getExtension(file.getName());
        File target = new File(base, filename);

        // since the filename is computed from the contents of the file, there only need to be one file with this name.
        if (!target.exists()) {
            FileUtils.moveFile(file, target);
        }
        return filename;
    }

    public String save(File file) throws IOException {
        return save(file, "default");
    }

    private String hashAsDirectories(String hash) {
        return hash.substring(0,4) + '/' + hash.substring(4, 8);
    }
}
