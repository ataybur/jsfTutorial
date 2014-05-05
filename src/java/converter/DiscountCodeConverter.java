/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package converter;

import derbyEntities.DiscountCode;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;

@Dependent
@Named(value = "discountCodeConverter")
@FacesConverter(value = "discountCodeConverter")
public class DiscountCodeConverter implements Converter, Serializable {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
       
        if (value == null || value.isEmpty()) {
            return null;
        }

        if (value.toString().equals("0")) {
            return null;
        }

        Object obj = null;

        ConverterHolder converterHolder = ConverterHolder.instance(context);

        Map<String, Object> map = converterHolder.getMap(component.getId());

        if (map != null) {
            obj = map.get(value);
        }


        return obj;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        ConverterHolder converterHolder = ConverterHolder.instance(context);
   
        Map<String, Object> map = converterHolder.getMap(component.getId());
        String str = "";

        

        if (value instanceof DiscountCode) {
            DiscountCode valueDiscountCode = (DiscountCode) value;
            str = valueDiscountCode.getClass().getName() + "_" + valueDiscountCode.getDiscountCode().toString();
        } else {
            if (value == null) {
                str = "----------";
            } else {
                str = "" + value.hashCode();
            }
        }

        if (map == null) {
            map = new HashMap<String, Object>();
        }
     

        map.put(str, value);
        converterHolder.addMap(component.getId(), map);
        return str;
    }

}
