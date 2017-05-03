/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2016 Oracle. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.html.context.impl;

import net.java.html.BrwsrCtx;
import org.netbeans.html.context.spi.Contexts;

/** Implementation detail. Holds list of technologies for particular
 * {@link BrwsrCtx}.
 *
 * @author Jaroslav Tulach
 */
public final class CtxImpl {
    private Bind<?>[] techs;
    private final Object[] context;
    
    public CtxImpl(Object[] context) {
        this(context, new Bind<?>[0]);
    }
    
    private CtxImpl(Object[] context, Bind<?>[] techs) {
        this.techs = techs;
        this.context = context;
    }
    
    public static <Tech> Tech find(BrwsrCtx context, Class<Tech> technology) {
        CtxImpl impl = CtxAccssr.getDefault().find(context);
        for (Bind<?> bind : impl.techs) {
            if (technology == bind.clazz) {
                return technology.cast(bind.impl);
            }
        }
        return null;
    }

    public BrwsrCtx build() {
        new BindCompare().sort(techs);
        CtxImpl impl = new CtxImpl(context, techs.clone());
        BrwsrCtx ctx = CtxAccssr.getDefault().newContext(impl);
        return ctx;
    }

    public <Tech> void register(Class<Tech> type, Tech impl, int priority) {
        techs = new BindCompare().add(techs, new Bind<Tech>(type, impl, priority));
    }
    
    private static final class Bind<Tech> {
        private final Class<Tech> clazz;
        private final Tech impl;
        private final int priority;

        public Bind(Class<Tech> clazz, Tech impl, int priority) {
            this.clazz = clazz;
            this.impl = impl;
            this.priority = priority;
        }

        @Override
        public String toString() {
            return "Bind{" + "clazz=" + clazz + "@" + clazz.getClassLoader() + ", impl=" + impl + ", priority=" + priority + '}';
        }
    }
    
    private final class BindCompare {

        void sort(Bind<?>[] techs) {
            for (int i = 0; i < techs.length; i++) {
                Bind<?> min = techs[i];
                int minIndex = i;
                for  (int j = i + 1; j < techs.length; j++) {
                    if (compare(min, techs[j]) > 0) {
                        min = techs[j];
                        minIndex = j;
                    }
                }
                if (minIndex != i) {
                    techs[minIndex] = techs[i];
                    techs[i] = min;
                }
            }
        }

        private <Tech> Bind<?>[] add(Bind<?>[] techs, Bind<Tech> bind) {
            Bind<?>[] newArr = new Bind<?>[techs.length + 1];
            for (int i = 0; i < techs.length; i++) {
                newArr[i] = techs[i];
            }
            newArr[techs.length] = bind;
            return newArr;
        }
        private boolean isPrefered(Bind<?> b) {
            final Class<?> implClazz = b.impl.getClass();
            Contexts.Id id = implClazz.getAnnotation(Contexts.Id.class);
            if (id == null) {
                return false;
            }
            for (String v : id.value()) {
                for (Object c : context) {
                    if (v.equals(c)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        private int compare(Bind<?> o1, Bind<?> o2) {
            boolean p1 = isPrefered(o1);
            boolean p2 = isPrefered(o2);
            if (p1 != p2) {
                return p1 ? -1 : 1;
            }
            if (o1.priority != o2.priority) {
                return o1.priority - o2.priority;
            }
            return o1.clazz.getName().compareTo(o2.clazz.getName());
        }
    } // end of BindCompare
}
