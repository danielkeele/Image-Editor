import java.util.*;
import java.io.*;
import java.util.regex.*;

public class ImageEditor
{
    //motionblur operation
    //changes each pixel's color to the average of n pixels to the right
    public static Image MotionBlurImage(Image image, int n)
    {
        Image newImage = new Image();
        newImage.height = image.height;
        newImage.width = image.width;

        for (int x = 0; x < image.pixels.size(); x++)
        {
            ArrayList<Pixel> row = new ArrayList<Pixel>();

            for (int y = 0; y < image.pixels.get(x).size(); y++)
            {
                int rTotal = 0;
                int gTotal = 0;
                int bTotal = 0;
                int counter = 0;
                for (int z = y; z < image.width && z < (y + n); z++)
                {
                    counter++;
                    rTotal += image.pixels.get(x).get(z).r;
                    gTotal += image.pixels.get(x).get(z).g;
                    bTotal += image.pixels.get(x).get(z).b;
                }
                
                if (counter == 0)
                {
                    row.add(image.pixels.get(x).get(y));
                }
                else
                {
                    row.add(new Pixel(rTotal / counter, gTotal / counter, bTotal / counter));
                }
            }

            newImage.pixels.add(row);
        }

        return newImage;
    }

    //emboss operation
    public static Image EmbossImage(Image image)
    {
        Image newImage = new Image();
        newImage.height = image.height;
        newImage.width = image.width;

        for (int x = 0; x < image.pixels.size(); x++)
        {
            ArrayList<Pixel> row = new ArrayList<Pixel>();

            for (int y = 0; y < image.pixels.get(x).size(); y++)
            {
                int V;

                if (x == 0 || y == 0)
                {
                    V = 128;
                }
                else
                {
                    int redDiff = image.pixels.get(x).get(y).r - image.pixels.get(x-1).get(y-1).r;
                    int greenDiff = image.pixels.get(x).get(y).g - image.pixels.get(x-1).get(y-1).g;
                    int blueDiff = image.pixels.get(x).get(y).b - image.pixels.get(x-1).get(y-1).b;

                    int maxDifference = redDiff;
                    if (Math.abs(greenDiff) > Math.abs(maxDifference))
                    {
                        maxDifference = greenDiff;
                    }
                    if (Math.abs(blueDiff) > Math.abs(maxDifference))
                    {
                        maxDifference = blueDiff;
                    }

                    V = maxDifference + 128;

                    if (V < 0)
                    {
                        V = 0;
                    }
                    else if (V > 255)
                    {
                        V = 255;
                    }
                }

                row.add(SetVForRGBValue(V));
            }

            newImage.pixels.add(row);
        }

        return newImage;
    }

    //used by emboss opertation
    public static Pixel SetVForRGBValue(int V)
    {
        Pixel newPixel = new Pixel();
        newPixel.r = V;
        newPixel.g = V;
        newPixel.b = V;
        return newPixel;
    }

    //grayscale operation
    //sets each pixel's rgb values to the average of the rgb values
    public static Image GrayscaleImage(Image image)
    {
        Image newImage = new Image();
        newImage.height = image.height;
        newImage.width = image.width;

        for (int x = 0; x < image.pixels.size(); x++)
        {
            ArrayList<Pixel> row = new ArrayList<Pixel>();

            for (int y = 0; y < image.pixels.get(x).size(); y++)
            {
                row.add(GrayscaleRGBValue(image.pixels.get(x).get(y)));
            }

            newImage.pixels.add(row);
        }

        return newImage;
    }

    //used by grayscale operation
    public static Pixel GrayscaleRGBValue(Pixel p)
    {
        Pixel newPixel = new Pixel();
        int average = (p.r + p.g + p.b) / 3;
        newPixel.r = average;
        newPixel.g = average;
        newPixel.b = average;
        return newPixel;
    }

    //invert operation
    //sets each pixel's rgb values to the opposite side of the 0-255 rgb spectrum
    public static Image InvertImage(Image image)
    {
        Image newImage = new Image();
        newImage.height = image.height;
        newImage.width = image.width;

        for (int x = 0; x < image.pixels.size(); x++)
        {
            ArrayList<Pixel> row = new ArrayList<Pixel>();

            for (int y = 0; y < image.pixels.get(x).size(); y++)
            {
                row.add(InvertRGBValue(image.pixels.get(x).get(y)));
            }

            newImage.pixels.add(row);
        }

        return newImage;
    }

    //used by invert operation
    public static Pixel InvertRGBValue(Pixel p)
    {
        Pixel newPixel = new Pixel();
        newPixel.r = 255 - p.r;
        newPixel.g = 255 - p.g;
        newPixel.b = 255 - p.b;
        return newPixel;
    }
    
    public static void main(String[] args)
    {
        //default command line arguments if none given.
        String inputFileName = "cs_logo.ppm";
        String outputFileName = "output.ppm";
        String operation = "grayscale";
        String blurLength = "10";

        //other variables
        File file = new File(inputFileName);
        Scanner lines = null;
        Image image = new Image();
        int pixelLength = 0;
        ArrayList<Pixel> row = new ArrayList<Pixel>();
        Pixel pixel = new Pixel();
        Boolean foundTitle = false;
        Boolean foundDimmensions = false;
        Boolean foundMaxValue = false;

        //regex for parsing tokens
        Pattern comment = Pattern.compile("[\\s]*#");
        Pattern title = Pattern.compile("P3");
        
        //get commandline arguments
        try
        {
            inputFileName = args[0];
            outputFileName = args[1];
            operation = args[2];
            blurLength = args[3];
        }
        catch (ArrayIndexOutOfBoundsException e){}
        
        //tokenize inputfile
        try 
        {
            lines = new Scanner(file);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Unable to tokenize file.");
        }
        
        //create image object
        while (lines.hasNext())
        {
            //comments
            if (lines.hasNext(comment))
            {
                lines.nextLine();
            }
            //title
            else if (lines.hasNext(title))
            {
                foundTitle = true;
                lines.next();
            }
            else if ( !foundMaxValue && foundDimmensions)
            {
                foundMaxValue = true;
                lines.next();
            }
            //pixels
            else if (foundMaxValue)
            {
                if (pixelLength == 0)
                {
                    pixel.r = Integer.parseInt(lines.next());
                    pixelLength++;
                }
                else if (pixelLength == 1)
                {
                    pixel.g = Integer.parseInt(lines.next());
                    pixelLength++;
                }
                else if (pixelLength == 2)
                {
                    pixel.b = Integer.parseInt(lines.next());
                    pixelLength = 0;
                    
                    row.add(pixel);
                    pixel = new Pixel();

                    if (row.size() == image.width)
                    {
                        image.pixels.add(row);
                        row = new ArrayList<Pixel>();
                    }
                }
            }
            //dimmensions
            else if (foundTitle)
            {
                image.width = Integer.parseInt(lines.next());
                image.height = Integer.parseInt(lines.next());
                foundDimmensions = true;
            }
            //errors
            else
            {
                System.out.println("Problem with file syntax");
                break;
            }
        }
        lines.close();
        Image i = new Image();

        //perform operation from commandline
        if (operation.equals("grayscale"))
        {
            i = GrayscaleImage(image);
        }
        else if (operation.equals("invert"))
        {
            i = InvertImage(image);
        }
        else if (operation.equals("emboss"))
        {
            i = EmbossImage(image);
        }
        else if (operation.equals("motionblur"))
        {
            i = MotionBlurImage(image, Integer.parseInt(blurLength));
        }

        //Write output file
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName));
            out.write(WriteImageString(i));
            out.close();
         }
         catch (IOException e)
         {
             System.out.println("Couldn't write </3");
         }

    }

    //writes the object back to string format
    public static String WriteImageString(Image i)
    {
        StringBuilder file = new StringBuilder();

        file.append("P3");
        file.append("\n" + i.width + " " + i.height);
        file.append("\n255");

        for (int x = 0; x < i.pixels.size(); x++)
        {
            for (int y = 0; y < i.pixels.get(x).size(); y++)
            {
                file.append("\n" + i.pixels.get(x).get(y).r);
                file.append("\n" + i.pixels.get(x).get(y).g);
                file.append("\n" + i.pixels.get(x).get(y).b);
            }
        }
        file.append("\n");

        return file.toString();
    }
}