package com.github.liuanxin.api.model;

import com.github.liuanxin.api.constant.ApiConst;
import com.github.liuanxin.api.util.Tools;

import java.util.Collection;
import java.util.Map;

public class Recursive {

    private Recursive parent;

    private String fieldName;
    private Class<?> self;


    public Recursive() {
    }
    public Recursive(Recursive parent, String fieldName, Class<?> self) {
        this.parent = parent;
        this.fieldName = fieldName;
        this.self = self;
    }
    /**
     * use with field: new Recursive(parent, fieldName, field.getGenericType().toString())
     *
     * @param parent parent
     * @param fieldName field name
     * @param genericType generic type
     */
    public Recursive(Recursive parent, String fieldName, String genericType) {
        this.parent = parent;
        this.fieldName = fieldName;
        this.self = getGenericType(genericType);
    }


    public Recursive getParent() {
        return parent;
    }
    public Recursive setParent(Recursive parent) {
        this.parent = parent;
        return this;
    }

    public String getFieldName() {
        return fieldName;
    }
    public Recursive setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public Class<?> getSelf() {
        return self;
    }
    public Recursive setSelf(Class<?> self) {
        this.self = self;
        return this;
    }


    /**
     * <pre>
     * public class A { ... private A x; }
     *
     * OR
     *
     * public class A { ... private List&lt;A&gt; x; }
     *
     * OR
     *
     * public class A { ... private Map&lt;String, A&gt; x; }
     *
     * OR
     *
     * public class A { ... private B x; }
     * public class B { ... private A xx; }
     *
     * OR
     *
     * public class A { ... private B x; }
     * public class B { ... private List&lt;C&gt; xx; }
     * public class C { ... private Map&lt;String, A&gt; xxx; }
     *
     * will return <span style="color:green">true</span>
     * </pre>
     * @return recursive return true
     */
    public boolean checkRecursive() {
        return check(self, parent);
    }
    private static boolean check(Class<?> self, Recursive parent) {
        if (Tools.isBlank(parent)) {
            return false;
        } else if (parent.self == self) {
            return true;
        } else {
            return check(self, parent.parent);
        }
    }


    /**
     * <pre>
     * public class A { ... private A x; }
     * will return <span style="color:green">"A --&gt; A x"</span>
     *
     *
     * public class A { ... private B x; }
     * public class B { ... private A xx; }
     * will return <span style="color:green">"A --&gt; B x --&gt; A xx"</span>
     *
     *
     * public class A { ... private B x; }
     * public class B { ... private List&lt;C&gt; xx; }
     * public class C { ... private Map&lt;String, A&gt; xxx; }
     * will return <span style="color:green">"A --&gt; B x --&gt; C xx --&gt; A xxx --&gt; B x"</span>
     * </pre>
     *
     * @return orbit
     */
    public String getOrbit() {
        StringBuilder sbd = new StringBuilder();
        orbit(this, sbd);
        return sbd.toString();
    }
    private static void orbit(Recursive self, StringBuilder sbd) {
        if (Tools.isNotBlank(self)) {
            if (Tools.isNotBlank(self.parent)) {
                orbit(self.parent, sbd);
                sbd.append(" --> ");
            }
            sbd.append(self.self.getName());
            if (Tools.isNotBlank(self.fieldName)) {
                sbd.append(ApiConst.SPACE).append(self.fieldName);
            }
        }
    }


    private static Class<?> getGenericType(String str) {
        if (str.contains("<") && str.contains(">")) {
            Class<?> parent = getClass(str.substring(0, str.indexOf("<")));
            if (parent == null) {
                return getClass(str);
            } else {
                // List<XXX> or Map<String, XXX>
                String genericString = str.substring(str.indexOf("<") + 1, str.lastIndexOf(">"));
                if (Collection.class.isAssignableFrom(parent)) {
                    return getGenericType(genericString.trim());
                } else if (Map.class.isAssignableFrom(parent)) {
                    // Map use Value, ignore key
                    String[] keyValue = genericString.split(",");
                    if (keyValue.length == 2) {
                        return getGenericType(keyValue[1].trim());
                    }
                }
                return parent;
            }
        } else {
            return getClass(str);
        }
    }
    private static Class<?> getClass(String clazz) {
        try {
            String prefix = "class ";
            return Class.forName(clazz.startsWith(prefix) ? clazz.substring(prefix.length()) : clazz);
        } catch (ClassNotFoundException ignore) {
            return null;
        }
    }
}
