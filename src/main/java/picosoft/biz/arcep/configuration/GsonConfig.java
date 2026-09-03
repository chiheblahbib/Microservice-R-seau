package picosoft.biz.arcep.configuration;

import com.google.gson.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;

@Configuration
public class GsonConfig {

    @Bean
    public Gson gson() {
        return new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new JsonSerializer<ZonedDateTime>() {
                @Override
                public JsonElement serialize(ZonedDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.toString());
                }
            }).registerTypeAdapter(ZonedDateTime.class, new JsonDeserializer<ZonedDateTime>() {
                    @Override
                    public ZonedDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                            throws JsonParseException {
                        return ZonedDateTime.parse(json.getAsString());
                    }
            })
            .create();
    }
}
