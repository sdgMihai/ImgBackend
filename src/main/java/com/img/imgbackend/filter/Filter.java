package com.img.imgbackend.filter;


import com.img.imgbackend.utils.Image;

public abstract class Filter {
    public FilterAdditionalData filter_additional_data;
    /**
     * aplica un filtru pe imagine
     * @param image referinta catre imagine
     * @param newImage referinta catre obiectul tip Image
     *          care va contine imaginea rezultata in urma
     *          aplicarii filtrului.
     */
    public abstract void applyFilter(Image image, Image newImage);
}
