package utils;

import processing.core.PApplet;
import processing.core.PVector;
import processing.core.PGraphics;

public class LineUtils {
    private final PApplet p;

    public LineUtils(PApplet p) {
        this.p = p;
    }

    public void draftsmanLine(PGraphics g, float x1, float y1, float x2, float y2, int baseColor) {
        float distance = p.dist(x1, y1, x2, y2);
        int steps = p.max(1, (int)(distance * 1.5));

        float baseHue = p.hue(baseColor);
        float baseSat = p.saturation(baseColor);
        float baseBrt = p.brightness(baseColor);

        float hueScale = 0.01f;
        float satScale = 0.2f;
        float brtScale = 0.3f;
        float weightScale = 0.05f;
        float baseNoiseOffset = (float) p.randomGaussian() * 10;

        for (int j = 0; j < 2; j++) {
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                float x = p.lerp(x1, x2, t);
                float y = p.lerp(y1, y2, t);
                float n = baseNoiseOffset + t;

                float h = (baseHue + p.map(p.noise(x * hueScale, n), 0, 1, -100, 100) + 360) % 360;
                float s = p.constrain(baseSat + p.map(p.noise(x * satScale, n), 0, 1, -20, 20), 0, 100);
                float b = p.constrain(baseBrt + p.map(p.noise(x * brtScale, n), 0, 1, -20, 20), 0, 100);
                float w = p.map(p.noise(x * weightScale, n), 0, 1, 0.1f, 2f);

                g.stroke(h, s, b, 0.2f);
                g.strokeWeight(w);
                g.point(x + (float) p.randomGaussian() * 0.1f, y + (float) p.randomGaussian() * 0.1f);
            }
        }
    }

    public void threadbox(PGraphics g, float x, float y, float w, float h, float angleDegrees, float spacing, int c) {
        float angle = p.radians(angleDegrees);

        float dx = p.cos(angle);
        float dy = p.sin(angle);
        float ox = -dy;
        float oy = dx;

        float diag = p.dist(0, 0, w, h);

        for (float i = -diag; i <= diag; i += spacing) {
            float cx = x + w / 2 + i * ox;
            float cy = y + h / 2 + i * oy;

            float x1 = cx - dx * diag;
            float y1 = cy - dy * diag;
            float x2 = cx + dx * diag;
            float y2 = cy + dy * diag;

            PVector[] clipped = clipLineToRect(x1, y1, x2, y2, x, y, w, h);
            if (clipped != null) {
                draftsmanLine(g, clipped[0].x, clipped[0].y, clipped[1].x, clipped[1].y, c);
            }
        }
    }

    private PVector[] clipLineToRect(float x1, float y1, float x2, float y2, float rx, float ry, float rw, float rh) {
        float xmin = rx;
        float xmax = rx + rw;
        float ymin = ry;
        float ymax = ry + rh;

        int code1 = computeOutCode(x1, y1, xmin, xmax, ymin, ymax);
        int code2 = computeOutCode(x2, y2, xmin, xmax, ymin, ymax);

        boolean accept = false;

        while (true) {
            if ((code1 | code2) == 0) {
                accept = true;
                break;
            } else if ((code1 & code2) != 0) {
                break;
            } else {
                float x = 0, y = 0;
                int outcodeOut = (code1 != 0) ? code1 : code2;

                if ((outcodeOut & 8) != 0) {
                    x = x1 + (x2 - x1) * (ymax - y1) / (y2 - y1);
                    y = ymax;
                } else if ((outcodeOut & 4) != 0) {
                    x = x1 + (x2 - x1) * (ymin - y1) / (y2 - y1);
                    y = ymin;
                } else if ((outcodeOut & 2) != 0) {
                    y = y1 + (y2 - y1) * (xmax - x1) / (x2 - x1);
                    x = xmax;
                } else if ((outcodeOut & 1) != 0) {
                    y = y1 + (y2 - y1) * (xmin - x1) / (x2 - x1);
                    x = xmin;
                }

                if (outcodeOut == code1) {
                    x1 = x;
                    y1 = y;
                    code1 = computeOutCode(x1, y1, xmin, xmax, ymin, ymax);
                } else {
                    x2 = x;
                    y2 = y;
                    code2 = computeOutCode(x2, y2, xmin, xmax, ymin, ymax);
                }
            }
        }

        if (accept) {
            return new PVector[]{new PVector(x1, y1), new PVector(x2, y2)};
        } else {
            return null;
        }
    }

    private int computeOutCode(float x, float y, float xmin, float xmax, float ymin, float ymax) {
        int code = 0;
        if (x < xmin) code |= 1;
        else if (x > xmax) code |= 2;
        if (y < ymin) code |= 4;
        else if (y > ymax) code |= 8;
        return code;
    }
}
