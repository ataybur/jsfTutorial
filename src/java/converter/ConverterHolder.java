package converter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@SessionScoped
public class ConverterHolder implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Map<String, Object>> hashCode2ValueMapList = new HashMap<String, Map<String, Object>>();

    public ConverterHolder() {
	super();
    }

    public Map<String, Map<String, Object>> getHashCode2ValueMapList() {
	return this.hashCode2ValueMapList;
    }

    public void setHashCode2ValueMapList(Map<String, Map<String, Object>> hashCode2ValueMapList) {
	this.hashCode2ValueMapList = hashCode2ValueMapList;
    }

    public void addMap(String key, Map<String, Object> hashCode2ValueMap) {
	this.hashCode2ValueMapList.put(key, hashCode2ValueMap);
    }

    public Map<String, Object> getMap(String key) {
	return this.hashCode2ValueMapList.get(key);
    }

    public static ConverterHolder instance(FacesContext context) {
	// ELContext elContext = context.getELContext( );
	Application application = context.getApplication();
	return application.evaluateExpressionGet(context, "#{converterHolder}", ConverterHolder.class);
    }

}
