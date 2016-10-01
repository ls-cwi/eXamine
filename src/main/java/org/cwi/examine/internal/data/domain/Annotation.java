package org.cwi.examine.internal.data.domain;

/**
 * Created by kdinkla on 9/28/16.
 */
public class Annotation extends Element {

    private String category;
    private double score;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "category='" + category + '\'' +
                ", score=" + score +
                "} extends " + super.toString();
    }
}
