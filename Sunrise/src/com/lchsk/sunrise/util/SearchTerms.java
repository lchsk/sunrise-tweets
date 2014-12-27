package com.lchsk.sunrise.util;

public class SearchTerms
{
    private String[] array;
    private int words;
    private int index;
    
    public SearchTerms(String[] p_arr, int p_words)
    {
        array = p_arr;
        words = p_words;
        reset();
    }
    
    public String next()
    {
        StringBuilder sb = new StringBuilder();
        
        for (int i = index, j = 0; i < array.length; i++, j++)
        {
            if (j >= words)
                break;
            
            sb.append(" ").append(array[i]);
        }
        
        index++;
        
        if (sb.length() > 0)
            return sb.toString().trim();
        else
            return null;
    }
    
    public void reset()
    {
        index = 0;
    }
}
