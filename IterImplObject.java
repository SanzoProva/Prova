package com.jsoniter;

import java.io.IOException;

/**
 * class IterImplObject
 * 
 * @author MaxiBon
 *
 */
class IterImplObject {

	/**
	 * constructor
	 */
	private IterImplObject() {
	}

	/**
	 * String variables
	 */
	final static String READ_OBJECT = "readObject";
	/**
	 * String variables
	 */
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
		String field = null;
		switch (c) {
		case 'n':
			int n = 3;
			IterImpl.skipFixedBytes(iter, n);
			return field;
		case '{':
			c = IterImpl.nextToken(iter);
			if (c == '"') {
				iter.unreadByte();
				field = iter.readString();
				return cyclomaticComplexity(field, iter);
			}
			if (c == '}') {
				return field; // end of object
			}
			throw iter.reportError(READ_OBJECT, "expect \" after {");
		case ',':
			field = iter.readString();
			return cyclomaticComplexity(field, iter);
		case '}':
			return field; // end of object
		default:
			throw iter.reportError("readObject", "expect { or , or } or n, but found: " + Byte.toString(c).charAt(0));
		}
	}

	/**
	 * @param field
	 * @param iter
	 * @return
	 * @throws IOException
	 */
	private static String cyclomaticComplexity(String field, JsonIterator iter) throws IOException {
		if (IterImpl.nextToken(iter) != ':') {
			throw iter.reportError(READ_OBJECT, EXPECT);
		}
		return field;
	}

	/**
	 * @param iter
	 * @param cb
	 * @param attachment
	 * @return
	 * @throws IOException
	 */
	public static boolean readObjectCB(JsonIterator iter, JsonIterator.ReadObjectCallback cb, Object attachment)
			throws IOException {
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

	/**
	 * 
	 * @param iter
	 * @param cb
	 * @param attachment
	 * @return
	 * @throws IOException
	 */
	private static boolean subReadObjectCB(JsonIterator iter, JsonIterator.ReadObjectCallback cb, Object attachment)
			throws IOException {
		boolean flag = true;
		iter.unreadByte();
		String field = iter.readString();
		if (IterImpl.nextToken(iter) != ':') {
			throw iter.reportError(READ_OBJECT, EXPECT);
		}
		if (!cb.handle(iter, field, attachment)) {
			flag = false;
		}
		int intero = IterImpl.nextToken(iter);
		while (intero == ',') {
			field = iter.readString();
			if (IterImpl.nextToken(iter) != ':') {
				throw iter.reportError(READ_OBJECT, EXPECT);
			}
			if (!cb.handle(iter, field, attachment)) {
				flag = false;
			}
			intero = IterImpl.nextToken(iter);
		}
		return flag;
	}
}
