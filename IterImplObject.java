package com.jsoniter;

import java.io.IOException;

/**
 * class IterImplObject
 * 
 * @author MaxiBon
 *
 */
class IterImplObject {

	private IterImplObject() {
	}

	/**
	 * String variables
	 */
	final static String READ_OBJECT = "readObject";
	final static String EXPECT = "expect :";
	
	/**
	 * readObject
	 * 
	 * @param iter
	 * @return
	 * @throws IOException
	 */
	public static String funReadObject(JsonIterator iter) throws IOException {
		byte c = IterImpl.nextToken(iter);
		switch (c) {
		case 'n':
			int n = 3;
			IterImpl.skipFixedBytes(iter, n);
			return null;
		case '{':
			c = IterImpl.nextToken(iter);
			if (c == '"') {
				iter.unreadByte();
				String field = iter.readString();
				if (IterImpl.nextToken(iter) != ':') {
					
					throw iter.reportError(READ_OBJECT, EXPECT);
				}
				return field;
			}
			if (c == '}') {
				return null; // end of object
			}
			throw iter.reportError(READ_OBJECT, "expect \" after {");
		case ',':
			String field = iter.readString();
			if (IterImpl.nextToken(iter) != ':') {
				throw iter.reportError(READ_OBJECT, EXPECT);
			}
			return field;
		case '}':
			return null; // end of object
		default:
			throw iter.reportError("readObject", "expect { or , or } or n, but found: " + Byte.toString(c).charAt(0));
		}
	}

	
	/**
	 * @param iter
	 * @param cb
	 * @param attachment
	 * @return
	 * @throws IOException
	 */
	public static boolean readObjectCB(JsonIterator iter, JsonIterator.ReadObjectCallback cb, Object attachment) throws IOException {
		byte c = IterImpl.nextToken(iter);
		if ('{' == c) {
			c = IterImpl.nextToken(iter);
			if ('"' == c) {
				return subReadObjectCB(iter, cb, attachment);
			}
			if ('}' == c) {
				return true;
			}
			throw iter.reportError("readObjectCB", "expect \" after {");
		}
		if ('n' == c) {
			int n = 3;
			IterImpl.skipFixedBytes(iter, n);
			return true;
		}
		throw iter.reportError("readObjectCB", "expect { or n");
	}
	
	private static boolean subReadObjectCB(JsonIterator iter, JsonIterator.ReadObjectCallback cb, Object attachment) throws IOException {
		iter.unreadByte();
		String field = iter.readString();
		if (IterImpl.nextToken(iter) != ':') {
			throw iter.reportError(READ_OBJECT, EXPECT);
		}
		if (!cb.handle(iter, field, attachment)) {
			return false;
		}
		int intero = IterImpl.nextToken(iter);
		while (intero == ',') {
			field = iter.readString();
			if (IterImpl.nextToken(iter) != ':') {
				throw iter.reportError(READ_OBJECT,EXPECT);
			}
			if (!cb.handle(iter, field, attachment)) {
				return false;
			}
			intero = IterImpl.nextToken(iter);
		}
		return true;
	}
}
