package dev.ramar.jrun;

import java.io.IOException;
import java.io.InputStream;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public final class JRunMeta
{
    private static final String RESOURCE = "/jrunner.properties";
    private static Properties instance = null;
    public static Properties instance()
    {
        synchronized(JRunMeta.class)
        {
            if( instance == null )
                instance = Load();

            return instance;
        }
    }

    public static String Get(String key)
    {
        return Get(key, null);
    }
    
    public static String Get(String key, String defaultValue)
    {
        Properties instance = instance();
        if( instance == null )
            return null;

        return instance.getProperty(key, defaultValue);
    }


    private JRunMeta() 
    {}



    public static Properties Load()
    {
        try(InputStream in = JRunMeta.class.getResourceAsStream(RESOURCE))
        {
            if(in == null)
                return null;

            Properties props = new Properties();
            props.load(in);
            return props;
        }
        catch(IOException ex)
        {
            return null;
        }
    }


    public static Map<String, String> LoadMap()
    {
        Properties props = Load();
        if(props == null)
            return null;

        Map<String, String> map = new HashMap<>();

        for(String name : props.stringPropertyNames())
            map.put(name, props.getProperty(name));

        return Collections.unmodifiableMap(map);
    }
}