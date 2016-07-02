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
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
package net.java.html.boot.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.java.JavaInterop;
import com.oracle.truffle.api.interop.java.MethodMessage;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.boot.spi.Fn.Presenter;

/**
 * Implementation of {@link Presenter} that delegates to Truffle.
 *
 * @author Jaroslav Tulach
 */
final class TrufflePresenter implements Fn.KeepAlive,
    Presenter, Fn.FromJavaScript, Fn.ToJavaScript, Executor {

    private Eval eval;
    private WrapArray copy;
    private final Executor exc;

    TrufflePresenter(Executor exc, TruffleObject eval) {
        this.exc = exc;
        this.eval = eval == null ? null : JavaInterop.asJavaFunction(Eval.class, eval);
    }

    @Override
    public Fn defineFn(String code, String... names) {
        return defineImpl(code, names, null);
    }

    @Override
    public Fn defineFn(String code, String[] names, boolean[] keepAlive) {
        return defineImpl(code, names, keepAlive);
    }

    private FnImpl defineImpl(String code, String[] names, boolean[] keepAlive) {
        StringBuilder sb = new StringBuilder();
        sb.append("(function() {\n");
        sb.append("  return function(");
        String sep = "";
        if (names != null) {
            for (String n : names) {
                sb.append(sep).append(n);
                sep = ",";
            }
        }
        sb.append(") {\n");
        sb.append(code);
        sb.append("\n  };\n");
        sb.append("})()\n");

        TruffleObject fn = (TruffleObject) getEval().eval(sb.toString());
        return new FnImpl(this, fn, names.length);
    }

    @Override
    public void displayPage(URL page, Runnable onPageLoad) {
        if (onPageLoad != null) {
            onPageLoad.run();
        }
    }

    @Override
    public void loadScript(Reader code) throws Exception {
        Source src = Source.fromReader(code, "unknown.js");
        getEval().eval(src.getCode());
    }

    interface IsArray {
        @MethodMessage(message = "HAS_SIZE")
        public boolean hasSize();
    }

    interface IsNull {
        @MethodMessage(message = "IS_NULL")
        public boolean isNull();
    }

    interface WrapArray {
        public Object copy(Object arr);
    }

    interface Eval {
        public Object eval(String code);
    }

    final Object checkArray(Object val) throws Exception {
        if (val instanceof TruffleObject) {
            final TruffleObject truffleObj = (TruffleObject)val;
            IsArray arrayTest = JavaInterop.asJavaObject(IsArray.class, truffleObj);
            try {
                if (arrayTest.hasSize()) {
                    List<?> list = JavaInterop.asJavaObject(List.class, truffleObj);
                    return list.toArray();
                }
            } catch (NegativeArraySizeException ex) {
                // swallow
            }
        }
        return val;
    }

    @Override
    public Object toJava(Object jsArray) {
        if (jsArray instanceof JavaValue) {
            jsArray = ((JavaValue) jsArray).get();
        }
        if (jsArray instanceof TruffleObject) {
            IsNull checkNull = JavaInterop.asJavaFunction(IsNull.class, (TruffleObject)jsArray);
            try {
                if (checkNull.isNull()) {
                    return null;
                }
            } catch (NegativeArraySizeException ex) {
                System.err.println("negative size for " + jsArray);
                ex.printStackTrace();
            }
        }
        try {
            return checkArray(jsArray);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Object toJavaScript(Object conv) {
        return JavaValue.toJavaScript(conv, getWrap());
    }

    @Override
    public void execute(final Runnable command) {
        if (Fn.activePresenter() == this) {
            command.run();
            return;
        }

        class Wrap implements Runnable {

            public void run() {
                try (Closeable c = Fn.activate(TrufflePresenter.this)) {
                    command.run();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        final Runnable wrap = new Wrap();
        if (exc == null) {
            wrap.run();
        } else {
            exc.execute(wrap);
        }
    }

    private Eval getEval() {
        if (eval == null) {
            try {
                PolyglotEngine engine = PolyglotEngine.newBuilder().build();
                TruffleObject fn = (TruffleObject) engine.eval(
                    Source.fromText("eval.bind(this)", "eval.js").withMimeType("text/javascript")
                ).get();
                eval = JavaInterop.asJavaFunction(Eval.class, fn);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return eval;
    }

    private WrapArray getWrap() {
        if (copy == null) {
            TruffleObject fn = (TruffleObject) getEval().eval("(function(arr) {\n"
                + "  var n = [];\n"
                + "  for (var i = 0; i < arr.length; i++) {\n"
                + "    n[i] = arr[i];\n"
                + "  }\n"
                + "  return n;\n"
                + "}).bind(this)"
            );
            copy = JavaInterop.asJavaFunction(WrapArray.class, fn);
        }
        return copy;
    }

    private class FnImpl extends Fn {

        private final CallTarget fn;
        private final boolean[] keepAlive;

        public FnImpl(Presenter presenter, TruffleObject fn, int arity) {
            super(presenter);
            this.fn = Truffle.getRuntime().createCallTarget(new FnRootNode(fn, arity));
            this.keepAlive = null;
        }

        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            List<Object> all = new ArrayList<>(args.length + 1);
            all.add(thiz == null ? fn : toJavaScript(thiz));
            for (int i = 0; i < args.length; i++) {
                Object conv = args[i];
                conv = toJavaScript(conv);
                all.add(conv);
            }
            Object ret = fn.call(all.toArray());
            if (ret instanceof JavaValue) {
                ret = ((JavaValue)ret).get();
            }
            if (ret == fn) {
                return null;
            }
            return toJava(ret);
        }
    }

    static abstract class JavaLang extends TruffleLanguage<Object> {
    }
}
