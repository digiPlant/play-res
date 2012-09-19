package se.digiplant.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static play.libs.Scala.orNull;
import static play.libs.Scala.toSeq;

import scala.collection.JavaConversions.*;

public class Resource {

    private static final List<String> emptyList = new ArrayList<String>();

    /**
     * Retrieves a file with the specified fileuid and if specified all meta attributes
     * @param fileuid The SHA1 filename with the extension, it can also include the meta if you don't want to specify it separately
     * @param source The configured source name
     * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
     * @return A File
     */
    public static File get(String fileuid, String source, List<String> meta) {
        return orNull(se.digiplant.resource.api.Resource.get(fileuid, source, toSeq(meta), play.api.Play.unsafeApplication()));
    }

    public static File get(String fileuid, String source) {
        return get(fileuid, source, emptyList);
    }

    public static File get(String fileuid) {
        return get(fileuid, "default");
    }

    /**
     * Puts a file into the supplied source
     * @param file A file to be stored
     * @param source The configured source name
     * @param filename Override the sha1 checksum generated filename
     * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
     * @return The unique file name with the metadata appended
     */
    public static String put(File file, String source, String filename, List<String> meta) {
        return orNull(se.digiplant.resource.api.Resource.put(file, source, filename, toSeq(meta), play.api.Play.unsafeApplication()));
    }

    public static String put(File file, String source, String filename) {
        return put(file, source, filename, emptyList);
    }

    public static String put(File file, String source) {
        return put(file, source, "");
    }

    public static String put(File file) {
        return put(file, "default");
    }

    /**
     * Deletes a file with the specified fileuid
     * @param fileuid The SHA1 filename with the extension, it can also include the meta if you don't want to specify it separately
     * @param source The configured source name
     * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
     * @return true if file was deleted, false if it failed
     */
    public static boolean delete(String fileuid, String source, List<String> meta) {
        return se.digiplant.resource.api.Resource.delete(fileuid, source, toSeq(meta), play.api.Play.unsafeApplication());
    }

    public static boolean delete(String fileuid, String source) {
        return delete(fileuid, source, emptyList);
    }

    public static boolean delete(String fileuid) {
        return delete(fileuid, "default");
    }

    /**
     * Retrieves a file with the specified filepath and if specified all meta attributes
     * @param filePath The filepath relative to the play app, it can also include the meta if you don't want to specify it separately
     * @param meta A list of meta data you want to append to the filename, they are separated by _ so don't use that in the meta names
     * @return File or null
     */
    public static File fileWithMeta(String filePath, List<String> meta) {
        return orNull(se.digiplant.resource.api.Resource.fileWithMeta(filePath, toSeq(meta), play.api.Play.unsafeApplication()));
    }
}
