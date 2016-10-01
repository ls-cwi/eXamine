package org.cwi.examine.internal.data.domain;

/**
 * Created by kdinkla on 9/28/16.
 */
public class Element {

    private String identifier;
    private String name;
    private String url;
    private double score;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Element{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", score=" + score +
                '}';
    }
}
