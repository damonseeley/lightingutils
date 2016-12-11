package net.electroland.utils;

@SuppressWarnings("serial")
public class OptionException extends RuntimeException
{
    public OptionException(){
        super();
    }

    public OptionException(String mssg)
    {
        super(mssg);
    }

    public OptionException(Exception e)
    {
        super(e.getMessage());
    }
}