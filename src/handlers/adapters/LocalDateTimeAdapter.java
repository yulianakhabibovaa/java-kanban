package handlers.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;

import static tasks.Task.DATE_TIME_FORMATER;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
        if (localDateTime == null) {
            if (jsonWriter.getSerializeNulls()) {
                jsonWriter.nullValue();
            }
        } else {
            jsonWriter.value(localDateTime.format(DATE_TIME_FORMATER));
        }
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }

        return LocalDateTime.parse(jsonReader.nextString(), DATE_TIME_FORMATER);
    }
}