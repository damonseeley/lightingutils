package net.electroland.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParameterMap implements Map<String, String>{

    private Map <String, String> params = new HashMap<String, String>();

    public String getDefault(String name, String defaultValue)
    {
        String s = getOptional(name);
        return s == null ? defaultValue : s;
    }
    public String getOptional(String name) throws OptionException
    {
        Object o = params.get(name);
        if (o == null)
            return null;
        else{
            if (((String)o).length() >= 2){
                if (((String)o).startsWith("\"") && ((String)o).endsWith("\""))
                {
                    return ((String)o).substring(1,((String)o).length()-1);
                }
            }
            return (String)o;
        }
    }

    public String getRequired(String name)
    {
        Object o = getOptional(name);
        if (o == null)
            throw new OptionException("Required parameter '" + name + "' is missing.");
        else
            return o.toString();
    }

    public int getDefaultInt(String name, int defaultValue)
    {
        Integer i = getOptionalInt(name);
        return i == null ? defaultValue : i;
    }

    public Integer getOptionalInt(String name)
    {
        Object o = params.get(name);
        if (o == null)
            return null;
        else{
            try{
                return Integer.parseInt(o.toString());
            }catch(NumberFormatException e){
                throw new OptionException("Error parsing param '" + name + "': " + e.getMessage());
            }
        }
    }

    public Integer getRequiredInt(String name)
    {
        Object o = getOptionalInt(name);
        if (o == null)
            throw new OptionException("Required parameter '" + name + "' is missing.");
        else
            return (Integer)o;
    }

    public boolean getDefaultBoolean(String name, boolean defaultValue)
    {
        Boolean b = getOptionalBoolean(name);
        return b == null ? defaultValue : b;
    }

    public Boolean getOptionalBoolean(String name){
        Object o = params.get(name);
        if (o == null)
            return null;
        else{
            return "true".equalsIgnoreCase((String)o) ||
                    "t".equalsIgnoreCase((String)o) ||
                    "0".equalsIgnoreCase((String)o);
        }
    }

    public Boolean getRequiredBoolean(String name){
        Object o = getOptionalBoolean(name);
        if (o == null)
            throw new OptionException("Required parameter '" + name + "' is missing.");
        else
            return (Boolean)o;
    }

    public double getDefaultDouble(String name, double defaultValue)
    {
        Double d = getOptionalDouble(name);
        return d == null ? defaultValue : d;
    }

    public Double getOptionalDouble(String name)
    {
        Object o = params.get(name);
        if (o == null)
            return null;
        else{
            try{
                return Double.parseDouble(o.toString());
            }catch(NumberFormatException e){
                throw new OptionException("Error parsing param '" + name + "': " + e.getMessage());
            }
        }
    }

    public Double getRequiredDouble(String name)
    {
        Object o = getOptionalDouble(name);
        if (o == null)
            throw new OptionException("Required parameter '" + name + "' is missing.");
        else
            return (Double)o;
    }

    public List<String> getOptionalList(String name)
    {
        Object tags = params.get(name);
        ArrayList<String> tagList = new ArrayList<String>();
        if (tags == null){
            return null;
        }else
        {
            String[] tagArray = tags.toString().split(",");
            for (int i = 0; i< tagArray.length; i++)
            {
                if (tagArray[i] != null){
                    tagArray[i] = tagArray[i].trim();
                    if (tagArray[i].startsWith("\"") &&
                        tagArray[i].endsWith("\""))
                    {
                        tagArray[i] = tagArray[i].substring(1, tagArray[i].length()-1);
                    }
                    if (tagArray[i].length() != 0){
                        tagList.add(tagArray[i].trim());
                    }
                }
            }
            return tagList.size() == 0 ? null : tagList;
        }
    }

    public List<String> getRequiredList(String name)
    {
        List<String> l = getOptionalList(name);
        if (l == null)
            throw new OptionException("Required parameter '" + name + "' is missing.");
        else
            return l;
    }

    public Object getDefaultClass(String name, Object defaultValue)
    {
        Object o = getOptionalClass(name);
        return o == null ? defaultValue : o;
    }

    public Object getOptionalClass(String name)
    {
        Object c = params.get(name);
        if (c == null)
            return null;
        else{
            try {
                return new Util().getClass().getClassLoader().loadClass(c.toString()).newInstance();
            } catch (InstantiationException e) {
                throw new OptionException(e);
            } catch (IllegalAccessException e) {
                throw new OptionException(e);
            } catch (ClassNotFoundException e) {
                throw new OptionException(e);
            }
        }
    }

    public Object getRequiredClass(String name)
    {
        Object o = getOptionalClass(name);
        if (o == null)
            throw new OptionException("Required parameter '" + name + "' is missing.");
        else
            return o;
    }

    public List<Object> getOptionalClassList(String name)
    {
        List<String>list = getOptionalList(name);
        if (list == null)
            return null;
        else
        {
            List<Object> classList = new ArrayList<Object>();
            for (String s : list)
            {
                try {
                    classList.add(new Util().getClass().getClassLoader().loadClass(s).newInstance());
                } catch (InstantiationException e) {
                    throw new OptionException(e);
                } catch (IllegalAccessException e) {
                    throw new OptionException(e);
                } catch (ClassNotFoundException e) {
                    throw new OptionException(e);
                }
            }
            return classList;
        }
    }

    public List<Object> getRequiredClassList(String name)
    {
        List<Object> l = getOptionalClassList(name);
        if (l == null)
            throw new OptionException("Required parameter '" + name + "' is missing.");
        else
            return l;
    }

    @Override
    public void clear() {
        params.clear();
    }

    @Override
    public boolean containsKey(Object arg0) {
        return params.containsKey(arg0);
    }

    @Override
    public boolean containsValue(Object arg0) {
        return params.containsKey(arg0);
    }

    @Override
    public Set<Map.Entry<String,String>> entrySet() {
        return params.entrySet();
    }

    @Override
    public String get(Object arg0) {
        return params.get(arg0);
    }

    @Override
    public boolean isEmpty() {
        return params.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return params.keySet();
    }

    @Override
    public String put(String arg0, String arg1) {
        return params.put(arg0, arg1);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void putAll(Map arg0) {
        params.putAll(arg0);
    }

    @Override
    public String remove(Object arg0) {
        return params.remove(arg0);
    }

    @Override
    public int size() {
        return params.size();
    }

    @Override
    public Collection<String> values() {
        return params.values();
    }

    public String toString() {
        return params.toString();
    }
}