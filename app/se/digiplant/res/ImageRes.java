package se.digiplant.res;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.ArrayUtils;

import javax.imageio.ImageIO;
import javax.persistence.*;
import java.awt.image.BufferedImage;
import java.io.File;

@Entity
@DiscriminatorValue("image")
public class ImageRes extends ResBase {

    public ImageRes() {}

    public ImageRes(File file, String source) throws IllegalArgumentException {
        super(file, source);

        String[] imageTypes = new String[]{"jpg", "jpeg", "gif", "png"};

        if (!ArrayUtils.contains(imageTypes, extension())) {
            throw new IllegalArgumentException("File must be of the following type: " + Joiner.on(",").join(imageTypes));
        }
    }

    public void calculateImageSize() {
        BufferedImage source = null;
        try {
            source = ImageIO.read(file);
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

    public int width;
    public int height;
}
