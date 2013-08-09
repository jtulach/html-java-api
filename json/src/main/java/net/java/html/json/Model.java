/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package net.java.html.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;

/** Defines a model class that represents a single 
 * <a href="http://en.wikipedia.org/wiki/JSON">JSON</a>-like object
 * named {@link #className()}. The generated class contains
 * getters and setters for properties defined via {@link #properties()} and
 * getters for other, derived properties defined by annotating methods
 * of this class by {@link ComputedProperty}. Each property
 * can be of primitive type, an {@link Enum enum type} or (in order to create 
 * nested <a href="http://en.wikipedia.org/wiki/JSON">JSON</a> structure)
 * of another {@link Model class generated by @Model} annotation. Each property
 * can either represent a single value or be an array of its values.
 * <p>
 * The {@link #className() generated class}'s <code>toString</code> method
 * converts the state of the object into 
 * <a href="http://en.wikipedia.org/wiki/JSON">JSON</a> format. One can
 * use {@link Models#parse(net.java.html.BrwsrCtx, java.lang.Class, java.io.InputStream)}
 * method to read the JSON text stored in a file or other stream back into the Java model. 
 * One can also use {@link OnReceive @OnReceive} annotation to read the model
 * asynchronously from a {@link URL}.
 * <p>
 * An example where one defines class <code>Person</code> with four
 * properties (<code>firstName</code>, <code>lastName</code>, array of <code>addresses</code> and
 * <code>fullName</code>) follows:
 * <pre>
 * {@link Model @Model}(className="Person", properties={
 *   {@link Property @Property}(name = "firstName", type=String.class),
 *   {@link Property @Property}(name = "lastName", type=String.class)
 *   {@link Property @Property}(name = "addresses", type=Address.class, array = true)
 * })
 * static class PersonModel {
 *   {@link ComputedProperty @ComputedProperty}
 *   static String fullName(String firstName, String lastName) {
 *     return firstName + " " + lastName;
 *   }
 * 
 *   {@link Model @Model}(className="Address", properties={
 *     {@link Property @Property}(name = "street", type=String.class),
 *     {@link Property @Property}(name = "town", type=String.class)
 *   })
 *   static class AddressModel {
 *   }
 * }
 * </pre>
 * The generated model class has a default constructor, and also <em>quick
 * instantiation</em> constructor that accepts all non-array properties 
 * (in the order used in the {@link #properties()} attribute) and vararg list
 * for the first array property (if any). One can thus use following code
 * to create an instance of the Person and Address classes:
 * <pre>
 * 
 * Person p = new Person("Jaroslav", "Tulach",
 *   new Address("Markoušovice", "Úpice"),
 *   new Address("V Parku", "Praha")
 * );
 * // p.toString() then returns equivalent of following <a href="http://en.wikipedia.org/wiki/JSON">JSON</a> object
 * {
 *   "firstName" : "Jaroslav",
 *   "lastName" : "Tulach",
 *   "addresses" : [
 *     { "street" : "Markoušovice", "town" : "Úpice" },
 *     { "street" : "V Parku", "town" : "Praha" },
 *   ]
 * }
 * </pre>
 * <p>
 * In case you are using <a href="http://knockoutjs.com/">Knockout technology</a>
 * for Java then you can associate such model object with surrounding HTML page by
 * calling: <code>p.applyBindings();</code>. The page can then use regular
 * <a href="http://knockoutjs.com/">Knockout</a> bindings to reference your
 * model and create dynamic connection between your model in Java and 
 * live DOM structure in the browser:
 * <pre>
 * Name: &lt;span data-bind="text: fullName"&gt;
 * &lt;div data-bind="foreach: addresses"&gt;
 *   Lives in &lt;span data-bind="text: town"/&gt;
 * &lt;/div&gt;
 * </pre>
 * 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Model {
    /** Name of the model class */
    String className();
    /** List of properties in the model.
     */
    Property[] properties();
}
