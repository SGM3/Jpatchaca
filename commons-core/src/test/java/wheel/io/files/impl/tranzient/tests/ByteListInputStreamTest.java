package wheel.io.files.impl.tranzient.tests;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import wheel.io.files.impl.Closeable;
import wheel.io.files.impl.tranzient.ByteListInputStream;

public class ByteListInputStreamTest  extends CloseableStreamTest  {

	public void testByteListInputStream() throws IOException{
		final ArrayList<Byte> bytesList = new ArrayList<Byte>();
		final InputStream stream = new ByteListInputStream(bytesList);

		bytesList.add((byte)10);
		bytesList.add((byte)-1);
		
		assertEquals((byte)10, stream.read());
		final int unsigned = -1 & 0xFF;
		assertEquals(unsigned, stream.read());
		
		assertEquals(-1, stream.read());
		
		bytesList.add((byte)2);
		assertEquals(2, stream.read());
		
		try {
			stream.close();
		} catch (Exception e) {
		}
	}

	@Override
	protected Closeable createSubject() {
		return new ByteListInputStream(new ArrayList<Byte>());
	}
}
