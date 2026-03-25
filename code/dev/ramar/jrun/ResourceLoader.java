package dev.ramar.jrun;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ResourceLoader
{

    public static byte[] ReadFile(String subpath) throws IOException
    { return ReadFileNear(null, subpath); }


    public static byte[] ReadFile(Class<?> anchor, String subpath) throws IOException
    { return ReadFileNear(anchor, subpath); }


    public static byte[] ReadFileNear(Class<?> anchor, String subpath) throws IOException
    {
        String cleanPath = CleanPath(subpath);

        if( anchor != null )
        {
            try (InputStream stream = anchor.getResourceAsStream(cleanPath))
            {
                if( stream != null )
                    return stream.readAllBytes();
            }

            Path classBinaryPath = GetClassBinaryPath(anchor);
            Path binaryFile = classBinaryPath.resolve(cleanPath).normalize();

            if( Files.isRegularFile(binaryFile) )
                return Files.readAllBytes(binaryFile);
        }
        else
        {
            if( IsRunningFromJar(ResourceLoader.class) )
            {
                try (InputStream stream = ResourceLoader.class.getResourceAsStream("/" + cleanPath))
                {
                    if( stream != null )
                        return stream.readAllBytes();
                }
            }
        }

        Path cwdFile = Paths.get("").toAbsolutePath().resolve(cleanPath).normalize();
        if( Files.isRegularFile(cwdFile) )
            return Files.readAllBytes(cwdFile);

        throw new IOException("File not found: " + subpath);
    }


    public static String ReadString(String subpath) throws IOException
    { return ReadString(null, subpath); }


    public static String ReadString(Class<?> anchor, String subpath) throws IOException
    { return new String(ReadFileNear(anchor, subpath), StandardCharsets.UTF_8); }


    public static Properties ReadProperties(String subpath) throws IOException
    { return ReadProperties(null, subpath); }


    public static Properties ReadProperties(Class<?> anchor, String subpath) throws IOException
    {
        byte[] data = ReadFileNear(anchor, subpath);

        Properties props = new Properties();

        try (InputStreamReader reader = new InputStreamReader(
            new ByteArrayInputStream(data),
            StandardCharsets.UTF_8
        ))
        {
            props.load(reader);
        }

        return props;
    }


    public static void LoadPropertiesIntoSystem(String subpath) throws IOException
    { LoadPropertiesIntoSystem(null, subpath); }


    public static void LoadPropertiesIntoSystem(Class<?> anchor, String subpath) throws IOException
    {
        Properties props = ReadProperties(anchor, subpath);

        for( String key : props.stringPropertyNames() )
            System.setProperty(key, props.getProperty(key));
    }


    public static Path GetClassBinaryPath(Class<?> anchor) throws IOException
    {
        Path binaryRoot = GetBinaryRoot(anchor);

        String packagePath = anchor.getPackageName().replace(".", "/");
        if( packagePath.isEmpty() )
            return binaryRoot;

        return binaryRoot.resolve(packagePath).normalize();
    }


    public static Path GetBinaryRoot(Class<?> anchor) throws IOException
    {
        try
        {
            Path location = Paths.get(
                anchor
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
            );

            if( Files.isRegularFile(location) )
                return location.getParent();

            return location.normalize();
        }
        catch (URISyntaxException ex)
        {
            throw new IOException("Failed to resolve binary root for " + anchor.getName(), ex);
        }
    }


    public static boolean IsRunningFromJar(Class<?> anchor) throws IOException
    {
        try
        {
            Path location = Paths.get(
                anchor
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
            );

            return Files.isRegularFile(location);
        }
        catch (URISyntaxException ex)
        {
            throw new IOException("Failed to resolve code source for " + anchor.getName(), ex);
        }
    }


    private static String CleanPath(String subpath)
    {
        return subpath
            .replace("\\", "/")
            .replaceFirst("^/+", "");
    }
}
