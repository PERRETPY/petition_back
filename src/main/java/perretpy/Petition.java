package perretpy;

import java.util.HashSet;

public class Petition {
    private String title;
    private String description;
    private HashSet<String> tag;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashSet<String> getTag() {
        return tag;
    }

    public void setTags(HashSet<String> tag) {
        this.tag = tag;
    }
}
