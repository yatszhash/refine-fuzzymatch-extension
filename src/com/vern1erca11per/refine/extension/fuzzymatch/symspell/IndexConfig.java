package com.vern1erca11per.refine.extension.fuzzymatch.symspell;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.json.JSONObject;
import org.json.JSONWriter;

import java.io.Serializable;
import java.util.Properties;

@Data
@AllArgsConstructor
public class IndexConfig implements Serializable {
    private int maxEditDistance;
    private int prefixLength;

    public void write(JSONWriter jsonWriter, Properties properties) {
        jsonWriter.object();
        jsonWriter.key("maxEditDistance");
        jsonWriter.value(maxEditDistance);
        jsonWriter.key("prefixLength");
        jsonWriter.value(prefixLength);
        jsonWriter.endObject();
    }

    public static IndexConfig load(JSONObject jsonObject) {
        return new IndexConfig(
                jsonObject.getInt("maxEditDistance"),
                jsonObject.getInt("prefixLength")
        );
    }
}
