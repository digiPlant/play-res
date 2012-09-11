package se.digiplant.resource;

import java.io.File;

public class ImageResource extends ResourceBase {

    public ImageResource(File file) {

        /*String[] imageTypes = new String[]{"jpg", "jpeg", "gif", "png"};
        for( String type: imageTypes ){
            if( fileType.equals(type) ) return true;
        }

        @Transient
        public boolean isImageType(){
            if( fileType == null ) return false;
            String[] imageTypes = new String[]{"jpg", "jpeg", "gif", "png"};
            for( String type: imageTypes ){
                if( fileType.equals(type) ) return true;
            }
            return false;
        }*/

        super(file);

        /*if (extension()) {
            BufferedImage source = null;
            try {
                source = ImageIO.read(file);
                this.width = source.getWidth();
                this.height = source.getHeight();
            } catch(Exception ex){}
            finally {
                source = null;
            }
        }*/
    }

    public int width;
    public int height;
}
