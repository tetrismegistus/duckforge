import java.io.File;
import javax.swing.JFileChooser;
import java.util.ArrayList;


import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PGraphics;


import static parameters.Parameters.SEED;
import static save.SaveUtil.saveSketch;
import com.krab.lazy.LazyGui;

import effects.InvertEffect;
import effects.Effect;

public class DuckforgeApp extends PApplet {
    public static void main(String[] args) {
        PApplet.main(DuckforgeApp.class);
    }

    LazyGui gui;
    PImage img;
    PGraphics edited;
    ArrayList<Effect> effects = new ArrayList<>();

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
        effects.add(new InvertEffect());
        background(255);
    }

    @Override
    public void draw() {
        background(255);

        if (gui.button("load/load image")) {
            selectImage();
        }

        if (gui.button("save/export image")) {
            String filename = "out/duckforge_" + timestamp() + ".png";
            edited.save(filename);
            println("Saved image to: " + filename);
        }


        if (img != null) {
            float scale = gui.slider("image/scale", 1.0f, 0.1f, 5.0f);
            float x = gui.slider("image/x", 0, -img.width, img.width);
            float y = gui.slider("image/y", 0, -img.height, img.height);



            edited.beginDraw();
            edited.background(255);
            edited.imageMode(CENTER);
            edited.pushMatrix();
            edited.translate(edited.width / 2f + x, edited.height / 2f + y);
            edited.image(img, 0, 0);
            edited.popMatrix();

            // 🔥 Apply all effects inside the draw context
            for (Effect fx : effects) {
                fx.apply(edited, gui);
            }

            edited.endDraw();

            // Show scaled-down preview
            imageMode(CENTER);
            int previewMargin = 50;

            float previewScale = min(
                    (float)(width - 2 * previewMargin) / edited.width,
                    (float)(height - 2 * previewMargin) / edited.height
            );

            float previewX = width / 2f;
            float previewY = height / 2f;
            pushMatrix();
            translate(previewX, previewY);
            scale(previewScale);
            imageMode(CENTER);
            image(edited, 0, 0);
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

                // Match output buffer to image size
                edited = createGraphics(img.width, img.height, P2D);

                // Auto scale to fit window
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

    private String timestamp() {
        return year() + nf(month(), 2) + nf(day(), 2) + "_" + nf(hour(), 2) + nf(minute(), 2) + nf(second(), 2);
    }

}
