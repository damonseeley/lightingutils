package net.electroland.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * To do: document.  add validation handling.  log4j.  make it possible to
 *        parse a single line from a props file handed to this as two strings.
 * @author bradley
 *
 */

public class ElectrolandProperties {

    private static String ARG_MARKER = " $";

    // main is just a unit test
    public static void main(String args[]){
        Properties p = new Properties();
        p.put("dog.betty", "$type   yorkie  $attitude  0 $class net.electroland.utils.Dog $tags short,frantic");
        p.put("dog.sophie","$type mutt  $attitude  10 $class net.electroland.utils.Dog $tags tall,friendly,bites,men");
        p.put("cat.clay","$type tabby $class net.electroland.utils.Cat");
        p.put("cat.cool","$is t $was 0 $can false $ooh true");
        p.put("cat.blue","$multi \"\"");

        ElectrolandProperties op = new ElectrolandProperties(p);

        System.out.println(op.getDefault("cat","cool","isnt", "neat"));
        
        // get dog names
        Set<String> dogNames = op.getObjectNames("dog");
        for (String name : dogNames)
        {
            System.out.println(">" + name + "<");
        }

        System.out.println(op.getOptional("cat","blue","multi"));
        System.out.println(op.getRequiredBoolean("cat","cool","is"));
        System.out.println(op.getRequiredBoolean("cat","cool","was"));
        System.out.println(op.getRequiredBoolean("cat","cool","can"));
        System.out.println(op.getRequiredBoolean("cat","cool","ooh"));
        System.out.println(op.getOptionalBoolean("cat","cool","ah"));
        System.out.println(op.getOptionalBoolean("cat","cool","ooh"));
        
        // get all dogs (this is useful if you want to pass this subset
        // of the properties to a class that needs to access properties
        // for just dogs)
        Map <String,ParameterMap> dogs = op.getObjects("dog");
        // and tell me about Betty's type
        ParameterMap betty = dogs.get("betty");
        String type = betty.getRequired("type");
        System.out.println(">" + type + "<");

        // alternately
        String type2 = op.getRequired("dog", "betty", "type");
        System.out.println(">" + type2 + "<");

        // tags
        List<String> tags = op.getRequiredList("dog", "betty", "tags");
        System.out.println(tags);

        // load an instance of the class the represents betty
        Object c = op.getOptionalInt("dog", "betty", "attifftude");
        System.out.println(c);

    }

    //  objectType -> named_object -> parameters (key value pairs)
    private Map<String,Map<String,ParameterMap>> objects;

    /**
     * Take properties file provided by client
     * @param p
     */
    public ElectrolandProperties(Properties p)
    {
        this.objects = init(p);
    }

    /**
     * load resourceName from classpath
     * @param resourceName
     */
    public ElectrolandProperties(String resourceName)
    {
        Properties p = new Properties();

        InputStream is = new Util().getClass().getClassLoader().getResourceAsStream(resourceName);
        if (is != null)
        {
            try {
                p.load(is);
            } catch (IOException e) {
                throw new OptionException("Please make sure " + resourceName + " is in your classpath.");
            }
        }else{
            throw new OptionException("Please make sure " + resourceName + " is in your classpath.");
        }
        this.objects = init(p);
    }

    public static Map<String,Map<String,ParameterMap>> init(Properties p)
    {
        // java generics syntax is bullshit and a half!
        Hashtable<String,Map<String,ParameterMap>> objects 
                = new Hashtable<String,Map<String,ParameterMap>>();

        // get every named properties from the props file
        Enumeration <Object> g = p.keys();
        while (g.hasMoreElements())
        {
            // parse out the namespace from "objectType.name"
            String key = ("" + g.nextElement()).trim();
            
            // make sure there is at least one '.'?
            int endOfName = key.indexOf('.');
            if (endOfName == -1 || endOfName == key.length() - 1){
                throw new OptionException("object '" + key + "' requires a name.");
            }

            StringBuffer output = new StringBuffer();

            String objectType = key.substring(0,endOfName);
            output.append("objectType: '").append(objectType);
            String objectName = key.substring(endOfName + 1, key.length());
            output.append("', objectName: '").append(objectName);
            ParameterMap params = parse("" + p.get(key));
            output.append("', params: ").append(params);
            
            // see if the objectType exists or not:
            Map<String,ParameterMap> names = objects.get(objectType);
            if (names == null)
            {
                names = new Hashtable<String,ParameterMap>();
                objects.put(objectType, names);
            }

            names.put(objectName, params);
        }

        return objects;
    }

    /**
     * Ask for a type of object, and get the names of all instances of that
     * object.
     * 
     * @param objectType
     * @return
     * @throws OptionException
     */
    public Set<String> getObjectNames(String objectType)
    {
        Map<String,ParameterMap> type = objects.get(objectType);
        if (type == null){
            return Collections.<String>emptySet();
        }else{
            return type.keySet();
        }
    }

    public Map<String, ParameterMap> getObjects(String objectType)
    {
        Map<String,ParameterMap> type = objects.get(objectType);
        if (type == null){
            return Collections.emptyMap();
        }else{
            return type;
        }
    }

    // TODO: for backwards compatibility, add getAll?
    // PROBLEM: getAll() returned a map where the keys started with '$', so you'd
    // have to go massage all the keys before returning int.
    // Therefore: see if anyone is using getAll().  Probably better to just update those code bases.

    public ParameterMap getParams(String objectType, String objectName)
    {
        Map<String,ParameterMap> type = objects.get(objectType);
        if (type == null){
            throw new OptionException("no object of type '" + objectType + "' was found.");
        }else{
            ParameterMap params = type.get(objectName);
            if (params == null)
            {
                // TODO: ParameterMap.emptyMap
                throw new OptionException("no object named '" + objectName + "' of type '" + objectType + "' was found.");
            }else
            {
                return params;
            }
        }
    }

    // TODO: all of the methods below should add some debugging info to tell you which objectType and objectName
    // threw the OptionException
    public String getDefault(String objectType, String objectName, String paramName, String defaultVal)
    {
        return getParams(objectType, objectName).getDefault(paramName, defaultVal);
    }
    public String getOptional(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getOptional(paramName);
    }
    public String getRequired(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getRequired(paramName);
    }

    public Boolean getDefaultBoolean(String objectType, String objectName, String paramName, boolean defaultVal)
    {
        return getParams(objectType, objectName).getDefaultBoolean(paramName, defaultVal);
    }
    public Boolean getOptionalBoolean(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getOptionalBoolean(paramName);
    }
    public Boolean getRequiredBoolean(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getRequiredBoolean(paramName);
    }

    public Double getDefaultDouble(String objectType, String objectName, String paramName, double defaultValue)
    {
        return getParams(objectType, objectName).getDefaultDouble(paramName, defaultValue);
    }
    public Double getOptionalDouble(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getOptionalDouble(paramName);
    }
    public Double getRequiredDouble(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getRequiredDouble(paramName);
    }

    public Integer getDefaultInt(String objectType, String objectName, String paramName, int defaultVal)
    {        
        return getParams(objectType, objectName).getDefaultInt(paramName, defaultVal);
    }
    public Integer getOptionalInt(String objectType, String objectName, String paramName)
    {        
        return getParams(objectType, objectName).getOptionalInt(paramName);
    }
    public Integer getRequiredInt(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getRequiredInt(paramName);
    }

    public Object getOptionalClass(String objectType, String objectName, String paramName, Object defaultVal)
    {
        return getParams(objectType, objectName).getDefaultClass(paramName, defaultVal);
    }
    public Object getOptionalClass(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getOptionalClass(paramName);
    }
    public Object getRequiredClass(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getRequiredClass(paramName);
    }

    public List<String> getOptionalList(String objectType, String objectName, String paramName)
    {
        List<String> list = getParams(objectType, objectName).getOptionalList(paramName);
        return list == null ? Collections.<String>emptyList() : list;
    }

    public List<String> getRequiredList(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getRequiredList(paramName);
    }

    public List<Object> getOptionalClassList(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getOptionalClassList(paramName);
    }

    public List<Object> getRequiredClassList(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getRequiredClassList(paramName);
    }
    /**
     * Deprecated.  Use getOptionalList(...) for name clarity.
     * 
     * @deprecated
     * @param objectType
     * @param objectName
     * @param paramName
     * @return
     * @throws OptionException
     */
    public List<String> getOptionalArray(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getOptionalList(paramName);
    }

    /**
     * Deprecated.  Use getRequiredList(...) for name clarity.
     * @deprecated
     * @param objectType
     * @param objectName
     * @param paramName
     * @return
     * @throws OptionException
     */
    public List<String> getRequiredArray(String objectType, String objectName, String paramName)
    {
        return getParams(objectType, objectName).getRequiredList(paramName);
    }

    /**
     * This is a primitive parse for [-key val] style option variables in Strings.
     * For example, for the input:
     * 
     *     -key1 val1 -key2 val2 val3 -key3 val4
     * 
     * We'll return a Map with the following key value pairs:
     * 
     *     key        value
     *     ------    ---------
     *     -key1    val1
     *     -key2    val2 val3
     *     -key3    val4
     * 
     * WARNING: This parser knowingly converts all tabs to spaces, so any 
     *             arguments you pass it that contain tabs will be affected as such.
     * 
     * @param str
     * @return a Map of the keys and their values.
     * @throws OptionException if the string does not properly start with a flag. 
     */
    private static ParameterMap parse(String str)
    {
        ParameterMap map = new ParameterMap();
        if (str == null)
        {
            return map;
        }

        // all we are really doing is tokenizing on " -" (ARG_MARKER).  Then
        // splitting each token on the first space into a kehy and a value.

        // sorry.  no tabs.
        str.replace('\t', ' ');
        // make sure the first and last tokens are delimited.
        str = ' ' + str.trim() + ARG_MARKER; 

        int flagStart = str.indexOf(ARG_MARKER);

        // SPECIAL CASE: first token (or only token) doesn't start with "-".
        if (!str.startsWith(ARG_MARKER))
        {
            throw new OptionException("Unknown option " + 
                                        str.substring(1, flagStart));
        }

        int realEnd = str.length() - 2;
        while (true)
        {
            if (flagStart == realEnd) // out of tokens.
            {
                break;
            } else {
                int nextFlagStart = str.indexOf(ARG_MARKER, flagStart + 2);
                String tok = str.substring(flagStart, nextFlagStart).trim();
                int flagEnd = tok.indexOf(' ');
                if (flagEnd == -1){
                    map.put(tok, null); // SPECIAL CASE: flag has no value.
                }else{
                    map.put(tok.substring(1, flagEnd), 
                            tok.substring(flagEnd + 1, tok.length()).trim());
                    // at some point, we should parse the values attributed to
                    // each flag into an object here.
                }
                flagStart = nextFlagStart;
            }
        }
        return map;
    }
}