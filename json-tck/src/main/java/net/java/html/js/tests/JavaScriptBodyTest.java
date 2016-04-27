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
package net.java.html.js.tests;

import java.io.StringReader;
import java.util.Arrays;
import java.util.concurrent.Callable;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.json.tck.KOTest;

/**
 *
 * @author Jaroslav Tulach
 */
public class JavaScriptBodyTest {
    @KOTest public void sumTwoNumbers() {
        int res = Bodies.sum(5, 3);
        assertEquals(res, 8, "Expecting 8: " + res);
    }

    @KOTest public void sumFromCallback() {
        int res = Bodies.sumJS(5, 3);
        assertEquals(res, 8, "Expecting 8: " + res);
    }
    
    @KOTest public void accessJsObject() {
        Object o = Bodies.instance(10);
        int ten = Bodies.readIntX(o);
        assertEquals(ten, 10, "Expecting ten: " + ten);
    }

    @KOTest public void callWithNoReturnType() {
        Object o = Bodies.instance(10);
        Bodies.incrementX(o);
        int ten = Bodies.readIntX(o);
        assertEquals(ten, 11, "Expecting eleven: " + ten);
    }
    
    @KOTest public void callbackToRunnable() {
        R run = new R();
        Bodies.callback(run);
        assertEquals(run.cnt, 1, "Can call even private implementation classes: " + run.cnt);
    }
    
    private R asyncRun;
    @KOTest public void asyncCallbackToRunnable() throws InterruptedException {
        if (asyncRun == null) {
            asyncRun = new R();
            Bodies.asyncCallback(asyncRun);
        }
        if (asyncRun.cnt == 0) {
            throw new InterruptedException();
        }
        assertEquals(asyncRun.cnt, 1, "Even async callback must arrive once: " + asyncRun.cnt);
    }

    @KOTest public void asyncCallbackFlushed() throws InterruptedException {
        R r = new R();
        for (int i = 0; i < 10; i++) {
            Bodies.asyncCallback(r);
        }
        int fourtyTwo = Bodies.sum(35, 7);
        assertEquals(r.cnt, 10, "Ten calls: " + r.cnt);
        assertEquals(fourtyTwo, 42, "Meaning of the world expected: " + fourtyTwo);
    }
    
    @KOTest public void typeOfCharacter() {
        String charType = Bodies.typeof('a', false);
        assertEquals("number", charType, "Expecting number type: " + charType);
    }
    @KOTest public void typeOfBoolean() {
        String booleanType = Bodies.typeof(true, false);
        assertEquals("boolean", booleanType, "Expecting boolean type: " + booleanType);
    }

    @KOTest public void typeOfPrimitiveBoolean() {
        String booleanType = Bodies.typeof(true);
        assertTrue("boolean".equals(booleanType) || "number".equals(booleanType), 
            "Expecting boolean or at least number type: " + booleanType);
    }

    @KOTest public void typeOfInteger() {
        String intType = Bodies.typeof(1, false);
        assertEquals("number", intType, "Expecting number type: " + intType);
    }

    @KOTest public void typeOfString() {
        String strType = Bodies.typeof("Ahoj", false);
        assertEquals("string", strType, "Expecting string type: " + strType);
    }

    @KOTest public void typeOfDouble() {
        String doubleType = Bodies.typeof(0.33, false);
        assertEquals("number", doubleType, "Expecting number type: " + doubleType);
    }
    
    @KOTest public void typeOfBooleanValueOf() {
        String booleanType = Bodies.typeof(true, true);
        assertEquals("boolean", booleanType, "Expecting boolean type: " + booleanType);
    }

    @KOTest public void typeOfIntegerValueOf() {
        String intType = Bodies.typeof(1, true);
        assertEquals("number", intType, "Expecting number type: " + intType);
    }

    @KOTest public void typeOfStringValueOf() {
        String strType = Bodies.typeof("Ahoj", true);
        assertEquals("string", strType, "Expecting string type: " + strType);
    }

    @KOTest public void typeOfDoubleValueOf() {
        String doubleType = Bodies.typeof(0.33, true);
        assertEquals("number", doubleType, "Expecting number type: " + doubleType);
    }

    @KOTest public void computeInARunnable() {
        final int[] sum = new int[2];
        class First implements Runnable {
            @Override public void run() {
                sum[0] = Bodies.sum(22, 20);
                sum[1] = Bodies.sum(32, 10);
            }
        }
        Bodies.callback(new First());
        assertEquals(sum[0], 42, "Computed OK " + sum[0]);
        assertEquals(sum[1], 42, "Computed OK too: " + sum[1]);
    }
    
    @KOTest public void doubleCallbackToRunnable() {
        final R run = new R();
        final R r2 = new R();
        class First implements Runnable {
            @Override public void run() {
                Bodies.callback(run);
                Bodies.callback(r2);
            }
        }
        Bodies.callback(new First());
        assertEquals(run.cnt, 1, "Can call even private implementation classes: " + run.cnt);
        assertEquals(r2.cnt, 1, "Can call even private implementation classes: " + r2.cnt);
    }
    
    @KOTest public void identity() {
        Object p = new Object();
        Object r = Bodies.id(p);
        assertEquals(r, p, "The object is the same");
    }

    @KOTest public void encodingString() {
        Object p = "Ji\n\"Hi\"\nHon";
        Object r = Bodies.id(p);
        assertEquals(p, r, "The object is the same: " + p + " != " + r);
    }

    @KOTest public void encodingBackslashString() {
        Object p = "{\"firstName\":\"/*\\n * Copyright (c) 2013\",\"lastName\":null,\"sex\":\"MALE\",\"address\":{\"street\":null}}";
        Object r = Bodies.id(p);
        assertEquals(p, r, "The object is the same: " + p + " != " + r);
    }

    @KOTest public void nullIsNull() {
        Object p = null;
        Object r = Bodies.id(p);
        assertEquals(r, p, "The null is the same");
    }
    
    @KOTest public void callbackWithTrueResult() {
        Callable<Boolean> c = new C(true);
        String b = Bodies.yesNo(c);
        assertEquals(b, "yes", "Should return true");
    }

    @KOTest public void callbackWithFalseResult() {
        Callable<Boolean> c = new C(false);
        String b = Bodies.yesNo(c);
        assertEquals(b, "no", "Should return false");
    }
    
    @KOTest public void callbackWithParameters() throws InterruptedException {
        Sum s = new Sum();
        int res = Bodies.sumIndirect(s, 40, 2);
        assertEquals(res, 42, "Expecting 42");
    }
    
    @KOTest public void selectFromStringJavaArray() {
        String[] arr = { "Ahoj", "Wo\nrld" };
        Object res = Bodies.select(arr, 1);
        assertEquals("Wo\nrld", res, "Expecting World, but was: " + res);
    }

    @KOTest public void selectFromObjectJavaArray() {
        Object[] arr = { new Object(), new Object() };
        Object res = Bodies.select(arr, 1);
        assertEquals(arr[1], res, "Expecting " + arr[1] + ", but was: " + res);
    }

    @KOTest public void lengthOfJavaArray() {
        String[] arr = { "Ahoj", "World" };
        int res = Bodies.length(arr);
        assertEquals(res, 2, "Expecting 2, but was: " + res);
    }

    @KOTest public void isJavaArray() {
        String[] arr = { "Ahoj", "World" };
        boolean is = Bodies.isArray(arr);
        assertTrue(is, "Expecting it to be an array: " + is);
    }

    @KOTest public void javaArrayInOutIsCopied() {
        String[] arr = { "Ahoj", "Wo\nrld" };
        Object res = Bodies.id(arr);
        assertNotNull(res, "Non-null is returned");
        assertTrue(res instanceof Object[], "Returned an array: " + res);
        assertFalse(res instanceof String[], "Not returned a string array: " + res);
        
        Object[] ret = (Object[]) res;
        assertEquals(arr.length, ret.length, "Same length: " + ret.length);
        assertEquals(arr[0], ret[0], "Same first elem");
        assertEquals(arr[1], ret[1], "Same 2nd elem");
    }

    @KOTest public void modifyJavaArrayHasNoEffect() {
        String[] arr = { "Ah\noj", "World" };
        String value = Bodies.modify(arr, 0, "H\tello");
        assertEquals("H\tello", value, "Inside JS the value is changed: " + value);
        assertEquals("Ah\noj", arr[0], "From a Java point of view it remains: " + arr[0]);
    }

    @KOTest
    public void callbackWithArray() {
        class A implements Callable<String[]> {
            @Override
            public String[] call() throws Exception {
                return new String[] { "He\nllo" };
            }
        }
        Callable<String[]> a = new A();
        Object b = Bodies.callbackAndPush(a, "Worl\nd!");
        assertTrue(b instanceof Object[], "Returns an array: " + b);
        Object[] arr = (Object[]) b;
        String str = Arrays.toString(arr);
        assertEquals(arr.length, 2, "Size is two " + str);
        assertEquals("He\nllo", arr[0], "Hello expected: " + arr[0]);
        assertEquals("Worl\nd!", arr[1], "World! expected: " + arr[1]);
    }
    
    @KOTest public void sumVector() {
        double[] arr = { 1.0, 2.0, 3.0 };
        double res = Bodies.sumVector(arr);
        assertEquals(6.0, res, "Expecting six: " + res);
    }

    @KOTest public void sumMatrix() {
        double[][] arr = { { 1.0 }, { 1.0, 1.0 }, { 1.0, 1.0, 1.0 } };
        double res = Bodies.sumMatrix(arr);
        assertEquals(6.0, res, "Expecting six: " + res);
    }

    @KOTest public void truth() {
        assertTrue(Bodies.truth(), "True is true");
    }
    
    @KOTest public void factorial2() {
        assertEquals(new Factorial().factorial(2), 2);
    }
    
    @KOTest public void factorial3() {
        assertEquals(new Factorial().factorial(3), 6);
    }
    
    @KOTest public void factorial4() {
        assertEquals(new Factorial().factorial(4), 24);
    }
    
    @KOTest public void factorial5() {
        assertEquals(new Factorial().factorial(5), 120);
    }
    
    @KOTest public void factorial6() {
        assertEquals(new Factorial().factorial(6), 720);
    }
    
    @KOTest public void sumArray() {
        int r = Bodies.sumArr(new Sum());
        assertEquals(r, 6, "Sum is six: " + r);
    }
    
    @KOTest public void staticCallback() {
        int r = Bodies.staticCallback();
        assertEquals(r, 42, "Expecting 42: " + r);
    }

    @KOTest public void delayCallback() {
        Object fn = Bodies.delayCallback();
        Object r = Bodies.invokeFn(fn);
        assertNotNull(r, "Is not null");
        assertTrue(r instanceof Number, "Is number " + r);
        assertEquals(((Number)r).intValue(), 42, "Expecting 42: " + r);
    }
    
    @KOTest public void asyncCallFromAJSCallbackNeedToFinishBeforeReturnToJS() {
        int r = Bodies.incAsync();
        assertEquals(r, 42, "Expecting 42: " + r);
    }
    
    @KOTest public void iterateArray() {
        String[] arr = { "Ahoj", "Hi", "Ciao" };
        Object[] ret = Bodies.forIn(arr);
        assertEquals(ret.length, 3, "Three elements returned: " + ret.length);
        assertNotEquals(ret, arr, "Different arrays");
        assertEquals(ret[0], "Ahoj", "Expecting Ahoj: " + ret[0]);
        assertEquals(ret[1], "Hi", "Expecting Hi: " + ret[1]);
        assertEquals(ret[2], "Ciao", "Expecting Ciao: " + ret[2]);
    }
    
    @KOTest public void primitiveTypes() {
        String all = Bodies.primitiveTypes(new Sum());
        assertEquals("Ahojfalse12356.07.0 TheEND", all, "Valid return type: " + all);
    }

    @KOTest public void returnUnknown() {
        Object o = Bodies.unknown();
        assertNull(o, "Unknown is converted to null");
    }

    @KOTest public void returnUndefinedString() {
        Object o = Bodies.id("undefined");
        assertNotNull(o, "String remains string");
    }

    @KOTest public void returnUnknownArray() {
        Object[] arr = Bodies.unknownArray();
        assertEquals(arr.length, 2, "Two elements");
        assertNull(arr[0], "1st element is null");
        assertNull(arr[1], "2nd element is null");
    }

    @KOTest public void callbackKnown() {
        Sum s = new Sum();
        boolean nonNull = Bodies.nonNull(s, "x");
        assertTrue(nonNull, "x property exists");
    }
    
    @KOTest public void callbackUnknown() {
        Sum s = new Sum();
        boolean isNull = Bodies.nonNull(s, "y");
        assertTrue(isNull, "y property doesn't exist");
    }

    @KOTest public void callbackUnknownArray() {
        Sum s = new Sum();
        int nullAndUnknown = Bodies.sumNonNull(s);
        assertEquals(nullAndUnknown, 1, "Only one slot");
    }

    @KOTest public void problematicString() {
        String orig = Bodies.problematicString();
        String js = Bodies.problematicCallback();
        if (orig.equals(js)) {
            return;
        }
        int len = Math.min(orig.length(), js.length());
        for (int i = 0; i < len; i++) {
            if (orig.charAt(i) != js.charAt(i)) {
                fail("Difference at position " + i + 
                    "\norig: " +
                    orig.substring(i - 5, Math.min(i + 10, orig.length())) +
                    "\n  js: " +
                    js.substring(i - 5, Math.min(i + 10, js.length())));
            }
        }
        fail("The JS string is different: " + js);
    }
    
    Later l;
    @KOTest public void callLater() throws Exception{
        final Fn.Presenter p = Fn.activePresenter();
        if (p == null) {
            return;
        }
        if (l == null) {
            p.loadScript(new StringReader(
                "if (typeof window === 'undefined') window = {};"
            ));
            l = new Later();
            l.register();
            p.loadScript(new StringReader(
                "window.later();"
            ));
        }
        if (l.call != 42) {
            throw new InterruptedException();
        }
        assertEquals(l.call, 42, "Method was called: " + l.call);
    }

    @KOTest
    public void globalStringAvailable() throws Exception {
        GlobalString.init();
        assertEquals("HTML/Java", Bodies.readGlobalString());
    }
    
    private static class R implements Runnable {
        int cnt;
        private final Thread initThread;
        
        public R() {
            initThread = Thread.currentThread();
        }

        @Override
        public void run() {
            assertEquals(initThread, Thread.currentThread(), "Expecting to run in " + initThread + " but running in " + Thread.currentThread());
            cnt++;
        }
    }
    
    private static class C implements Callable<Boolean> {
        private final boolean ret;

        public C(boolean ret) {
            this.ret = ret;
        }
        
        @Override
        public Boolean call() throws Exception {
            return ret;
        }
    }
    static void assertEquals(Object a, Object b, String msg) {
        if (a == b) {
            return;
        }
        if (a != null && a.equals(b)) {
            return;
        }
        throw new AssertionError(msg);
    }
    private static void assertNotEquals(Object a, Object b, String msg) {
        if (a == null) {
            if (b == null) {
                throw new AssertionError(msg);
            }
            return;
        }
        if (a.equals(b)) {
            throw new AssertionError(msg);
        }
    }
    static void assertEquals(Object a, Object b) {
        if (a == b) {
            return;
        }
        if (a != null && a.equals(b)) {
            return;
        }
        throw new AssertionError("Expecting " + b + " but found " + a);
    }
    private static void fail(String msg) {
        throw new AssertionError(msg);
    }

    private static void assertTrue(boolean c, String msg) {
        if (!c) {
            throw new AssertionError(msg);
        }
    }

    private static void assertFalse(boolean c, String msg) {
        if (c) {
            throw new AssertionError(msg);
        }
    }

    private static void assertNull(Object o, String msg) {
        if (o != null) {
            throw new AssertionError(msg);
        }
    }

    static void assertNotNull(Object o, String msg) {
        if (o == null) {
            throw new AssertionError(msg);
        }
    }
}
