package palimpsest;

import java.io.File;
import javax.swing.JFileChooser;
import java.util.ArrayList;
import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PGraphics;

import static parameters.Parameters.SEED;

import com.krab.lazy.LazyGui;

import effects.InvertEffect;
import effects.fbmEffect;
import effects.Effect;

public class palimpsestApp extends PApplet {
    public static void main(String[] args) {
        PApplet.main(palimpsestApp.class);
    }

    LazyGui gui;
    PImage img;
    PGraphics edited;
    ArrayList<Effect> effects = new ArrayList<>();

    fbmEffect fbm;  // ← store fbmEffect instance

    boolean shouldResize = false;
    int targetWidth, targetHeight;
    int bg;

    @Override
    public void settings() {
        size(1600, 2200, P2D);  // 8x11 inches at 200 DPI
        randomSeed(SEED);
        noiseSeed(floor(random(MAX_INT)));
    }

    @Override
    public void setup() {
        colorMode(HSB, 360, 100, 100);
        bg = color(0, 0, 100);
        gui = new LazyGui(this);

        effects.add(new InvertEffect());

        fbm = new fbmEffect();
        effects.add(fbm);

        img = null;
        edited = null;
    }


    @Override
    public void draw() {
        // === Background Color Picker ===
        bg = gui.colorPicker("init/background_color").hex;
        fbm.setBackgroundColor(bg);

        background(color(0, 0, 100));  // base clear background

        // === One-time Canvas Initialization ===
        if (img == null) {
            int canvasW = width;
            int canvasH = height;

            img = createImage(canvasW, canvasH, HSB);
            img.loadPixels();
            Arrays.fill(img.pixels, bg);  // fill with selected color
            img.updatePixels();

            edited = createGraphics(canvasW, canvasH, P2D);
            gui.sliderSet("image/scale", 1.0f);
        }

        // === Export Button ===
        if (gui.button("save/export image")) {
            String filename = "out/duckforge_" + timestamp() + ".png";
            edited.save(filename);
            println("Saved image to: " + filename);
        }

        // === Regenerate Button ===
        if (gui.button("init/regenerate canvas")) {
            img = null;  // triggers re-init next frame
        }

        // === Main Render Pass ===
        if (img != null) {
            float scale = gui.slider("image/scale", 1.0f, 0.1f, 5.0f);
            float x = gui.slider("image/x", 0, -img.width, img.width);
            float y = gui.slider("image/y", 0, -img.height, img.height);

            edited.beginDraw();
            edited.imageMode(CENTER);
            edited.pushMatrix();
            edited.translate(edited.width / 2f + x, edited.height / 2f + y);
            edited.scale(scale);
            edited.image(img, 0, 0);
            edited.popMatrix();

            // Apply effects in order
            for (Effect fx : effects) {
                fx.apply(edited, gui);
            }
            edited.endDraw();

            // === Preview to Screen ===
            imageMode(CENTER);
            int previewMargin = 50;
            float previewScale = min(
                    (float) (width - 2 * previewMargin) / edited.width,
                    (float) (height - 2 * previewMargin) / edited.height
            );
            float previewX = width / 2f;
            float previewY = height / 2f;

            pushMatrix();
            translate(previewX, previewY);
            scale(previewScale);
            imageMode(CENTER);
            image(edited, 0, 0);
            popMatrix();
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
                edited = createGraphics(img.width, img.height, P2D);

                float windowAspect = (float) width / height;
                float imgAspect = (float) img.width / img.height;
                float fitScale = (imgAspect > windowAspect)
                        ? (float) width / img.width
                        : (float) height / img.height;

                gui.sliderSet("image/scale", fitScale);
            }
        }
    }

    private String timestamp() {
        return year() + nf(month(), 2) + nf(day(), 2) + "_" + nf(hour(), 2) + nf(minute(), 2) + nf(second(), 2);
    }

    float[] hsbToRGB(float h, float s, float b) {
        h = constrain(h, 0, 360);
        s = constrain(s, 0, 100);
        b = constrain(b, 0, 100);


        int c = color(h, s, b);
        colorMode(RGB);
        float r = red(c) / 255f;
        float g = green(c) / 255f;
        float bl = blue(c) / 255f;
        colorMode(HSB, 360, 100, 100);
        return new float[]{r, g, bl};
    }

}
