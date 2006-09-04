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
package net.freetar.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Anton
 */
public class FileUtils {
    
    public static String getExtension(File f){
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    
    public static FileFilter createFileFilter(String fileExtensions, String description){
        return new CustomFileFilter(fileExtensions, description);
    }
    
    private static class CustomFileFilter extends FileFilter{
        private String[] fileExtensions;
        private String description;
        
        public CustomFileFilter(String fileExtensions, String description){
            this.fileExtensions = fileExtensions.split(",");
            this.description = description;
        }
        
        public boolean accept(File f) {
            if(f.isDirectory()) return true;
            
            String ext = getExtension(f);
            for(String acceptableExtension : fileExtensions){
                if(ext != null && ext.equalsIgnoreCase(acceptableExtension.trim())){
                    return true;
                }
            }
            return false;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public static String readLine(BufferedReader in) throws IOException {
        String nextLine = null;
        while((nextLine = in.readLine()) != null){
            nextLine = nextLine.trim();
            if(nextLine.length() > 0 && !nextLine.matches("^//.*")){
                return nextLine;
            }
        }
        return null;
    }
    
    public static void writeLine(String line, BufferedWriter out) throws IOException{
        out.write(line);
        out.newLine();
    }
}
