//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2014.02.04 um 12:22:03 PM CET 
//

package org.w3._2005._08.addressing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

/**
 * <p>
 * Java-Klasse f�r EndpointReferenceType complex type.
 * 
 * <p>
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * <complexType name="EndpointReferenceType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="Address" type="{http://www.w3.org/2005/08/addressing}AttributedURIType"/>
 *         <element name="ReferenceParameters" type="{http://www.w3.org/2005/08/addressing}ReferenceParametersType" minOccurs="0"/>
 *         <element ref="{http://www.w3.org/2005/08/addressing}Metadata" minOccurs="0"/>
 *         <any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <anyAttribute processContents='lax' namespace='##other'/>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EndpointReferenceType", propOrder = { "address", "referenceParameters", "metadata", "any" })
public class EndpointReferenceType {

	@XmlElement(name = "Address", required = true)
	protected AttributedURIType address;
	@XmlElement(name = "ReferenceParameters")
	protected ReferenceParametersType referenceParameters;
	@XmlElement(name = "Metadata")
	protected MetadataType metadata;
	@XmlAnyElement(lax = true)
	protected List<Object> any;
	@XmlAnyAttribute
	private Map<QName, String> otherAttributes = new HashMap<QName, String>();

	/**
	 * Ruft den Wert der address-Eigenschaft ab.
	 * 
	 * @return possible object is {@link AttributedURIType }
	 * 
	 */
	public AttributedURIType getAddress() {
		return address;
	}

	/**
	 * Legt den Wert der address-Eigenschaft fest.
	 * 
	 * @param value
	 *            allowed object is {@link AttributedURIType }
	 * 
	 */
	public void setAddress(AttributedURIType value) {
		this.address = value;
	}

	/**
	 * Ruft den Wert der referenceParameters-Eigenschaft ab.
	 * 
	 * @return possible object is {@link ReferenceParametersType }
	 * 
	 */
	public ReferenceParametersType getReferenceParameters() {
		return referenceParameters;
	}

	/**
	 * Legt den Wert der referenceParameters-Eigenschaft fest.
	 * 
	 * @param value
	 *            allowed object is {@link ReferenceParametersType }
	 * 
	 */
	public void setReferenceParameters(ReferenceParametersType value) {
		this.referenceParameters = value;
	}

	/**
	 * Ruft den Wert der metadata-Eigenschaft ab.
	 * 
	 * @return possible object is {@link MetadataType }
	 * 
	 */
	public MetadataType getMetadata() {
		return metadata;
	}

	/**
	 * Legt den Wert der metadata-Eigenschaft fest.
	 * 
	 * @param value
	 *            allowed object is {@link MetadataType }
	 * 
	 */
	public void setMetadata(MetadataType value) {
		this.metadata = value;
	}

	/**
	 * Gets the value of the any property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present inside the JAXB object.
	 * This is why there is not a <CODE>set</CODE> method for the any property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAny().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Element } {@link Object }
	 * 
	 * 
	 */
	public List<Object> getAny() {
		if (any == null) {
			any = new ArrayList<Object>();
		}
		return this.any;
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
