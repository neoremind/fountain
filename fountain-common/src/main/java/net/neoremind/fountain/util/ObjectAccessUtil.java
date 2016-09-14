package net.neoremind.fountain.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author hanxu
 *
 * 2013年8月1日
 */
public class ObjectAccessUtil {

	public static Object readObject(byte[] result) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bos = new ByteArrayInputStream(result);
		ObjectInputStream oos = new ObjectInputStream(bos);
		Object obj = oos.readObject();
		bos.close();
		oos.close();
		return obj;
	}

	public static byte[] getBytesFromObject(Serializable data) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream objos = new ObjectOutputStream(os);
		objos.writeObject(data);
		objos.flush();
		byte[] result = os.toByteArray();
		os.close();
		objos.close();
		return result;
	}
}
