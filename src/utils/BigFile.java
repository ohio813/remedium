/*
 * Class imported from the Internet
 */
package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;

/**
 *
 * @author Nuno Brito, 29th of July 2011 in Darmstadt, Germany
 */
public class BigFile implements Iterable<String>
{
    private BufferedReader _reader;
 
    public BigFile(File filePath) throws Exception
    {
	_reader = new BufferedReader(new FileReader(filePath));
    }
 
    public void Close()
    {
	try
	{
	    _reader.close();
	}
	catch (Exception ex) {}
    }
 
    public Iterator<String> iterator()
    {
	return new FileIterator();
    }
 
    private class FileIterator implements Iterator<String>
    {
	private String _currentLine;
 
	public boolean hasNext()
	{
	    try
	    {
		_currentLine = _reader.readLine();
	    }
	    catch (Exception ex)
	    {
		_currentLine = null;
	    }
 
	    return _currentLine != null;
	}
 
	public String next()
	{
	    return _currentLine;
	}
 
	public void remove()
	{
	}
    }
}


//BigFile file = new BigFile("C:\Temp\BigFile.txt");
// 
//for (String line : file)
//    System.out.println(line)