package tv.game88.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class JsonUtil {

    public JsonUtil() {
    }

    public static ObjectMapper getObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    public static JavaType getJavaType(String className) {
        return TypeFactory.defaultInstance().constructFromCanonical(className);
    }

    public static Map json2Map(String json) {
        Map map = new HashMap<>();

        if (StringUtils.isNotBlank(json)) {
            try {
                map = getObjectMapper().readValue(json, Map.class);
            } catch (Exception e) {
                log.error(json + "转化为Map失败" , e.getMessage(), e);
            }
        }
        return map;
    }

    public static <T> T json2Object(String json, JavaType javaType) {
        if (StringUtils.isNotBlank(json)) {
            try {
                return getObjectMapper().readValue(json, javaType);
            } catch (Exception e) {
                log.error(json + "转化为" + javaType + "对象失败" , e.getMessage(), e);
            }
        }
        return null;
    }

    public static <T> T json2Object(String json, Class<T> valueType) {
        if (StringUtils.isNotBlank(json)) {
            try {
                return getObjectMapper().readValue(json, valueType);
            } catch (Exception e) {
                log.error(json + "转化为" + valueType + "对象失败" , e.getMessage(), e);
            }
        }
        return null;
    }

    public static <T> T json2Object(String json, TypeReference<T> valueType) {
        if (StringUtils.isNotBlank(json)) {
            try {
                return getObjectMapper().readValue(json, valueType);
            } catch (Exception e) {
                log.error(json + "转化为" + valueType + "对象失败" , e.getMessage());
            }
        }
        return null;
    }

    public static String object2Json(Object object) {
        if (object != null) {
            try {
                return getObjectMapper().writeValueAsString(object);
            } catch (Exception e) {
                log.error(object.toString() + "转化为json字符串失败" , e.getMessage(), e);
            }
        }
        return null;
    }

    public static <T> T json2Array(String json, TypeReference<T> valueType) {
        if (StringUtils.isNotBlank(json)) {
            try {
                return getObjectMapper().readValue(json, valueType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static <T> T map2Object(Map map, Class<T> valueType) {
        if (!CollectionUtils.isEmpty(map)) {
            try {
                return getObjectMapper().convertValue(map, valueType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static <T> T map2Object(Map map, JavaType javaType) {
        if (!CollectionUtils.isEmpty(map)) {
            try {
                return getObjectMapper().convertValue(map, javaType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static <T> T map2Object(Map<?, ?> map, TypeReference<T> valueType) {
        if (!CollectionUtils.isEmpty(map)) {
            try {
                return getObjectMapper().convertValue(map, valueType);
            } catch (Exception e) {
                log.error(map.toString() + "转化为" + valueType + "对象失败" , e.getMessage());
            }
        }
        return null;
    }

    public static Map object2Map(Object obj) {
        if (obj != null) {
            try {
                return getObjectMapper().convertValue(obj, Map.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Map<String, String> object2MapStr(Object obj) {
        if (obj != null) {
            try {
                return getObjectMapper().convertValue(obj, new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
