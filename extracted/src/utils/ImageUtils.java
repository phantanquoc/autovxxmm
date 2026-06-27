/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.batik.transcoder.SVGAbstractTranscoder
 *  org.apache.batik.transcoder.TranscoderInput
 *  org.apache.batik.transcoder.TranscoderOutput
 *  org.apache.batik.transcoder.image.ImageTranscoder
 */
package utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

public class ImageUtils {
    private static final int DEFAULT_ICON_WITH = 32;
    private static final int DEFAULT_ICON_HEIGHT = 32;

    public static Icon loadSvgIcon(String path) {
        Image image = ImageUtils.loadSvgImage(path);
        return new ImageIcon(image);
    }

    public static Icon loadSvgIcon(String path, int width, int height) {
        Image image = ImageUtils.loadSvgImage(path, width, height);
        return new ImageIcon(image);
    }

    public static Image loadSvgImage(String path) {
        return ImageUtils.loadSvgImage(path, 32, 32);
    }

    public static Image loadSvgImage(String path, int width, int height) {
        InputStream stream = ImageUtils.class.getResourceAsStream(path);
        if (stream == null) {
            return null;
        }
        BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, Float.valueOf(width));
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, Float.valueOf(height));
        try {
            TranscoderInput input = new TranscoderInput(stream);
            transcoder.transcode(input, null);
            stream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return transcoder.getImage();
    }

    public static class BufferedImageTranscoder
    extends ImageTranscoder {
        private BufferedImage image = null;

        public BufferedImage createImage(int w, int h) {
            return new BufferedImage(w, h, 2);
        }

        public void writeImage(BufferedImage img, TranscoderOutput out) {
            this.image = img;
        }

        public BufferedImage getImage() {
            return this.image;
        }
    }
}

