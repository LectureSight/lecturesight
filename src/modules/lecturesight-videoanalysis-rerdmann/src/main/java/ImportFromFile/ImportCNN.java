package ImportFromFile;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLImage2D;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.util.NIOUtils;
import cv.lecturesight.opencl.OpenCLService;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.color.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.io.File;

/**
 *
 * @author rerdmann
 */
public class ImportCNN {

    /**
     * @param args the command line arguments
     */
    /*
    model.add(InputLayer(input_shape=[64,64,3]))
    model.add(Conv2D(filters=32, kernel_size=3,strides=1,padding='same', activation='relu'))
    model.add(MaxPool2D(pool_size=5, padding='same'))
    model.add(Conv2D(filters=32, kernel_size=3,strides=1,padding='same', activation='relu'))#50
    model.add(MaxPool2D(pool_size=5, padding='same'))
    model.add(Conv2D(filters=64, kernel_size=3,strides=1,padding='same', activation='relu'))#80
    model.add(MaxPool2D(pool_size=5, padding='same'))
    model.add(Dropout(0.25))
    model.add(Flatten())
    model.add(Dense(512, activation='relu'))
    model.add(Dropout(rate=0.5))
    model.add(Dense(2, activation='softmax'))
     */

    public static void main(String[] args) {
        float [][][] conv2d_1 = readWeights("/home/rerdmann/Desktop/MasterThesis/INRIAPerson/weights/Conv2d_1.txt",3,32,9);
        float [][][] conv2d_1_weights = extractWeights(conv2d_1);
        float [] conv2d_1_bias = extractBias(conv2d_1);
        System.out.println("Importet Conv2d_1: channel: "+conv2d_1_weights.length+" filter: "+conv2d_1_weights[0].length+" kernel: "+conv2d_1_weights[0][0].length + " weightcount: " +getElemCount(conv2d_1_weights));
        //float[][] test = getWeights(conv2d_1_weights, 0);        
        
        float [][][] conv2d_2 = readWeights("/home/rerdmann/Desktop/MasterThesis/INRIAPerson/weights/Conv2d_2.txt",32,32,9);
        float [][][] conv2d_2_weights = extractWeights(conv2d_2);
        float [] conv2d_2_bias = extractBias(conv2d_2);     
        System.out.println("Importet Conv2d_2: channel: "+conv2d_2_weights.length+" filter: "+conv2d_2_weights[0].length+" kernel: "+conv2d_2_weights[0][0].length);   
        
        //printWeights(conv2d_2);
        
        float [][][] conv2d_3 = readWeights("/home/rerdmann/Desktop/MasterThesis/INRIAPerson/weights/Conv2d_3.txt",32,64,9);
        float [][][] conv2d_3_weights = extractWeights(conv2d_3);
        float [] conv2d_3_bias = extractBias(conv2d_3);
        System.out.println("Importet Conv2d_3: channel: "+conv2d_3_weights.length+" filter: "+conv2d_3_weights[0].length+" kernel: "+conv2d_3_weights[0][0].length);
        
        //printWeights(conv2d_3_weights);
        //printBias(conv2d_3_bias);
        
        float [][][] dense_1 = readWeights("/home/rerdmann/Desktop/MasterThesis/INRIAPerson/weights/Dense_1.txt",64,512,1);
        float [][][] dense_1_weights = extractWeights(dense_1);
        float [] dense_1_bias = extractBias(dense_1);    
        System.out.println("Importet Dense_1: channel: "+dense_1_weights.length+" filter: "+dense_1_weights[0].length+" kernel: "+dense_1_weights[0][0].length); 
        
        //printWeights(dense_1_weights);
        //printBias(dense_1_bias);
        
        float [][][] dense_2 = readWeights("/home/rerdmann/Desktop/MasterThesis/INRIAPerson/weights/Dense_2.txt",512,2,1);
        float [][][] dense_2_weights = extractWeights(dense_2);
        float [] dense_2_bias = extractBias(dense_2);
        System.out.println("Importet Dense_2: channel: "+dense_2_weights.length+" filter: "+dense_2_weights[0].length+" kernel: "+dense_2_weights[0][0].length);
        
        //printWeights(dense_2_weights);
        //printBias(dense_2_bias);
    }
    
    public static float[][][] readWeights (String filename, int channel, int filter, int kernel)
    {
        float weights[][][] = new float[channel+1][filter][kernel];
        boolean filter_end = false;
        boolean channel_end = false;
        boolean kernel_end = false;
        int filter_pos = 0;
        int channel_pos = 0;
        int kernel_pos = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {                
                filter_end = line.contains("]");
                kernel_end = line.contains("]]");
                //clear string
                line = clearString(line);
                String str_content[] = line.split(",");
                float f_content[] = new float[str_content.length];
                
                //Weights
                for (int i = 0; i < str_content.length; i++) {             
                    try{
                        if(str_content[i].contains("dtype=float32")){
                            kernel_pos=0;
                            filter_pos=0;
                            channel_pos = channel;
                            filter_end = false;
                            kernel_end = false;
                            continue;
                        }
                        f_content[i] = Float.parseFloat(str_content[i].replace(",", ""));
                        //System.out.println(channel_pos + ": " + kernel_pos + ": " + filter_pos + ": " + f_content[i]);
                        weights[channel_pos][filter_pos][kernel_pos] = f_content[i];
                        filter_pos++;
                    } catch (NumberFormatException e) {
                    }
                }
                
                if(filter_end) //0-31
                {
                    filter_pos=0;
                    channel_pos++;
                }
                if(kernel_end) //0-8
                {
                    kernel_pos++;
                    channel_pos = 0;
                }
                if(channel_end) // 0-2
                {
                    kernel_pos=0;
                }
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return weights;
    }
    
    public static String clearString(String str)
    {
        str = str.replace("[", "");
        str = str.replace("]", "");
        str = str.replace("(", "");
        str = str.replace(")", "");     
        str = str.replace("array", "");  
        str = str.replace(" ", "");  
        return str;
    }

    public static void printWeights(float[][][] weights) {
        for (int i=0; i < weights.length; i++)
        {
            System.out.println("Kanal: " + i);
            for (int j=0; j<weights[i].length;j++)
            {
                System.out.println("Filter: " + j);
                for(int k=0; k<weights[i][j].length; k++)
                {
                    System.out.println("Kern: " + k + " = " + weights[i][j][k]);
                    //System.out.print(weights[i][j][k]);
                }
            }
        }
    }
    
    public static float[][] getWeights(float[][][] weights, int j) {
        float[][] result = new float[weights.length][weights[0][0].length];
        for (int i=0; i < weights.length; i++)
        {
            for(int k=0; k<weights[i][j].length; k++)
            {
                result[i][k]= weights[i][j][k];
                //System.out.print(weights[i][j][k]);
            }
        }
        return result;
    }
 
    public static void printBias(float[] bias) {
        
        for(int k=0; k<bias.length; k++)
        {
            System.out.println("Bias: " + k + " = " + bias[k]);
            //System.out.print(weights[i][j][k]);
        }
    }

    public static float[][][] extractWeights(float[][][] weights) {
        float[][][] result = new float[weights.length-1][weights[0].length][weights[0][0].length];
        for (int i=0; i < result.length; i++)
        {
            for (int j=0; j<result[i].length;j++)
            {
                for(int k=0; k<result[i][j].length; k++)
                {
                    result[i][j][k] = weights[i][j][k];                    
                }
            }
        }
        return result;
    }

    public static float[] extractBias(float[][][] weights) {
        float[] bias = new float[weights[0].length];        
        for(int i = 0; i<weights[0].length;i++)
        {
            bias[i] = weights[0][i][weights[0][0].length-1];            
        }
        return bias;
    }

    public static CLBuffer<Float> makeFloatBuffer(OpenCLService ocl, float[] fb) {
        
        FloatBuffer dataBuffer = NIOUtils.directFloats(fb.length, ocl.context().getByteOrder()); 
        //Writing Weights to Buffer
        for(int i = 0; i < fb.length; i++) {
            dataBuffer.put(i, fb[i]);
        }
        return ocl.context().createFloatBuffer(CLMem.Usage.InputOutput, dataBuffer, true);
    }
    
    public static float[] getFlatWeights(float[][][] weights)
    {
        float[] result = new float[weights.length*weights[0].length*weights[0][0].length];
        int cnt=0;
        for (int i=0; i < weights.length; i++)
        {
            for (int j=0; j<weights[i].length;j++)
            {
                for(int k=0; k<weights[i][j].length; k++)
                {
                    result[cnt]=weights[i][j][k];
                    cnt++;
                }
            }
        }
        return result;
    }
    
    public static int getElemCount(float[][][] weights)
    {
        return weights.length*weights[0].length*weights[0][0].length;
    }
    
    public static void print_CLBuffer_Float(CLQueue queue, CLBuffer<Float> dataBuffer)
    {
        FloatBuffer outputBuffer;
        float output = 0;      
        float pos = 0;
        outputBuffer = dataBuffer.read(queue);
        System.out.println("Float Buffer");
        while (outputBuffer.hasRemaining())
        {
            output = outputBuffer.get();
            pos=pos+1;
            System.out.println("Buffer position " + pos + ": " + output);
        }            
    }
    public static BufferedImage createBufferedImage(File file)
    {
        BufferedImage image = null;
        try
        {
            image = ImageIO.read(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        int sizeX = image.getWidth();
        int sizeY = image.getHeight();

        BufferedImage result = new BufferedImage(
            sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
        Graphics g = result.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }
    
    public static BufferedImage createBufferedImage(String fileName)
    {
        BufferedImage image = null;
        try
        {
            image = ImageIO.read(new File(fileName));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        int sizeX = image.getWidth();
        int sizeY = image.getHeight();

        BufferedImage result = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }
}
