package bc5151;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CWDictionary {
	public ArrayList<String> dictFull;
	public String[][] dictPart;
	ArrayList<String> ignore;
	TransformChar charMap;

	CWDictionary(String name){ 
		charMap=new TransformChar();
		dictPart=new String[30][];
		ignore = new ArrayList<String>();
		ignore.add(".*-.*");
                ignore.add(".*'.*");
		BufferedReader inputStream = null;
		dictFull = new ArrayList<String>();
		try {
			Reader reader = new InputStreamReader(new FileInputStream(name),
					"ISO-8859-1");
			inputStream = new BufferedReader(reader);
			String item;
			while ((item = inputStream.readLine()) != null) {
				addItem(item.toUpperCase());
			}
		} catch (UnsupportedEncodingException ex) {
                Logger.getLogger(CWDictionary.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CWDictionary.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CWDictionary.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
			if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException ex) {
                                Logger.getLogger(CWDictionary.class.getName()).log(Level.SEVERE, null, ex);
                            }
			}
		}
		for(int i=1;i<30;i++){
			ArrayList<String> l=new ArrayList<String>();
			for (String w:dictFull){
				if(w.length()==i)
					l.add(w);
			}
			dictPart[i]=new String[l.size()];
			l.toArray(dictPart[i]);
		}
	}

	public String[] getStrings() {
		return dictFull.toArray(new String[0]);
	}

	public void addItem(String item) {
		for (String block : ignore) {
			if(item.matches(block)){return;}
		}
		String str="";
		for(int i=0;i<item.length();i++){
			str+=Character.toString(charMap.getChar(item.charAt(i)));
		}
		dictFull.add(str);
	}
}
