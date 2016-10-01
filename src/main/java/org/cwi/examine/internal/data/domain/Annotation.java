package org.cwi.examine.internal.data.domain;

/**
 * Created by kdinkla on 9/28/16.
 */
public class Annotation extends Element {

    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "category='" + category +
                "} extends " + super.toString();
    }
}
