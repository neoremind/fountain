package net.neoremind.fountain.pack.impl;

import net.neoremind.fountain.pack.UnpackClazResolver;

/**
 * 通过给定的class的fullname给出解包时需要的class
 *
 * @author hexiufeng
 */
public class NameUnpackClazResolver implements UnpackClazResolver {
    private String className = "ChangeDataSet";
    private Class<?> claz;
    private volatile boolean inited = false;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * 初始化方法，spring构建对象时执行，需要配置在init-method中
     */
    public synchronized void init() {
        if (inited) {
            return;
        }
        try {
            claz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        inited = true;
    }

    @Override
    public Class<?> getClaz() {
        if (!inited) {
            init();
        }
        return claz;
    }
}
