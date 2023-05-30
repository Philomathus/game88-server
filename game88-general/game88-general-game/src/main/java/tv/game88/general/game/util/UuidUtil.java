package tv.game88.general.game.util;

import java.util.UUID;

/**
 * UUID工具类
 *
 */
public final class UuidUtil {
    private UuidUtil(){
        throw new RuntimeException("UuidUtil.class can't be instantiated");
    }

    /**
     * 获取随机的UUID小写字符串
     */
    public static String getRandomUuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().toLowerCase();
    }

    /**
     * 从UUID字符串中去除“-”分隔符
     */
    public static String removeSeparatorFromUuid(String uuid) {
        uuid = uuid.replaceAll("-","");
        return uuid;
    }

    /**
     * 获取不包含“-”分隔符的uuid字符串
     */
    public static String getRandomUuidWithoutSeparator() {
        String uuid = getRandomUuid();
        return removeSeparatorFromUuid(uuid);
    }

    /**
     * 获取大写16位唯一unid
     */
    public static String getUnid() {
        String uuid = getRandomUuid();
        uuid = uuid.toUpperCase();
        return removeSeparatorFromUuid(uuid);
    }
}
