package com.img.imgbackend;

import com.img.imgbackend.utils.Image;
import com.img.imgbackend.utils.ThreadSpecificData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThreadSpecificDataTest {
    @Test
    public void testEncapsulation() {
        Image input = new Image(1, 1);
        Image output = new Image(1, 1);
        int PARALLELISM = 1;
        ThreadSpecificData capsule = new ThreadSpecificData(
                PARALLELISM
                , input
                , output
        );
        assertEquals(input, capsule.getImage());
        assertEquals(output, capsule.getNewImage());
        assertEquals(PARALLELISM, capsule.getPARALLELISM());
    }
}
