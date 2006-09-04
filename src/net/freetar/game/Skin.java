/*
 * Copyright (C) 2006  Antonie Struyk
 *
 * This file is part of Freetar Hero.
 *
 *    Freetar Hero is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    Freetar Hero is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Freetar Hero; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.freetar.game;

import java.util.Enumeration;
import java.util.zip.ZipFile;
import net.freetar.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Anton
 */
public class Skin {
    private static Skin instance = new Skin();
    public static Skin getInstance(){
        return instance;
    }
    
    private Map<String, URL> loadedSkinResources;
    
    private Skin(){
        loadedSkinResources = new HashMap<String, URL>();
    }
    
    public URL getResource(String name){
        if(loadedSkinResources.containsKey(name)){
            return loadedSkinResources.get(name);
        }
        
        return this.getClass().getClassLoader().getResource(name);
    }
    
    public void setSkin(File skinFile){
        //Clear any old resources
        loadedSkinResources.clear();
        
        if(skinFile == null) return;
        
        //Open the skin ZipFile
        ZipFile zf = null;
        try {
            zf = new ZipFile(skinFile, ZipFile.OPEN_READ);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        //If the zipfile could not be opened return
        if(zf == null){
            return;
        }
        
        //Map the entries in the zip file to URL's describing them
        System.out.println("Entries: " + zf.size());
        Enumeration entries = zf.entries();
        while(entries.hasMoreElements()){
            ZipEntry currentEntry = (ZipEntry) entries.nextElement();
            try {
                loadedSkinResources.put(currentEntry.getName(),
                        new URL("jar:" + skinFile.toURL().toString() + "!/" + currentEntry.getName()));
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void verifySkinFile(){
    }
}
