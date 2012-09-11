package se.digiplant.resource;

import org.apache.commons.io.FilenameUtils;
import play.db.ebean.Model;
import play.libs.Scala;

import javax.persistence.*;
import java.io.File;

@Table(name="resources")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="res_type", discriminatorType=DiscriminatorType.STRING, length=20)
public abstract class ResourceBase extends Model {

    @Id
    public String filename;

    @Id
    public String source;

    public String mimetype;

    private File file;

    public ResourceBase(File file, String source) {
        this.source = source;
        this.file = file;
        this.mimetype = Scala.orNull(play.api.libs.MimeTypes.forExtension(filename));
    }

    public ResourceBase(File file) {
        this.source = "default";
        this.file = file;
        this.mimetype = Scala.orNull(play.api.libs.MimeTypes.forExtension(filename));
    }

    public String extension() {
        return filename != null ? FilenameUtils.getExtension(filename) : "";
    }

    public boolean exists() {
        return file != null && file.exists();
    }

    public static Finder<Long, ResourceBase> find = new Finder<Long, ResourceBase>(Long.class, ResourceBase.class);
}
