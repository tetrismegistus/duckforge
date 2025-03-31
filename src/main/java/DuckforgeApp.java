import java.io.File;
import javax.swing.JFileChooser;

import processing.core.PApplet;
import processing.core.PImage;

import static parameters.Parameters.SEED;
import static save.SaveUtil.saveSketch;
import com.krab.lazy.LazyGui;

public class DuckforgeApp extends PApplet {
    public static void main(String[] args) {
        PApplet.main(DuckforgeApp.class);
    }

    LazyGui gui;
    PImage img;
    boolean shouldResize = false;
    int targetWidth, targetHeight;

    @Override
    public void settings() {
        size(1700, 1100, P2D);
        randomSeed(SEED);
        noiseSeed(floor(random(MAX_INT)));
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        img = null; // start empty
        background(255);
    }

    @Override
    public void draw() {
        background(255);

        if (gui.button("load/load image")) {
            selectImage();
        }

        if (img != null) {
            float scale = gui.slider("image/scale", 1.0f, 0.1f, 5.0f);
            float x = gui.slider("image/x", 0, -width, width);
            float y = gui.slider("image/y", 0, -height, height);

            pushMatrix();
            translate(width / 2f + x, height / 2f + y);
            scale(scale);
            imageMode(CENTER);
            image(img, 0, 0);
            popMatrix();
        } else {
            fill(0);
            textAlign(CENTER, CENTER);
            text("No image loaded. Click 'load image' in GUI.", width / 2f, height / 2f);
        }
    }

    private void selectImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select an image");
        int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            PImage loaded = loadImage(selectedFile.getAbsolutePath());

            if (loaded != null) {
                img = loaded;

                float windowAspect = (float) width / height;
                float imgAspect = (float) img.width / img.height;
                float fitScale;

                if (imgAspect > windowAspect) {
                    fitScale = (float) width / img.width;
                } else {
                    fitScale = (float) height / img.height;
                }

                gui.sliderSet("image/scale", fitScale);
            }
        }
    }
}
