package se.digiplant.res;

import org.apache.commons.io.FilenameUtils;
import play.db.ebean.Model;
import se.digiplant.res.Res;

import javax.persistence.*;
import java.io.File;

@Table(name="res")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="res_type", discriminatorType=DiscriminatorType.STRING, length=20)
public class ResBase extends Model {

    @Id
    public String id;

    public String source;

    public String mimetype;

    protected File file = null;
    public File getFile() {
        if (file == null) {
            file = Res.get(id, source);
        }
        return file;
    }

    public String extension() {
        return id != null ? FilenameUtils.getExtension(id) : "";
    }

    public boolean exists() {
        return file != null && file.exists();
    }

    public static Finder<Long, ResBase> find = new Finder<Long, ResBase>(Long.class, ResBase.class);
}
