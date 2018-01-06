package com.jsoniter.extra;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Since;
import com.google.gson.annotations.Until;
import com.jsoniter.JsonIterator;
import com.jsoniter.SupportBitwise;
import com.jsoniter.ValueType;
import com.jsoniter.annotation.JsonIgnore;
import com.jsoniter.annotation.JsonProperty;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Config;
import com.jsoniter.spi.JsonException;

/**
 * Public Class GsonCompatibilityMode.
 * 
 * @author MaxiBon
 *
 */
public class GsonCompatibilityMode extends Config {

	private final static int SURR1_FIRST = 0xD800;
	private final static int SURR1_LAST = 0xDBFF;
	private final static int SURR2_FIRST = 0xDC00;
	private final static int SURR2_LAST = 0xDFFF;
	private static final String[] REPLACEMENT_CHARS;
	private static final String[] HTML_SAFE_REPLACEMENT_CHARS;

	static {
		REPLACEMENT_CHARS = new String[128];
		for (int i = 0; i <= 0x1f; i++) {
			REPLACEMENT_CHARS[i] = String.format("\\u%04x", i);
		}
		REPLACEMENT_CHARS['"'] = "\\\"";
		REPLACEMENT_CHARS['\\'] = "\\\\";
		REPLACEMENT_CHARS['\t'] = "\\t";
		REPLACEMENT_CHARS['\b'] = "\\b";
		REPLACEMENT_CHARS['\n'] = "\\n";
		REPLACEMENT_CHARS['\r'] = "\\r";
		REPLACEMENT_CHARS['\f'] = "\\f";
		HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
		HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
		HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
		HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
		HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
		HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
	}

	private GsonCompatibilityMode(String configName, Builder builder) {
		super(configName, builder);
	}

	protected Builder builder() {
		Builder b = null;
		if (super.builder() instanceof Builder) {
			b = (Builder) super.builder();
		}
		return b;
	}

	/**
	 * Public Class Builder.
	 *
	 * @author MaxiBon
	 *
	 */
	public static class Builder extends Config.Builder {
		private boolean excludeFieldsWithoutExposeAnnotation = false;
		private boolean disableHtmlEscaping = false;
		private ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, java.util.Locale.US);
			}
		};
		private FieldNamingStrategy fieldNamingStrategy;
		private Double version;
		private Set<ExclusionStrategy> serializationExclusionStrategies = new java.util.HashSet<ExclusionStrategy>();
		private Set<ExclusionStrategy> deserializationExclusionStrategies = new java.util.HashSet<ExclusionStrategy>();

		/**
		 * Builder.
		 */
		public Builder() {
			omitDefaultValue(true);
		}

		public Builder setExcludeFieldsWithoutExposeAnnotation() {
			excludeFieldsWithoutExposeAnnotation = true;
			return this;
		}

		public Builder serializeNulls() {
			omitDefaultValue(false);
			return this;
		}

		public Builder setDateFormat(final int dateStyle, final int timeStyle) {
			dateFormat = new ThreadLocal<DateFormat>() {
				@Override
				protected DateFormat initialValue() {
					return DateFormat.getDateTimeInstance(dateStyle, timeStyle, java.util.Locale.US);
				}
			};
			return this;
		}

		public Builder setDateFormat() {
			/**
			 * Class JdkDatetimeSupport.
			 * 
			 * @author MaxiBon
			 *
			 */
			class JdkDatetimeSupport {
				// 2014-04-01 10:45
				/**
				 * LocalDateTime dateTime
				 */
				int y = 2014;
				int d = 1;
				int h = 10;
				int m = 45;
				LocalDateTime dateTime = LocalDateTime.of(y, java.time.Month.APRIL, d, h, m);
				// format as ISO week date (2014-W08-4)
				/**
				 * 
				 */
				String asIsoWeekDate = dateTime.format(DateTimeFormatter.ISO_WEEK_DATE);
				// using a custom pattern (01/04/2014)
				/**
				 * 
				 */
				String asCustomPattern = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
				// french date formatting (1. avril 2014)
				/**
				 * 
				 */
				String frenchDate = dateTime.format(DateTimeFormatter.ofPattern("d. MMMM yyyy", new java.util.Locale("fr")));
				// using short german date/time formatting (01.04.14 10:45)
				/**
				 * 
				 */
				DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(java.time.format.FormatStyle.SHORT)
						.withLocale(new java.util.Locale("de"));
				/**
				 * 
				 */
				String germanDateTime = dateTime.format(formatter);
				// parsing date strings
				/**
				 * 
				 */
				LocalDate fromIsoDate = LocalDate.parse("2014-01-20");
				/**
				 * 
				 */
				LocalDate fromIsoWeekDate = LocalDate.parse("2014-W14-2", DateTimeFormatter.ISO_WEEK_DATE);
				/**
				 * 
				 */
				LocalDate fromCustomPattern = LocalDate.parse("20.01.2014", DateTimeFormatter.ofPattern("dd.MM.yyyy"));

			}

			return this;
		}

		public Builder setFieldNamingStrategy(FieldNamingStrategy fieldNameStrategy) {
			this.fieldNamingStrategy = fieldNameStrategy;
			return this;
		}

		public Builder setFieldNamingPolicy(FieldNamingPolicy namingConvention) {
			this.fieldNamingStrategy = namingConvention;
			return this;
		}

		public Builder setPrettyPrinting() {
			int n = 2;
			indentionStep(n);
			return this;
		}

		public Builder setDisableHtmlEscaping() {
			disableHtmlEscaping = true;
			return this;
		}

		public Builder setVersion(double versions) {
			this.version = versions;
			return this;
		}

		public Builder setExclusionStrategies(ExclusionStrategy... strategies) {
			for (ExclusionStrategy strategy : strategies) {
				addSerializationExclusionStrategy(strategy);
			}
			return this;
		}

		public Builder addSerializationExclusionStrategy(ExclusionStrategy exclusionStrategy) {
			serializationExclusionStrategies.add(exclusionStrategy);
			return this;
		}

		public Builder addDeserializationExclusionStrategy(ExclusionStrategy exclusionStrategy) {
			deserializationExclusionStrategies.add(exclusionStrategy);
			return this;
		}

		public GsonCompatibilityMode build() {
			escapeUnicode(false);
			GsonCompatibilityMode g = null;
			if (super.build() instanceof GsonCompatibilityMode) {
				g = (GsonCompatibilityMode) super.build();
			}
			return g;
		}

		@Override
		protected Config doBuild(String configName) {
			return new GsonCompatibilityMode(configName, this);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}

			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			if (super.equals(o) == false) {
				return false;
			}

			Builder builder = null;
			if (o instanceof Builder) {
				builder = (Builder) o;
			}

			if (excludeFieldsWithoutExposeAnnotation != builder.excludeFieldsWithoutExposeAnnotation) {
				return false;
			}

			if (disableHtmlEscaping != builder.disableHtmlEscaping) {
				return false;
			}

			if (!dateFormat.get().equals(builder.dateFormat.get())) {
				return false;
			}

			if (fieldNamingStrategy != null ? !fieldNamingStrategy.equals(builder.fieldNamingStrategy)
					: builder.fieldNamingStrategy != null) {
				return false;
			}

			if (version != null ? (version.equals(builder.version) == false) : builder.version != null) {
				return false;
			}

			if (serializationExclusionStrategies != null
					? !serializationExclusionStrategies.equals(builder.serializationExclusionStrategies)
					: builder.serializationExclusionStrategies != null) {
				return false;
			}

			return deserializationExclusionStrategies != null
					? deserializationExclusionStrategies.equals(builder.deserializationExclusionStrategies)
					: builder.deserializationExclusionStrategies == null;
		}

		@Override
		/**
		 * hashCode.
		 */
		public int hashCode() {
			int result = super.hashCode();
			result = 31 * result + (excludeFieldsWithoutExposeAnnotation ? 1 : 0);
			result = 31 * result + (disableHtmlEscaping ? 1 : 0);
			result = 31 * result + dateFormat.get().hashCode();
			result = 31 * result + (fieldNamingStrategy != null ? fieldNamingStrategy.hashCode() : 0);
			result = 31 * result + (version != null ? version.hashCode() : 0);
			result = 31 * result
					+ (serializationExclusionStrategies != null ? serializationExclusionStrategies.hashCode() : 0);
			result = 31 * result
					+ (deserializationExclusionStrategies != null ? deserializationExclusionStrategies.hashCode() : 0);
			return result;
		}

		@Override
		public Config.Builder copy() {
			Builder copied = null;
			if (super.copy() instanceof Builder) {
				copied = (Builder) super.copy();
			}
			copied.excludeFieldsWithoutExposeAnnotation = excludeFieldsWithoutExposeAnnotation;
			copied.disableHtmlEscaping = disableHtmlEscaping;
			copied.dateFormat = dateFormat;
			copied.fieldNamingStrategy = fieldNamingStrategy;
			copied.version = version;
			copied.serializationExclusionStrategies = new java.util.HashSet<ExclusionStrategy>(serializationExclusionStrategies);
			copied.deserializationExclusionStrategies = new java.util.HashSet<ExclusionStrategy>(
					deserializationExclusionStrategies);
			return copied;
		}
	}

	@Override
	protected com.jsoniter.spi.OmitValue createOmitValue(Type valueType) {
		if (valueType instanceof Class) {
			Class clazz = (Class) valueType;
			if (clazz.isPrimitive()) {
				return null; // gson do not omit primitive zero
			}
		}
		return super.createOmitValue(valueType);
	}

	@Override
	public com.jsoniter.spi.Encoder createEncoder(String cacheKey, Type type) {
		final int[] v = { 0xc0, 6, 0x80, 0x3f, 0xe0, 12, 0xf0, 18 };
		if (Date.class == type) {
			return new com.jsoniter.spi.Encoder() {
				@Override
				public void encode(Object obj, JsonStream stream) throws IOException {
					DateFormat dateFormat = builder().dateFormat.get();
					stream.writeVal(dateFormat.format(obj));
				}
			};
		} else if (String.class == type) {
			final String[] replacements = encodeSupp6();
			return new com.jsoniter.spi.Encoder() {
				@Override
				public void encode(Object obj, JsonStream stream) throws IOException {
					String value = encodeSupp7(obj);
					stream.write('"');
					int n = value.length();
					for (int i = 0; i < n;) {
						int c = value.charAt(i);
						c = encodeSupp5(c, stream, replacements, i, value, v);
						i++;
					}
					stream.write('"');
				}
			};
		}
		return super.createEncoder(cacheKey, type);
	}
	
	/**
	 * 
	 * @param c
	 * @param firstPart
	 * @param stream
	 * @param v
	 * @return
	 * @throws IOException
	 */
	private int encodeSupp(int c, int firstPart, JsonStream stream, int[] v) throws IOException {
		int ret = c;
		if (ret < SURR2_FIRST || ret > SURR2_LAST) {
			throw new JsonException(
					"Broken surrogate pair: first char 0x" + Integer.toHexString(firstPart) + ", second 0x" + Integer.toHexString(c) + "; illegal combination");
		}
		ret = 0x10000 + ((firstPart - SURR1_FIRST) << 10) + (c - SURR2_FIRST);
		if (ret > 0x10FFFF) { 
			throw new JsonException("illegalSurrogate");
		}
		Integer n1 = Integer.valueOf(Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(v[6])).longValue(),Long.getLong(Integer.toString(c >> v[7])).longValue(), '|'))).intValue());
		Integer n2 = Integer.valueOf(Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(v[2])).longValue(),Long.getLong(Integer.toString(Integer.valueOf(Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(c >> v[5])).longValue(),Long.getLong(Integer.toString(v[3])).longValue(),'&'))).intValue()))).longValue(),'|'))).intValue());
		Integer n3 = Integer.valueOf(Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(v[2])).longValue(),Long.getLong(Integer.toString(Integer.valueOf(Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(c >> v[1])).longValue(),Long.getLong(Integer.toString(v[3])).longValue(),'&'))).intValue()))),'|'))));
		Integer n4 = Integer.valueOf(Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(v[2])).longValue(),Long.getLong(Integer.toString(Integer.valueOf(Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(c)).longValue(),Long.getLong(Integer.toString(v[3])).longValue(),'&'))).intValue()))),'|'))));
		stream.write(n1.byteValue(), n2.byteValue(), n3.byteValue(), n4.byteValue());
		return ret;
	}
	
	/**
	 * 
	 * @param stream
	 * @param c
	 * @param v
	 * @throws IOException
	 */
	private void encodeSupp2(JsonStream stream, int c, int[] v) throws IOException {
		Integer n1 = Integer.valueOf(Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(v[0])),Long.getLong(Integer.toString(c >> v[1])), '|'))).intValue());
		Integer n2 = Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(v[2])),Long.getLong(Integer.toString(Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(c)).longValue(),Long.getLong(Integer.toString(v[3])).longValue(),'&'))).intValue())).longValue(),'|'))).intValue();
		stream.write(n1.byteValue(), n2.byteValue());
	}

	/**
	 * 
	 * @param c
	 * @param stream 
	 * @param v 
	 * @return 
	 * @throws IOException 
	 */
	private int encodeSupp3(int c, JsonStream stream, int[] v) throws IOException {
		int ret = c;
		ret = gsonSupport(stream, ret, v);
		if (c > SURR1_LAST) {
			throw new JsonException("illegalSurrogate");
		}
		return ret;
	}
	
	/**
	 * 
	 * @param stream
	 * @param c
	 * @param replacements
	 * @throws IOException
	 */
	private void encodeSupp4(JsonStream stream, int c, String[] replacements) throws IOException {
		String ret = replacements[c];
		if (ret == null) {
			stream.write(c);
		} else {
			stream.writeRaw(ret);
		}
	}
	
	/**
	 * 
	 * @param c
	 * @param stream
	 * @param replacements
	 * @param i
	 * @param value
	 * @param v
	 * @return
	 * @throws IOException
	 */
	private int encodeSupp5(int c, JsonStream stream, String[] replacements, int i, String value, int[] v) throws IOException {
		int ret = c;
		int index = i;
		switch (ret) {
		case 128:
			encodeSupp4(stream, ret, replacements);
			break;
		case '\u2028':
			stream.writeRaw("\\u2028");
			break;
		case '\u2029':
			stream.writeRaw("\\u2029");
			break;
		default:
			if (ret < 0x800) {
				encodeSupp2(stream, ret, v);
			} else {
				ret = encodeSupp3(ret, stream, v);
				if (index >= value.length()) {
					break;
				}
				index++;
				ret = encodeSupp(value.charAt(index), ret, stream, v);
			}
		}
		return ret;
	}
	/**
	 * 
	 * @return
	 */
	private String[] encodeSupp6() {
		final String[] ret;
		if (builder().disableHtmlEscaping) {
			ret = REPLACEMENT_CHARS;
		} else {
			ret = HTML_SAFE_REPLACEMENT_CHARS;
		}
		return ret;
	}
	
	private String encodeSupp7(Object obj) {
		String value = null;
		if (obj instanceof String) {
			value = (String) obj;
		}
		return value;
	}

	public int gsonSupport(JsonStream stream, int c, final int[] v) throws IOException {
		if (c < SURR1_FIRST || c > SURR2_LAST) {
			Integer n1 = Integer.valueOf(Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(v[4])),Long.getLong(Integer.toString(c >> v[5])), '|'))).intValue());
			Integer n2 = Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(v[2])), Long.getLong(Integer.toString(Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(c >> v[1])).longValue(),Long.getLong(Integer.toString(v[3])).longValue(),'&'))).intValue())).longValue(),'|'))).intValue();
			Integer n3 = Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(v[2])).longValue(),Long.getLong(Integer.toString(Integer.getInteger(Long.toString(SupportBitwise.bitwise(Long.getLong(Integer.toString(c)).longValue(),Long.getLong(Integer.toString(v[3])).longValue(),'&'))).intValue())).longValue(),'|'))).intValue();
			stream.write(n1.byteValue(), n2.byteValue(), n3.byteValue());
		}
		return c;
	}

	@Override
	public com.jsoniter.spi.Decoder createDecoder(String cacheKey, Type type) {
		if (Date.class == type) {
			return new com.jsoniter.spi.Decoder() {
				@Override
				public Object decode(JsonIterator iter) throws IOException {
					DateFormat dateFormat = builder().dateFormat.get();
					try {
						String input = iter.readString();
						return dateFormat.parse(input);
					} catch (java.text.ParseException e) {
						throw new JsonException("Error: ParseException.");
					}
				}
			};
		} else if (String.class == type) {
			return new com.jsoniter.spi.Decoder() {
				@Override
				public Object decode(JsonIterator iter) throws IOException {
					ValueType valueType = iter.whatIsNext();
					if (valueType == ValueType.STRING) {
						return iter.readString();
					} else if (valueType == ValueType.NUMBER) {
						return iter.readNumberAsString();
					} else if (valueType == ValueType.BOOLEAN) {
						return iter.readBoolean() ? "true" : "false";
					} else if (valueType == ValueType.NULL) {
						iter.skip();
						return null;
					} else {
						throw new JsonException("expect string, but found " + valueType);
					}
				}
			};
		} else if (boolean.class == type) {
			return new com.jsoniter.spi.Decoder.BooleanDecoder() {
				@Override
				public boolean decodeBoolean(JsonIterator iter) throws IOException {
					ValueType valueType = iter.whatIsNext();
					switch (valueType) {
					case BOOLEAN:
						return iter.readBoolean();
					case NULL:
						iter.skip();
						return false;
					default:
						throw new JsonException("expect boolean, but found " + valueType);
					}
				}
			};
		} else if (long.class == type) {
			return new com.jsoniter.spi.Decoder.LongDecoder() {
				@Override
				public long decodeLong(JsonIterator iter) throws IOException {
					ValueType valueType = iter.whatIsNext();
					switch (valueType) {
					case NUMBER:
						return iter.readLong();
					case NULL:
						iter.skip();
						return 0;
					default:
						throw new JsonException("expect long, but found " + valueType);
					}
				}
			};
		} else if (int.class == type) {
			return new com.jsoniter.spi.Decoder.IntDecoder() {
				@Override
				public int decodeInt(JsonIterator iter) throws IOException {
					ValueType valueType = iter.whatIsNext();
					if (valueType == ValueType.NUMBER) {
						return iter.readInt();
					} else if (valueType == ValueType.NULL) {
						iter.skip();
						return 0;
					} else {
						throw new JsonException("expect int, but found " + valueType);
					}
				}
			};
		} else if (float.class == type) {
			return new com.jsoniter.spi.Decoder.FloatDecoder() {
				@Override
				public float decodeFloat(JsonIterator iter) throws IOException {
					ValueType valueType = iter.whatIsNext();
					if (valueType == ValueType.NUMBER) {
						return iter.readFloat();
					} else if (valueType == ValueType.NULL) {
						iter.skip();
						final float n = 0.0f;
						return n;
					} else {
						throw new JsonException("expect float, but found " + valueType);
					}
				}
			};
		} else if (double.class == type) {
			return new com.jsoniter.spi.Decoder.DoubleDecoder() {
				@Override
				public double decodeDouble(JsonIterator iter) throws IOException {
					ValueType valueType = iter.whatIsNext();
					if (valueType == ValueType.NUMBER) {
						return iter.readDouble();
					} else if (valueType == ValueType.NULL) {
						iter.skip();
						final double n = 0.0d;
						return n;
					} else {
						throw new JsonException("expect float, but found " + valueType);
					}
				}
			};
		}
		return super.createDecoder(cacheKey, type);
	}

	@Override
	public void updateClassDescriptor(com.jsoniter.spi.ClassDescriptor desc) {
		FieldNamingStrategy fieldNamingStrategy = builder().fieldNamingStrategy;
		for (com.jsoniter.spi.Binding binding : desc.allBindings()) {
			if (binding.method != null) {
				binding.toNames = newStringArray(0);
				binding.fromNames = newStringArray(0);
			}
			if (fieldNamingStrategy != null && binding.field != null) {
				String translated = fieldNamingStrategy.translateName(binding.field);
				binding.toNames = newStringArray(1);
				binding.toNames[0] = translated;
				binding.fromNames = newStringArray(1);
				binding.fromNames[0] = translated;
			}
			if (builder().version != null) {
				Since since = binding.getAnnotation(Since.class);
				if (since != null && builder().version < since.value()) {
					binding.toNames = newStringArray(0);
					binding.fromNames = newStringArray(0);
				}
				Until until = binding.getAnnotation(Until.class);
				if (until != null && builder().version >= until.value()) {
					binding.toNames = newStringArray(0);
					binding.fromNames = newStringArray(0);
				}
			}
			for (ExclusionStrategy strategy : builder().serializationExclusionStrategies) {
				if (strategy.shouldSkipClass(binding.clazz)) {
					binding.toNames = new String[0];
					continue;
				}
				if (strategy.shouldSkipField(new FieldAttributes(binding.field))) {
					binding.toNames = new String[0];
				}
			}
			for (ExclusionStrategy strategy : builder().deserializationExclusionStrategies) {
				if (strategy.shouldSkipClass(binding.clazz)) {
					binding.fromNames = new String[0];
					continue;
				}
				if (strategy.shouldSkipField(new FieldAttributes(binding.field))) {
					binding.fromNames = new String[0];
				}
			}
		}
		super.updateClassDescriptor(desc);
	}

	@Override
	protected JsonProperty getJsonProperty(Annotation[] annotations) {
		JsonProperty jsoniterObj = super.getJsonProperty(annotations);
		if (jsoniterObj != null) {
			return jsoniterObj;
		}
		final SerializedName gsonObj = getAnnotation(annotations, SerializedName.class);
		if (gsonObj == null) {
			return null;
		}
		return new JsonProperty() {

			@Override
			public String value() {
				return "";
			}

			@Override
			public String[] from() {
				return new String[] { gsonObj.value() };
			}

			@Override
			public String[] to() {
				return new String[] { gsonObj.value() };
			}

			@Override
			public boolean required() {
				return false;
			}

			@Override
			public Class<? extends com.jsoniter.spi.Decoder> decoder() {
				return com.jsoniter.spi.Decoder.class;
			}

			@Override
			public Class<?> implementation() {
				return Object.class;
			}

			@Override
			public Class<? extends com.jsoniter.spi.Encoder> encoder() {
				return com.jsoniter.spi.Encoder.class;
			}

			@Override
			public boolean nullable() {
				return true;
			}

			@Override
			public boolean collectionValueNullable() {
				return true;
			}

			@Override
			public String defaultValueToOmit() {
				return "";
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return JsonProperty.class;
			}
		};
	}

	@Override
	protected JsonIgnore getJsonIgnore(Annotation[] annotations) {

		JsonIgnore jsoniterObj = super.getJsonIgnore(annotations);
		if (jsoniterObj != null) {
			return jsoniterObj;
		}
		if (builder().excludeFieldsWithoutExposeAnnotation) {
			final Expose gsonObj = getAnnotation(annotations, Expose.class);
			if (gsonObj != null) {
				return new JsonIgnore() {
					@Override
					public boolean ignoreDecoding() {
						return !gsonObj.deserialize();
					}

					@Override
					public boolean ignoreEncoding() {
						return !gsonObj.serialize();
					}

					@Override
					public Class<? extends Annotation> annotationType() {
						return JsonIgnore.class;
					}
				};
			}
			return new JsonIgnore() {
				@Override
				public boolean ignoreDecoding() {
					return true;
				}

				@Override
				public boolean ignoreEncoding() {
					return true;
				}

				@Override
				public Class<? extends Annotation> annotationType() {
					return JsonIgnore.class;
				}
			};
		}
		return null;
	}

	// CREATA DA ENRICO
	String[] newStringArray(int n) {
		return new String[n];
	}
}
