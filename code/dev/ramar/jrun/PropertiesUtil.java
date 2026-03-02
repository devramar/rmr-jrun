package dev.ramar.jrun;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import java.util.stream.Stream;

import dev.ramar.iter.StreamAssist;

import dev.ramar.json.JObject;
import dev.ramar.json.JToken;

public final class PropertiesUtil
{
    private PropertiesUtil() { }


    public static Properties FromMap(Map<String, String> map)
    {
        Objects.requireNonNull(map, "map");

        Properties props = new Properties();

        for(Map.Entry<String, String> e : map.entrySet())
        {
            if( e.getKey() == null )
                continue;

            String val = e.getValue();
            if( val == null )
                val = "";

            props.setProperty(e.getKey(), val);
        }

        return props;
    }


    public static Map<String, String> ToMap(Properties props)
    {
        Objects.requireNonNull(props, "props");

        Map<String, String> map = new HashMap<>();

        for(String name : props.stringPropertyNames())
            map.put(name, props.getProperty(name));

        return map;
    }


    public static JObject ToJObject(Properties props)
    {
        Objects.requireNonNull(props, "props");

        Map<String, Object> map = new HashMap<>();

        for(String name : props.stringPropertyNames())
            map.put(name, props.getProperty(name));

        return JObject.From(map);
    }


    public static Properties FromJObject(JObject obj)
    {
        Objects.requireNonNull(obj, "obj");


        Properties props = new Properties();

        for(String key : obj.keys())
        {
            JToken tok = obj.get(key);
            String val = tok.tryString();

            if( val == null )
                continue;

            props.setProperty(key, val);
        }

        return props;
    }


    /// Deterministic conversion when you already know which keys you care about.
    public static Properties FromJObject(JObject obj, Set<String> keys)
    {
        Objects.requireNonNull(obj, "obj");
        Objects.requireNonNull(keys, "keys");

        Properties props = new Properties();

        Stream<String> outKeys = obj.keys().stream()
            .filter(key -> keys.contains(key))
        ;

        for(String key : StreamAssist.Wrap(outKeys))
        {
            String val = obj.get(key).toString();
            if( val == null )
                continue;

            props.setProperty(key, val);
        }

        return props;
    }

}