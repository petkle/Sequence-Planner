//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.05.25 at 01:59:34 fm CEST 
//


package sequenceplanner.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}operation" maxOccurs="unbounded"/>
 *         &lt;element name="operationData" type="{}operationData"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{}id use="required""/>
 *       &lt;attribute ref="{}name"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "operation",
    "operationData"
})
@XmlRootElement(name = "operation")
public class Operation {

    @XmlElement(required = true)
    protected List<Operation> operation;
    @XmlElement(required = true)
    protected OperationData operationData;
    @XmlAttribute(required = true)
    protected int id;
    @XmlAttribute
    protected String name;

    /**
     * Gets the value of the operation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the operation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOperation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Operation }
     * 
     * 
     */
    public List<Operation> getOperation() {
        if (operation == null) {
            operation = new ArrayList<Operation>();
        }
        return this.operation;
    }

    /**
     * Gets the value of the operationData property.
     * 
     * @return
     *     possible object is
     *     {@link OperationData }
     *     
     */
    public OperationData getOperationData() {
        return operationData;
    }

    /**
     * Sets the value of the operationData property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperationData }
     *     
     */
    public void setOperationData(OperationData value) {
        this.operationData = value;
    }

    /**
     * Gets the value of the id property.
     * 
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(int value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
