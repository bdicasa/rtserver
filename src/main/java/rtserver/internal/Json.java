
package rtserver.internal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Reader;
import java.util.Map;
import java.util.List;

/**
 *
 * @author Brian
 */
public class Json {

    private static final Gson gson = new Gson();

    /**
     * Takes a string of JSON and returns a map representation of the json.
     * @param json The JSON to convert to a map.
     * @return A map representation of the JSON.
     */
    public static Map<String, String> toMap(Reader json) {
        Map<String, String> jsonMap =
            gson.fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
        return jsonMap;
    }

    /**
     * Takes a string of JSON a returns a list of maps from that JSON.
     * @param json The JSON to convert to a list of maps.
     * @return A list of maps representing the passed in JSON.
     */
    public static List<Map<String, String>> toListMap(Reader json) {
        List<Map<String, String>> jsonList =
            gson.fromJson(json, new TypeToken<List<Map<String, String>>>() {}.getType());
        return jsonList;
    }

    /**
     * Takes a string of JSON a returns a list of maps from that JSON.
     * @param json The JSON to convert to a list of maps.
     * @return A list of maps representing the passed in JSON.
     */
    public static List<Map<String, String>> toListMap(String json) {
        List<Map<String, String>> jsonList =
            gson.fromJson(json, new TypeToken<List<Map<String, String>>>() {}.getType());
        return jsonList;
    }
    
    /**
     * Takes a string of JSON and returns a map representation of the json.
     * @param json The JSON to convert to a map.
     * @return A map representation of the JSON.
     */
    public static Map<String, Object> toMap(String json) {
        Map<String, Object> jsonMap =
            gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
        return jsonMap;
    }
    
    public static String stringify(Object data) {
    	return gson.toJson(data);
    }
}

