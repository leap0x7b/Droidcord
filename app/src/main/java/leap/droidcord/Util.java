package leap.droidcord;

public class Util {
    public static int[] resizeFit(int imgW, int imgH, int maxW, int maxH) {
        int imgAspect = imgW * 100 / imgH;
        int maxAspect = maxW * 100 / maxH;
        int width, height;

        if (imgW <= maxW && imgH <= maxH) {
            width = imgW;
            height = imgH;
        } else if (imgAspect > maxAspect) {
            width = maxW;
            height = (maxW * 100) / imgAspect;
        } else {
            height = maxH;
            width = (maxH * imgAspect) / 100;
        }

        return new int[]{width, height};
    }

    public static String fileSizeToString(int size) {
        if (size >= 1000000)
            return "" + size / 1000000 + " MB";
        if (size >= 1000)
            return "" + size / 1000 + " kB";
        return "" + size + " bytes";
    }

    public static int indexOfAny(String haystack, String[] needles,
                                 int startIndex) {
        int result = -1;

        for (int i = 0; i < needles.length; i++) {
            int current = haystack.indexOf(needles[i], startIndex);
            if (current != -1 && (current < result || result == -1)) {
                result = current;
            }
        }
        return result;
    }

    public static int hsvToRgb(int h, int s, int v) {
        int r, g, b;

        // Ensure hue is between 0 and 359
        h = h % 360;
        if (h < 0)
            h += 360;

        // Normalize s and v to be between 0 and 255
        s = Math.min(Math.max(s, 0), 255);
        v = Math.min(Math.max(v, 0), 255);

        int region = h / 60;
        int remainder = (h % 60) * 255 / 60;

        int p = (v * (255 - s)) / 255;
        int q = (v * (255 - (s * remainder) / 255)) / 255;
        int t = (v * (255 - (s * (255 - remainder)) / 255)) / 255;

        switch (region) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            default:
                r = v;
                g = p;
                b = q;
                break;
        }
        return (r << 16) | (g << 8) | b;
    }
}
