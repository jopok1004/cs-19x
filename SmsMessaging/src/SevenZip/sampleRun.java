package SevenZip;
import java.io.*;

import android.util.Log;
public class sampleRun{
	public static void compress7z(File inFile, File outFile) {
		try {
			Log.i("FILE NAME",inFile.getName());
			BufferedInputStream inStream  = new BufferedInputStream(new java.io.FileInputStream(inFile));
			BufferedOutputStream outStream = new BufferedOutputStream(new java.io.FileOutputStream(outFile));
			
			System.out.println("Inside encode.");
			SevenZip.Compression.LZMA.Encoder encoder = new SevenZip.Compression.LZMA.Encoder();
			
			encoder.SetEndMarkerMode(true);
			Log.i("SET END MARKER","ENC");
			encoder.WriteCoderProperties(outStream);
			Log.i("WRITING","ENC");
			long fileSize;
			fileSize = inFile.length();
			for (int i = 0; i < 8; i++){
				outStream.write((int)(fileSize >>> (8 * i)) & 0xFF);
				Log.i("FOR LOOP","ENC");
			}
			encoder.Code(inStream, outStream, -1, -1, null);
			
			Log.i("ENCODING2","ENC");
			outStream.flush();
			outStream.close();
			inStream.close();
			
		} catch (IOException e) {
			System.out.println("Exception is" + e);
		}
	}
	public static void decompress7z(File inFile, File outFile) throws Exception {
		try {
			BufferedInputStream inStream  = new BufferedInputStream(new java.io.FileInputStream(inFile));
			BufferedOutputStream outStream = new BufferedOutputStream(new java.io.FileOutputStream(outFile));
			
			System.out.println("Inside decode.");
			int propertiesSize = 5;
			byte[] properties = new byte[propertiesSize];
			if (inStream.read(properties, 0, propertiesSize) != propertiesSize)
				throw new Exception("input .lzma file is too short");
			SevenZip.Compression.LZMA.Decoder decoder = new SevenZip.Compression.LZMA.Decoder();
			if (!decoder.SetDecoderProperties(properties))
				throw new Exception("Incorrect stream properties");
			long outSize = 0;
			for (int i = 0; i < 8; i++)
			{
				int v = inStream.read();
				if (v < 0)
					throw new Exception("Can't read stream size");
				outSize |= ((long)v) << (8 * i);
			}
			if (!decoder.Code(inStream, outStream, outSize))
				throw new Exception("Error in data stream");
			
			outStream.flush();
			outStream.close();
			inStream.close();
			
		} catch (IOException e) {
			System.out.println("Exception is" + e);
		}
	}
	public static void main(String args[]){
		
		try {
			decompress7z(new File("latex.7z"), new File("latex.pdf"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Successful.");
		return;
	}
}
