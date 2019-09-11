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

    private Class<?> self;

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
        }
    }
}
