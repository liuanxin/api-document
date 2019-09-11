package com.github.liuanxin.api.model;

import com.github.liuanxin.api.util.Tools;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Recursive {

    private Recursive parent;

    private String fieldName;
    private Class<?> self;


    /**
     * <pre>
     * public class A { ... private A x; }
     *
     * OR
     *
     * public class A { ... private B x; }
     * public class B { ... private A xx; }
     *
     * OR
     *
     * public class A { ... private B x; }
     * public class B { ... private List&lt;C> xx; }
     * public class C { ... private Map&lt;String, A> xxx; }
     *
     * will return <span style="color:green">true</span>
     * </pre>
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
     * will return <span style="color:green">"A --> A x"</span>
     *
     *
     * public class A { ... private B x; }
     * public class B { ... private A xx; }
     * will return <span style="color:green">"A --> B x --> A xx"</span>
     *
     *
     * public class A { ... private B x; }
     * public class B { ... private List&lt;C> xx; }
     * public class C { ... private Map&lt;String, A> xxx; }
     * will return <span style="color:green">"A --> B x --> C xx --> A xxx --> B x"</span>
     * </pre>
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
                sbd.append(" ").append(self.fieldName);
            }
        }
    }
}
