//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2014.02.04 um 12:22:03 PM CET 
//

package org.w3._2005._08.addressing;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

/**
 * <p>
 * Java-Klasse f�r AttributedUnsignedLongType complex type.
 * 
 * <p>
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * <complexType name="AttributedUnsignedLongType">
 *   <simpleContent>
 *     <extension base="<http://www.w3.org/2001/XMLSchema>unsignedLong">
 *       <anyAttribute processContents='lax' namespace='##other'/>
 *     </extension>
 *   </simpleContent>
 * </complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttributedUnsignedLongType", propOrder = { "value" })
public class AttributedUnsignedLongType {

	@XmlValue
	@XmlSchemaType(name = "unsignedLong")
	protected BigInteger value;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<QName, String>();

	/**
	 * Ruft den Wert der value-Eigenschaft ab.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getValue() {
		return value;
	}

	/**
	 * Legt den Wert der value-Eigenschaft fest.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setValue(BigInteger value) {
		this.value = value;
	}

	/**
	 * Gets a map that contains attributes that aren't bound to any typed property on this class.
	 * 
	 * <p>
	 * the map is keyed by the name of the attribute and the value is the string value of the attribute.
	 * 
	 * the map returned by this method is live, and you can add new attribute by updating the map directly. Because of this design, there's no setter.
	 * 
	 * 
	 * @return always non-null
	 */
	public Map<QName, String> getOtherAttributes() {
		return otherAttributes;
	}

}
