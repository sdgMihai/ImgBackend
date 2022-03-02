package com.img.imgbackend;

import com.img.imgbackend.filter.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestFilterFactory {
    @Test
    public void checkFilterByName() {
        Filter filter = FilterFactory.filterCreate("sharpen");
        assert (filter instanceof SharpenFilter);

        filter = FilterFactory.filterCreate("emboss");
        assert (filter instanceof EmbossFilter);

        filter = FilterFactory.filterCreate("sepia");
        assert (filter instanceof SepiaFilter);

        filter = FilterFactory.filterCreate("contrast");
        assert (filter instanceof ContrastFilter);

        filter = FilterFactory.filterCreate("brightness");
        assert (filter instanceof BrightnessFilter);

        filter = FilterFactory.filterCreate("black-white");
        assert (filter instanceof BlackWhiteFilter);

        filter = FilterFactory.filterCreate("gaussian-blur");
        assert (filter instanceof GaussianBlurFilter);

        filter = FilterFactory.filterCreate("non-maximum-suppression");
        assert (filter instanceof NonMaximumSuppressionFilter);

        filter = FilterFactory.filterCreate("double-threshold");
        assert (filter instanceof DoubleThresholdFilter);

        filter = FilterFactory.filterCreate("edge-tracking");
        assert (filter instanceof EdgeTrackingFilter);

        filter = FilterFactory.filterCreate("gradient");
        assert (filter instanceof GradientFilter);

        filter = FilterFactory.filterCreate("canny-edge-detection");
        assert (filter instanceof CannyEdgeDetectionFilter);
    }
}
