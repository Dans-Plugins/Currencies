package dansplugins.currencies.objects;

import java.util.Map;

public interface Saveable {
    Map<String, String> save();
    void load(Map<String, String> data);
}
