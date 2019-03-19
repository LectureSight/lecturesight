/*
 * JOCL - Java bindings for OpenCL
 *
 * Copyright (c) 2009-2012 Marco Hutter - http://www.jocl.org
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.jocl;

import java.io.*;
import java.util.Locale;

/**
 * Utility class for detecting the operating system and architecture
 * types, and automatically loading the matching native library
 * as a resource or from a file. <br />
 * <br />
 * The architecture and OS detection has been adapted from 
 * http://javablog.co.uk/2007/05/19/making-jni-cross-platform/
 * and extended with http://lopica.sourceforge.net/os.html 
 */
final class LibUtils
{
    /**
     * Enumeration of common operating systems, independent of version 
     * or architecture. 
     */
    public static enum OSType
    {
    	APPLE, LINUX, SUN, WINDOWS, UNKNOWN
    }
    
    /**
     * Enumeration of common CPU architectures.
     */
    public static enum ARCHType
    {
        PPC, PPC_64, SPARC, X86, X86_64, ARM, MIPS, RISC, UNKNOWN
    }
    
    /**
     * Loads the specified library. The full name of the library
     * is created by calling {@link LibUtils#createLibName(String)}
     * with the given argument. The method will attempt to load
     * the library using the usual System.loadLibrary call,
     * and, if this fails, it will try to load it as a as a 
     * resource (for usage within a JAR).
     *    
     * @param baseName The base name of the library
     * @throws UnsatisfiedLinkError if the native library 
     * could not be loaded.
     */
    public static void loadLibrary(String baseName)
    {
        String libName = LibUtils.createLibName(baseName);

        Throwable throwable = null;
        try
        {
            System.loadLibrary(libName);
            initNativeLibrary();
            return;
        }
        catch (Throwable t) 
        {
        	throwable = t;
        }
        
        try
        {
            loadLibraryResource(libName);
            initNativeLibrary();
        	return;
        }
        catch (Throwable t)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            
            pw.println("Error while loading native library \"" +
	        		libName + "\" with base name \""+baseName+"\"");
            pw.println("Operating system name: "+
	        		System.getProperty("os.name"));
            pw.println("Architecture         : "+
	        		System.getProperty("os.arch"));
            pw.println("Architecture bit size: "+
	        		System.getProperty("sun.arch.data.model"));
	        
            pw.println("---(start of nested stack traces)---");
            pw.println(
	            "Stack trace from the attempt to " +
	            "load the library as a file:");
	        throwable.printStackTrace(pw);
	        
	        pw.println(
        		"Stack trace from the attempt to " +
        		"load the library as a resource:");
	        t.printStackTrace(pw);
            pw.println("---(end of nested stack traces)---");
	        
	        pw.close();
	        throw new UnsatisfiedLinkError(sw.toString());
        }
    }
    
    
    /**
     * Initialize the native library by passing the name of the OpenCL
     * implementation to the {@link CL#initNativeLibrary(String)} 
     * method.
     * 
     * @throws UnsatisfiedLinkError If the implementation library 
     * could not be loaded.
     */
    private static void initNativeLibrary()
    {
        String implementationName = createImplementationName();
        boolean initialized = 
            CL.initNativeLibrary(implementationName);
        if (!initialized)
        {
            throw new UnsatisfiedLinkError(
                "Could not initialize native library. Implementation " +
                "library '"+implementationName+"' could not be loaded");
        }
    }
    
    /**
     * Create the name for the OpenCL implementation that will be passed 
     * to the dlopen/LoadLibrary call on native side. For Windows and
     * Linux, this will be the name of the OpenCL libary itself.
     * For MacOS, it will be the path to the OpenCL framework.
     * 
     * @return The name of the implementation library
     */
    private static String createImplementationName()
    {
        OSType osType = calculateOS();
        if (OSType.APPLE.equals(osType))
        {
            return "/System/Library/Frameworks/OpenCL.framework/" +
            		"Versions/Current/OpenCL";
        }
        return createFullName("OpenCL");
    }
    

    /**
     * Load the library with the given name from a resource. 
     * The extension for the current OS will be appended.
     * 
     * @param libName The library name
     * @throws Throwable If the library could not be loaded
     */
    private static void loadLibraryResource(String libName) throws Throwable
    {
        // Build the full name of the library 
        String fullNameWithExt = createFullName(libName);

    	// If a temporary file with the resulting name
    	// already exists, it can simply be loaded
        String tempDirName = System.getProperty("java.io.tmpdir");
        String tempFileName = tempDirName + File.separator + fullNameWithExt;
        File tempFile = new File(tempFileName);
        if (tempFile.exists())
        {
            //System.out.println("Loading from existing file: "+tempFile);
            System.load(tempFile.toString());
            return;
        }
    	
        // No file with the resulting name exists yet. Try to write
        // the library data from the JAR into the temporary file, 
        // and load the newly created file.
        String resourceName = "/lib/" + fullNameWithExt;
        InputStream inputStream = 
        	LibUtils.class.getResourceAsStream(resourceName);
        if (inputStream == null)
        {
        	throw new NullPointerException(
        			"No resource found with name '"+resourceName+"'");
        }
        OutputStream outputStream = null;
        try
        {
        	outputStream = new FileOutputStream(tempFile);
	        byte[] buffer = new byte[8192];
	        while (true)
	        {
	        	int read = inputStream.read(buffer);
	        	if (read < 0)
	        	{
	        		break;
	        	}
	        	outputStream.write(buffer, 0, read);	
	        }
	        outputStream.flush();
	        outputStream.close();
	        outputStream = null;
	        
            //System.out.println("Loading from newly created file: "+tempFile);
            System.load(tempFile.toString());
        }
        finally 
        {
        	if (outputStream != null)
        	{
        		outputStream.close();
        	}
        }
    }
    
    
    /**
     * Create the full library file name, including the extension
     * and prefix, for the given library name. For example, the
     * name 'JOCL' will become <br />
     * JOCL.dll on Windows <br />
     * libJOCL.so on Linux <br />
     * JOCL.dylib on MacOS <br />
     * 
     * @param libName The library name
     * @return The full library name, with extension
     */
    private static String createFullName(String libName)
    {
        String libPrefix = createLibPrefix();
        String libExtension = createLibExtension();
        String fullName = libPrefix + libName + "." + libExtension;
        return fullName;
    }


    /**
     * Returns the extension for dynamically linked libraries on the
     * current OS. That is, returns "dylib" on Apple, "so" on Linux
     * and Sun, and "dll" on Windows.
     * 
     * @return The library extension
     */
    private static String createLibExtension()
    {
        OSType osType = calculateOS();
        switch (osType) 
        {
            case APPLE:
                return "dylib";
            case LINUX:
                return "so";
            case SUN:
                return "so";
            case WINDOWS:
                return "dll";
            default:
                break;
        }
        return "";
    }

    /**
     * Returns the prefix for dynamically linked libraries on the
     * current OS. That is, returns "lib" on Apple, Linux and Sun, 
     * and the empty String on Windows.
     * 
     * @return The library prefix
     */
    private static String createLibPrefix()
    {
        OSType osType = calculateOS();
        switch (osType) 
        {
            case APPLE:
            case LINUX:
            case SUN:
                return "lib";
            case WINDOWS:
                return "";
            default:
                break;
        }
        return "";
    }
    
    
    /**
     * Creates the name for the native library with the given base
     * name for the current operating system and architecture.
     * The resulting name will be of the form<br />
     * baseName-OSType-ARCHType<br />
     * where OSType and ARCHType are the <strong>lower case</strong> Strings
     * of the respective enum constants. Example: <br />
     * JOCL-windows-x86<br /> 
     * 
     * @param baseName The base name of the library
     * @return The library name
     */
    public static String createLibName(String baseName)
    {
        OSType osType = calculateOS();
        ARCHType archType = calculateArch();
        String libName = baseName;
        libName += "-" + osType.toString().toLowerCase(Locale.ENGLISH);
        libName += "-" + archType.toString().toLowerCase(Locale.ENGLISH);
        return libName;
    }
    
    /**
     * Calculates the current OSType
     * 
     * @return The current OSType
     */
    public static OSType calculateOS()
    {
        String osName = System.getProperty("os.name");
        osName = osName.toLowerCase(Locale.ENGLISH);
        if (osName.startsWith("mac os"))
        {
            return OSType.APPLE;
        }
        if (osName.startsWith("windows"))
        {
            return OSType.WINDOWS;
        }
        if (osName.startsWith("linux"))
        {
            return OSType.LINUX;
        }
        if (osName.startsWith("sun"))
        {
            return OSType.SUN;
        }
        return OSType.UNKNOWN;
    }


    /**
     * Calculates the current ARCHType
     * 
     * @return The current ARCHType
     */
    public static ARCHType calculateArch()
    {
        String osArch = System.getProperty("os.arch");
        osArch = osArch.toLowerCase(Locale.ENGLISH);
        if (osArch.equals("i386") || 
            osArch.equals("x86")  || 
            osArch.equals("i686"))
        {
            return ARCHType.X86; 
        }
        if (osArch.startsWith("amd64") || osArch.startsWith("x86_64"))
        {
            return ARCHType.X86_64;
        }
        if (osArch.equals("ppc") || osArch.equals("powerpc"))
        {
            return ARCHType.PPC;
        }
        if (osArch.startsWith("ppc"))
        {
            return ARCHType.PPC_64;
        }
        if (osArch.startsWith("sparc"))
        {
            return ARCHType.SPARC;
        }
        if (osArch.startsWith("arm"))
        {
            return ARCHType.ARM;
        }
        if (osArch.startsWith("mips"))
        {
            return ARCHType.MIPS;
        }
        if (osArch.contains("risc"))
        {
            return ARCHType.RISC;
        }
        return ARCHType.UNKNOWN;
    }    

    /**
     * Private constructor to prevent instantiation.
     */
    private LibUtils()
    {
    }
}
