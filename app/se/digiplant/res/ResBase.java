package se.digiplant.res;

import org.apache.commons.io.FilenameUtils;
import play.db.ebean.Model;
import play.libs.Scala;

import javax.persistence.*;
import java.io.File;

@Table(name="res")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="res_type", discriminatorType=DiscriminatorType.STRING, length=20)
public abstract class ResBase extends Model {

    @Id
    public String filename;

    @Id
    public String source;

    public String mimetype;

    protected File file = null;
    public File getFile() {
        if (file == null) {
            file = Res.get(filename, source);
        }
        return file;
    }

    protected ResBase() {}

    public ResBase(String filename) {
        this.filename = filename;
    }

    public ResBase(File file, String source) {
        this.source = source;
        this.file = file;
        this.mimetype = Scala.orNull(play.api.libs.MimeTypes.forExtension(filename));
    }

    public ResBase(File file) {
        this(file, "default");
    }

    public String extension() {
        return filename != null ? FilenameUtils.getExtension(filename) : "";
    }

    public boolean exists() {
        return file != null && file.exists();
    }

    public static Finder<Long, ResBase> find = new Finder<Long, ResBase>(Long.class, ResBase.class);
}
