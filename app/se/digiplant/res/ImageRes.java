package se.digiplant.res;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.ArrayUtils;
import play.mvc.Http;
import play.api.libs.MimeTypes;
import se.digiplant.res.Res;

import javax.imageio.ImageIO;
import javax.persistence.*;
import java.awt.image.BufferedImage;
import java.io.File;
import static play.libs.Scala.orNull;

@Entity
@DiscriminatorValue("image")
public class ImageRes extends ResBase {

    public int width;
    public int height;

    public ImageRes() {}

    public ImageRes(File file, String source) throws IllegalArgumentException {
        String[] imageTypes = new String[]{"image/jpeg", "image/png", "image/gif"};
        String mime = orNull(MimeTypes.forFileName(file.getName()));
        if (!ArrayUtils.contains(imageTypes, mime)) {
            throw new IllegalArgumentException("File must be of the following type: " + Joiner.on(",").join(imageTypes));
        }

        this.id = Res.put(file, source);
        this.source = source;
        this.mimetype = mime;
    }

    public ImageRes(Http.MultipartFormData.FilePart part, String source) throws IllegalArgumentException {
        String[] imageTypes = new String[]{"image/jpeg", "image/png", "image/gif"};
        if (!ArrayUtils.contains(imageTypes, part.getContentType())) {
            throw new IllegalArgumentException("File must be of the following type: " + Joiner.on(",").join(imageTypes));
        }

        this.id = Res.put(part, source);
        this.source = source;
        this.mimetype = part.getContentType();
    }

    public void calculateImageSize() {
        BufferedImage source = null;
        try {
            source = ImageIO.read(getFile());
            this.width = source.getWidth();
            this.height = source.getHeight();
        } catch(Exception ex){}
        finally {
            source = null;
        }
    }

    @Override
    public void save() {
        calculateImageSize();
        super.save();
    }

    @Override
    public void update() {
        calculateImageSize();
        super.update();
    }
}
