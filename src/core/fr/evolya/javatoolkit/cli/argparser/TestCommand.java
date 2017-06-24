package fr.evolya.javatoolkit.cli.argparser;

import java.io.File;


public class TestCommand {
	
    @Argument(value = "input", description = "This is the input file", required = true)
    protected String inputFilename;

    @Argument(value = "output", alias = "o", description = "This is the output file", required = true)
    protected File outputFile;

    @Argument(description = "This flag can optionally be set")
    protected boolean someflag;

    @Argument(description = "Minimum", alias = "m")
    protected Integer minimum;

    @Argument(description = "List of values", delimiter = ":")
    public void setValues(Integer[] values) {
        this.values = values;
    }
    public Integer[] getValues() {
        return values;
    }
    private Integer[] values;

    @Argument(description = "List of strings", delimiter = ";")
    protected String[] strings;

    @Argument(description = "not required")
    protected boolean notRequired;

    /*public void testArgsParse() {
        TestCommand tc = new TestCommand();
        Args.usage(tc);
        String[] args = {"-input", "inputfile", "-o", "outputfile", "extra1", "-someflag", "extra2", "-m", "10", "-values", "1:2:3", "-strings", "sam;dave;jolly"};
        List<String> extra = Args.parse(tc, args);
        assertEquals("inputfile", tc.inputFilename);
        assertEquals(new File("outputfile"), tc.outputFile);
        assertEquals(true, tc.someflag);
        assertEquals(10, tc.minimum.intValue());
        assertEquals(3, tc.values.length);
        assertEquals(2, tc.values[1].intValue());
        assertEquals("dave", tc.strings[1]);
        assertEquals(2, extra.size());
    }*/
    
}