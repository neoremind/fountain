package net.neoremind.fountain.util;

import java.util.Collection;

/**
 * 集合工具
 * 
 * @author hexiufeng
 * 
 */
public class CollectionUtils {

    private CollectionUtils() {

    }

    /**
     * collection 是否为空
     * 
     * @param c 集合
     * @return true or false
     */
    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.size() == 0;
    }

}
