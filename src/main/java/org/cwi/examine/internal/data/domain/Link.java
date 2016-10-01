package org.cwi.examine.internal.data.domain;


/**
 *
 */
public class Link {

    private String source, target;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "Link{" +
                "source='" + source + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
