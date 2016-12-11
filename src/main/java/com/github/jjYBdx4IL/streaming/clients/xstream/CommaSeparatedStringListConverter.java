package com.github.jjYBdx4IL.streaming.clients.xstream;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author jjYBdx4IL
 */
public class CommaSeparatedStringListConverter implements SingleValueConverter {

    public static final String SEPARATOR = ",";
    
    @SuppressWarnings("rawtypes")
	@Override
    public boolean canConvert(Class clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    public Object fromString(String arg0) {
        Collection<String> collection = new ArrayList<>();
        String[] strings = arg0.split(SEPARATOR);
        for (int i = 0; i < strings.length; i++) {
            collection.add(strings[i]);
        }
        return collection;
    }

    @Override
    public String toString(Object arg0) {
        List<?> collection = (List<?>) arg0;
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Object object : collection) {
            if (first) {
                first = false;
            } else {
                sb.append(SEPARATOR);
            }
            sb.append(object.toString());
        }
        return sb.toString();
    }
}
