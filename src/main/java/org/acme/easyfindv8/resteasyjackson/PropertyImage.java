package org.acme.easyfindv8.resteasyjackson;

import io.smallrye.mutiny.Uni;

public class PropertyImage extends Property {
    public Property property;
    public ImageModel imageModel;

    public PropertyImage() {
        //NO ARGS
    }

    public PropertyImage(Property property, ImageModel imageModel) {
        this.property = property;
        this.imageModel = imageModel;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public ImageModel getImageModel() {
        return imageModel;
    }

    public void setImageModel(ImageModel imageModel) {
        this.imageModel = imageModel;
    }
}
